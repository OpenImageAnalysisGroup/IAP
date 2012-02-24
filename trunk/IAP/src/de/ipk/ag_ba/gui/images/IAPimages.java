package de.ipk.ag_ba.gui.images;

public class IAPimages {
	
	private static boolean ns = false;
	
	public static String saveAsArchive() {
		return "img/ext/gpl2/Add-Files-To-Archive-64.png";
	}
	
	public static String getDownloadIcon() {
		return "img/ext/gpl2/Gnome-Emblem-Downloads-64.png";
	}
	
	public static String getWebCam() {
		return "img/ext/axis.png";
		// return "img/ext/gpl2/Gnome-Camera-Web-64.png";
		// "img/ext/cctv.png"
	}
	
	public static String getWebCam2() {
		return "img/ext/sp105.png";
	}
	
	public static String getLogout() {
		// return "img/ext/gpl2/Gnome-Dialog-Password-64.png";
		return "img/ext/system-lock-screen.png";
	}
	
	public static String getCheckstatus() {
		if (ns)
			return "img/ext/gpl2/Gnome-Utilities-System-Monitor-64.png";
		else
			return "img/ext/network-server-status.png";
	}
	
	public static String getCloudComputer() {
		return "img/ext/poweredge-r810-overview1.png";
	}
	
	public static String getComputerConsole() {
		return "img/ext/poweredge-r810-overview6.png";
		// return "img/ext/computer.png";
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
	
	public static String getAddNews() {
		return "img/ext/gpl2/Gnome-User-Desktop-64.png";
	}
	
	public static String getArchive() {
		return "img/ext/gpl2/Gnome-System-File-Manager-64.png";
	}
	
	public static String saveToHsmArchive() {
		return "img/ext/gpl2/Gnome-Media-Tape-64.png";
	}
	
	public static String getFileHistory() {
		return "img/ext/gpl2/Gnome-Document-Open-Recent-64.png";
	}
	
	public static String getFileRoller() {
		return "img/ext/gpl2/File-Roller-64.png";
	}
	
	public static String getToolbox() {
		return "img/ext/gpl2/Gnome-Applications-System-64.png";
	}
	
	public static String getTashWith2docs() {
		return "img/ext/trash2.png";
	}
	
	public static String getTashDeleteAll2() {
		return "img/ext/trash-delete-all2.png";
	}
	
	public static String getFileCleaner() {
		return "img/ext/gpl2/Gnome-Edit-Clear-64.png";
	}
	
	public static String getAnalyzeAll() {
		return "img/ext/gpl2/Gnome-Application-X-Executable-64.png";
	}
	
	public static String getCopyToClipboard() {
		return "img/ext/gpl2/Gnome-Edit-Paste-64.png";
	}
	
	public static String getMergeDatasets() {
		return "img/ext/gpl2/Gnome-Text-X-Script-64.png";
	}
}
