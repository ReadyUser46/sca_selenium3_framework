package setup;

import browsers.Browsers;
import browsers.standard.StandarChrome;
import browsers.standard.StandarEdge;
import browsers.standard.StandarFirefox;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SetupWebdriver {

    private static final String REMOTE_WEBDRIVER_URL = "http://localhost:4444/wd/hub";
    private static final int pageTimeout = 90; // The Timeout in seconds when a load page expectation is called
    private static final int implicitTimeout = 40; // The Timeout in seconds when an implicit expectation is called
    private static final int scriptTimeout = 600; // The script Timeout in seconds when a load script expectation is called
    protected WebDriver driver;
    protected String testCaseName;
    private String browserName = null;
    private String targetUrl = null;
    private Logger logger;

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    private void setLogger(Logger logger) {
        this.logger = logger;
    }

    private Logger customLog() {
        return logger;
    }

    @BeforeMethod(alwaysRun = true)
    public void setup() {

        HashMap<String, Browsers> selectBrower = new HashMap<>();
        selectBrower.put("chrome", new StandarChrome());
        selectBrower.put("firefox", new StandarFirefox());
        selectBrower.put("edge", new StandarEdge());

        Browsers browser = selectBrower.get(browserName);
        driver = browser.getLocalDriver();

        /*manage timeouts*/
        driver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(pageTimeout, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);

        /*Logger*/
        setLogger(Logger.getLogger("setup"));
        customLog().info(String.format("\n[INIT] Test case = '%s' will be executed\n" +
                        "[INIT] Browser = '%s' | Target url = %s\n" +
                        "[INIT] Webdriver initialized",
                testCaseName, browserName, targetUrl));


        /*go to target url*/
        driver.get(targetUrl);

    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
            //todo takeRemoteScreenshot(customWebDriver(), customWebDriver().getCurrentUrl());
            driver.quit();
            customLog().log(Level.INFO, String.format("Webdriver Released for: '%s'", testCaseName));
        } catch (Exception e) {
            customLog().log(Level.SEVERE, String.format("Error closing Webdriver for: '%s'", testCaseName));
        }
    }


}
