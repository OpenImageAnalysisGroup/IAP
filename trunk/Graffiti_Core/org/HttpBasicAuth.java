package org;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.soap.encoding.soapenc.Base64;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

public class HttpBasicAuth {
	
	public static InputStream downloadFileWithAuth(String urlStr, String user, String pass) throws Exception {
		// URL url = new URL ("http://host:port/path");
		URL url = new URL(urlStr);
		String authStr = user + ":" + pass;
		String authEncoded = Base64.encode(authStr.getBytes());
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.setRequestProperty("Authorization", "Basic " + authEncoded);
		
		InputStream in = connection.getInputStream();
		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		ResourceIOManager.copyContent(in, out);
		
		return new MyByteArrayInputStream(out.getBuffTrimmed());
	}
}
