package browsers.standard;

import org.openqa.selenium.WebDriver;

public interface Browser {

   String getBrowserName();

   WebDriver getLocalDriver();
}
