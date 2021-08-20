import com.custom.fwk.Utils.commonOES.CommonUtils;
import com.custom.fwk.connectors.beans.TestExecutionBean;
import com.custom.fwk.constants.TestState;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ScriptBaseTest {

  private static final String SLASH = File.separator;
  private static final String DEFAULT_BROWSER = "chrome";
  private static final String REMOTE_WEBDRIVER_URL = "http://localhost:4444/wd/hub";
  private static final String SCREENSHOTS_DIR = "." + SLASH + "target" + SLASH + "site" + SLASH + "images";
  protected String testCaseName = null;
  private String remoteWebDriverUrl;
  private String browser = null;
  private String prefix = null;
  private String configDirectory = null;
  private String startUrl = null;
  private String proxyPac = null;
  private String nodeName;
  private Logger logger;
  private boolean validatePDF;

  public ScriptBaseTest(String testCaseName, boolean validatePDF, String nodeName) {
    setTestCaseName(testCaseName);
    setValidationPDF(validatePDF);
    setNodeName(nodeName);
  }

  /**
   * Take Screenshots from Remote WebDriver
   *
   * @param driver ""
   * @return ""
   */
  @Attachment(value = "Screenshot jpg attachment", type = "image/jpg")
  @Step("Taking a screenshot from Assert")
  public static byte[] takeRemoteScreenshot(customWebDriver driver)
          throws URISyntaxException, IOException {
    try {
      String filename = generateRandomFilename("ForceFail");
      WebDriver augmentedDriver = new Augmenter().augment(driver.getcustomWebdriver());
      File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
      FileUtils.copyFile(screenshot, new File(SCREENSHOTS_DIR + SLASH + "2" + filename));
      return Files.readAllBytes(Paths.get(screenshot.toURI()));
    } catch (IOException e) {

    }
    return null;
  }

  /**
   * Take Screenshots from Remote WebDriver
   *
   * @param driver  ""
   * @param message "text to be displayed in the step"
   * @return "screenshot"
   */
  @Attachment(value = "Screenshot jpg attachment", type = "image/jpg")
  @Step("Taking screenshot from: {1}")
  public static byte[] takeRemoteScreenshot(customWebDriver driver, String message) throws IOException {

    String filename = generateRandomFilename("ForceFail");
    WebDriver augmentedDriver = new Augmenter().augment(driver.getcustomWebdriver());
    File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
    FileUtils.copyFile(screenshot, new File(SCREENSHOTS_DIR + SLASH + "2" + filename));
    return Files.readAllBytes(Paths.get(screenshot.toURI()));

  }

  /**
   * Generate random name for screenshots
   *
   * @param filename ""
   * @return ""
   */
  public static String generateRandomFilename(String filename) {
    Calendar c = Calendar.getInstance();
    filename = "Test.jpg";
    filename =
            ""
                    + c.get(Calendar.YEAR)
                    + "-"
                    + c.get(Calendar.MONTH)
                    + "-"
                    + c.get(Calendar.DAY_OF_MONTH)
                    + "-"
                    + c.get(Calendar.HOUR_OF_DAY)
                    + "-"
                    + c.get(Calendar.MINUTE)
                    + "-"
                    + c.get(Calendar.SECOND)
                    + "-"
                    + filename;
    return filename;
  }

  public String getTestCaseName() {
    return testCaseName;
  }

  public void setTestCaseName(String testCaseName) {
    this.testCaseName = testCaseName;
  }

  public void setValidationPDF(boolean validatePDF) {
    this.validatePDF = validatePDF;
  }

  public void setNodeName(String nodeName) {
    nodeName = (nodeName == null) ? "" : nodeName;
    this.nodeName = nodeName;
  }

  public void setRemoteWebDriverUrl(String remoteWebDriverUrl) {
    this.remoteWebDriverUrl = remoteWebDriverUrl;
  }

  public String getBrowser() {
    return browser;
  }

  public void setBrowser(String browser) {
    this.browser = browser;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getConfigDirectory() {
    return configDirectory;
  }

  public void setConfigDirectory(String configDirectory) {
    this.configDirectory = configDirectory;
  }

  public void setProxyPac(String proxyPac) {
    this.proxyPac = proxyPac;
  }

  /**
   * WebDriver related functions
   */
  protected customWebDriver customWebDriver() {
    return customWebDriver(0);
  }

  protected customWebDriver customWebDriver(int indice) {
    return customWebDriverInheritableThreadLocal[indice].get();
  }

  protected void setcustomWebDriver(int indice) {
    // customWebdriverList.add(new InheritableThreadLocal<customWebDriver>());
    this.customWebDriverInheritableThreadLocal[indice].set(new customWebDriver());
  }

  /**
   * Getter and Setter from startUrl, remoteWebDriverUrl, testCaseName, testTool and browser.
   */
  public String getStartUrl() {
    return startUrl;
  }

  public void setStartUrl(String startUrl) {
    this.startUrl = startUrl;
  }

  protected void setLogger(Logger logger) {
    this.logger = logger;
  }


  /**
   * Logger related functions
   */
  protected Logger customLog() {
    return logger;
  }

  /**
   * Set up the environment for test based on default settings + parametrized settings
   */
  private void setUpEnvironment() {
    // Set up the general core parameters (only once in the whole run)
    // If system properties are not set (local core), get them from the static fields
    String browserFromParameter =
            System.getProperty("browser"); // esto recupera el -Dbrowser
    if (browserFromParameter == null || browserFromParameter.equalsIgnoreCase("")) {
      browserFromParameter = DEFAULT_BROWSER;
    }
    setBrowser(browserFromParameter);
    // Set Remote URL webdriver from Default parameter
    String remoteWebDriverUrlParameter =
            System.getProperty("WEBDRIVERURL"); // esto recupera el -DWEBDRIVERURL
    if (remoteWebDriverUrlParameter == null || remoteWebDriverUrlParameter.equalsIgnoreCase("")) {
      remoteWebDriverUrlParameter = REMOTE_WEBDRIVER_URL;
    }
    this.setRemoteWebDriverUrl(remoteWebDriverUrlParameter);

    // Set prefix
    String prefix = System.getProperty("prefix"); // esto recupera el -Dprefix
    if (prefix == null || prefix.equalsIgnoreCase("")) {
      prefix = DEFAULT_PREFIX;
    }
    this.setPrefix(prefix);

    // Set config directory
    String configDirectory =
            System.getProperty("configDirectory"); // esto recupera el -DconfigDirectory
    if (configDirectory == null || configDirectory.equalsIgnoreCase("")) {
      configDirectory = DEFAULT_CONFIG_DIRECTORY;
    }
    this.setConfigDirectory(configDirectory);

    // Set proxy
    String proxyPac =
            System.getProperty("proxyPac"); // esto recupera el -DproxyPac
    if (proxyPac == null || proxyPac.equalsIgnoreCase("")) {
      proxyPac = DEFAULT_PROXY_CAP;
    }
    this.setProxyPac(proxyPac);

  }

  /**
   * If assert Fail, we take a capture
   *
   * @throws IOException        ""
   * @throws URISyntaxException ""
   */
  @Step("Comprobando que se cumple la condicion")
  public void assertTrue(Boolean bol, String texto, int indice)
          throws URISyntaxException, IOException, AssertionError {
    try {
      Assert.assertTrue(bol, texto);
      state = TestState.OK;
      // takeRemoteScreenshot(customWebDriver().getcustomWebdriver());
      // return true;
    } catch (AssertionError e) {
      state = TestState.FAIL;
      if (!browser.equals("REST")) takeRemoteScreenshot(customWebDriver(indice));
      // return false;
      throw new AssertionError(e.getMessage());
    }
  }

  public void assertTrue(Boolean bol, String texto)
          throws URISyntaxException, IOException, AssertionError {
    assertTrue(bol, texto, 0);
  }

  /**
   * If assert Fail, we take a capture
   *
   * @throws IOException        ""
   * @throws URISyntaxException ""
   */
  @Step("Comprobando que se cumple la condicion")
  public void assertTrue(Boolean bol, String texto, Boolean alwaysCapture, int indice)
          throws URISyntaxException, IOException, AssertionError {
    try {
      Assert.assertTrue(bol, texto);
      state = TestState.OK;
      if (alwaysCapture) {
        takeRemoteScreenshot(customWebDriver(indice));
      }
      // return true;
    } catch (AssertionError | Exception e) {
      state = TestState.FAIL;
      takeRemoteScreenshot(customWebDriver(indice));
      // return false;
      throw new AssertionError(e.getMessage());
    }

  }


  /**
   * This assert displays custom step and custom error message
   *
   * @param bol      "condition"
   * @param msgError "error to display if fails"
   * @param step     "custom step to display in allure"
   */
  @Step("{1}")
  public void assertTrue(Boolean bol, String step, String msgError) throws URISyntaxException, IOException, AssertionError {
    try {
      Assert.assertTrue(bol, msgError);
      takeRemoteScreenshot(customWebDriver());
    } catch (AssertionError | Exception e) {
      takeRemoteScreenshot(customWebDriver());
      throw new AssertionError(e.getMessage());
    }

  }


  public void assertTrue(Boolean bol, String texto, Boolean alwaysCapture)
          throws URISyntaxException, IOException, AssertionError {
    assertTrue(bol, texto, alwaysCapture, 0);
  }

  @Step("Comprobando que la comparación es igual")
  public void assertEquals(String var0, String var1, Boolean alwaysCapture, int indice)
          throws URISyntaxException, IOException, AssertionError {
    try {
      Assert.assertEquals(var0, var1, null);
      state = TestState.OK;
      if (alwaysCapture) {
        takeRemoteScreenshot(customWebDriver(indice));
      }
      // return true;
    } catch (AssertionError | Exception e) {
      state = TestState.FAIL;
      if (!browser.equals("REST")) takeRemoteScreenshot(customWebDriver(indice));
      // return false;
      throw new AssertionError(e.getMessage());
    }

  }


  public void assertEquals(String var0, String var1, Boolean alwaysCapture)
          throws URISyntaxException, IOException, AssertionError {
    assertEquals(var0, var1, alwaysCapture, 0);
  }

  public void setup(Method m, int indice) throws IOException {
    boolean isRealBrowser = true;
    try {
      state = TestState.RUNNING;
      setLogger(Logger.getLogger("setup"));
      setcustomWebDriver(indice);
      customWebDriver(indice).setup(getBrowser(), customLog(), remoteWebDriverUrl, proxyPac, nodeName, validatePDF);
      customWebDriver(indice).setTestCaseName(getTestCaseName());
      if (browser.equals("ANDROID_APK")) isRealBrowser = false;
      if (browser.equals("REST")) isRealBrowser = false;
      customWebDriver(indice).setBaseURL(startUrl);

      /*Real init*/
      if (isRealBrowser) customWebDriver(indice).getcustomSeleniumWebdriver().get(startUrl);

      /*Logger*/
      customLog().info(String.format("\n[INIT] Test case = '%s' will be executed\n" +
                      "[INIT] Browser = '%s' | Target url = %s\n" +
                      "[INIT] Webdriver initialized\n" +
                      "[INIT] Raising browser in node's IP: %s \n",
              testCaseName, browser, startUrl, CommonUtils.getNodeIpAddress(customWebDriver())));


    } catch (MalformedURLException | InterruptedException e) {
      customLog().log(Level.SEVERE, "Error creating webdriver");
    }
  }

  /**
   * Before Method to create webdriver
   */
  @BeforeMethod(alwaysRun = true)
  public void setup(Method m) throws IOException {
    setUpEnvironment();
    setup(m, 0);
  }


  /**
   * After Method to close webdriver
   */
  @AfterMethod(alwaysRun = true)
  public void releaseDriver() {
    //for (int i = 0; i < customWebDriverInheritableThreadLocal.length; i++) {
    try {
      if (!browser.equals("REST")) {
        takeRemoteScreenshot(customWebDriver(), customWebDriver().getCurrentUrl());
        customWebDriver().getcustomSeleniumWebdriver().quit();
        customLog().log(Level.INFO, String.format("Webdriver Released for: '%s'", testCaseName));
      }
    } catch (Exception e) {
      customLog().log(Level.SEVERE, String.format("Error closing Webdriver for: '%s'", testCaseName));
      state = TestState.BLOCKED;
    } finally {
      if (state == TestState.RUNNING) {
        state = TestState.NOT_EXECUTED;
      }
      // si no queremos usar la test tool????
      // if (getTestTool()) {
      // Test result sent to TestManagement tool
      if (tmConnector != null) {
        TestExecutionBean resultData = makeExecutionResult();
        tmConnector.setExecutionResult(tcBean, resultData, "comments");
      }
    }
  }
  //}

  /**
   * Generate a result bean with value and evidences
   *
   * @return ""
   */
  private TestExecutionBean makeExecutionResult() {
    // Obtener evidencias para incluirlas al bean
    return new TestExecutionBean(state);
  }

  /**
   * Method to skip a test
   *
   * @param reason reason why you need to skip the test
   */
  protected void skipTest(String reason) {
    throw new SkipException(reason);
  }

  protected String readProperties(String property) {
    return readProperties(property, "1");
  }

  /**
   * Method to read a property from file, and if it not exists, then it create the file
   *
   * @param property to read from file
   */
  protected String readProperties(String property, String default_value) {
    String path = getConfigDirectory(); // "C:\\UTILS\\workingFiles\\";
    String propFileName = path + getPrefix() + "_config.properties";
    String propertyToReturn = null;
    try {
      File tmpDir = new File(propFileName);
      boolean exists = tmpDir.exists();
      if (!exists) {
        createPropertyFile(propFileName);
        updateProperty(property, default_value); // ,getPrefix());
      }

      PropertiesConfiguration config = new PropertiesConfiguration(propFileName);
      // get the property value to sent*/
      propertyToReturn = config.getProperty(property).toString(); // prop.getProperty(property);

    } catch (Exception ignored) {
    }
    return propertyToReturn;
  }

  /**
   * Method to update a property value in config file
   *
   * @param property ""
   * @param value    ""
   */
  protected void updateProperty(String property, String value) {
    String path = getConfigDirectory(); // "C:\\UTILS\\workingFiles\\";
    String propFileName = path + getPrefix() + "_config.properties";
    try {
      File tmpDir = new File(propFileName);
      boolean exists = tmpDir.exists();
      if (!exists) {
        createPropertyFile(propFileName);
      }
      // You have to create config.properties file under resources folder or anywhere you want :)
      // Here I'm updating file which is already exist under /Documents
      PropertiesConfiguration config = new PropertiesConfiguration(propFileName);
      config.setProperty(property, value);
      config.save();
    } catch (Exception ignored) {
    }
  }

  /**
   * Method to create a property file if it doesn't exist
   *
   * @param propertyFile ""
   */
  protected void createPropertyFile(String propertyFile) {
    try {
      Properties props = new Properties();
      Date date = new Date();
      DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
      String strDate = dateFormat.format(date);
      props.setProperty("CreationDate", strDate);
      props.store(new FileOutputStream(new File(propertyFile)), "Config file");
    } catch (Exception ignored) {
    }
  }

  /**
   * wait until all Angular page is loaded
   */
  protected void waitForAngularPageToLoad() {
    waitForAngularPageToLoad(0);
  }

  protected void waitForAngularPageToLoad(int indice) {
    customWebDriver(indice).getcustomAngularWebDriver().waitForAngularRequestsToFinish();
  }

}
