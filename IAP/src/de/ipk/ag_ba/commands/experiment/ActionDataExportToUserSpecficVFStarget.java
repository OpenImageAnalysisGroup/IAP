package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import javax.swing.JLabel;

import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataExportToUserSpecficVFStarget extends AbstractNavigationAction implements NavigationAction {
	
	private final ArrayList<ExperimentReferenceInterface> experimentReference;
	private final MongoDB m;
	private VfsFileProtocol p;
	
	private String host, user, pass, directory, vfsName;
	private boolean saveVFS = false, savePassWithVFS = false;
	private boolean ignoreOutliers;
	
	private final ArrayList<MainPanelComponent> results = new ArrayList<MainPanelComponent>();
	
	public ActionDataExportToUserSpecficVFStarget(String tooltip) {
		super(tooltip);
		m = null;
		experimentReference = null;
	}
	
	public ActionDataExportToUserSpecficVFStarget(String tooltip, MongoDB m,
			ArrayList<ExperimentReferenceInterface> experimentReference,
			VfsFileProtocol p,
			boolean ignoreOutliers) {
		super(tooltip);
		this.m = m;
		this.experimentReference = experimentReference;
		this.p = p;
		this.ignoreOutliers = ignoreOutliers;
	}
	
	@Override
	public ParameterOptions getParameters() {
		ParameterOptions po = new ParameterOptions(
				"<html><br>Please specify target properties:<br>&nbsp;",
				new Object[] {
						"Host name/IP", "",
						"User name", "user",
						"Password", "pass",
						"Sub-directory", "",
						"", new JLabel("<html>&nbsp;"),
						"<html>Create permanent<br>VFS entry", false,
						"Save Password", false,
						"VFS entry name", "Storage 1",
						"", new JLabel("<html><small><font color='gray'>"
								+ "The VFS entry is only created if the connection<br>"
								+ "to the target site can be established. The main<br>"
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
		host = (String) parameters[i++];
		user = (String) parameters[i++];
		pass = (String) parameters[i++];
		directory = (String) parameters[i++];
		i++;
		saveVFS = (Boolean) parameters[i++];
		savePassWithVFS = (Boolean) parameters[i++];
		vfsName = (String) parameters[i++];
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> rl = new ArrayList<String>();
		for (MainPanelComponent mc : results) {
			rl.addAll(mc.getHTML());
		}
		return new MainPanelComponent(rl);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		results.clear();
		if (experimentReference == null)
			return;
		String pref = "remote." + (p + "").toLowerCase() + "." + System.currentTimeMillis();
		if (saveVFS)
			pref = "vfs." + StringManipulationTools.getFileSystemName(vfsName).toLowerCase() + "." + System.currentTimeMillis();
		VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2(
				pref,
				p,
				"Custom Remote Target",
				"temporary defined " + p + " I/O",
				host,
				user,
				pass,
				directory,
				false,
				false,
				null);
		for (ExperimentReferenceInterface er : experimentReference) {
			results.add(vfs.saveExperiment(m, er, getStatusProvider(), ignoreOutliers));
		}
		if (saveVFS) {
			SystemOptions.getInstance().setBoolean("VFS", "enabled", true);
			int n = SystemOptions.getInstance().getInteger("VFS", "n", 0);
			int idx = n + 1;
			SystemOptions.getInstance().setInteger("VFS", "n", n + 1);
			SystemOptions.getInstance().setBoolean("VFS-" + idx, "enabled", true);
			SystemOptions.getInstance().setString("VFS-" + idx, "url_prefix", pref);
			SystemOptions.getInstance().setString("VFS-" + idx, "description", vfsName);
			SystemOptions.getInstance().setString("VFS-" + idx, "vfs_type", p + "");
			SystemOptions.getInstance().setString("VFS-" + idx, "protocol_description", p + " I/O");
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
		return "img/ext/gpl2/Gnome-Insert-Object-64_save.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "" + p + "";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
	
}
