package de.ipk.ag_ba.gui.util;

public class WebCamInfo {
	
	private final String url;
	private final String name;
	
	public WebCamInfo(String url, String name) {
		this.url = url;
		this.name = name;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getName() {
		return name;
	}
}
