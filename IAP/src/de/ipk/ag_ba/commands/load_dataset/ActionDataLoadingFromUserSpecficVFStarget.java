package de.ipk.ag_ba.commands.load_dataset;

import java.util.ArrayList;

import javax.swing.JLabel;

import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.experiment.hsm.ActionHsmDataSourceNavigation;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.file_system.VfsFileSystemSource;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataLoadingFromUserSpecficVFStarget extends AbstractNavigationAction implements NavigationAction {
	
	private VfsFileProtocol vfsProtocol;
	
	private String host = "", user = "", pass = "", directory = "", description = "", vfsName;
	private boolean saveVFS = false, savePassWithVFS = false;
	private ActionHsmDataSourceNavigation vfsAction;
	private NavigationButton src;
	
	private VirtualFileSystemVFS2 vfsEntry;
	
	public ActionDataLoadingFromUserSpecficVFStarget(String tooltip) {
		super(tooltip);
	}
	
	public ActionDataLoadingFromUserSpecficVFStarget(String tooltip, VfsFileProtocol p) {
		super(tooltip);
		this.vfsProtocol = p;
	}
	
	@Override
	public ParameterOptions getParameters() {
		if (vfsAction != null)
			return null;
		ParameterOptions po = new ParameterOptions(
				"<html><br>Please specify " + (vfsProtocol != VfsFileProtocol.LOCAL ? "remote" : "") + " access information:<br>&nbsp;",
				new Object[] {
						vfsProtocol != VfsFileProtocol.LOCAL ? "Host name/IP" : null, vfsProtocol != VfsFileProtocol.LOCAL ? host : new JLabel(),
						vfsProtocol != VfsFileProtocol.LOCAL ? "User name" : null, vfsProtocol != VfsFileProtocol.LOCAL ? user : new JLabel(),
						vfsProtocol != VfsFileProtocol.LOCAL ? "Password" : null, vfsProtocol != VfsFileProtocol.LOCAL ? pass : new JLabel(),
						vfsProtocol != VfsFileProtocol.LOCAL ? "Sub-directory" : "Directory", directory,
						"Description", description,
						"", new JLabel("<html>&nbsp;"),
						"<html>Create permanent<br>VFS entry", false,
						vfsProtocol != VfsFileProtocol.LOCAL ? "Save Password" : null, vfsProtocol != VfsFileProtocol.LOCAL ? savePassWithVFS : new JLabel(),
						"VFS entry name", "Storage 1",
						"", new JLabel("<html><small><font color='gray'>"
								+ "The VFS entry is only created if the connection<br>"
								+ "with the target site can be established. The main<br>"
								+ "Copy command displays defined VFS entries as new<br>"
								+ "targets. The Home action command displays them as<br>"
								+ "additional storage sites. Click Settings do disable<br>" +
								"specific entries later."),
						"", new JLabel("<html>&nbsp;")
				});
		return po;
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		super.setParameters(parameters);
		int i = 0;
		host = vfsProtocol != VfsFileProtocol.LOCAL ? (String) parameters[i++] : null;
		user = vfsProtocol != VfsFileProtocol.LOCAL ? (String) parameters[i++] : null;
		pass = vfsProtocol != VfsFileProtocol.LOCAL ? (String) parameters[i++] : null;
		if (vfsProtocol == VfsFileProtocol.LOCAL)
			i += 3;
		directory = (String) parameters[i++];
		description = (String) parameters[i++];
		i++;
		saveVFS = (Boolean) parameters[i++];
		savePassWithVFS = vfsProtocol != VfsFileProtocol.LOCAL ? (Boolean) parameters[i++] : false;
		if (vfsProtocol == VfsFileProtocol.LOCAL)
			i += 1;
		vfsName = (String) parameters[i++];
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		if (vfsAction != null)
			return;
		String pref = "remote." + (vfsProtocol + "").toLowerCase() + "." + System.currentTimeMillis();
		if (saveVFS)
			pref = "vfs." + StringManipulationTools.getFileSystemName(vfsName).toLowerCase() + "." + System.currentTimeMillis();
		VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2(
				pref,
				vfsProtocol,
				vfsProtocol != VfsFileProtocol.LOCAL ?
						(description.isEmpty() ? (user != null && !user.trim().isEmpty() ? user + "@" + host : host) : description) :
						description,
				"temporary defined " + vfsProtocol + " I/O",
				host,
				user,
				pass == null || pass.isEmpty() ? "?" : pass,
				directory,
				false,
				false,
				null);
		this.vfsEntry = vfs;
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
		if (saveVFS) {
			IAPoptions.getInstance().setBoolean("VFS", "enabled", true);
			int n = SystemOptions.getInstance().getInteger("VFS", "n", 0);
			int idx = n + 1;
			SystemOptions.getInstance().setInteger("VFS", "n", n + 1);
			SystemOptions.getInstance().setBoolean("VFS-" + idx, "enabled", true);
			SystemOptions.getInstance().setString("VFS-" + idx, "url_prefix", pref);
			SystemOptions.getInstance().setString("VFS-" + idx, "description", vfsName);
			SystemOptions.getInstance().setString("VFS-" + idx, "vfs_type", vfsProtocol + "");
			SystemOptions.getInstance().setString("VFS-" + idx, "protocol_description", vfsProtocol + " I/O");
			SystemOptions.getInstance().setString("VFS-" + idx, "host", host);
			SystemOptions.getInstance().setString("VFS-" + idx, "user", user);
			SystemOptions.getInstance().setString("VFS-" + idx, "password", savePassWithVFS ? pass : "?");
			SystemOptions.getInstance().setString("VFS-" + idx, "directory", directory);
			SystemOptions.getInstance().setBoolean("VFS-" + idx, "Store Mongo-DB files", false);
			SystemOptions.getInstance().setBoolean("VFS-" + idx, "Use only for Mongo-DB storage", false);
			SystemOptions.getInstance().setString("VFS-" + idx, "Mongo-DB database name", "");
		}
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Insert-Object-64_load.png";
	}
	
	@Override
	public String getDefaultTitle() {
		if (vfsAction == null || vfsEntry == null)
			return "" + vfsProtocol + "";
		else
			return "<html>" + vfsProtocol + "<br><small><font color='gray'>"
					+ vfsEntry
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
			vfsAction = null;
		return res;
	}
	
}
