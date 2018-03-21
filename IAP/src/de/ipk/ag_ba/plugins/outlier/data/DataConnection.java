package de.ipk.ag_ba.plugins.outlier.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

/**
 * @author Christian Klukas
 */
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
		try {
			if (data.contains("://")) {
				data = data.substring("data:".length());
				IOurl url = new IOurl(data);
				ResourceIOHandler hh = ResourceIOManager.getHandlerFromPrefix(url.getPrefix());
				return hh.getPreviewInputStream(url);
			} else {
				data = data.replaceFirst("^.*;base64,", "");
				byte[] bytes = Base64.getDecoder().decode(data);
				return new ByteArrayInputStream(bytes);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}