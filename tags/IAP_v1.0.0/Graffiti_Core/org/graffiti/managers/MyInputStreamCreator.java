package org.graffiti.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.graffiti.plugin.io.resources.IOurl;

public class MyInputStreamCreator {
	
	private String absolutePath;
	private boolean gzip;
	private File file;
	private URL url;
	private IOurl ioURL;
	
	public MyInputStreamCreator(boolean gzip, String absolutePath) {
		this.absolutePath = absolutePath;
		this.gzip = gzip;
	}
	
	public MyInputStreamCreator(File file) {
		this.file = file;
	}
	
	@Override
	public String toString() {
		if (url != null)
			return url.toString();
		else
			if (ioURL != null)
				return ioURL.toString();
			else
				return super.toString();
	}
	
	public MyInputStreamCreator(URL url) {
		this.url = url;
	}
	
	public MyInputStreamCreator(IOurl ioURL) {
		this.ioURL = ioURL;
	}
	
	public InputStream getNewInputStream() throws Exception {
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
		if (ioURL != null)
			return ioURL.getInputStream();
		return null;
	}
	
}
