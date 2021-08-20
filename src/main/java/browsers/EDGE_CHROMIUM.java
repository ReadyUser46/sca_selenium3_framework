package browsers;

import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.CapabilityType;

public class custom_EDGE_CHROMIUM extends BrowserCapability {
  private static final EdgeOptions edgeOptions = new EdgeOptions();
  Proxy proxy = new Proxy();

  public custom_EDGE_CHROMIUM(String proxyPac, String node, String seleniumVersion) {
    super(edgeOptions);
    proxy.setProxyAutoconfigUrl(proxyPac);
    edgeOptions.setProxy(proxy);
    edgeOptions.setCapability("platform", Platform.ANY);

    if (!node.isEmpty()) {
      super.setCapability("applicationName", node);
    }

    /*Needed to avoid initial screen 'your connection is not private..'*/
    if (seleniumVersion.equals("4")) {
      edgeOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
      edgeOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

    }
  }
}
