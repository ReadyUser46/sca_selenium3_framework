package browsers.standard;

import browsers.Browsers;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class StandarChrome extends Browsers {
    private static final ChromeOptions chromeOptions = new ChromeOptions();

    public StandarChrome() {
        super(chromeOptions);
    }

    @Override
    public String getBrowserName() {
        return "chrome";
    }

  @Override
  public WebDriver getLocalDriver() {
    System.setProperty("webdriver.chrome.driver", "C:\\Selenium\\drivers\\chromedriver.exe");
    return new ChromeDriver();
  }
}
