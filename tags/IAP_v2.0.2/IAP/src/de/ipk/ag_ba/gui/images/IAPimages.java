package de.ipk.ag_ba.gui.images;

import javax.swing.ImageIcon;

import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.structures.Image;

public class IAPimages {
	
	private static boolean ns = false;
	
	/**
	 * Please use getIcon, if possible, as this function supports high resolution displays.
	 */
	@Deprecated
	public static java.awt.Image getImage(String image) {
		return IAPservice.getImage(IAPmain.class, image);
	}
	
	/**
	 * Please use getIcon, if possible, as this function supports high resolution displays.
	 */
	@Deprecated
	public static java.awt.Image getImage(String image, int maxSize) {
		java.awt.Image img = IAPservice.getImage(IAPmain.class, image);
		Image f = new Image(img);
		return f.resize(maxSize, maxSize, true).getAsBufferedImage(true);
	}
	
	public static Image getImageIAP(String image, int maxSize) {
		java.awt.Image img = IAPservice.getImage(IAPmain.class, image);
		Image f = new Image(img);
		return f.resize(maxSize, maxSize, true);
	}
	
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
		// if (ns )
		return "img/ext/gpl2/Gnome-Utilities-System-Monitor-64.png";
		// else
		// return "img/ext/network-server-status.png";
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
		return "img/ext/folder-remote-open_t.png";
	}
	
	public static String getFolderRemoteClosed() {
		return "img/ext/folder-remote_t.png";
	}
	
	public static String getCloudResult() {
		return "img/ext/applications-office.png";
	}
	
	public static String getCloudResultActive() {
		return "img/ext/applications-office.png";
	}
	
	public static String getPhytochamber() {
		return "img/ext/arabidopsis.middle.blue.cover.png";// phyto.png";
	}
	
	public static String getBarleyGreenhouse() {
		return "img/000Grad_3_.png";
	}
	
	public static String getRoots() {
		return "img/root.png";
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
	
	public static String getFileRoller3() {
		return "img/ext/gpl2/File-Roller-64-warning.png";
	}
	
	public static String getToolbox() {
		return "img/ext/gpl2/Gnome-Applications-System-64.png";
	}
	
	public static String getApplications() {
		return "img/ext/gpl2/Gnome-Applications-Other-64.png";
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
	
	public static String getClimaImport() {
		return "img/ext/gpl2/Gnome-Weather-Few-Clouds-64.png";
	}
	
	public static String copyToServer() {
		return "img/ext/transfer2.png";
	}
	
	public static String getNetworkedServers() {
		return "img/ext/network.png";
	}
	
	public static String getWLAN() {
		return "img/ext/gpl2/Gnome-Network-Wireless-64.png";
	}
	
	public static String getBookIcon() {
		return "img/ext/gpl2/Gnome-Applications-Office-64.png";
	}
	
	public static String getAdressBookClearFront() {
		return "img/ext/gpl2/Gnome-X-Office-Address-Book-64_metaData.png";
	}
	
	public static String getCloseCross() {
		return "img/ext/gpl2/Gnome-Window-Close-64.png";
	}
	
	public static String getComputerOffline() {
		return "img/ext/gpl2/Gnome-Network-Offline-64.png";
	}
	
	public static String getCamera() {
		return "img/ext/camera.png";
	}
	
	public static String getSystemWheel() {
		return "img/ext/applications2.png";
	}
	
	public static String getClock() {
		return "img/ext/gpl2/time.png";
	}
	
	public static String getHistogramIcon() {
		return "img/ext/applications-office.png";
	}
	
	public static String getThreeDocuments() {
		return "img/ext/gpl2/Gnome-Emblem-Documents-64.png";
	}
	
	public static String getLeafDiseaseImage() {
		return "img/leaf_analysis.png";
	}
	
	public static String getRapeseedImage() {
		return "img/rapeseed.png";
	}
	
	public static String getImageFromFileExtensionGenericIfNotKnown(String extension) {
		String r = getImageFromFileExtension(extension);
		if (r != null)
			return r;
		else
			return "img/ext/gpl2/Gnome-Document-New-64.png";
	}
	
	public static String getImageFromFileExtension(String extension) {
		if (extension.equals(".zip") || extension.equals(".tar") || extension.equals(".gz"))
			return "img/ext/gpl2/Gnome-Emblem-Package-64.png";
		if (extension.equals(".tmp") || extension.equals(".temp"))
			return "img/ext/gpl2/Gnome-Edit-Delete-64.png";
		if (extension.equals(".png") || extension.equals(".bmp") || extension.equals(".jpg"))
			return "img/ext/gpl2/Gnome-Image-X-Generic-64.png";
		if (extension.equals(".eml"))
			return "img/ext/gpl2/Gnome-Mail-Read-64.png";
		if (extension.equals(".bat") || extension.equals(".cmd"))
			return "img/ext/gpl2/Gnome-Text-X-Script-64.png";
		if (extension.equals(".xlsx") || extension.equals(".xlsx"))
			return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
		if (extension.equals(".doc") || extension.equals(".docx"))
			return "img/ext/gpl2/Gnome-X-Office-Document-64.png";
		if (extension.equals(".txt"))
			return "img/ext/gpl2/Gnome-Text-X-Generic-64.png";
		if (extension.equals(".pdf"))
			return "img/ext/gpl2/Gnome-Text-X-Generic-Template-64.png";
		if (extension.equals(".url") || extension.equals(".webloc"))
			return "img/ext/gpl2/Gnome-Emblem-Web-64.png";
		if (extension.equals(".txt") || extension.equals(".doc") || extension.equals(".docx"))
			return "img/ext/gpl2/Gnome-Text-X-Generic-64.png";
		return null;
	}
	
	public static String getTobaccoImage() {
		return "img/tobacco.png";
	}
	
	/**
	 * With High-Res support (for mac)
	 */
	public static ImageIcon getIcon(String img, int w, int h) {
		ImageIcon icon;
		if (SystemAnalysis.isRetina())
			icon = new GravistoService.RetinaIcon(GravistoService.getScaledImage(
					GravistoService.loadIcon(IAPmain.class, img).getImage(),
					(int) (w * SystemAnalysis.getHiDPIScaleFactor()),
					(int) (h * SystemAnalysis.getHiDPIScaleFactor())));
		else
			icon = GravistoService.loadIcon(IAPmain.class, img, w, h, false);
		return icon;
	}
}
