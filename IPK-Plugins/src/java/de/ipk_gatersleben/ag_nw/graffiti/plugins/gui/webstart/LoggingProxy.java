package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;

public class LoggingProxy extends ProxySelector {
	ProxySelector defsel = null;
	
	public LoggingProxy(ProxySelector def) {
		defsel = def;
		// System.out.println("Proxy information will be printed to output for debugging purposes.");
	}
	
	@Override
	public java.util.List<Proxy> select(URI uri) {
		java.util.List<Proxy> defProxy = defsel.select(uri);
		// try {
		// System.out.println("Proxy setting");
		// if (uri==null)
		// System.out.println("   URI: null");
		// else
		// System.out.println("   URI: "+uri.toString());
		// if (defProxy==null) {
		// System.out.println("   Proxy List: null");
		// } else {
		// if (defProxy.size()==0) {
		// System.out.println("   Proxy List: empty");
		// } else {
		// for (int i = 0; i<defProxy.size(); i++) {
		// System.out.println("   Proxy "+(i+1)+": "+defProxy.toString());
		// }
		// }
		// }
		// } catch(Exception e) {
		// e.printStackTrace();
		// }
		return defProxy;
	}
	
	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		if (defsel != null)
			defsel.connectFailed(uri, sa, ioe);
	}
}