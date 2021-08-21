package utils;

import io.qameta.allure.Step;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;
import org.testng.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommonUtils {

    protected static final Logger LOGGER = Logger.getLogger(UtilsPageObject.class.getName());


    public static void uploadLocalFile(WebDriver driver, WebElement inputFileElement, String filePath, boolean showElement) {

        //javascript to show the element
        if (showElement) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].style.display = 'block';", inputFileElement);
        }

        //for headless browser
        ((RemoteWebElement) inputFileElement).setFileDetector(new LocalFileDetector());

        try {
            inputFileElement.sendKeys(filePath);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Make sure the webElement passed is an input file type", e);
        }
    }


    @Step("{1}}")
    public void assertTrue(Boolean bol, String checkMessage) throws URISyntaxException, IOException, AssertionError {
        try {
            Assert.assertTrue(bol, checkMessage);
        } catch (AssertionError e) {
            throw new AssertionError(e.getMessage());
        }
    }

    @Step("{3}}")
    public void assertEquals(String condition1, String condition2, String assertMessage) throws URISyntaxException, IOException, AssertionError {
        try {
            Assert.assertEquals(condition1, condition2, null);
            // return true;
        } catch (AssertionError | Exception e) {
            throw new AssertionError(e.getMessage());
        }

    }

}