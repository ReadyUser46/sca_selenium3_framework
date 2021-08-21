package utils;

import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;

public class Screenshots {

    private static final String SLASH = File.separator;
    private static final String SCREENSHOTS_DIR = "." + SLASH + "target" + SLASH + "site" + SLASH + "images";


    /**
     * Take Screenshots from Remote WebDriver
     *
     * @param driver ""
     * @return ""
     */
    @Attachment(value = "Screenshot jpg attachment", type = "image/jpg")
    @Step("Taking a screenshot from Assert")
    public static byte[] takeRemoteScreenshot(WebDriver driver) throws URISyntaxException, IOException {
        try {
            String filename = generateRandomFilename("ForceFail");
            WebDriver augmentedDriver = new Augmenter().augment(driver);
            File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
            //FileUtils.copyFile(screenshot, new File(SCREENSHOTS_DIR + SLASH + "2" + filename));
            return Files.readAllBytes(Paths.get(screenshot.toURI()));
        } catch (IOException e) {

        }
        return null;
    }

    /**
     * Generate random name for screenshots
     *
     * @param filename ""
     * @return ""
     */
    public static String generateRandomFilename(String filename) {
        Calendar c = Calendar.getInstance();
        filename = "Test.jpg";
        filename =
                ""
                        + c.get(Calendar.YEAR)
                        + "-"
                        + c.get(Calendar.MONTH)
                        + "-"
                        + c.get(Calendar.DAY_OF_MONTH)
                        + "-"
                        + c.get(Calendar.HOUR_OF_DAY)
                        + "-"
                        + c.get(Calendar.MINUTE)
                        + "-"
                        + c.get(Calendar.SECOND)
                        + "-"
                        + filename;
        return filename;
    }


    // SCREENSHOTS
/*    @Attachment(value = "Screenshot jpg attachment", type = "image/jpg")
    @Step("Taking a screenshot from PageObject")
    public byte[] makeScreenshot() {
        try {
            String filename = generateRandomFilename("ForceFail");
            WebDriver augmentedDriver = new Augmenter().augment(driver.getcustomSeleniumWebdriver());
            File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
            //FileUtils.copyFile(screenshot, new File(reportsDir + SLASH + "2" + filename));
            return Files.readAllBytes(Paths.get(screenshot.toURI()));
        } catch (IOException e) {
            //LOGGER.log(Level.SEVERE, String.format("Error taking screenshot for: '%s'", testCaseName));
        }
        return null;
    }*/
}
