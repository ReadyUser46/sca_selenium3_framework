package browsers.customs;

import browsers.BrowserCapability;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.ie.InternetExplorerOptions;

import java.util.LinkedHashMap;

public class CustomIE extends BrowserCapability {
    private static final InternetExplorerOptions iexplorerOptions = new InternetExplorerOptions();
    Proxy proxy = new Proxy();

    public CustomIE(String proxyPac, String node) {
        super(iexplorerOptions);
        LinkedHashMap<String, Integer> mapTimeoutCapabilities = getMapTimeoutCapabilities();
        iexplorerOptions.setCapability("timeouts", mapTimeoutCapabilities);
        iexplorerOptions.introduceFlakinessByIgnoringSecurityDomains();
        proxy.setProxyAutoconfigUrl(proxyPac);
        iexplorerOptions.setProxy(proxy);
        iexplorerOptions.usePerProcessProxy();
        iexplorerOptions.ignoreZoomSettings();

        if (!node.isEmpty()) {
      super.setCapability("applicationName", node);
    }
  }

  private LinkedHashMap<String, Integer> getMapTimeoutCapabilities() {
    LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
    map.put("implicit", 20000); // 20 segundos
    map.put("pageLoad", 300000); // 5 minutos
    map.put("script", 600000); // 10 minutos
    return map;
  }

}
