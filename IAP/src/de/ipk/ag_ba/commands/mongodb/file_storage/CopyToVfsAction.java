package de.ipk.ag_ba.commands.mongodb.file_storage;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;

public class CopyToVfsAction extends AbstractNavigationAction {
	
	private final MongoDB m;
	private final VirtualFileSystemVFS2 v2;
	private final int gb;
	protected StringBuilder messages = new StringBuilder();
	
	public CopyToVfsAction(MongoDB m, VirtualFileSystemVFS2 v2, int gb) {
		super("Copy files from MongoDB storage to VFS " + v2.getTargetName());
		this.m = m;
		this.v2 = v2;
		this.gb = gb;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		messages = new StringBuilder();
		m.fileStorage().copy(v2, gb, status, messages);
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("<html>" + messages);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public String getDefaultTitle() {
		if (gb > 0)
			return "Copy " + gb + " GB to VFS";
		else
			return "Copy all files to VFS";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/network.png";
	}
}
