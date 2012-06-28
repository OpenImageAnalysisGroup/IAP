package org.graffiti.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class MyInputStreamCreator {
	
	private String absolutePath;
	private boolean gzip;
	private File file;
	private URL url;
	
	public MyInputStreamCreator(boolean gzip, String absolutePath) {
		this.absolutePath = absolutePath;
		this.gzip = gzip;
	}
	
	public MyInputStreamCreator(File file) {
		this.file = file;
	}
	
	public MyInputStreamCreator(URL url) {
		this.url = url;
	}
	
	public InputStream getNewInputStream() throws IOException {
		if (absolutePath != null) {
			if (gzip)
				return new GZIPInputStream(new FileInputStream(absolutePath));
			else
				return new FileInputStream(absolutePath);
		}
		if (file != null)
			return new FileInputStream(file);
		if (url != null)
			return url.openStream();
		return null;
	}
	
}
