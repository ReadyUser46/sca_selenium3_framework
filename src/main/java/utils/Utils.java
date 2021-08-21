package utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.qameta.allure.Step;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import setup.SetupWebdriver;

import java.io.*;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("JavaDoc")
public class Utils extends SetupWebdriver {

    /**
     * @author Sergio Caballero
     */

    protected static final Logger LOGGER = Logger.getLogger(Utils.class.getName());
    private final String testCaseName;
    private SetupWebdriver setupWebdriver;

    public Utils() {
        testCaseName = getTestCaseName();

    }

    protected static String createFile(String fileName, long sizeMb, String linuxPath) {

        // Define folder path
        String folderPath;
        if (System.getProperty("os.name").contains("Windows")) {
            folderPath = System.getProperty("user.dir") + "\\src\\test\\resources\\createdFiles\\";
        } else {
            folderPath = linuxPath;
        }

        // Create file for a given path+name and set size
        File file = new File(folderPath + fileName);
        if (!file.exists()) {
            if (file.getParentFile().mkdirs()) LOGGER.info("The file was created in: " + file);
            else LOGGER.info("[ERROR] Error creating directory");
            try (RandomAccessFile f = new RandomAccessFile(file, "rw")) {
                f.setLength(1024 * sizeMb);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception in createFile: ", e);
            }
        }
        //return path in String format
        return file.getPath();
    }

    //DATA GENERATOR --> SERGIO CABALLERO
    public static String generadorMatriculaAleatoria(String pais) {
        char[] array = new char[]{'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'};
        String matricula = "";
        switch (pais) {
            case "ES":
                for (int i = 0; i < 7; ++i) {
                    Random rnd = new Random();
                    int ale = (int) (rnd.nextDouble() * (double) array.length);
                    int ale2 = (int) (rnd.nextDouble() * 10.0D);
                    if (i > 3) {
                        matricula = matricula + array[ale];
                    } else {
                        matricula = matricula + ale2;
                    }
                }
                break;
            case "BR":
                for (int i = 0; i < 7; ++i) {
                    Random rnd = new Random();
                    int ale = (int) (rnd.nextDouble() * (double) array.length);
                    int ale2 = (int) (rnd.nextDouble() * 10.0D);
                    if (i > 2) {
                        matricula = matricula + ale2;
                    } else {
                        matricula = matricula + array[ale];
                    }
                }
                break;
            case "PT":
                for (int i = 0; i < 6; ++i) {
                    Random rnd = new Random();
                    int ale = (int) (rnd.nextDouble() * (double) array.length);
                    int ale2 = (int) (rnd.nextDouble() * 10.0D);
                    if (i <= 1 || i > 3) {
                        matricula = matricula + ale2;
                    } else {
                        matricula = matricula + array[ale];
                    }
                    if (i == 1 || i == 3) {
                        matricula = matricula + "-";
                    }
                }
                break;
            case "CO":
                for (int i = 0; i < 6; ++i) {
                    Random rnd = new Random();
                    int ale = (int) (rnd.nextDouble() * (double) array.length);
                    int ale2 = (int) (rnd.nextDouble() * 10.0D);
                    if (i > 2) {
                        matricula = matricula + ale2
                        ;
                    } else {
                        matricula = matricula + array[ale];
                    }
                }
                break;
        }
        return matricula;
    }

    public static String readProperty(String key) {
        //this method read the user/pass located in /asf-oes/config.properties
        //this method is only valid for the framework itself
        String propertiesPath = System.getProperty("user.dir") + "/asf-oes/config.properties";

        String value = null;

        try {
            PropertiesConfiguration config = new PropertiesConfiguration(propertiesPath);
            value = config.getProperty(key).toString();

        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Exception occur while reading user/pass values in", e);
        }
        return value;

    }

    /**
     * Execute a bash command. We can handle complex bash commands including
     * multiple executions (; | && ||), quotes, expansions ($), escapes (\), e.g.:
     * "cd /abc/def; mv ghi 'older ghi '$(whoami)"
     *
     * @param command
     * @return true if bash got started, but your command may have failed.
     */
    public static boolean executeBashCommand(String command) {
        boolean success = false;
        System.out.println("Executing BASH command:\n   " + command);
        Runtime r = Runtime.getRuntime();
        // Use bash -c so we can handle things like multi commands separated by ; and
        // things like quotes, $, |, and \. My tests show that command comes as
        // one argument to bash, so we do not need to quote it to make it one thing.
        // Also, exec may object if it does not have an executable file as the first thing,
        // so having bash here makes it happy provided bash is installed and in path.
        String[] commands = {"bash", "-c", command};
        try {
            Process p = r.exec(commands);

            p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";

            while ((line = b.readLine()) != null) {
                System.out.println(line);
            }

            b.close();
            success = true;
        } catch (Exception e) {
            System.err.println("Failed to execute bash with command: " + command);
            e.printStackTrace();
        }
        return success;
    }

    //HARDCODE SLEEPS

    public static void uploadLocalFile(WebDriver driver, WebElement inputFileElement, String filePath, boolean showElement) {

        //javascript to show the element
        if (showElement) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].style.display = 'block';", inputFileElement);
        }

        //for headless browser
        ((RemoteWebElement) inputFileElement).setFileDetector(new LocalFileDetector());

        try {
            inputFileElement.sendKeys(filePath);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Make sure the webElement passed is an input file type", e);
        }
    }

    protected void espera(int s) {
        try {
            Thread.sleep(1000 * s);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception occur");
        }
    }

    protected void espera() {
        espera(1);
    }

    protected void espera(WebElement element, int s) {
        for (int i = 1; i < s; i++) {
            try {
                if (element.isEnabled()) {
                    i = s;
                } else {
                    Thread.sleep(1000);
                }
            } catch (Exception ie) {
                // do nothing
            }
        }
    }

    // WINDOWS
    public List<String> getWindowsNameList(int countWindows) {
        List<String> windowsNameList = new ArrayList<>();
        for (int x = 0; x < countWindows; x++) {
            String text = driver.getWindowHandles().toArray()[x].toString();
            windowsNameList.add(text);
        }
        return windowsNameList;
    }

    public String getWindowName() {
        return driver.getWindowHandle();
    }

    public void changeToDesiredWindow(String name) {
        driver.switchTo().window(name);
    }

    public void closeActualWindowAndChangeTo(String windowName) {
        driver.close();
        driver.switchTo().window(windowName);
    }

    public void changeToNextWindow() {
        final int countWindows = driver.getWindowHandles().size();
        String actualWindowName = getWindowName();
        List<String> windowsNameList = getWindowsNameList(countWindows);
        for (String winHandle : windowsNameList) {
            if (!actualWindowName.equals(winHandle)) {
                changeToDesiredWindow(winHandle);
            }
        }
    }

    public void moveToNextWindow() {
        for (String winHandle : driver.getWindowHandles()) {
            driver.switchTo().window(winHandle);
        }
    }

    public void setOnFront() {
        ((JavascriptExecutor) driver).executeScript("window.focus();");
    }

    protected void refresh() {
        driver.navigate().refresh();
    }

    public void acceptAlert(long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, String.format("alert not present for: '%s' --> continue", testCaseName));
        }
    }

    public void removeAlertJS() {
        //Metodo que modifica la funcion de creacion de una alerta para que no se produzcan
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "window.alert = function(){return true}; ");
    }

    public void executeJavaScript(String funcion) {
        espera();
        if (driver instanceof JavascriptExecutor) {
            ((JavascriptExecutor) driver)
                    .executeScript(funcion);
        }
        espera();
    }

    public void cancelAlert() {
        WebDriverWait wait = new WebDriverWait(this.driver, 5);
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            this.driver.switchTo().alert().dismiss();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("CancelAlert not present for: '%s'", testCaseName));
        }
    }

    //SET AND GET VALUES FROM EXTERNAL FILE
    public String getAuxValue(String key, String valuesFileName) {

        String propertiesFilePath = System.getProperty("configDirectory") + valuesFileName;
        String value = null;

        try {
            PropertiesConfiguration config = new PropertiesConfiguration(propertiesFilePath);
            value = config.getProperty(key).toString();

        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Exception occur", e);
        }
        return value;
    }

    public void setAuxValue(String key, String value, String propertyFileName) {

        //ensure the configDirectory is declared in pom.xml as a property and points to linux directory
        String propertiesFilePath = System.getProperty("configDirectory") + propertyFileName;

        if (!new File(propertiesFilePath).exists()) {
            createPropertyFile_new(propertyFileName);
        }

        try {
            PropertiesConfiguration config = new PropertiesConfiguration(propertiesFilePath);
            config.setProperty(key, value);
            config.save();
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Exception occur", e);
        }
    }

    private void createPropertyFile_new(String propertyFileName) {


        // Create file for a given path+name and set size
        File file = new File(System.getProperty("configDirectory") + propertyFileName);
        try {
            if (file.createNewFile()) LOGGER.info("The file was created in: " + file);
            else LOGGER.info("[ERROR] Error creating directory");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception occur", e);
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        setAuxValue("CreationDate", todayDate, propertyFileName);
    }

    public void waitForVisible(By element) {
        waitForVisible(element, 60);
    }

    //NEW STABILITY METHODS --> SERGIO CABALLERO
    public void waitForClickable(By element) {
        waitForClickable(element, 60);
    }

    public boolean isElementVisible(By element) {
        return isElementVisible(element, 60);
    }

    public boolean isElementVisible(By element, int timeoutInSeconds) {
        if (isElementLocated(element, timeoutInSeconds)) { //First, we check the element is located
            return driver.findElement(element).isDisplayed();
        } else return false;
    }

    public boolean isElementVisibleAngular(By element) {
        return isElementVisibleAngular(element, 60);
    }

    public boolean isElementVisibleAngular(By element, int timeoutInSeconds) {
        for (int i = 0; i < timeoutInSeconds; i++) {
            if (!isElementVisible(element, 1)) {
                espera();
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean isElementLocated(By element, int timeoutInSeconds) { //locates an element
        driver.manage().timeouts().implicitlyWait(timeoutInSeconds, TimeUnit.SECONDS);
        boolean exists = !driver.findElements(element).isEmpty();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        return exists;
    }

    public boolean isElementLocated(By element) {
        return isElementLocated(element, 60);
    }

    public void executeJS(String funcion, WebElement e) {
        ((JavascriptExecutor) driver).executeScript(funcion, e);
    }

    public void executeJS(String funcion) {
        ((JavascriptExecutor) driver).executeScript(funcion);
    }

    public void waitForInvisible(By element) {
        waitForInvisible(element, 60);
    }

    public void waitForClickable(By element, int timeOutInSeconds) {
        //Clickable = located on DOM + visible + clickable
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            espera(); //needed for angular
        } catch (TimeoutException e) {
            LOGGER.log(Level.SEVERE, String.format("Element not clickable for: '%s'", testCaseName));
            Assert.fail("Element might not be located && visible && clickable");
        }
    }

    public void waitForVisible(By element, int timeOutInSeconds) {
        //Visible = located on DOM + visible
        WebDriverWait image = new WebDriverWait(driver, timeOutInSeconds);
        try {
            image.until(ExpectedConditions.visibilityOfElementLocated(element));
            espera(); //needed for angular
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Element not visible for: '%s'", testCaseName));
            Assert.fail("Element is not visible");
        }
    }

    public void waitForJSandJqueryFinish() {
        waitForJSandJqueryFinish(60);
    }

    public void waitForInvisible(By element, int timeOutInSeconds) {
        WebDriverWait image = new WebDriverWait(driver, timeOutInSeconds);
        image.until(ExpectedConditions.invisibilityOfElementLocated(element));
        espera(); //needed for angular
    }

    public void waitForNumberOfWindows(int numberOfWindows) {
        waitForNumberOfWindows(numberOfWindows, 60);
    }

    public void waitForNumberOfWindows(final int numberOfWindows, int timeOutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeOutSeconds);
        ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return (driver.getWindowHandles().size() == numberOfWindows);
            }
        };
        wait.until(expectation);
        espera(); //needed for angular
    }

    public void waitForJSandJqueryFinish(int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
        // wait for jQuery to load
        ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    return ((Long) ((JavascriptExecutor) driver).executeScript("return jQuery.active") == 0);
                } catch (Exception e) {
                    return true;
                }
            }
        };
        // wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };
        boolean waitFlag = wait.until(jQueryLoad) && wait.until(jsLoad);
        espera(); //needed for angular
    }

    public void sendKeysAndTrigger(WebElement element, String keys) {
        sendKeysAndTrigger(element, keys, true);
    }

    public void performBackgroundClick(boolean wait) {
        // this method clicks on the page background, simulating a human click, to invoke all the events (onChangeText, onLostFocus, onBlur...)
        By backgroundLocator = By.xpath("//body");
        waitForClickable(backgroundLocator);
        driver.findElement(backgroundLocator).click();
        if (wait) {
            waitForJSandJqueryFinish();
        }
    }

    public void performBackgroundClick() {
        performBackgroundClick(true);
    }

    //COMMON ELEMENT ACTIONS - SERGIO CABALLERO
    public void sendKeysAndTrigger(WebElement element, String keys, boolean wait) {
        element.clear();
        element.sendKeys(keys);
        performBackgroundClick(wait);
    }

    public void waitForLocated(By element) {
        //Located = located on DOM
        WebDriverWait image = new WebDriverWait(driver, 60);
        try {
            image.until(ExpectedConditions.presenceOfElementLocated(element));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Element not located for: '%s'", testCaseName));
            Assert.fail("Element is not located");
        }
    }

    public void uploadLocalFile(WebElement inputFileElement, String filePath) {

        //javascript to show the element
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.display = 'block';", inputFileElement);

        //for headless browser
        ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
        espera();

        try {
            inputFileElement.sendKeys(filePath);
        } catch (NoSuchElementException e) {
            LOGGER.log(Level.SEVERE, String.format("Exception thrown for: '%s'", testCaseName));
            LOGGER.log(Level.WARNING, "Make sure the webElement passed is an input file type: " + testCaseName);
        }
    }

    public void executeSshCommand(String username, String password,
                                  String host, int port, String command) throws Exception {

        Session session = null;
        ChannelExec channel = null;

        try {
            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            System.out.println("[INFO][AUTOMATION] Executing command shell: " + command);
            channel.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect();

            while (channel.isConnected()) {
                Thread.sleep(100);
            }

            String responseString = new String(responseStream.toByteArray());
            System.out.println("[INFO][AUTOMATION] Command executed: " + command);

            System.out.println(responseString);
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    public boolean verifyTextIsPresentInPDF(String textToVerify, String filePath) throws URISyntaxException {
        try {

            String pdfOutput = null;
            File pdfFile = new File(filePath);
            PDDocument document = PDDocument.load(pdfFile);
            pdfOutput = new PDFTextStripper().getText(document);
            return pdfOutput.contains(textToVerify);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getTextFromPdf(String filePath) {
        try {

            String pdfOutput = null;
            File pdfFile = new File(filePath);
            PDDocument document = PDDocument.load(pdfFile);
            pdfOutput = new PDFTextStripper().getText(document);
            return pdfOutput;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Error when getting text from pdf";
    }

    private void executeCommandShell(String command) {

        String s;
        Process p;
        try {
            p = new ProcessBuilder(command).start();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            System.out.println("[INFO][AUTOMATION] Executing command shell: " + command);
            while ((s = br.readLine()) != null)
                System.out.println("line: " + s);
            p.waitFor();
            System.out.println("exit: " + p.exitValue());
            p.destroy();
        } catch (Exception e) {
            System.out.println("[INFO][AUTOMATION] Error while executing command shell: " + command);
            e.printStackTrace();
        }
    }

    public void scrollToElement(By by) {
        WebElement element = driver.findElement(by);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void sendKeysJS(WebElement element, String value) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[1].value = arguments[0]; ", value, element);
    }

    public String[] getPdfDetails() {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        //open downloads page
        jsExecutor.executeScript("window.open()");
        espera();
        changeToNextWindow();
        driver.get("chrome://downloads");

        //getDownloadDetails
        String fileName = (String) jsExecutor.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList downloads-item').shadowRoot.querySelector('div#content #file-link').text");
        String sourceURL = (String) jsExecutor.executeScript("return document.querySelector('downloads-manager').shadowRoot.querySelector('#downloadsList downloads-item').shadowRoot.querySelector('div#content #file-link').href");

        return new String[]{fileName, sourceURL};
    }

    @Step("{1}}")
    public void assertTrue(Boolean bol, String checkMessage) throws URISyntaxException, IOException, AssertionError {
        try {
            Assert.assertTrue(bol, checkMessage);
        } catch (AssertionError e) {
            throw new AssertionError(e.getMessage());
        }
    }

    @Step("{3}}")
    public void assertEquals(String condition1, String condition2, String assertMessage) throws URISyntaxException, IOException, AssertionError {
        try {
            Assert.assertEquals(condition1, condition2, null);
            // return true;
        } catch (AssertionError | Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

}
