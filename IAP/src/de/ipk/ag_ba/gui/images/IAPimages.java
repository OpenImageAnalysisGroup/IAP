package de.ipk.ag_ba.gui.images;

public class IAPimages {
	
	private static boolean ns = false;
	
	public static String saveAsArchive() {
		return "img/ext/gpl2/Add-Files-To-Archive-64.png";
	}
	
	public static String getWebCam() {
		return "img/ext/gpl2/Gnome-Camera-Web-64.png";
		// "img/ext/cctv.png"
	}
	
	public static String getLogout() {
		return "img/ext/gpl2/Gnome-Dialog-Password-64.png";
		// "img/ext/system-lock-screen.png"
	}
	
	public static String getCheckstatus() {
		if (ns)
			return "img/ext/gpl2/Gnome-Utilities-System-Monitor-64.png";
		else
			return "img/ext/network-server-status.png";
	}
	
	public static String getNetworkPCoffline() {
		if (ns)
			return "img/ext/gpl2/Gnome-Network-Offline-64.png";
		else
			return "img/ext/network-workgroup.png";
	}
	
	public static String getNetworkPConline() {
		if (ns)
			return "img/ext/gpl2/Gnome-Network-Transmit-Receive-64.png";
		else
			return "img/ext/network-workgroup-power.png";
	}
	
	public static String getFolderRemoteOpen() {
		return "img/ext/folder-remote-open.png";
	}
	
	public static String getFolderRemoteClosed() {
		return "img/ext/folder-remote.png";
	}
	
	public static String getCloudResult() {
		return "img/ext/applications-office.png";
	}
	
	public static String getCloudResultActive() {
		return "img/ext/applications-office.png";
	}
	
	public static String getPhytochamber() {
		return "img/ext/phyto.png";
	}
	
	public static String getBarleyGreenhouse() {
		return "img/000Grad_3_.png";
	}
	
	public static String getMaizeGreenhouse() {
		return "img/maisMultiple.png";
	}
	
}
