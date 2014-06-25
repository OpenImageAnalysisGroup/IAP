package de.ipk.ag_ba.plugins.outlier.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

public class DataConnection extends URLConnection {
	
	public DataConnection(URL u) {
		super(u);
	}
	
	@Override
	public void connect() throws IOException {
		connected = true;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		String data = url.toString();
		data = data.substring("data:".length());
		try {
			IOurl url = new IOurl(data);
			return ResourceIOManager.getHandlerFromPrefix(url.getPrefix()).getPreviewInputStream(url);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}