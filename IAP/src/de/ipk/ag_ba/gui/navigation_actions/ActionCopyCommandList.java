package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.SettingsHelperDefaultIsFalse;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionCopyToMongo;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabAglet;

public class ActionCopyCommandList extends AbstractNavigationAction implements NavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	private ArrayList<MongoDB> ml;
	
	public ActionCopyCommandList(String tooltip) {
		super(tooltip);
	}
	
	public ActionCopyCommandList(MongoDB m, ExperimentReference experimentReference, GUIsetting guiSetting) {
		this("Copy dataset");
		this.m = m;
		this.experimentReference = experimentReference;
		this.guiSetting = guiSetting;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ml = MongoDB.getMongos();
		boolean addUDPcopy = new SettingsHelperDefaultIsFalse().isEnabled(TabAglet.ENABLE_BROADCAST_SETTING);
		boolean addHSMcopy = true;
		ArrayList<VirtualFileSystem> vl = VirtualFileSystemFolderStorage.getKnown();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (MongoDB m : ml)
			res.add(new NavigationButton(new ActionCopyToMongo(m, experimentReference), guiSetting));
		return res;
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getNetworkedServers();
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getNetworkedServers();
	}
	
	@Override
	public String getDefaultTitle() {
		return "Copy Dataset";
	}
}
