package baseobjects;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import setup.SetupWebdriver;

public class BaseTest extends SetupWebdriver {

    private static final String browerName = "chrome";
    private static final String targetUrl = "www.google.com";

    public BaseTest(String testCaseName) {
        setBrowserName(browerName);
        setTestCaseName(testCaseName);
        setTargetUrl(targetUrl);
    }

    @BeforeMethod(alwaysRun = true)
    @Override
    public void setup() {
        super.setup();
    }

    @AfterMethod(alwaysRun = true)
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
