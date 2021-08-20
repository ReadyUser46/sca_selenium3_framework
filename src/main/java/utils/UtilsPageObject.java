package utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.custom.fwk.ScriptBaseTest.generateRandomFilename;
import static com.custom.fwk.ScriptBaseTest.reportsDir;
import static org.testng.AssertJUnit.fail;

@SuppressWarnings("JavaDoc")
public class UtilsPageObject {

  /**
   * @author ivan.delviso
   * <p>
   * Refactored by
   * @author Sergio Caballero
   */

  public static final int NUM_INTENTOS = 5;
  public static final int WAIT_60_SEG = 60;
  public static final long timeOutInSeconds = 15L;
  public static final String POPUP_XPATH = "//a[contains(text(), '[X]')]";
  protected static final int implicitWait = 30;
  protected static final int pageLoadTimeout = 60;
  protected static final Logger LOGGER = Logger.getLogger(UtilsPageObject.class.getName());
  private static final String SLASH = File.separator;
  private static final String FRAME_APPAREA_ID = "wAppArea";
  private static final String FRAME_DIALOGMODAL_ID = "dialog-modal-content";
  public customWebDriver driver;
  protected String browser;
  private String testCaseName;

  public UtilsPageObject(customWebDriver driver) {
    this.driver = driver;
    browser = driver.getBaseBrowser();
    testCaseName = driver.getTestCaseName();
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

  public String getBrowser() {
    return browser;
  }

  //HARDCODE SLEEPS
  public void sleep(int millis) {
    // Metodo para hacer una espera de tiempo dependiendo del navegador
    // Try to avoid it !!!
    if (browser.toLowerCase().contains("firefox")) {
      try {
        int timeInMs = millis * 2;
        Thread.sleep(timeInMs);
      } catch (InterruptedException e) {
        LOGGER.log(Level.SEVERE, "Exception occur");
      }
    } else if (browser.toLowerCase().contains("iexplorer")) {
      try {
        int timeInMs = millis * 3;
        Thread.sleep(timeInMs);
      } catch (InterruptedException e) {
        LOGGER.log(Level.SEVERE, "Exception occur");
      }
    } else {
      try {
        Thread.sleep(millis);
      } catch (InterruptedException e) {
        LOGGER.log(Level.SEVERE, "Exception occur");
      }
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
      String text = driver.getcustomWebdriver().getWindowHandles().toArray()[x].toString();
      windowsNameList.add(text);
    }
    return windowsNameList;
  }

  public String getWindowName() {
    return driver.getcustomWebdriver().getWindowHandle();
  }

  public void changeToDesiredWindow(String name) {
    driver.getcustomWebdriver().switchTo().window(name);
  }

  public void closeActualWindowAndChangeTo(String windowName) {
    driver.getcustomWebdriver().close();
    driver.getcustomWebdriver().switchTo().window(windowName);
  }

  public void changeToNextWindow() {
    final int countWindows = driver.getcustomWebdriver().getWindowHandles().size();
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
    ((JavascriptExecutor) driver.getcustomWebdriver()).executeScript("window.focus();");
  }

  protected void refresh() {
    driver.getcustomWebdriver().navigate().refresh();
  }

  //WEBELEMENTS - OLDS
  public void moveMouseToCoordinatesOnPage(WebElement elemento) {
    /*
     * Move Mouse to web element coordinates. Work for all browsers. Useful for Angular menu
     *
     * @param elemento ""
     */
    if (driver.getBaseBrowser().toLowerCase().contains("iexplorer")) {
      JavascriptExecutor executor = (JavascriptExecutor) driver.getcustomWebdriver();
      int xCoordinates = elemento.getLocation().getX();
      int yCoordinates = elemento.getLocation().getY();

      // Get Browser dimensions
      int browserWidth = driver.getcustomWebdriver().manage().window().getSize().width;
      int browserHeight = driver.getcustomWebdriver().manage().window().getSize().height;

      // Get dimensions of the window displaying the web page
      int pageWidth =
              Integer.parseInt(
                      executor.executeScript("return document.documentElement.clientWidth").toString());
      int pageHeight =
              Integer.parseInt(
                      executor.executeScript("return document.documentElement.clientHeight").toString());

      // Calculate the space the browser is using for toolbars
      int browserFurnitureOffsetX = browserWidth - pageWidth;
      int browserFurnitureOffsetY = browserHeight - pageHeight;

      // Calculate the correct X/Y coordinates based upon the browser furniture offset and the
      // position of the browser on the desktop
      int xPosition =
              driver.getcustomWebdriver().manage().window().getPosition().x
                      + browserFurnitureOffsetX
                      + xCoordinates;
      int yPosition =
              driver.getcustomWebdriver().manage().window().getPosition().y
                      + browserFurnitureOffsetY
                      + yCoordinates;

      // Move the mouse to the calculated X/Y coordinates
      Robot robot = null;
      try {
        robot = new Robot();
      } catch (AWTException e) {
        LOGGER.log(Level.SEVERE, "Exception occur");
      }
      assert robot != null;
      robot.mouseMove(xPosition, yPosition);
      robot.waitForIdle();
    } else {
      Actions actions = new Actions(driver.getcustomWebdriver());
      actions.moveToElement(elemento).perform();
    }
  }

  // SCREENSHOTS
  @Attachment(value = "Screenshot jpg attachment", type = "image/jpg")
  @Step("Taking a screenshot from PageObject")
  public byte[] makeScreenshot() {
    try {
      String filename = generateRandomFilename("ForceFail");
      WebDriver augmentedDriver = new Augmenter().augment(driver.getcustomSeleniumWebdriver());
      File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
      FileUtils.copyFile(screenshot, new File(reportsDir + SLASH + "2" + filename));
      return Files.readAllBytes(Paths.get(screenshot.toURI()));
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, String.format("Error taking screenshot for: '%s'", testCaseName));
    }
    return null;
  }

  public void acceptAlert(long timeOutInSeconds) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeOutInSeconds);
    try {
      wait.until(ExpectedConditions.alertIsPresent());
      driver.switchTo().alert().accept();
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, String.format("alert not present for: '%s' --> continue", testCaseName));
    }
  }

  public void removeAlertJS() {
    //Metodo que modifica la funcion de creacion de una alerta para que no se produzcan
    JavascriptExecutor js = (JavascriptExecutor) driver.getcustomWebdriver();
    js.executeScript(
            "window.alert = function(){return true}; ");
  }

  public String fixATforIE(String text) {
    String AT;
    if (browser.toLowerCase().contains("iexplorer") && System.getProperty("user.language").equals("es")) {
      String[] getTextUntilAt = text.split("@");
      AT = getTextUntilAt[0] + Keys.CONTROL + Keys.ALT + "2" + Keys.NULL + getTextUntilAt[1];
    } else {
      AT = text;
    }
    return AT;
  }

  public void executeJavaScript(String funcion) {
    sleep(1000);
    if (driver instanceof JavascriptExecutor) {
      ((JavascriptExecutor) driver.getcustomWebdriver())
              .executeScript(funcion);
    }
    sleep(1000);
  }

  public boolean isClickElementToOpenPopup(WebElement element, int seg, Boolean clickWithJS, Boolean clickWithActions, String executeScript) {
    boolean result = false;
    final int countWindows = driver.getWindowHandles().size();
    List<String> windowsNameList = getWindowsNameList(countWindows);
    clickTypeOpenPopup(element, clickWithJS, executeScript, clickWithActions);
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), seg);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        int windowNow = webDriver.getWindowHandles().size();
        return windowNow > countWindows;
      }
    };
    try {
      wait.until(expectation);
    } catch (Exception e) {
      result = true;
    }
    int newCountWindows = driver.getWindowHandles().size();
    for (int x = newCountWindows; x > 0; x--) {
      String windowArray = driver.getWindowHandles().toArray()[x - 1].toString();
      boolean found = false;
      for (String winHandle : windowsNameList) {
        if (windowArray.equals(winHandle)) {
          found = true;
        }
      }
      if (!found) {
        driver.switchTo().window(windowArray);
      }
    }
    return result;
  }

  public void clickElementToOpenPopup(WebElement element) {
    clickElementToOpenPopup(element, implicitWait, false, false, null);
  }

  public void clickElementToOpenPopup(WebElement element, int seg) {
    clickElementToOpenPopup(element, seg, false, false, null);
  }

  public void clickElementToOpenPopup(WebElement element, int seg, Boolean clickWithJS, Boolean clickWithActions, String executeScript) {
    final int countWindows = driver.getWindowHandles().size();
    List<String> windowsNameList = getWindowsNameList(countWindows);
    clickTypeOpenPopup(element, clickWithJS, executeScript, clickWithActions);
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), seg, 100);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        int windowNow = webDriver.getWindowHandles().size();
        return windowNow > countWindows;
      }
    };
    wait.until(expectation);
    int newCountWindows = driver.getWindowHandles().size();
    for (int x = newCountWindows; x > 0; x--) {
      String windowArray = driver.getWindowHandles().toArray()[x - 1].toString();
      boolean found = false;
      for (String winHandle : windowsNameList) {
        if (windowArray.equals(winHandle)) {
          found = true;
        }
      }
      if (!found) {
        driver.switchTo().window(windowArray);
      }
    }
  }

  public void cancelAlert() {
    WebDriverWait wait = new WebDriverWait(this.driver.getcustomWebdriver(), 5);
    try {
      wait.until(ExpectedConditions.alertIsPresent());
      this.driver.switchTo().alert().dismiss();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("CancelAlert not present for: '%s'", testCaseName));
    }
  }

  public void clickElementToOpenPopupRetry(WebElement element, int seg, Boolean clickWithJS, Boolean clickWithActions, String executeScript) {
    int cont = 0;
    while (isClickElementToOpenPopup(element, seg, clickWithJS, clickWithActions, executeScript) && cont < NUM_INTENTOS) {
      cont++;
    }
  }

  private void clickTypeOpenPopup(WebElement element, Boolean clickWithJS, String executeScript, Boolean clickWithActions) {
    sleep(1000);
    if (clickWithJS) {
      clickByJS(element);
    } else if (clickWithActions) {
      Actions actions = new Actions(driver.getcustomWebdriver());
      actions.moveToElement(element).build().perform();
      actions.click(element).build().perform();
        /*} else if (executeScript != null) {
            driver.executeScript(executeScript);
        */
      sleep(2000);
    } else {
      element.click();
    }
    acceptAlert(3);
    sleep(1000);
  }

  public void moveDoubleClickElementToOpenPopup(WebElement element, int seg) {
    final int countWindows = driver.getWindowHandles().size();
    List<String> windowsNameList = getWindowsNameList(countWindows);

    moveToElementAndDoubleClickByActions(element);

    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), seg);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        int windowNow = webDriver.getWindowHandles().size();
        return windowNow > countWindows;
      }
    };
    wait.until(expectation);
    int newCountWindows = driver.getWindowHandles().size();
    for (int x = newCountWindows; x > 0; x--) {
      String windowArray = driver.getWindowHandles().toArray()[x - 1].toString();
      boolean found = false;
      for (String winHandle : windowsNameList) {
        if (windowArray.equals(winHandle)) {
          found = true;
        }
      }
      if (!found) {
        driver.switchTo().window(windowArray);
      }
    }
  }

  public void moveToElementAndDoubleClickByActions(WebElement element) {
    Actions actions = new Actions(driver.getcustomWebdriver());
    actions.moveToElement(element).doubleClick().build().perform();
    sleep(2000);
  }

  public void clickByJS(WebElement element) {
    JavascriptExecutor executor = (JavascriptExecutor) driver.getcustomWebdriver();
    executor.executeScript("arguments[0].click();", element);
  }

  public void maximizeWindows() {
    if (getBrowser().toLowerCase().contains("iexplorer") || getBrowser().toLowerCase().contains("custom_IEXPLORER")) {
//            driver.manage().window().maximize();
    } else {
      Dimension dimension = new Dimension(1920, 1080);
      driver.manage().window().setSize(dimension);
    }
  }

  public boolean existsElement(By by, int timeOut) {
    boolean result;
    espera(timeOut);
    //driver.manage().timeouts().implicitlyWait(timeOut, TimeUnit.SECONDS);
    try {
      driver.findElement(by).isDisplayed();
      result = true;
    } catch (Exception e) {
      result = false;
    }
    //driver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
    return result;
  }

  public boolean existsElement(By by) {
    return existsElement(by, implicitWait);
  }

  public void scrollToElement(By by) {
    WebElement element = driver.findElement(by);
    ((JavascriptExecutor) driver.getcustomWebdriver()).executeScript("arguments[0].scrollIntoView(true);", element);
  }

  public WebElement findElement(By by) {
    WebElement webElement;
    try {
      this.waitForPageLoad();
      webElement = driver.findElement(by);
    } catch (NoSuchElementException | TimeoutException e) {
      this.existsElement(by, implicitWait);
      webElement = driver.findElement(by);
    } catch (UnhandledAlertException e) {
      //this.makeScreenshot();
      this.acceptAlert(2);
      webElement = driver.findElement(by);
    } catch (ElementNotSelectableException e) {
      this.waitForEditable(by);
      webElement = driver.findElement(by);
    } catch (ElementNotInteractableException e) {
      this.scrollToElement(by);
      webElement = driver.findElement(by);
    }
    return webElement;
  }

  public void focusByElementJS(WebElement element) {
    JavascriptExecutor executor = (JavascriptExecutor) driver.getcustomWebdriver();
    executor.executeScript("arguments[0].focus();", element);
  }

  public void focusByIdElementJS(String id) {
    ((JavascriptExecutor) driver.getcustomWebdriver()).executeScript("document.getElementById('" + id + "').focus();");
  }

  public void focusToElementByActions(WebElement element) {
    Actions actions = new Actions(driver.getcustomWebdriver());
    actions.moveToElement(element).build().perform();
    sleep(2000);
  }

  public void clickJSElementRetry(WebElement element, By condicionBy) {
    int cont = 0;
    while (existsElement(condicionBy, 20) && cont < NUM_INTENTOS) {
      cont++;
      clickByJS(element);
      sleep(3000);
    }
  }

  /**
   * Metodo para seguir haciendo click hasta que exista el elemento de la condicion "elementCondition"
   *
   * @param element
   * @param elementCodicion
   */
  public void clickElementRetry(WebElement element, By elementCodicion) {
    clickElementRetry(element, elementCodicion, NUM_INTENTOS);
  }

  /**
   * Metodo para seguir haciendo click hasta que exista el elemento de la condicion "elementCondition"
   *
   * @param element
   * @param elementCodicion
   * @param numIntentos
   */
  public void clickElementRetry(WebElement element, By elementCodicion, int numIntentos) {
    int cont = 0;
    while (existsElement(elementCodicion) && cont < numIntentos) {
      cont++;
      element.click();
      sleep(2500);
    }
  }

  /**
   * Metodo para seguir haciendo click hasta que exista el elemento de la condicion "elementCondition"
   *
   * @param element
   * @param elementCodicion
   */
  public void clickElementJSRetry(WebElement element, By elementCodicion) {
    clickElementRetry(element, elementCodicion, NUM_INTENTOS);
  }

  /**
   * Metodo para seguir haciendo click hasta que exista el elemento de la condicion "elementCondition"
   *
   * @param element
   * @param elementCodicion
   * @param numIntentos
   */
  public void clickElementJSRetry(WebElement element, By elementCodicion, int numIntentos) {
    int cont = 0;
    while (existsElement(elementCodicion) && cont < numIntentos) {
      cont++;
      removeAlertJS();
      waitForJStoLoad();
      clickByJS(element);
      sleep(2500);
    }
  }

  /**
   * Metodo para seguir haciendo click hasta que exista el elemento de la condicion "elementCondition"
   *
   * @param element
   * @param elementCodicion
   */
  public void clickElementJSRetryVisible(WebElement element, WebElement elementCodicion) {
    clickElementJSRetryVisible(element, elementCodicion, NUM_INTENTOS);
  }

  /**
   * Metodo para seguir haciendo click hasta que exista el elemento de la condicion "elementCondition"
   *
   * @param element
   * @param elementCodicion
   * @param numIntentos
   */
  public void clickElementJSRetryVisible(WebElement element, WebElement elementCodicion, int numIntentos) {
    int cont = 0;
    while (elementCodicion.isDisplayed() && cont < numIntentos) {
      cont++;
      removeAlertJS();
      waitForJStoLoad();
      clickByJS(element);
      sleep(2500);
    }
  }

  /**
   * Metodo para seguir haciendo click hasta que exista el elemento de la condicion "elementCondition"
   *
   * @param element
   * @param elementCodicion
   */
  public void clickElementRetryVisible(WebElement element, WebElement elementCodicion) {
    clickElementRetryVisible(element, elementCodicion, NUM_INTENTOS);
  }

  /**
   * Metodo para seguir haciendo click hasta que exista el elemento de la condicion "elementCondition"
   *
   * @param element
   * @param elementCodicion
   * @param numIntentos
   */
  public void clickElementRetryVisible(WebElement element, WebElement elementCodicion, int numIntentos) {
    int cont = 0;
    while (elementCodicion.isDisplayed() && cont < numIntentos) {
      cont++;
      removeAlertJS();
      waitForJStoLoad();
      element.click();
      sleep(2500);
    }
  }

  public void sendKeys(WebElement element, String value) {
    element.clear();
    if (getBrowser().toLowerCase().contains("iexplorer")) {
      sendKeysJS(element, value);
      setAttributeValueWebElement(element, value);
      element.sendKeys(Keys.TAB);

    } else {
      element.sendKeys(value);
    }
    sleep(100);
  }

  public void sendKeysJS(WebElement element, String value) {
    JavascriptExecutor js = (JavascriptExecutor) driver.getcustomWebdriver();
    js.executeScript("arguments[1].value = arguments[0]; ", value, element);
  }

  public void setAttributeValueWebElement(WebElement webElement, String valueAtribute) {
    setAttributeWebElement(webElement, "value", valueAtribute);
  }

  public void setAttributeWebElement(WebElement webElement, String atribute, String valueAtribute) {
    JavascriptExecutor js = (JavascriptExecutor) driver.getcustomWebdriver();
    js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);",
            webElement, atribute, valueAtribute);
  }

  public void onChangeByElementJS(WebElement element) {
    if (element.getAttribute("onchange") != null) {
      JavascriptExecutor executor = (JavascriptExecutor) driver.getcustomWebdriver();
      executor.executeScript("arguments[0].onchange();", element);
    }
  }

  public void executeJavaScriptOnChange(WebElement element) {
    executeJavaScriptOnAttribute(element, "onchange");
  }

  public void executeJavaScriptOnAttribute(WebElement element, String atributo) {
    String onClick = element.getAttribute(atributo);
    executeJavaScript(onClick);
    sleep(1000);
  }

  public void onKeyPressByElementJS(WebElement element) {
    if (element.getAttribute("onkeypress") != null) {
      JavascriptExecutor executor = (JavascriptExecutor) driver.getcustomWebdriver();
      executor.executeScript("arguments[0].onkeypress();", element);
    }
  }

  public void onClickByElementJS(WebElement element) {
    if (element.getAttribute("onclick") != null) {
      JavascriptExecutor executor = (JavascriptExecutor) driver.getcustomWebdriver();
      executor.executeScript("arguments[0].onclick();", element);
    }
  }

  public Boolean isEmptyElement(By by) {
    String value = getTextFromByElement(by);
    return value.isEmpty();
  }

  public String getTextFromByElement(By by) {
    String content = "";
    WebElement webElement = findElement(by);
    content = webElement.getAttribute("value");
    if (content == null) {
      content = webElement.getText();
    }
    return content;
  }

  public void removeReadOnly(WebElement element) {
    ((JavascriptExecutor) driver.getcustomWebdriver()).executeScript(
            "arguments[0].removeAttribute('readonly','readonly')", element);
    sleep(1000);
  }

  public void sendKeysInteger(WebElement element, Integer value) {
    element.clear();
    if (getBrowser().toLowerCase().contains("iexplorer")) {
      sendKeysJSInteger(element, value);
      setAttributeValueWebElementInteger(element, value);
      element.sendKeys(Keys.TAB);
    }
    sleep(100);
  }

  public void sendKeysJSInteger(WebElement element, Integer value) {
    JavascriptExecutor js = (JavascriptExecutor) driver.getcustomWebdriver();
    js.executeScript("arguments[1].value = arguments[0]; ", value, element);
  }

  public void setAttributeValueWebElementInteger(WebElement webElement, Integer valueAtribute) {
    setAttributeWebElementInteger(webElement, "value", valueAtribute);
  }

  public void setAttributeWebElementInteger(WebElement webElement, String atribute, Integer valueAtribute) {
    JavascriptExecutor js = (JavascriptExecutor) driver.getcustomWebdriver();
    js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);",
            webElement, atribute, valueAtribute);
  }

  public Boolean isDialogModalPresent() {
    return existsElement(By.id(FRAME_DIALOGMODAL_ID), 3);
  }

  public void changeToDefaultFrame() {
    driver.switchTo().defaultContent();
  }

  public void changeToDialogModalFrame() {
    waitForClickable(getFrameDialogModal());
    driver.switchTo().frame(getFrameDialogModal());
  }

  public void changeToAppAreaFrame() {
    waitForClickable(getFrameAppArea());
    driver.switchTo().frame(getFrameAppArea());
    sleep(1000);
  }

  public void waitAndChangeToAppAreaFrame() {
    waitAndChangeToFrame(getFrameAppArea());
  }

  public void waitAndchangeToDialogModalFrame() {
    waitAndChangeToFrame(getFrameDialogModal());
  }

  public void clickElementToOpenPopupNotExp(WebElement element, int seg, Boolean clickWithJS, Boolean clickWithActions, String executeScript) {
    try {
      final int countWindows = driver.getWindowHandles().size();
      List<String> windowsNameList = getWindowsNameList(countWindows);
      clickTypeOpenPopup(element, clickWithJS, executeScript, clickWithActions);
      WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), seg);
      ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
        public Boolean apply(WebDriver webDriver) {
          int windowNow = webDriver.getWindowHandles().size();
          return windowNow > countWindows;
        }
      };
      wait.until(expectation);
      int newCountWindows = driver.getWindowHandles().size();
      for (int x = newCountWindows; x > 0; x--) {
        String windowArray = driver.getWindowHandles().toArray()[x - 1].toString();
        boolean found = false;
        for (String winHandle : windowsNameList) {
          if (windowArray.equals(winHandle)) {
            found = true;
          }
        }
        if (!found) {
          driver.switchTo().window(windowArray);
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Exception thrown for: '%s'", testCaseName));
    }
  }

  private WebElement getFrameDialogModal() {
    return driver.findElement(By.id(FRAME_DIALOGMODAL_ID));
  }

  private WebElement getFrameAppArea() {
    return driver.findElement(By.id(FRAME_APPAREA_ID));
  }

  /**
   * Metodo para rellenar el campo de texto en IE cuando este no se rellene correctamente con sendKeys
   *
   * @param element
   * @param value
   */
  public void sendKeysJSWhenBrowserIsIE(WebElement element, String value) {
    element.sendKeys(value);
    if (getBrowser().toLowerCase().contains("iexplorer")) {
      if (element.getText().isEmpty() || !value.equals(element.getText())) {
        element.clear();
        sendKeysJS(element, value);
        setAttributeValueWebElement(element, value);
        element.sendKeys(Keys.TAB);
      }
    }
    sleep(500);
  }

  public void waitAndChangeToFrame(WebElement element) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
    try {
      wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(element));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Frame not available for: '%s'", testCaseName));
    }
    sleep(1000);
  }

  /**
   * Metodo para esperar a que este disponible el elemento
   * si salta la excepcion de tiempo devuelve verdadero y sino
   * devuelve falso
   *
   * @param locator
   * @return
   */
  public Boolean isWaitForEditableRetry(By locator) {
    boolean result = true;
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        return (webDriver.findElement(locator).getAttribute("readonly") == null);
      }
    };
    try {
      wait.until(expectation);
      result = false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Element not editable for: '%s'", testCaseName));
    }
    return result;
  }

  /**
   * Metodo que espera a que el elemento este disponible y si salta la excepcion lo
   * intenta hasta NUM_INTENTOS
   *
   * @param locator
   */
  public void waitForEditableRetry(By locator) {
    int cont = 0;
    while (isWaitForEditableRetry(locator) && cont < NUM_INTENTOS) {
      cont++;
    }
  }

  public void defaultContentWindowAndChangeTo(String windowName) {
    driver.close();
    driver.switchTo().window(windowName);
    driver.switchTo().defaultContent();
  }

  public void waitForEditableCathExp(By locator) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        return (webDriver.findElement(locator).getAttribute("readonly") == null);
      }
    };
    try {
      wait.until(expectation);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Element not editable for: '%s'", testCaseName));
    }
  }

  public Date getTodayNewDate() {
    Date date = new Date();
    DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    String fecha = format.format(date);
    try {
      date = format.parse(fecha);
    } catch (ParseException e) {
      LOGGER.log(Level.SEVERE, String.format("Error parsing date for: '%s'", testCaseName));
    }
    return date;
  }

  public void selectDropDownByVisibleText(WebElement element, String value) {
    Select dropdown = new Select(element);
    dropdown.selectByVisibleText(value);
  }

  public void clickElementToOpenPopup(WebElement element, int seg, Boolean clickWithJS, Boolean clickWithActions, Boolean doubleClick, String executeScript, Boolean reintentos, By by) {
    final int countWindows = driver.getWindowHandles().size();
    List<String> windowsNameList = getWindowsNameList(countWindows);

    clickTypeOpenPopup(element, clickWithJS, executeScript, clickWithActions, doubleClick, reintentos, by);

    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), seg, 100);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        int windowNow = webDriver.getWindowHandles().size();
        return windowNow > countWindows;
      }
    };
    wait.until(expectation);
    int newCountWindows = driver.getWindowHandles().size();
    for (int x = newCountWindows; x > 0; x--) {
      String windowArray = driver.getWindowHandles().toArray()[x - 1].toString();
      boolean found = false;
      for (String winHandle : windowsNameList) {
        if (windowArray.equals(winHandle)) {
          found = true;
        }
      }
      if (!found) {
        driver.switchTo().window(windowArray);
      }
    }
  }

  /**
   * Metodo para convertir un string fecha
   *
   * @param fecha
   * @return
   */
  public Date toDateString(String fecha) {
    Date date = null;
    DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    try {
      date = format.parse(fecha);
    } catch (ParseException e) {
      LOGGER.log(Level.SEVERE, String.format("Error parsing date for: '%s'", testCaseName));
    }
    return date;
  }

  /**
   * Metodo para que espere a que este cargado el Frame
   *
   * @param frame
   */
  public void waitFrameAndSwitchToIt(By frame) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), WAIT_60_SEG);
    try {
      wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
    } catch (TimeoutException e) {
      LOGGER.log(Level.SEVERE, String.format("Frame not available for: '%s'", testCaseName));
    }
  }

  public void onKeyupByElementJS(WebElement element) {
    if (element.getAttribute("onkeyup") != null) {
      JavascriptExecutor executor = (JavascriptExecutor) driver.getcustomWebdriver();
      executor.executeScript("arguments[0].onkeyup();", element);
      sleep(1000);
    }
  }

  public String getTextUploadPath() {
    return getResource("src/main/resources/text/liquidacion.txt");
  }

  private String getResource(String path) {
    File file = new File(path);
    return file.getAbsolutePath();
  }

  /**
   * Metodo para cambiar al frame por defecto y
   * esperar que este disponible el pasado por parametro
   * y cambiar a este
   *
   * @param frame
   */
  public void defaultFrameWaitFrameNewAndSwitchToIt(By frame) {
    driver.switchTo().defaultContent();
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), WAIT_60_SEG);
    try {
      wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
    } catch (TimeoutException e) {
      LOGGER.log(Level.SEVERE, String.format("Frame not available for: '%s'", testCaseName));
    }
  }

  public void waitForClickableCatchExp(WebElement element) {
    waitForClickableCatchExp(element, pageLoadTimeout);
  }

  public void moveToElementAndClickByActions(WebElement element) {
    Actions actions = new Actions(driver.getcustomWebdriver());
    actions.moveToElement(element).click().build().perform();
    sleep(2000);
  }

  public void focusAndClickByActionss(WebElement element) {
    focusToElementByActions(element);
    moveToElementAndClickByActions(element);
  }

  public void onMouseupkByElementJS(WebElement element) {
    if (element.getAttribute("onmouseup") != null) {
      JavascriptExecutor executor = (JavascriptExecutor) driver.getcustomWebdriver();
      executor.executeScript("arguments[0].onmouseup();", element);
    }
  }

  public void waitUntilElementExists(final By locator, int waitUntilSeconds) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), waitUntilSeconds);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        boolean result;
        webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        try {
          webDriver.findElement(locator).isDisplayed();
          result = true;
        } catch (Exception e) {
          result = false;
        }
        webDriver.manage().timeouts().implicitlyWait(implicitWait, TimeUnit.SECONDS);
        return result;
      }
    };
    wait.until(expectation);
  }

  public void waitForClickableCatchExp(WebElement element, int segWait) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), segWait);
    try {
      wait.until(ExpectedConditions.elementToBeClickable(element));
    } catch (TimeoutException e) {
      LOGGER.log(Level.SEVERE, String.format("Element not clicable for: '%s'", testCaseName));
    }
  }

  public String getNowHourMinuteByTimeZone(String timezone, Boolean reduce) {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
    if (reduce)
      cal.add(Calendar.MINUTE, -1);

    String hora = cal.get(Calendar.HOUR_OF_DAY) < 10 ?
            "0" + cal.get(Calendar.HOUR_OF_DAY) :
            String.valueOf(cal.get(Calendar.HOUR_OF_DAY));

    String minuto = cal.get(Calendar.MINUTE) < 10 ?
            "0" + cal.get(Calendar.MINUTE) :
            String.valueOf(cal.get(Calendar.MINUTE));

    return hora + ":" + minuto;
  }

  public String getImageUploadPath() {
    return getResource("/images/coche.jpg");
  }

  /**
   * Metodo para reintentar hacer click en un elemento dado el webElement y by
   * El elemento debe dejar de ser visible cuando se haga click para que el metodo funcione
   *
   * @param element
   * @param by
   */
  public void clickRetryElement(WebElement element, By by) {
    int cont = 0;
    while (existsElement(by, 20) && element.isDisplayed() && cont < NUM_INTENTOS) {
      scrollToElement(by);
      try {
        element.click();
      } catch (Exception e) {
        focusAndClickByActionss(element);
      }
      cont++;
      sleep(2000);
    }
  }

  public void focusAndDoubleClickByActionss(WebElement element) {
    focusToElementByActions(element);
    moveToElementAndDoubleClickByActions(element);
  }

  private void clickTypeOpenPopup(WebElement element, Boolean clickWithJS, String executeScript, Boolean clickWithActions, Boolean doubleClick, Boolean reintentos, By by) {
    sleep(1000);
    if (clickWithJS) {
      clickByJS(element);
    } else if (clickWithActions) {
      focusAndClickByActionss(element);
    } else if (doubleClick) {
      focusAndDoubleClickByActionss(element);
    } else if (executeScript != null) {
      executeJavaScript(executeScript);
    } else {
      if (!reintentos && by == null) {
        element.click();
      } else {
        clickRetryElement(element, by);
      }
    }
    acceptAlert(15);
    sleep(1000);
  }

  protected void closePopUpIfExist() {
    try {
      driver.findElement(By.xpath(POPUP_XPATH)).click();
      espera(3);
    } catch (Exception e) {
      espera(3);
    }
  }

  public String randomTimmer() {
    double min = 0;
    double max = 15;
    double diff = max - min;
    DecimalFormat formatter = new DecimalFormat("#0.000");  // edited here.
    double randomValue = min + Math.random() * diff;
    // double tempRes = Math.floor(randomValue * 10);
    double tempRes = Double.parseDouble(formatter.format(randomValue)) * 1000;
    while (tempRes < 1000) {
      tempRes = tempRes * 10;
    }
    DecimalFormat formatter2 = new DecimalFormat("###");
    return formatter2.format(tempRes);
  }

  public String getAttribtueValue(WebElement element, String attribute, String value) {
    return element.getAttribute(attribute);
  }

  public void defaultFrameAndWaitFrameAndSwitchToIt(By frame) {
    driver.switchTo().defaultContent();
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), 120);
    try {
      wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
    } catch (TimeoutException e) {
      LOGGER.log(Level.SEVERE, String.format("Frame not available for: '%s'", testCaseName));
    }
  }

  public boolean isTextPresent(WebElement element, String text) {
    boolean r = false;
    if ((element != null) && (text != null)) {
      String textElement = element.getText();
      if (textElement.contains(text)) {
        r = true;
      }
    }
    return r;
  }

  public boolean isTextPresentInBody(String text) {
    boolean found = false;

    WebElement element = waitAndGetElement(By.xpath("//body"),
            timeOutInSeconds * 2L);

    found = isTextPresent(element, text);

    return found;
  }

  public WebElement waitAndGetElement(By by, long timeOutInSeconds) {
    WebElement webElement = null;
    if ((this.driver != null) && (by != null)) {
      WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeOutInSeconds);
      isElementPresent(by);
      webElement = this.driver.findElement(by);
    }
    return webElement;
  }

  public WebElement waitUntilTextIsPresent(By by, String text, long timeOutInSeconds) {
    WebElement webElement = null;
    if ((this.driver != null) && (by != null)) {
      WebDriverWait wait = new WebDriverWait((WebDriver) driver, timeOutInSeconds);
      wait.until(ExpectedConditions.textToBePresentInElementLocated(by, text));
      webElement = this.driver.findElement(by);
    }
    return webElement;
  }

  public void select(String id, String textOption, long timeOutInSeconds) throws NotFoundException {
    WebElement element = waitAndGetElement(By.id(id), timeOutInSeconds);
    Select select = new Select(element);
    List<WebElement> options = select.getOptions();
    boolean found = false;
    int size = options.size();
    int i = 0;
    WebElement option = null;
    while ((i < size) && (!found)) {
      option = options.get(i);
      if (option.getText().trim().equals(textOption.trim())) {
        found = true;
      } else {
        i++;
      }
    }
    if (found) {
      select.selectByIndex(i);
    } else
      throw new NotFoundException();
  }

  public Alert getAlert() {
    return this.driver.switchTo().alert();
  }

  public void changeWindow() {
    for (String handle : driver.getWindowHandles()) {
      driver.switchTo().window(handle);
    }
  }

  /**
   * Return <code>true</code> if the element is found.
   * <p>
   *
   * @param by element to find
   * @return <code>true</code> when element is found, otherwise
   * <code>false</code>
   */
  public boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  /**
   * Waits until a requested page is loaded by the browser.
   * <p>
   */
  public void waitForPageLoaded() {
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver driver) {
        return ((JavascriptExecutor) driver).executeScript(
                "return document.readyState").equals("complete");
      }
    };

    Wait<WebDriver> wait = new WebDriverWait((WebDriver) driver, timeOutInSeconds);
    try {
      wait.until(expectation);
    } catch (Throwable error) {
      fail("Timeout waiting for Page Load Request to complete.");
    }
  }

  /**
   * Waits until the requested element is visible.
   * <p>
   */
  public WebElement waitElementVisibility(By by) {
    WebDriverWait wdwait = new WebDriverWait((WebDriver) driver, timeOutInSeconds);
    WebElement we = null;

    we = wdwait.until(ExpectedConditions.visibilityOfElementLocated(by));
    return we;
  }

  public void hover(WebElement element) {
    Actions action = new Actions(this.driver.getcustomWebdriver());
    action.moveToElement(element).perform();
  }

  public void waitForElementTextToChange(final By locator, String previousText) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
    ExpectedCondition<Boolean> elementTextEqualsString = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        return Objects.equals(webDriver.findElement(locator).getText(), previousText);
      }
    };
    wait.until(elementTextEqualsString);
  }

  // OBTENCIÓN DATOS
  public String getTodayDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    return sdf.format(new Date());
  }

  public String getYesterdayDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    return sdf.format(yesterday());
  }

  public String getRelativeTodayDate(int yearOffset, int monthOffset, int dayOffset) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    Calendar cal = Calendar.getInstance();
    if (yearOffset > 0)
      cal.add(Calendar.YEAR, yearOffset);
    if (monthOffset > 0)
      cal.add(Calendar.MONTH, monthOffset);
    if (dayOffset > 0)
      cal.add(Calendar.DAY_OF_MONTH, dayOffset);
    // Evitar fines de semana para algunas pruebas
    if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
      cal.add(Calendar.DAY_OF_MONTH, 1);
    else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
      cal.add(Calendar.DAY_OF_MONTH, 2);
    return sdf.format(cal.getTime());
  }

  public String getNowHourMinute() {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    return format.format(date);
  }

  public String getNowHourMinuteByTimeZone(String timezone) {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
    String hora = cal.get(Calendar.HOUR_OF_DAY) < 10 ?
            "0" + cal.get(Calendar.HOUR_OF_DAY) :
            String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
    String minuto = cal.get(Calendar.MINUTE) < 10 ?
            "0" + cal.get(Calendar.MINUTE) :
            String.valueOf(cal.get(Calendar.MINUTE));
    return hora + ":" + minuto;
  }

  /**
   * Metodo para obtener la hora actual con zona horaria relativa los parametros de entrada
   *
   * @param timezone
   * @param horaOffset
   * @param minOffset
   * @return
   */
  public String getRelativeHourMinuteByTimeZone(String timezone, int horaOffset, int minOffset) {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timezone));
    cal.add(Calendar.HOUR, horaOffset);
    cal.add(Calendar.MINUTE, minOffset);
    String hora = cal.get(Calendar.HOUR_OF_DAY) < 10 ?
            "0" + cal.get(Calendar.HOUR_OF_DAY) :
            String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
    String minuto = cal.get(Calendar.MINUTE) < 10 ?
            "0" + cal.get(Calendar.MINUTE) :
            String.valueOf(cal.get(Calendar.MINUTE));
    return hora + ":" + minuto;
  }

  public String getNextMonthDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    return sdf.format(NextMonth());
  }

  public String getFirstDayNextMonthDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    return sdf.format(FirstDayNextMonth());
  }

  private Date NextMonth() {
    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, +30);
    return cal.getTime();
  }

  private Date FirstDayNextMonth() {
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    return cal.getTime();
  }

  private Date yesterday() {
    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    return cal.getTime();
  }

  // WAITS - OLD
  public void waitForEditable(final By locator) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        return (webDriver.findElement(locator).getAttribute("readonly") == null);
      }
    };
    wait.until(expectation);
  }

  public void waitForEditable(final By locator, int seg) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), seg);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        return (webDriver.findElement(locator).getAttribute("readonly") == null);
      }
    };
    wait.until(expectation);
  }

  public boolean isAttribtuePresentValue(WebElement element, String attribute, String value) {
    boolean result = false;
    try {
      String valueReal = element.getAttribute(attribute);
      if (valueReal.equalsIgnoreCase(value)) {
        result = true;
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Attribute not present for: '%s'", testCaseName));
    }

    return result;
  }

  public void waitForClickable(WebElement element) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
    wait.until(ExpectedConditions.elementToBeClickable(element));
  }

  public void waitForEditableCatchException(final By locator) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), pageLoadTimeout);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        return (webDriver.findElement(locator).getAttribute("readonly") == null);
      }
    };
    try {
      wait.until(expectation);
    } catch (TimeoutException e) {
      LOGGER.log(Level.SEVERE, String.format("Element not editable for: '%s'", testCaseName));
    }
  }

  public void waitForStaleness(WebElement element) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
    wait.until(ExpectedConditions.stalenessOf(element));
  }

  public void waitStaleElementDisplay(By element) {
    waitStaleElementDisplay(element, implicitWait);
  }

  public void waitForClickableCatchException(WebElement element) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), pageLoadTimeout);
    try {
      wait.until(ExpectedConditions.elementToBeClickable(element));
    } catch (TimeoutException e) {
      LOGGER.log(Level.SEVERE, String.format("Element not clickable for: '%s'", testCaseName));
    }
  }

  public void waitStaleElementDisplay(By element, int timeout) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeout);
    try {
      wait.ignoring(StaleElementReferenceException.class).until(ExpectedConditions.visibilityOfElementLocated(element));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Element not visible for: '%s'", testCaseName));
    }
    sleep(2000);
  }

  public void waitStaleElementReference(By element, int timeout) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeout);
    try {
      wait.ignoring(StaleElementReferenceException.class).until(ExpectedConditions.elementToBeClickable(element));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Element not clickable for: '%s'", testCaseName));
    }
    sleep(1000);
  }

  public Boolean isExceptionWaitStaleElementReference(By element, int timeout) {
    boolean result = false;
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeout);
    try {
      wait.ignoring(StaleElementReferenceException.class).until(ExpectedConditions.elementToBeClickable(element));
    } catch (Exception e) {
      result = true;
    }
    sleep(1000);
    return result;
  }

  public Boolean isExceptionWaitPresenceOfElement(By element) {
    boolean result = false;
    try {
      WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
      wait.until(ExpectedConditions.presenceOfElementLocated(element));
    } catch (Exception e) {
      result = true;
    }
    return result;
  }

  public Boolean isExcepcionWaitForEditable(final By locator) {
    boolean result = false;
    try {
      WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), implicitWait);
      ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
        public Boolean apply(WebDriver webDriver) {
          return (webDriver.findElement(locator).getAttribute("readonly") == null);
        }
      };
      wait.until(expectation);
    } catch (Exception e) {
      result = true;
    }
    return result;
  }

  public boolean waitForJStoLoad() {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), 60);
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
    return wait.until(jQueryLoad) && wait.until(jsLoad);
  }

  public String getAuxValue(String key) {
    return getAuxValue(key, "config.properties");
  }

  public void setAuxValue(String key, String value) {
    setAuxValue(key, value, "config.properties");
  }

  public void waitNotInteractableElementReference(By element, int timeout) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeout);
    try {
      wait.ignoring(ElementNotInteractableException.class).until(ExpectedConditions.elementToBeClickable(element));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Element not clickable for: '%s'", testCaseName));
    }
    sleep(1000);
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

  /**
   * Metodo para esperar hasta que este cargado tanto los jQuery como
   * JS
   *
   * @return
   */
  public boolean waitForJStoLoadIE() {
    boolean result = false;
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), WAIT_60_SEG);
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
        try {
          return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
        } catch (Exception e) {
          return true;
        }
      }
    };
    try {
      result = wait.until(jQueryLoad) && wait.until(jsLoad);
    } catch (TimeoutException e) {
      LOGGER.log(Level.SEVERE, String.format("Exception thrown in JS for: '%s'", testCaseName));
    }
    return result;
  }

  public boolean isElementVisible(By element) {
    return isElementVisible(element, 60);
  }

  public boolean isElementVisible(By element, int timeoutInSeconds) {
    if (elementExists(element, timeoutInSeconds)) { //First, we check the element is located
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

  public boolean elementExists(By element, int timeoutInSeconds) { //locates an element
    driver.manage().timeouts().implicitlyWait(timeoutInSeconds, TimeUnit.SECONDS);
    boolean exists = !driver.findElements(element).isEmpty();
    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
    return exists;
  }

  public boolean elementExists(By element) {
    return elementExists(element, 60);
  }

  public void executeJS(String funcion, WebElement e) {
    ((JavascriptExecutor) driver.getcustomWebdriver()).executeScript(funcion, e);
  }

  public void executeJS(String funcion) {
    ((JavascriptExecutor) driver.getcustomWebdriver()).executeScript(funcion);
  }

  public void waitForAjaxToFinish(int timeoutSeconds) {

    /*check that Ajax has finished loading by using jQuery.active property.
     This is in case jQuery is used in the application under test. If this property is 0 then there are no active Ajax request to the server.*/

    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeoutSeconds);
    wait.until(new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver wdriver) {
        return ((JavascriptExecutor) driver.getcustomWebdriver()).executeScript("return jQuery.active == 0").equals(true);
      }
    });
  }

  public void waitForAjaxToFinish() {
    waitForAjaxToFinish(30);
  }

  public void waitForInvisible(By element) {
    waitForInvisible(element, 60);
  }

  public void waitForClickable(By element, int timeOutInSeconds) {
    //Clickable = located on DOM + visible + clickable
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeOutInSeconds);
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
    WebDriverWait image = new WebDriverWait(driver.getcustomWebdriver(), timeOutInSeconds);
    try {
      image.until(ExpectedConditions.visibilityOfElementLocated(element));
      espera(); //needed for angular
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Element not visible for: '%s'", testCaseName));
      Assert.fail("Element is not visible");
    }
  }

  public boolean waitForJSandJqueryFinish() {
    return waitForJSandJqueryFinish(60);
  }

  public void waitForInvisible(By element, int timeOutInSeconds) {
    WebDriverWait image = new WebDriverWait(driver.getcustomWebdriver(), timeOutInSeconds);
    image.until(ExpectedConditions.invisibilityOfElementLocated(element));
    espera(); //needed for angular
  }

  public void waitForClickableIgnoringStaleElementReference(By element, int timeout) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeout);
    try {
      wait.ignoring(StaleElementReferenceException.class).until(ExpectedConditions.elementToBeClickable(element));
    } catch (Exception ignored) {
    }
    espera(1);
  }

  public void waitForNumberOfWindows(int numberOfWindows) {
    waitForNumberOfWindows(numberOfWindows, 60);
  }

  public boolean waitForJSandJqueryFinish(int timeoutInSeconds) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeoutInSeconds);
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
    return waitFlag;
  }

  public void waitForPageLoad() {
    waitForPageLoad(30);
  }

  public boolean longWait(By element) {

    //The web page isn't loading properly on Firefox browser. The webdriver waits 5 minutes maximum for a page to respond.
    //This lines are needed for a proper wait to the element to be loaded on DOM.
    //This lines wait recursively for a element to exists. 600s max. Once the element is visible, the wait get stopped
    for (int i = 0; i < 60; i++) {
      if (isElementVisible(element)) {
        return true;
      } else {
        espera(10);
      }
    }
    return isElementVisible(element);
  }

  public void waitForNumberOfWindows(int numberOfWindows, int timeOutSeconds) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeOutSeconds);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver driver) {
        return (driver.getWindowHandles().size() == numberOfWindows);
      }
    };
    wait.until(expectation);
    espera(); //needed for angular
  }

  public void waitForPageLoad(int timeoutSeconds) {
    WebDriverWait wait = new WebDriverWait(driver.getcustomWebdriver(), timeoutSeconds);
    ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
      public Boolean apply(WebDriver webDriver) {
        return ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete");
      }
    };
    wait.until(expectation);
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

  /*
   * wait until all Angular page is loaded
   */
  public void waitForAngularPageToLoad() {
    driver.getcustomAngularWebDriver().waitForAngularRequestsToFinish();
  }

  //COMMON ELEMENT ACTIONS - SERGIO CABALLERO
  public void sendKeysAndTrigger(WebElement element, String keys, boolean wait) {
    element.clear();
    element.sendKeys(keys);
    performBackgroundClick(wait);
  }

  public void waitForLocated(By element) {
    //Located = located on DOM
    WebDriverWait image = new WebDriverWait(driver.getcustomWebdriver(), 60);
    try {
      image.until(ExpectedConditions.presenceOfElementLocated(element));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, String.format("Element not located for: '%s'", testCaseName));
      Assert.fail("Element is not located");
    }
  }

  public void uploadLocalFile(WebElement inputFileElement, String filePath) {

    //javascript to show the element
    JavascriptExecutor js = (JavascriptExecutor) driver.getcustomWebdriver();
    js.executeScript("arguments[0].style.display = 'block';", inputFileElement);

    //for headless browser
    ((RemoteWebDriver) driver.getcustomWebdriver()).setFileDetector(new LocalFileDetector());
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


  public String[] getPdfDetails() {
    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver.getcustomWebdriver();

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

}
