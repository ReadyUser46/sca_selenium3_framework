package browsers;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class BrowserCapability {

  private DesiredCapabilities capability;
  private MutableCapabilities options;


  //---------------------- CONTRUCTORS ---------------------------

  //Working
  public BrowserCapability(MutableCapabilities options) {
    this.options = options;
  }

  //---------------------- METHODS ---------------------------
  protected void setCapability(String key, Object value) {
    capability.setCapability(key, value);
  }

  public WebDriver getRemoteWebdriver(String baseURL, String remoteWebDriverURl) throws MalformedURLException {
    return new RemoteWebDriver(new URL(remoteWebDriverURl), options);
  }

  public WebDriver getLocalDriver(String browser) throws MalformedURLException {
    WebDriver customWebdriver;
    switch (browser) {
      case "firefox":
        System.setProperty("webdriver.gecko.driver", "C:\\utils\\selenium\\Drivers\\geckodriver.exe");
        customWebdriver = new FirefoxDriver();
        break;
      case "chrome":
        System.setProperty("webdriver.chrome.driver", "C:\\utils\\selenium\\Drivers\\chromedriver.exe");
        customWebdriver = new ChromeDriver();
        break;
      case "edge":
        System.setProperty("webdriver.edge.driver", "C:\\utils\\selenium\\Drivers\\msedgedriver.exe");
        customWebdriver = new EdgeDriver();
        break;
      default:
        throw new IllegalStateException("Unexpected value for browser: " + browser);
    }
    return customWebdriver;
  }

}
