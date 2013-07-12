package de.ipk.ag_ba.gui.util;

public class WebCamInfo {
	
	private final String url, name, contentType;
	
	public WebCamInfo(String url, String name, String contentType) {
		this.url = url;
		this.name = name;
		this.contentType = contentType;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getName() {
		return name;
	}
	
	public String getContentType(String defaultContentType) {
		if (contentType == null)
			return defaultContentType;
		else
			return contentType;
	}
}
