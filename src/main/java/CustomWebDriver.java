import com.custom.fwk.Utils.browsers.custom_CHROME;
import com.custom.fwk.Utils.browsers.custom_EDGE_CHROMIUM;
import com.custom.fwk.Utils.browsers.custom_FIREFOX;
import com.custom.fwk.Utils.browsers.custom_IEXPLORER;
import com.paulhammant.ngwebdriver.NgWebDriver;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Extension of webdriver used in atSistemas tests to inicialize core
 *
 * @author ivan.delviso
 * <p>
 * Refactored by
 * @author Sergio Caballero
 */
public class customWebDriver {

  public static final String BROWSER_FIREFOX = "firefox";
  public static final String BROWSER_IE = "iexplorer";
  public static final String BROWSER_CHROME = "chrome";
  public Logger logger = Logger.getLogger(customWebDriver.class.getName());
  private WebDriver customSeleniumWebdriver;
  private NgWebDriver customAngularWebDriver;
  private RequestSpecification customRestAssured;
  // Declare variables to initiate atSistemas WebDriver
  private String baseURL = "about:blank";
  private String baseBrowser = "chrome";
  private String remoteWebDriverUrl = "http://localhost:4444/wd/hub";
  private String testCaseName;

  public customWebDriver() {
  }

  public WebElement findElement(By var1) {
    return customSeleniumWebdriver.findElement(var1);
  }

  public List<WebElement> findElements(By var1) {
    return customSeleniumWebdriver.findElements(var1);
  }

  public WebDriver.TargetLocator switchTo() {
    return customSeleniumWebdriver.switchTo();
  }

  public Set<String> getWindowHandles() {
    return customSeleniumWebdriver.getWindowHandles();
  }

  public String getWindowHandle() {
    return customSeleniumWebdriver.getWindowHandle();
  }

  public WebDriver.Options manage() {
    return customSeleniumWebdriver.manage();
  }

  public void close() {
    customSeleniumWebdriver.close();
  }

  public void get(String s) {
    customSeleniumWebdriver.get(s);
  }

  public String getCurrentUrl() {
    return customSeleniumWebdriver.getCurrentUrl();
  }

  /**
   * Setup the webdriver to open
   *
   * @param browser            Browser to use in webdriver
   * @param logger             default logger
   * @param remoteWebDriverUrl url where WebDriver is opened
   * @return the WebDriver used
   * @throws MalformedURLException url exception
   */
  public WebDriver setup(String browser, Logger logger, String remoteWebDriverUrl, String proxyPac, String node, boolean validatePDF)
          throws MalformedURLException, InterruptedException {
    this.logger = logger;
    setBaseBrowser(browser);
    this.remoteWebDriverUrl = remoteWebDriverUrl;
    boolean isRealBrowser = true;
    boolean localDriver = setBooleanLocalDrive();

    // Add capabilities for every browser (including test one, only for test)
    switch (browser) {
      case "custom_FIREFOX":
        if (localDriver) {
          customSeleniumWebdriver = new custom_FIREFOX(proxyPac, node).getLocalDriver(getBaseURL(), browser);
        } else {
          customSeleniumWebdriver = new custom_FIREFOX(proxyPac, node).getDriver(getBaseURL(), getRemoteWebDriverUrl());
        }
        break;
      case "custom_CHROME":
        if (localDriver) {
          customSeleniumWebdriver = new custom_CHROME(proxyPac, node, "3", validatePDF).getLocalDriver(getBaseURL(), browser);
        } else {
          customSeleniumWebdriver = new custom_CHROME(proxyPac, node, "3", validatePDF).getDriver(getBaseURL(), getRemoteWebDriverUrl());
        }
        break;
      case "custom_CHROME_4":
        customSeleniumWebdriver =
                new custom_CHROME(proxyPac, node, "4", validatePDF).getDriver(getBaseURL(), getRemoteWebDriverUrl());
        break;
      case "custom_IEXPLORER":
        customSeleniumWebdriver =
                new custom_IEXPLORER(proxyPac, node).getDriver(getBaseURL(), getRemoteWebDriverUrl());
        break;
      case "custom_EDGE_CHROMIUM":
        if (localDriver) {
          customSeleniumWebdriver = new custom_EDGE_CHROMIUM(proxyPac, node, "3").getLocalDriver(getBaseURL(), browser);
        } else {
          customSeleniumWebdriver = new custom_EDGE_CHROMIUM(proxyPac, node, "3").getDriver(getBaseURL(), getRemoteWebDriverUrl());
        }
        break;
      case "custom_EDGE_CHROMIUM_4":
        customSeleniumWebdriver =
                new custom_EDGE_CHROMIUM(proxyPac, node, "4").getDriver(getBaseURL(), getRemoteWebDriverUrl());
        break;
      case "REST":
        customRestAssured = RestAssured.with(); // new RestAssured();
        isRealBrowser = false;
        break;
    }

    if (isRealBrowser) customSeleniumWebdriver.manage().window().fullscreen();

    // Angular
    if (isRealBrowser)
      customAngularWebDriver = new NgWebDriver((JavascriptExecutor) customSeleniumWebdriver);

    return customSeleniumWebdriver;
  }

  private boolean setBooleanLocalDrive() {
    if (System.getProperty("localDriver") != null) {
      return System.getProperty("localDriver").equals("ON");
    }
    return false;
  }

  public String getBaseBrowser() {
    return baseBrowser;
  }

  public void setBaseBrowser(String baseBrowser) {
    this.baseBrowser = baseBrowser;
  }

  public String getBaseURL() {
    return baseURL;
  }

  public void setBaseURL(String baseURL) {
    this.baseURL = baseURL;
  }

  public String getRemoteWebDriverUrl() {
    return remoteWebDriverUrl;
  }

  public void setRemoteWebDriverUrl(String remoteWebDriverUrl) {
    this.remoteWebDriverUrl = remoteWebDriverUrl;
  }

  public WebDriver getcustomSeleniumWebdriver() {
    return customSeleniumWebdriver;
  }

  public void setcustomSeleniumWebdriver(WebDriver driver) {
    customSeleniumWebdriver = driver;
  }

  public String getTestCaseName() {
    return testCaseName;
  }

  public void setTestCaseName(String testCaseName) {
    this.testCaseName = testCaseName;
  }

  public NgWebDriver getcustomAngularWebDriver() {
    return customAngularWebDriver;
  }

  public RequestSpecification getcustomRestAssured() {
    return customRestAssured;
  }

  public WebDriver getcustomWebdriver() {
    return customSeleniumWebdriver;
  }
}
