package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.experiment.hsm.ActionHsmDataSourceNavigation;
import de.ipk.ag_ba.commands.settings.ActionToggleSettingDefaultIsFalse;
import de.ipk.ag_ba.datasources.file_system.VfsFileSystemSource;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

/**
 * @author klukas
 */
public abstract class VirtualFileSystem {
	
	private static LinkedHashSet<VirtualFileSystemVFS2> knownFileSystems = new LinkedHashSet<VirtualFileSystemVFS2>();
	
	public static ArrayList<VirtualFileSystem> getKnown(boolean excludeNonUserItems) {
		return getKnown(excludeNonUserItems, true);
	}
	
	public static ArrayList<VirtualFileSystem> getKnown(boolean excludeNonUserItems, boolean excludeReadOnly) {
		ArrayList<VirtualFileSystem> res = new ArrayList<VirtualFileSystem>(knownFileSystems);
		
		boolean enabled = SystemOptions.getInstance().getBoolean("VFS", "enabled", true);
		int n = SystemOptions.getInstance().getInteger("VFS", "n", 1);
		int realN = n;
		if (n < 1)
			n = 2;
		
		for (int idx = 1; idx <= n; idx++) {
			boolean en = SystemOptions.getInstance().getBoolean("VFS-" + idx, "enabled",
					idx == 1 ? true : false);
			String url_prefix = SystemOptions.getInstance().getString("VFS-" + idx, "url_prefix",
					idx == 1 ? "web-example" : "desktop");
			String desc = SystemOptions.getInstance().getString("VFS-" + idx, "description",
					idx == 1 ? "Example Dataset" : "Desktop/VFS");
			VfsFileProtocol vfs_type = idx == 1 ? VfsFileProtocol.HTTP : VfsFileProtocol.LOCAL;
			String types = vfs_type + "";
			if (idx > 2) {
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
					idx == 1 ? "HTTP download" : (idx < 3 ? "local file I/O" : "#protocol description"));
			String host = SystemOptions.getInstance().getString("VFS-" + idx, "host",
					idx == 1 ? "iap.ipk-gatersleben.de" : (idx < 3 ? "" : "#IP/hostname"));
			String user = SystemOptions.getInstance().getString("VFS-" + idx, "user", idx < 2 ? "null" : "");
			String pass = SystemOptions.getInstance().getString("VFS-" + idx, "password", idx < 2 ? "null" : "");
			String dir = SystemOptions.getInstance().getString("VFS-" + idx, "directory",
					idx == 1 ? "datasets" : (idx < 3 ? SystemAnalysis.getDesktopFolder() + File.separator + "IAP" : "#/subdir"));
			boolean useForMongoFileStorage = SystemOptions.getInstance().getBoolean("VFS-" + idx, "Store Mongo-DB files", false);
			boolean useOnlyForMongoFileStorage = SystemOptions.getInstance().getBoolean("VFS-" + idx, "Use only for Mongo-DB storage", false);
			String useForMongoFileStorageCloudName = SystemOptions.getInstance().getString("VFS-" + idx, "Mongo-DB database name", "");
			
			if (en) {
				VirtualFileSystem v = null;
				
				for (ResourceIOHandler rh : ResourceIOManager.getInstance().getHandlers()) {
					if (rh instanceof VirtualFileSystemHandler) {
						VirtualFileSystemHandler vfsh = (VirtualFileSystemHandler) rh;
						if (url_prefix != null && url_prefix.equals(vfsh.getPrefix())) {
							VirtualFileSystem vfs = vfsh.getVFS();
							v = vfs;
						}
					}
				}
				if (v == null)
					v = new VirtualFileSystemVFS2(
							url_prefix, vfs_type,
							desc, protocol_desc,
							host,
							user, pass,
							dir,
							useForMongoFileStorage,
							useOnlyForMongoFileStorage,
							useForMongoFileStorageCloudName);
				else {
					if (v instanceof VirtualFileSystemVFS2) {
						VirtualFileSystemVFS2 v2 = (VirtualFileSystemVFS2) v;
						v2.setHost(host);
						if (!("" + user).equals("" + v2.getUser()))
							v2.setPassword(pass);
						v2.setUser(user);
						v2.setFolder(dir);
						v2.setUseForMongo(useForMongoFileStorage);
						v2.setMongoFileStorageName(useForMongoFileStorageCloudName);
						v2.setUseOnlyForMongoFileStorage(useOnlyForMongoFileStorage);
						v2.setDescription(desc);
					}
				}
				if (excludeNonUserItems && useOnlyForMongoFileStorage) {
					// don't add such item to the result
				} else
					if (en && enabled && idx <= realN)
						if (!excludeReadOnly || v.isAbleToSaveData())
							res.add(v);
			}
		}
		
		return res;
	}
	
	public void setIcon(String icon) {
		this.desiredIcon = icon;
	}
	
	private final ArrayList<NavigationAction> additionalNavigationActions = new ArrayList<NavigationAction>();
	private String desiredIcon = null;
	
	public void addNavigationAction(ActionToggleSettingDefaultIsFalse navAction) {
		additionalNavigationActions.add(navAction);
	}
	
	public ArrayList<NavigationAction> getAdditionalNavigationActions() {
		return additionalNavigationActions;
	}
	
	public abstract String getTargetName();
	
	public abstract String getProtocolName();
	
	public abstract String getTargetPathName();
	
	public abstract String getPrefix();
	
	/**
	 * @return List of file names found at root of VFS source
	 * @throws Exception
	 */
	public abstract ArrayList<String> listFiles(String optSubDirectory) throws Exception;
	
	public abstract ArrayList<String> listFolders(String optSubDirectory) throws Exception;
	
	public abstract IOurl getIOurlFor(String fileName);
	
	public MainPanelComponent saveExperiment(MongoDB m, ExperimentReferenceInterface experimentReference,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider, boolean ignoreOutliers) throws Exception {
		ActionDataExportToVfs a = new ActionDataExportToVfs(m, experimentReference,
				(VirtualFileSystemVFS2) this, ignoreOutliers, null);
		if (statusProvider != null)
			a.setStatusProvider(statusProvider);
		
		boolean execute = true;
		ParameterOptions params = a.getParameters();
		if (params != null && params.userRequestNeeded()) {
			Object[] res = MyInputHelper.getInput(params.getDescription(), "Copy Dataset " + experimentReference.getExperimentName(),
					params.getParameterField());
			if (res == null) {
				execute = false;
			} else
				a.setParameters(res);
		}
		
		if (execute) {
			a.performActionCalculateResults(null);
			return a.getResultMainPanel();
		} else
			return new MainPanelComponent("Execution has been skipped according to user command input.");
	}
	
	public String[] listFiles(String subdirectory, FilenameFilter optFilenameFilter) throws Exception {
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
	
	public abstract InputStream getInputStream(IOurl url) throws Exception;
	
	public abstract InputStream getPreviewInputStream(IOurl url) throws Exception;
	
	public abstract long getFileLength(IOurl url) throws Exception;
	
	public abstract VfsFileObject getFileObjectFor(String fileName) throws Exception;
	
	public NavigationButton getNavigationButton(GUIsetting guiSetting) {
		assert guiSetting != null;
		VirtualFileSystem vfsEntry = this;
		Library lib = new Library();
		String ico = IAPimages.getFolderRemoteClosed();
		String ico2 = IAPimages.getFolderRemoteOpen();
		String ico3 = IAPimages.getFolderRemoteClosed();
		String ico4 = IAPimages.getFolderRemoteOpen();
		if (vfsEntry.getProtocolName().contains("UDP")) {
			ico = "img/ext/network-workgroup.png";
			ico2 = "img/ext/network-workgroup-power.png";
			ico3 = IAPimages.getFolderRemoteClosed();
			ico4 = IAPimages.getFolderRemoteOpen();
		}
		if (vfsEntry.getDesiredIcon() != null) {
			ico = vfsEntry.getDesiredIcon();
			ico2 = vfsEntry.getDesiredIcon();
			ico3 = vfsEntry.getDesiredIcon();
			ico4 = vfsEntry.getDesiredIcon();
		}
		VfsFileSystemSource dataSourceHsm = new VfsFileSystemSource(lib, vfsEntry.getTargetName(), vfsEntry,
				new String[] { ".txt", ".url", ".webloc", ".gml", ".graphml", ".pdf", ".html", ".htm" },
				IAPmain.loadIcon(ico),
				IAPmain.loadIcon(ico2),
				IAPmain.loadIcon(ico3),
				IAPmain.loadIcon(ico4));
		ActionHsmDataSourceNavigation action = new ActionHsmDataSourceNavigation(dataSourceHsm);
		for (NavigationAction na : vfsEntry.getAdditionalNavigationActions()) {
			action.addAdditionalEntity(new NavigationButton(na, guiSetting));
		}
		NavigationButton vfsSrc = new NavigationButton(vfsEntry.getTargetName(), action, guiSetting);
		vfsSrc.setToolTipText("Target: " + vfsEntry.getTargetPathName() + " via " + vfsEntry.getProtocolName());
		return vfsSrc;
	}
	
	public abstract boolean isAbleToSaveData();
}
