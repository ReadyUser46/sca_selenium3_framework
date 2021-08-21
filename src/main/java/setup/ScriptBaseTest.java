package setup;


import browsers.standard.Browser;
import browsers.standard.StandarChrome;
import browsers.standard.StandarEdge;
import browsers.standard.StandarFirefox;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScriptBaseTest {

    private static final String REMOTE_WEBDRIVER_URL = "http://localhost:4444/wd/hub";
    private static final int pageTimeout = 300000; // The Timeout in miliseconds when a load page expectation is called
    private static final int implicitTimeout = 20000; // The Timeout in miliseconds when an implicit expectation is called
    private static final int scriptTimeout = 600000; // The script Timeout in miliseconds when a load script expectation is called
    protected String testCaseName;
    protected WebDriver driver;
    private String browserName = null;
    private String targetUrl = null;
    private Logger logger;

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }


    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    protected void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected Logger customLog() {
        return logger;
    }


    /**
     * Before Method to create webdriver
     */
    @BeforeMethod(alwaysRun = true)
    public void setup() {
        setLogger(Logger.getLogger("setup"));

        HashMap<String, Browser> selectBrower = new HashMap<>();
        selectBrower.put("chrome", new StandarChrome());
        selectBrower.put("firefox", new StandarFirefox());
        selectBrower.put("edge", new StandarEdge());

        Browser browser = selectBrower.get(browserName);
        driver = browser.getLocalDriver();

        /*manage timeouts*/
        driver.manage().timeouts().implicitlyWait(implicitTimeout, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().pageLoadTimeout(pageTimeout, TimeUnit.MILLISECONDS);
        driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.MILLISECONDS);
        /*Logger*/
        customLog().info(String.format("\n[INIT] Test case = '%s' will be executed\n" +
                        "[INIT] Browser = '%s' | Target url = %s\n" +
                        "[INIT] Webdriver initialized",
                testCaseName, browserName, targetUrl));


        /*go to target url*/
        driver.get(targetUrl);

    }

    /**
     * After Method to close webdriver
     */
    @AfterMethod(alwaysRun = true)
    public void releaseDriver() {
        try {
            //todo takeRemoteScreenshot(customWebDriver(), customWebDriver().getCurrentUrl());
            driver.quit();
            customLog().log(Level.INFO, String.format("Webdriver Released for: '%s'", testCaseName));
        } catch (Exception e) {
            customLog().log(Level.SEVERE, String.format("Error closing Webdriver for: '%s'", testCaseName));
        }
    }

    /**
     * Method to skip a test
     *
     * @param reason reason why you need to skip the test
     */
    protected void skipTest(String reason) {
        throw new SkipException(reason);
    }


}
