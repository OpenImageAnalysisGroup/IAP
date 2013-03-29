package de.ipk.ag_ba.commands.experiment;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JLabel;

import org.OpenFileDialogService;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataExportToUserSpecficVFStarget extends AbstractNavigationAction implements NavigationAction {
	
	private final ArrayList<ExperimentReference> experimentReference;
	private final MongoDB m;
	private VfsFileProtocol p;
	
	public ActionDataExportToUserSpecficVFStarget(String tooltip) {
		super(tooltip);
		m = null;
		experimentReference = null;
	}
	
	public ActionDataExportToUserSpecficVFStarget(String tooltip, MongoDB m, ArrayList<ExperimentReference> experimentReference,
			VfsFileProtocol p) {
		super(tooltip);
		this.m = m;
		this.experimentReference = experimentReference;
		this.p = p;
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
						"Create Bookmark", false,
						"Save Password", false,
						"Bookmark Name", "Bookmark",
						"", new JLabel("<html><small><font color='gray'>"
								+ "The bookmark is only created if the connection<br>"
								+ "to the target site can be established. The main<br>"
								+ "Copy command displays defined bookmarks as new<br>"
								+ "targets. Bookmarks can be deleted from the settings<br>"
								+ "folder (file name ends with '.copy.bookmark').<br>"),
						"", new JLabel("<html>&nbsp;")
				});
		return po;
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		super.setParameters(parameters);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (experimentReference == null)
			return;
		
		File currentDirectory = OpenFileDialogService.getDirectoryFromUser("Select Target Folder");
		if (currentDirectory != null) {
			VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2(
					"user.dir",
					VfsFileProtocol.LOCAL,
					"User Selected Directory",
					"File I/O", "",
					null,
					null,
					currentDirectory.getCanonicalPath(),
					false,
					false,
					null);
			for (ExperimentReference er : experimentReference)
				vfs.saveExperiment(m, er, getStatusProvider());
		}
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Insert-Object-64_save.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Copy using " + p + "...";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
	
}
