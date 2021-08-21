package scripts;

import baseobjects.BaseTest;
import org.testng.annotations.Test;

public class CheckStatusTest extends BaseTest {

    private static final String testCaseName = "Check Status";

    public CheckStatusTest() {
        super(testCaseName);
    }

    @Test
    public void checkStatus() {
        System.out.println("test");
    }
}
