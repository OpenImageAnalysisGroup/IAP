package de.ipk.ag_ba.server.gwt;

import java.util.HashMap;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

public class UrlCacheManager {
	HashMap<Long, String> urlId2urlContent = new HashMap<Long, String>();
	HashMap<String, Long> urlContent2urlId = new HashMap<String, Long>();
	HashMap<Long, BinaryMeasurement> urlId2image = new HashMap<Long, BinaryMeasurement>();
	long urlId = 0;
	
	public synchronized long getId(BinaryMeasurement i, String url) {
		if (!urlContent2urlId.containsKey(url)) {
			urlId++;
			urlContent2urlId.put(url, urlId);
			urlId2urlContent.put(urlId, url);
			urlId2image.put(urlId, i);
		}
		return urlContent2urlId.get(url);
	}
	
	public synchronized String getUrl(Long s) {
		return urlId2urlContent.get(s);
	}
	
	public synchronized BinaryMeasurement getImage(Long s) {
		return urlId2image.get(s);
	}
}
