package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ReleaseInfo;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.ActionToggleSettingDefaultIsFalse;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabAglet;

/**
 * @author klukas
 */
public abstract class VirtualFileSystem {
	
	public static ArrayList<VirtualFileSystem> getKnown() {
		ArrayList<VirtualFileSystem> res = new ArrayList<VirtualFileSystem>();
		// res.add(new VirtualFileSystemFolderStorage(
		// "file-desktop",
		// "File I/O",
		// "Desktop" + File.separator + "VFS",
		// ReleaseInfo.getDesktopFolder() + File.separator + "VFS"));
		
		res.add(new VirtualFileSystemVFS2(
				"zhejiang-sftp",
				VfsFileProtocol.SFTP,
				"Zhejiang SFTP",
				"SFTP",
				"10.71.115.165",
				"chendijun",
				"chendijun",
				"/ipk_test"
				));
		
		res.add(new VirtualFileSystemVFS2(
				"localhost-sftp",
				VfsFileProtocol.SFTP,
				"Localhost SFTP",
				"SFTP",
				"localhost",
				"ssh",
				"ssh",
				"/VFS"
				));
		
		res.add(new VirtualFileSystemVFS2(
				"desktop-vfs",
				VfsFileProtocol.LOCAL,
				"Desktop/VFS",
				"File I/O",
				"",
				null,
				null,
				ReleaseInfo.getDesktopFolder() + File.separator + "VFS"
				));
		
		VirtualFileSystem vfsUdpInbox = new VirtualFileSystemFolderStorage(
				"udp-in",
				"Network UDP Receiver",
				"Desktop" + File.separator + "UDP" + File.separator + "Inbox",
				ReleaseInfo.getDesktopFolder() + File.separator + "UDP" + File.separator + "Inbox");
		vfsUdpInbox.setIcon(IAPimages.getWLAN());
		
		// VirtualFileSystem vfsUdpOutbox = new VirtualFileSystemUdp(
		// "udp-out",
		// "Network UDP Broadcaster",
		// "Desktop" + File.separator + "UDP" + File.separator + "Outbox",
		// ReleaseInfo.getDesktopFolder() + File.separator + "UDP" + File.separator + "Outbox");
		// res.add(vfsUdpOutbox);
		
		ActionToggleSettingDefaultIsFalse toggleUdpReceive = new ActionToggleSettingDefaultIsFalse(
				null, null,
				"Enable receiving of experiment data by opening a UDP port",
				"Receive Experiments (UDP)",
				TabAglet.ENABLE_BROADCAST_SETTING);
		vfsUdpInbox.addNavigationAction(toggleUdpReceive);
		res.add(vfsUdpInbox);
		return res;
	}
	
	private void setIcon(String icon) {
		this.desiredIcon = icon;
	}
	
	private ArrayList<NavigationAction> additionalNavigationActions = new ArrayList<NavigationAction>();
	private String desiredIcon = null;
	
	private void addNavigationAction(ActionToggleSettingDefaultIsFalse navAction) {
		additionalNavigationActions.add(navAction);
	}
	
	public ArrayList<NavigationAction> getAdditionalNavigationActions() {
		return additionalNavigationActions;
	}
	
	public abstract String getTargetName();
	
	public abstract String getTransferProtocolName();
	
	public abstract String getTargetPathName();
	
	public abstract String getPrefix();
	
	public String getResultPathNameForUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @return List of file names found at root of VFS source
	 */
	public abstract ArrayList<String> listFiles(String optSubDirectory);
	
	public abstract IOurl getIOurlFor(String fileName);
	
	public void saveExperiment(ExperimentReference experimentReference,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider) throws Exception {
		ActionDataExportToVfs a = new ActionDataExportToVfs(null, experimentReference,
				(VirtualFileSystemVFS2) this);
		a.performActionCalculateResults(null);
	}
	
	public String[] listFiles(String subdirectory, FilenameFilter optFilenameFilter) {
		ArrayList<String> files = new ArrayList<String>();
		for (String s : listFiles(subdirectory)) {
			if (optFilenameFilter == null || optFilenameFilter.accept(null, s))
				files.add(s);
		}
		return files.toArray(new String[] {});
	}
	
	public ArrayList<NavigationAction> getAdditionalEntries() {
		return new ArrayList<NavigationAction>();
	}
	
	public String getDesiredIcon() {
		return desiredIcon;
	}
}
