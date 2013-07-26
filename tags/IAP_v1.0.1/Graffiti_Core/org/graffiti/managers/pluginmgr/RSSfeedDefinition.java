package org.graffiti.managers.pluginmgr;

public class RSSfeedDefinition {
	
	private String url;
	private String name;
	
	public RSSfeedDefinition(String rssFeedURL, String rssFeedName) {
		url = rssFeedURL;
		name = rssFeedName;
	}
	
	public String getURL() {
		return url;
	}
	
	public String getName() {
		return name;
	}
	
}
