package browsers.standard;

import browsers.BrowserCapability;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class StandarFirefox extends BrowserCapability implements Browser {
  private static final FirefoxOptions firefoxOptions = new FirefoxOptions();

  public StandarFirefox() {
    super(firefoxOptions);

  }

  @Override
  public String getBrowserName() {
    return "firefox";
  }

  @Override
  public WebDriver getLocalDriver() {
    System.setProperty("webdriver.gecko.driver", "C:\\Selenium\\drivers\\geckodriver.exe");
    return new FirefoxDriver();
  }
}
