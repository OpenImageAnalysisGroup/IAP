package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

public enum FeedDownloadType {
	ADDON, PREFERENCE, URL, FEED, DOWNLOAD;
	
	public static FeedDownloadType getType(String btlabel) {
		FeedDownloadType type = FeedDownloadType.DOWNLOAD;
		if (btlabel.equalsIgnoreCase("addon"))
			type = ADDON;
		if (btlabel.equalsIgnoreCase("preference"))
			type = PREFERENCE;
		if (btlabel.equalsIgnoreCase("url"))
			type = URL;
		if (btlabel.equalsIgnoreCase("feed"))
			type = FEED;
		if (btlabel.equalsIgnoreCase("download"))
			type = DOWNLOAD;
		return type;
	}
}
