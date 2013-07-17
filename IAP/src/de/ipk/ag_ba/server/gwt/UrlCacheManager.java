package de.ipk.ag_ba.server.gwt;

import java.util.HashMap;

public class UrlCacheManager {
	HashMap<Long, String> urlId2urlContent = new HashMap<Long, String>();
	HashMap<String, Long> urlContent2urlId = new HashMap<String, Long>();
	long urlId = 0;
	
	public synchronized long getId(String url) {
		if (!urlContent2urlId.containsKey(url)) {
			urlId++;
			urlContent2urlId.put(url, urlId);
			urlId2urlContent.put(urlId, url);
		}
		return urlContent2urlId.get(url);
	}
	
	public synchronized String getUrl(Long s) {
		return urlId2urlContent.get(s);
	}
}
