package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.SettingsHelperDefaultIsFalse;
import org.SettingsHelperDefaultIsTrue;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionDataExport;
import de.ipk.ag_ba.commands.ActionDataExportAsFilesAction;
import de.ipk.ag_ba.commands.ActionDataExportTar;
import de.ipk.ag_ba.commands.hsm.ActionDataExportToHsmFolder;
import de.ipk.ag_ba.commands.hsm.ActionDataUdpBroadcast;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.commands.vfs.ActionDataExportToVfs;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabAglet;

public class ActionCopyCommandList extends AbstractNavigationAction implements NavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	private ArrayList<MongoDB> ml;
	private boolean addHSMcopy;
	private ArrayList<VirtualFileSystem> vl;
	private boolean addUDPcopy;
	
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
		this.addUDPcopy = new SettingsHelperDefaultIsFalse().isEnabled(TabAglet.ENABLE_BROADCAST_SETTING);
		this.addHSMcopy = new SettingsHelperDefaultIsTrue().isEnabled("ARCHIVE|enabled");
		this.vl = VirtualFileSystemFolderStorage.getKnown();
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
		res.add(new NavigationButton(new ActionDataExport(m, experimentReference), guiSetting));
		res.add(new NavigationButton(new ActionDataExportTar(m, experimentReference), guiSetting));
		res.add(new NavigationButton(new ActionDataExportAsFilesAction(m, experimentReference), guiSetting));
		for (MongoDB m : ml)
			res.add(new NavigationButton(new ActionCopyToMongo(m, experimentReference), guiSetting));
		for (VirtualFileSystem vx : vl) {
			if (vx instanceof VirtualFileSystemVFS2) {
				VirtualFileSystemVFS2 v = (VirtualFileSystemVFS2) vx;
				res.add(new NavigationButton(new ActionDataExportToVfs(m, experimentReference, v), guiSetting));
			}
		}
		
		if (addUDPcopy) {
			res.add(new NavigationButton(new ActionDataUdpBroadcast(m, experimentReference), guiSetting));
		}
		
		if (addHSMcopy) {
			String hsmf = IAPmain.getHSMfolder();
			if (hsmf != null)
				res.add(new NavigationButton(new ActionDataExportToHsmFolder(m, experimentReference, hsmf), guiSetting));
		}
		
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
