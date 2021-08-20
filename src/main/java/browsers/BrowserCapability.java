package browsers;

import org.openqa.selenium.By;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class BrowserCapability {

  private static final int pageTimeout = 300000; // The Timeout in miliseconds when a load page expectation is called
  private static final int implicitTimeout = 20000; // The Timeout in miliseconds when an implicit expectation is called
  private static final int scriptTimeout = 600000; // The script Timeout in miliseconds when a load script expectation is called
  private DesiredCapabilities capability;
  private MutableCapabilities options;


  //---------------------- CONTRUCTORS ---------------------------
  //Deprecated
  public BrowserCapability(DesiredCapabilities capability) {
    this.capability = capability;
  }

  //Working
  public BrowserCapability(MutableCapabilities options) {
    this.options = options;
  }

  //---------------------- METHODS ---------------------------
  protected void setCapability(String key, Object value) {
    capability.setCapability(key, value);
  }

  public WebDriver getDriver(String baseURL, String remoteWebDriverURl) throws MalformedURLException {
    WebDriver customWebdriver = new RemoteWebDriver(new URL(remoteWebDriverURl), options); // with no url, it only works for local
    customWebdriver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.MILLISECONDS);
    customWebdriver.manage().timeouts().pageLoadTimeout(pageTimeout, TimeUnit.MILLISECONDS);
    customWebdriver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.MILLISECONDS);
    customWebdriver.get(baseURL);
    return customWebdriver;
  }

  public WebDriver getLocalDriver(String baseURL, String browser) throws MalformedURLException {
    WebDriver customWebdriver;
    switch (browser) {
      case "custom_FIREFOX":
        System.setProperty("webdriver.gecko.driver", "C:\\utils\\selenium\\Drivers\\geckodriver.exe");
        customWebdriver = new FirefoxDriver();
        break;
      case "custom_CHROME":
        System.setProperty("webdriver.chrome.driver", "C:\\utils\\selenium\\Drivers\\chromedriver.exe");
        customWebdriver = new ChromeDriver();
        break;
      case "custom_EDGE_CHROMIUM":
        System.setProperty("webdriver.edge.driver", "C:\\utils\\selenium\\Drivers\\msedgedriver.exe");
        customWebdriver = new EdgeDriver();
        break;
      default:
        throw new IllegalStateException("Unexpected value for browser: " + browser);
    }
    customWebdriver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.MILLISECONDS);
    customWebdriver.manage().timeouts().pageLoadTimeout(pageTimeout, TimeUnit.MILLISECONDS);
    customWebdriver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.MILLISECONDS);
    customWebdriver.get(baseURL);
    return customWebdriver;
  }

  public WebDriver getDriverConfigProxy(String baseURL, String remoteWebDriverURl) throws MalformedURLException, InterruptedException {
    WebDriver customWebdriver = new RemoteWebDriver(new URL(remoteWebDriverURl), options); // with no url, it only works for local
    customWebdriver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.MILLISECONDS);
    customWebdriver.manage().timeouts().pageLoadTimeout(pageTimeout, TimeUnit.MILLISECONDS);
    customWebdriver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.MILLISECONDS);

    //With the .pac file, the browser request proxy authentication. This method install an extension to perform this auth.
    configureAuth(customWebdriver);

    customWebdriver.get(baseURL);
    return customWebdriver;
  }

  private void configureAuth(WebDriver driver) throws InterruptedException {

    Thread.sleep(5000);
    String extensionWindow = null;
    for (String window : driver.getWindowHandles()) {
      Thread.sleep(1000);
      driver.switchTo().window(window);
      if (!driver.getTitle().contains("Proxy Auto")) {
        Thread.sleep(1000);
        driver.close();
      } else {
        extensionWindow = window;
      }
    }

    /*try {
      driver.switchTo().window(initWindow);
      driver.get("chrome-extension://ggmdpepbjljkkkdaklfihhngmmgmpggp/options.html");
    } catch (org.openqa.selenium.TimeoutException te) {
      ((JavascriptExecutor) driver).executeScript("window.stop();");
    }*/
    driver.switchTo().window(extensionWindow);
    driver.findElement(By.id("login")).sendKeys("ratfunctester");
    driver.findElement(By.id("password")).sendKeys("J3nk1ns");
    driver.findElement(By.id("save")).click();

  }

}
