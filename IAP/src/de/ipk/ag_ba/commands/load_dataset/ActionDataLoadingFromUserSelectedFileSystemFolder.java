package de.ipk.ag_ba.commands.load_dataset;

import java.io.File;
import java.util.ArrayList;

import org.OpenFileDialogService;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.experiment.hsm.ActionHsmDataSourceNavigation;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.file_system.VfsFileSystemSource;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataLoadingFromUserSelectedFileSystemFolder extends AbstractNavigationAction implements NavigationAction {
	
	private final boolean provideSaveCommand;
	
	public ActionDataLoadingFromUserSelectedFileSystemFolder(String tooltip, boolean provideSaveCommand) {
		super(tooltip);
		this.provideSaveCommand = provideSaveCommand;
	}
	
	private ActionHsmDataSourceNavigation vfsAction;
	private NavigationButton src;
	private File currentDirectory;
	ArrayList<NavigationButton> resultActions = new ArrayList<NavigationButton>();
	private VirtualFileSystemVFS2 vfsEntry;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		this.vfsEntry = null;
		resultActions.clear();
		if (currentDirectory == null)
			currentDirectory = OpenFileDialogService.getDirectoryFromUser("Select Target Folder");
		if (currentDirectory != null) {
			VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2(
					"user.dir." + System.currentTimeMillis(),
					VfsFileProtocol.LOCAL,
					currentDirectory.getName(),
					"File I/O", "",
					null,
					null,
					currentDirectory.getCanonicalPath(),
					false,
					false,
					null);
			this.vfsEntry = vfs;
			Library lib = new Library();
			String ico = IAPimages.getFolderRemoteClosed();
			String ico2 = IAPimages.getFolderRemoteOpen();
			String ico3 = IAPimages.getFolderRemoteClosed();
			if (vfsEntry.getProtocolName().toUpperCase().contains("UDP")) {
				ico = "img/ext/network-workgroup.png";
				ico2 = "img/ext/network-workgroup-power.png";
				ico3 = IAPimages.getFolderRemoteClosed();
			} else
				if (vfsEntry.getDesiredIcon() != null) {
					ico = vfsEntry.getDesiredIcon();
					ico2 = vfsEntry.getDesiredIcon();
					ico3 = vfsEntry.getDesiredIcon();
				}
			VfsFileSystemSource dataSourceHsm = new VfsFileSystemSource(lib, vfsEntry.getTargetName(), vfsEntry,
					new String[] { ".txt", ".url", ".webloc", ".gml", ".graphml", ".pdf", ".html", ".htm" },
					IAPmain.loadIcon(ico),
					IAPmain.loadIcon(ico2),
					IAPmain.loadIcon(ico3));
			dataSourceHsm.readDataSource();
			ActionHsmDataSourceNavigation action = new ActionHsmDataSourceNavigation(dataSourceHsm);
			this.vfsAction = action;
			
			for (NavigationAction na : vfsEntry.getAdditionalNavigationActions()) {
				action.addAdditionalEntity(new NavigationButton(na, guiSetting));
			}
			vfsAction.performActionCalculateResults(src);
			resultActions.addAll(vfsAction.getResultNewActionSet());
		}
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Drive-Harddisk-64-load.png";
	}
	
	@Override
	public String getDefaultTitle() {
		if (currentDirectory == null)
			return "Load Dataset(s) from File System";
		else
			return "<html>Load IAP Dataset(s) from File System<br><small><font color='gray'>"
					+ currentDirectory
					+ "</font></small>";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (provideSaveCommand && currentDirectory != null && vfsEntry != null) {
			
			AbstractNavigationAction sa = new AbstractNavigationAction("Create permanent storage icon on start view") {
				boolean known = false;
				String knownIconName = "";
				
				@Override
				public void performActionCalculateResults(NavigationButton src) throws Exception {
					known = false;
					IAPoptions.getInstance().setBoolean("VFS", "enabled", true);
					int n = SystemOptions.getInstance().getInteger("VFS", "n", 0);
					
					if (n > 0)
						for (int idx = 1; idx <= n; idx++) {
							boolean enabled = SystemOptions.getInstance().getBoolean("VFS-" + idx, "enabled", true);
							if (!enabled)
								continue;
							boolean s1 = SystemOptions.getInstance().getString("VFS-" + idx, "vfs_type", "").equals(vfsEntry.getProtocolType() + "");
							boolean s2 = SystemOptions.getInstance().getString("VFS-" + idx, "directory", "").equals(vfsEntry.getDirectory() + "");
							if (s1 && s2) {
								known = true;
								knownIconName = SystemOptions.getInstance().getString("VFS-" + idx, "description", "(not defined)");
								break;
							}
						}
					if (!known) {
						int idx = n + 1;
						SystemOptions.getInstance().setInteger("VFS", "n", n + 1);
						SystemOptions.getInstance().setBoolean("VFS-" + idx, "enabled", true);
						SystemOptions.getInstance().setString("VFS-" + idx, "url_prefix", vfsEntry.getPrefix());
						SystemOptions.getInstance().setString("VFS-" + idx, "description", vfsEntry.getTargetName());
						SystemOptions.getInstance().setString("VFS-" + idx, "vfs_type", vfsEntry.getProtocolType() + "");
						SystemOptions.getInstance().setString("VFS-" + idx, "protocol_description", vfsEntry.getProtocolName());
						SystemOptions.getInstance().setString("VFS-" + idx, "host", vfsEntry.getHost());
						SystemOptions.getInstance().setString("VFS-" + idx, "user", vfsEntry.getUser());
						SystemOptions.getInstance().setString("VFS-" + idx, "password", vfsEntry.getStoredPass());
						SystemOptions.getInstance().setString("VFS-" + idx, "directory", vfsEntry.getDirectory());
						SystemOptions.getInstance().setBoolean("VFS-" + idx, "Store Mongo-DB files", vfsEntry.getStoreMongoDBfiles());
						SystemOptions.getInstance().setBoolean("VFS-" + idx, "Use only for Mongo-DB storage", vfsEntry.getUseOnlyForMongoDBfileStorage());
						SystemOptions.getInstance().setString("VFS-" + idx, "Mongo-DB database name", vfsEntry.getMongoDBdbName());
					}
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
					return currentSet;
				}
				
				@Override
				public String getDefaultTitle() {
					return "Create Permanent Icon";
				}
				
				@Override
				public MainPanelComponent getResultMainPanel() {
					if (known)
						return new MainPanelComponent("The existing storage location icon '<b>" + knownIconName
								+ "</b>' at the Start-screen already points to this storage location.<br><br>"
								+ "No new additional icon has been created.");
					else
						return new MainPanelComponent("The new storage location icon '<b>" + currentDirectory.getName() + "</b>' has been created and "
								+ "is available from the Start-screen.");
				}
				
				@Override
				public String getDefaultImage() {
					return "img/ext/gpl2/Gnome-Insert-Object-64_save.png";
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewActionSet() {
					return null;
				}
			};
			res.add(new NavigationButton(sa, src.getGUIsetting()));
		}
		res.addAll(this.resultActions);
		return res;
	}
}
