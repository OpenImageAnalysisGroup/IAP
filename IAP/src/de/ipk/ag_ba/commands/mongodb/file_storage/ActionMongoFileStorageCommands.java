package de.ipk.ag_ba.commands.mongodb.file_storage;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
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
				final VirtualFileSystemVFS2 v2 = (VirtualFileSystemVFS2) vfs;
				NavigationAction a = new AbstractNavigationAction("Move data to " + v2.getTargetName() + " (click to select amount to be moved)") {
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
						// empty
					}
					
					@Override
					public String getDefaultTitle() {
						return "Move data to " + v2.getTargetName() + " ...";
					}
					
					@Override
					public String getDefaultImage() {
						return "img/ext/gpl2/Gnome-Document-Send-64.png";
					}
					
					@Override
					public ArrayList<NavigationButton> getResultNewActionSet() {
						ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
						result.add(new NavigationButton(new MoveToVfsAction(m, v2, 10), src.getGUIsetting()));
						result.add(new NavigationButton(new MoveToVfsAction(m, v2, 50), src.getGUIsetting()));
						result.add(new NavigationButton(new MoveToVfsAction(m, v2, 250), src.getGUIsetting()));
						result.add(new NavigationButton(new MoveToVfsAction(m, v2, 500), src.getGUIsetting()));
						result.add(new NavigationButton(new MoveToVfsAction(m, v2, 750), src.getGUIsetting()));
						result.add(new NavigationButton(new MoveToVfsAction(m, v2, 1000), src.getGUIsetting()));
						result.add(new NavigationButton(new MoveToVfsAction(m, v2, -1), src.getGUIsetting()));
						return result;
					}
				};
				result.add(new NavigationButton(a, src.getGUIsetting()));
				
				NavigationAction b = new AbstractNavigationAction("Copy data to " + v2.getTargetName() + " (click to select amount to be copied)") {
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
						// empty
					}
					
					@Override
					public String getDefaultTitle() {
						return "Copy data to " + v2.getTargetName() + " ...";
					}
					
					@Override
					public String getDefaultImage() {
						return "img/ext/gpl2/Gnome-Document-Send-64.png";
					}
					
					@Override
					public ArrayList<NavigationButton> getResultNewActionSet() {
						ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
						result.add(new NavigationButton(new CopyToVfsAction(m, v2, 10), src.getGUIsetting()));
						result.add(new NavigationButton(new CopyToVfsAction(m, v2, 50), src.getGUIsetting()));
						result.add(new NavigationButton(new CopyToVfsAction(m, v2, 250), src.getGUIsetting()));
						result.add(new NavigationButton(new CopyToVfsAction(m, v2, 500), src.getGUIsetting()));
						result.add(new NavigationButton(new CopyToVfsAction(m, v2, 750), src.getGUIsetting()));
						result.add(new NavigationButton(new CopyToVfsAction(m, v2, 1000), src.getGUIsetting()));
						result.add(new NavigationButton(new CopyToVfsAction(m, v2, -1), src.getGUIsetting()));
						return result;
					}
				};
				result.add(new NavigationButton(b, src.getGUIsetting()));
				
			}
			// move files from MongoDB GridFS to this location
			// move from this location to MongoDB GridFS
			// move from this location to other locations
		}
		return result;
	}
}
