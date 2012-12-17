package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.settings.ActionToggleSettingDefaultIsFalse;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.vanted.plugin.VfsFileProtocol;

/**
 * @author klukas
 */
public abstract class VirtualFileSystem {
	
	private static LinkedHashSet<VirtualFileSystem> knownFileSystems = new LinkedHashSet<VirtualFileSystem>();
	
	public static ArrayList<VirtualFileSystem> getKnown() {
		ArrayList<VirtualFileSystem> res = new ArrayList<VirtualFileSystem>(knownFileSystems);
		
		boolean enabled = SystemOptions.getInstance().getBoolean("VFS", "enabled", false);
		int n = SystemOptions.getInstance().getInteger("VFS", "n", 0);
		int realN = n;
		if (n < 1)
			n = 1;
		
		for (int idx = 1; idx <= n; idx++) {
			boolean en = SystemOptions.getInstance().getBoolean("VFS-" + idx, "enabled", false);
			String url_prefix = SystemOptions.getInstance().getString("VFS-" + idx, "url_prefix", "desktop");
			String desc = SystemOptions.getInstance().getString("VFS-" + idx, "description",
					"Desktop/VFS");
			VfsFileProtocol vfs_type = VfsFileProtocol.LOCAL;
			String types = VfsFileProtocol.LOCAL + "";
			if (idx > 1) {
				String s = StringManipulationTools.getStringList(VfsFileProtocol.values(), ",");
				types = "#valid: " + s;
			}
			try {
				String s = SystemOptions.getInstance().getString("VFS-" + idx, "vfs_type", types);
				vfs_type = VfsFileProtocol.valueOf(s);
			} catch (Exception e) {
				SystemOptions.getInstance().setString("VFS-" + idx, "vfs_type", types);
			}
			String protocol_desc = SystemOptions.getInstance().getString("VFS-" + idx, "protocol_description",
					idx < 2 ? "local file I/O" : "#protocol description");
			String host = SystemOptions.getInstance().getString("VFS-" + idx, "host", idx < 2 ? "" : "#IP/hostname");
			String user = SystemOptions.getInstance().getString("VFS-" + idx, "user", idx < 2 ? "null" : "");
			String pass = SystemOptions.getInstance().getString("VFS-" + idx, "password", idx < 2 ? "null" : "");
			String dir = SystemOptions.getInstance().getString("VFS-" + idx, "directory",
					idx < 2 ? ReleaseInfo.getDesktopFolder() + File.separator + "IAP" : "#/subdir");
			VirtualFileSystemVFS2 v = new VirtualFileSystemVFS2(url_prefix, vfs_type, desc, protocol_desc, host, user, pass, dir);
			if (en && enabled && idx <= realN)
				res.add(v);
		}
		// res.add(new VirtualFileSystemFolderStorage(
		// "file-desktop",
		// "File I/O",
		// "Desktop" + File.separator + "VFS",
		// ReleaseInfo.getDesktopFolder() + File.separator + "VFS"));
		
		// res.add(new VirtualFileSystemVFS2(
		// "zhejiang-sftp",
		// VfsFileProtocol.SFTP,
		// "Zhejiang SFTP",
		// "SFTP",
		// "10.71.115.165",
		// "chendijun",
		// "chendijun",
		// "/ipk_test"
		// ));
		//
		// res.add(new VirtualFileSystemVFS2(
		// "localhost-sftp",
		// VfsFileProtocol.SFTP,
		// "Localhost SFTP",
		// "SFTP",
		// "localhost",
		// "ssh",
		// "ssh",
		// "/VFS"
		// ));
		
		// res.add(new VirtualFileSystemVFS2(
		// "desktop-vfs",
		// VfsFileProtocol.LOCAL,
		// "Desktop/VFS",
		// "File I/O",
		// "",
		// null,
		// null,
		// ReleaseInfo.getDesktopFolder() + File.separator + "VFS"
		// ));
		
		// VirtualFileSystem vfsUdpInbox = new VirtualFileSystemFolderStorage(
		// "udp-in",
		// "Network UDP Receiver",
		// "Desktop" + File.separator + "UDP" + File.separator + "Inbox",
		// ReleaseInfo.getDesktopFolder() + File.separator + "UDP" + File.separator + "Inbox");
		// vfsUdpInbox.setIcon(IAPimages.getWLAN());
		//
		// // VirtualFileSystem vfsUdpOutbox = new VirtualFileSystemUdp(
		// // "udp-out",
		// // "Network UDP Broadcaster",
		// // "Desktop" + File.separator + "UDP" + File.separator + "Outbox",
		// // ReleaseInfo.getDesktopFolder() + File.separator + "UDP" + File.separator + "Outbox");
		// // res.add(vfsUdpOutbox);
		//
		// ActionToggleSettingDefaultIsFalse toggleUdpReceive = new ActionToggleSettingDefaultIsFalse(
		// null, null,
		// "Enable receiving of experiment data by opening a UDP port",
		// "Receive Experiments (UDP)",
		// TabAglet.ENABLE_BROADCAST_SETTING);
		// vfsUdpInbox.addNavigationAction(toggleUdpReceive);
		// res.add(vfsUdpInbox);
		return res;
	}
	
	private void setIcon(String icon) {
		this.desiredIcon = icon;
	}
	
	private final ArrayList<NavigationAction> additionalNavigationActions = new ArrayList<NavigationAction>();
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
	
	public static void addItem(VirtualFileSystemVFS2 virtualFileSystemVFS2) {
		knownFileSystems.add(virtualFileSystemVFS2);
	}
	
	@Override
	public String toString() {
		return getTargetName();
	}
}
