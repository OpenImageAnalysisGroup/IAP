package org.graffiti.plugin.io.resources;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.HttpBasicAuth;

public class HTTPhandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "http";
	
	public String getPrefix() {
		return PREFIX;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.isEqualPrefix(getPrefix())) {
			if (url.getPrefix().contains("@")) {
				String ur = url.getPrefix();
				String userPass = ur.split("@")[0];
				String user = userPass.split(":")[0];
				String pass = userPass.split(":")[1];
				String uuu = url.toString().split("@", 2)[1];
				return HttpBasicAuth.downloadFileWithAuth(uuu, user, pass);
			} else
				return new URL(url.toString()).openStream();
		} else
			return null;
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
			throws Exception {
		throw new UnsupportedOperationException("HTTP save not supported");
	}
	
	public static IOurl getURL(String httpurl) {
		return new IOurl(httpurl);
	}
	
	@Override
	public Long getStreamLength(IOurl url) throws Exception {
		URLConnection conn = new URL(url.toString()).openConnection();
		int size = conn.getContentLength();
		conn.setConnectTimeout(1);
		if (size < 0)
			return null;
		else
			return (long) size;
	}
}
