package de.ipk.ag_ba.commands.load_dataset;

import java.io.File;
import java.util.ArrayList;

import org.OpenFileDialogService;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.experiment.hsm.ActionHsmDataSourceNavigation;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.file_system.VfsFileSystemSource;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataLoadingFromUserSelectedFileSystemFolder extends AbstractNavigationAction implements NavigationAction {
	
	public ActionDataLoadingFromUserSelectedFileSystemFolder(String tooltip) {
		super(tooltip);
	}
	
	private ActionHsmDataSourceNavigation vfsAction;
	private NavigationButton src;
	private File currentDirectory;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		if (currentDirectory == null)
			currentDirectory = OpenFileDialogService.getDirectoryFromUser("Select Target Folder");
		if (currentDirectory != null) {
			VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2(
					"user.dir." + System.currentTimeMillis(),
					VfsFileProtocol.LOCAL,
					"User Selected Directory",
					"File I/O", "",
					null,
					null,
					currentDirectory.getCanonicalPath(),
					false,
					false,
					null);
			VirtualFileSystemVFS2 vfsEntry = vfs;
			Library lib = new Library();
			String ico = IAPimages.getFolderRemoteClosed();
			String ico2 = IAPimages.getFolderRemoteOpen();
			String ico3 = IAPimages.getFolderRemoteClosed();
			if (vfsEntry.getTransferProtocolName().contains("UDP")) {
				ico = "img/ext/network-workgroup.png";
				ico2 = "img/ext/network-workgroup-power.png";
				ico3 = IAPimages.getFolderRemoteClosed();
			}
			if (vfsEntry.getDesiredIcon() != null) {
				ico = vfsEntry.getDesiredIcon();
				ico2 = vfsEntry.getDesiredIcon();
				ico3 = vfsEntry.getDesiredIcon();
			}
			VfsFileSystemSource dataSourceHsm = new VfsFileSystemSource(lib, vfsEntry.getTargetName(), vfsEntry,
					new String[] {},
					IAPmain.loadIcon(ico),
					IAPmain.loadIcon(ico2),
					IAPmain.loadIcon(ico3));
			ActionHsmDataSourceNavigation action = new ActionHsmDataSourceNavigation(dataSourceHsm);
			for (NavigationAction na : vfsEntry.getAdditionalNavigationActions()) {
				action.addAdditionalEntity(new NavigationButton(na, guiSetting));
			}
			this.vfsAction = action;
		}
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Drive-Harddisk-64-load.png";
	}
	
	@Override
	public String getDefaultTitle() {
		if (currentDirectory == null)
			return "Load IAP Dataset(s) from File System";
		else
			return "<html>Load IAP Dataset(s) from File System<br><small><font color='gray'>"
					+ currentDirectory
					+ "</font></small>";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (vfsAction != null) {
			try {
				vfsAction.performActionCalculateResults(src);
				res.addAll(vfsAction.getResultNewActionSet());
			} catch (Exception e) {
				e.printStackTrace();
				// add error icon
			}
		}
		if (res.size() == 0)
			currentDirectory = null;
		return res;
	}
	
}
