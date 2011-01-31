package org.graffiti.plugin.io.resources;

import java.io.InputStream;
import java.net.URL;

import org.HomeFolder;

public class FTPhandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "ftp";
	
	public String getPrefix() {
		return PREFIX;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.isEqualPrefix(getPrefix())) {
			MyByteArrayOutputStream out = new MyByteArrayOutputStream();
			HomeFolder.copyContent(new URL(url.toString()).openStream(), out);
			return new MyByteArrayInputStream(out.getBuff());
		} else
			return null;
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
						throws Exception {
		throw new UnsupportedOperationException("FTP save not supported");
	}
	
	public static IOurl getURL(String httpurl) {
		return new IOurl(httpurl);
	}
}
