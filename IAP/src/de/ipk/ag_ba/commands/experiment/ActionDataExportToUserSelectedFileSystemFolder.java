package de.ipk.ag_ba.commands.experiment;

import java.io.File;
import java.util.ArrayList;

import org.OpenFileDialogService;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataExportToUserSelectedFileSystemFolder extends AbstractNavigationAction implements NavigationAction {
	
	private final ArrayList<ExperimentReference> experimentReference;
	private final MongoDB m;
	private final boolean ignoreOutliers;
	
	public ActionDataExportToUserSelectedFileSystemFolder(String tooltip, MongoDB m,
			ArrayList<ExperimentReference> experimentReference, boolean ignoreOutliers) {
		super(tooltip);
		this.m = m;
		this.experimentReference = experimentReference;
		this.ignoreOutliers = ignoreOutliers;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (experimentReference == null)
			return;
		File currentDirectory = OpenFileDialogService.getDirectoryFromUser("Select Target Folder");
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
			for (ExperimentReference er : experimentReference) {
				vfs.saveExperiment(m, er, getStatusProvider(), ignoreOutliers);
			}
		}
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Document-Save-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "To Local File System";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
}
