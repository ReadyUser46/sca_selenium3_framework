package utils;

import com.custom.fwk.customWebDriver;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommonUtils {

    protected static final Logger LOGGER = Logger.getLogger(UtilsPageObject.class.getName());

    public static String getBrowserName(customWebDriver allzDriver) {
        Capabilities caps = getRemoteWebDriver(allzDriver).getCapabilities();
        return caps.getBrowserName();
    }

    public static String getBrowserVersion(customWebDriver allzDriver) {
        Capabilities caps = getRemoteWebDriver(allzDriver).getCapabilities();
        return caps.getVersion();
    }

    public static RemoteWebDriver getRemoteWebDriver(customWebDriver allzDriver) {
        return ((RemoteWebDriver) (allzDriver.getcustomWebdriver()));
    }

    public static String getNodeIpAddress(customWebDriver allzDriver) {
        try {
            RemoteWebDriver remoteWebDriver = getRemoteWebDriver(allzDriver);
            OkHttpClient client = new OkHttpClient();
            URL url = new URL("http://lnxdocker.intrcustom.es:61000/grid/api/testsession?session=" + remoteWebDriver.getSessionId().toString());
            Request getRequest = new Request.Builder().url(url).build();
            Response response = client.newCall(getRequest).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseString = response.body().string();
                Gson gson = new Gson();
                GridResponse gridResponse = gson.fromJson(responseString, GridResponse.class);
                //GridResponse gridResponse = deserializeGridResponse(responseString);
                return gridResponse.getProxyId();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting node IP");
        }
        return null;
    }

    public static void uploadLocalFile(customWebDriver allzDriver, WebElement inputFileElement, String filePath, boolean showElement) {

        //javascript to show the element
        if (showElement) {
            JavascriptExecutor js = (JavascriptExecutor) allzDriver.getcustomWebdriver();
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

    public static boolean elementExists(customWebDriver driver, By element, int timeoutInSeconds) {
        driver.manage().timeouts().implicitlyWait(timeoutInSeconds, TimeUnit.SECONDS);
        boolean exists = !driver.findElements(element).isEmpty();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        return exists;
    }


}