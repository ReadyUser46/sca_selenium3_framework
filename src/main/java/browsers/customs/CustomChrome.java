package browsers.customs;

import browsers.BrowserCapability;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.util.HashMap;

public class CustomChrome extends BrowserCapability {
  private static final ChromeOptions chromeOptions = new ChromeOptions();
  Proxy proxy = new Proxy();

  public CustomChrome(String proxyPac, String nodeName, String seleniumVersion, boolean validatePDF) {
    super(chromeOptions);
    chromeOptions.setCapability("browserName", "chrome");
    chromeOptions.setCapability("platform", Platform.LINUX);
    proxy.setProxyAutoconfigUrl(proxyPac);
    chromeOptions.setCapability(CapabilityType.PROXY, proxy);

    if (nodeName.isEmpty()) {
      chromeOptions.setCapability("platform", Platform.ANY);
      chromeOptions.setCapability("version", "78.0");
    } else {
      chromeOptions.setCapability("platform", Platform.ANY);
      chromeOptions.setCapability("applicationName", nodeName);
      chromeOptions.setCapability("version", "78.0_debug");

    }

    /*download pdf instead of open in default browser viewer pdf*/
    if (validatePDF) {
      HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
      chromePrefs.put("download.default_directory", "/home/seluser/Downloads/");
      chromePrefs.put("plugins.always_open_pdf_externally", true);
      chromePrefs.put("plugins.download.prompt_for_download", false);
      chromeOptions.setExperimentalOption("prefs", chromePrefs);
      chromeOptions.setCapability("applicationName", "zChromeNodePdf30");
      chromeOptions.setCapability("version", "78.0_debug_pdf");

    }

    /*Needed to avoid initial screen 'your connection is not private..'*/
    if (seleniumVersion.equals("4")) {
      chromeOptions.addArguments("--ignore-ssl-errors=yes");
      chromeOptions.addArguments("--ignore-certificate-errors");
    }

    /*Install extensions when proxypac is different than http://wpad.intrcustom.es/proxysrv.pac
    if (!proxyPac.contains("proxysrv")) {
      chromeOptions.addExtensions(new File("src/test/resources/Proxy_Auto_Auth_2.0.0.0.crx"));
    }*/

  }
}
