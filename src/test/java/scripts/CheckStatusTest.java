package scripts;

import org.testng.annotations.Test;
import pageobjects.BaseTest;

public class CheckStatusTest extends BaseTest {

    private static final String testCaseName = "test1";

    public CheckStatusTest() {
        super(testCaseName);
    }

    @Test
    public void checkStatus() {
        System.out.println("test");
    }
}
