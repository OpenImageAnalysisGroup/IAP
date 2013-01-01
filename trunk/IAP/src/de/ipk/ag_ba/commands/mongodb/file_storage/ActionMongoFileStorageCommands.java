package de.ipk.ag_ba.commands.mongodb.file_storage;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionMongoFileStorageCommands extends AbstractNavigationAction {
	
	private MongoDB m;
	private ArrayList<VirtualFileSystem> fsl;
	
	public ActionMongoFileStorageCommands(String tooltip) {
		super(tooltip);
	}
	
	public ActionMongoFileStorageCommands(MongoDB m, ArrayList<VirtualFileSystem> fsl) {
		super("Move files from the database to the external file system (or reverse)");
		this.m = m;
		this.fsl = fsl;
	}
	
	private NavigationButton src;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public String getDefaultTitle() {
		return "External File Storage";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/folder-remote.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>(currentSet);
		result.add(src);
		return result;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		for (VirtualFileSystem vfs : fsl) {
			if (vfs instanceof VirtualFileSystemVFS2) {
				VirtualFileSystemVFS2 v2 = (VirtualFileSystemVFS2) vfs;
			}
			// move files from MongoDB GridFS to this location
			// move from this location to MongoDB GridFS
			// move from this location to other locations
		}
		return result;
	}
	
}
