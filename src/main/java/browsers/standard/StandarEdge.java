package browsers.standard;

import browsers.Browsers;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class StandarEdge extends Browsers {
    private static final EdgeOptions edgeOptions = new EdgeOptions();

    public StandarEdge() {
        super(edgeOptions);

    }

    @Override
    public String getBrowserName() {
        return "edge";
    }

    @Override
    public WebDriver getLocalDriver() {
        System.setProperty("webdriver.edge.driver", "C:\\Selenium\\drivers\\msedgedriver.exe");
        return new EdgeDriver();
    }
}
