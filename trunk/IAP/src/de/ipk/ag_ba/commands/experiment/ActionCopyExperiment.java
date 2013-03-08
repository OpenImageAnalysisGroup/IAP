package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.SettingsHelperDefaultIsFalse;
import org.SettingsHelperDefaultIsTrue;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.hsm.ActionDataExportToHsmFolder;
import de.ipk.ag_ba.commands.experiment.hsm.ActionDataUdpBroadcast;
import de.ipk.ag_ba.commands.mongodb.ActionCopyListOfExperimentsToMongo;
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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabAglet;

/**
 * @author klukas
 */
public class ActionCopyExperiment extends AbstractNavigationAction implements NavigationAction {
	
	private MongoDB m;
	private ArrayList<ExperimentReference> experimentReferences;
	private NavigationButton src;
	private ArrayList<MongoDB> ml;
	private boolean addHSMcopy;
	private ArrayList<VirtualFileSystemVFS2> vl;
	private boolean addUDPcopy;
	
	public ActionCopyExperiment(String tooltip) {
		super(tooltip);
	}
	
	public ActionCopyExperiment(MongoDB m, ExperimentReference experimentReference, GUIsetting guiSetting) {
		this("Copy dataset");
		this.m = m;
		this.experimentReferences = new ArrayList<ExperimentReference>();
		this.experimentReferences.add(experimentReference);
		this.guiSetting = guiSetting;
	}
	
	public ActionCopyExperiment(MongoDB m, ArrayList<ExperimentHeaderInterface> experimentHeaderList, GUIsetting guiSetting) {
		this("Copy dataset");
		this.m = m;
		this.experimentReferences = new ArrayList<ExperimentReference>();
		for (ExperimentHeaderInterface eh : experimentHeaderList) {
			ExperimentReference er = new ExperimentReference(eh, m);
			this.experimentReferences.add(er);
		}
		this.guiSetting = guiSetting;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ml = MongoDB.getMongos();
		this.addUDPcopy = new SettingsHelperDefaultIsFalse().isEnabled(TabAglet.ENABLE_BROADCAST_SETTING);
		this.addHSMcopy = new SettingsHelperDefaultIsTrue().isEnabled("ARCHIVE|enabled");
		this.vl = VirtualFileSystemFolderStorage.getKnown(true);
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
			res.add(new NavigationButton(new ActionCopyListOfExperimentsToMongo(m, experimentReferences), guiSetting));
		for (VirtualFileSystem vx : vl) {
			if (vx instanceof VirtualFileSystemVFS2) {
				VirtualFileSystemVFS2 v = (VirtualFileSystemVFS2) vx;
				res.add(new NavigationButton(new ActionDataExportToVfs(m, experimentReferences, v), guiSetting));
			}
		}
		
		if (experimentReferences.size() == 1)
			if (addUDPcopy) {
				res.add(new NavigationButton(new ActionDataUdpBroadcast(m, experimentReferences.iterator().next()), guiSetting));
			}
		
		if (experimentReferences.size() == 1)
			if (addHSMcopy) {
				String hsmf = IAPmain.getHSMfolder();
				if (hsmf != null)
					res.add(new NavigationButton(new ActionDataExportToHsmFolder(m, experimentReferences.iterator().next(), hsmf), guiSetting));
			}
		
		res.add(new NavigationButton(new ActionDataExportToUserSelectedFileSystemFolder(
				"Copy dataset to a user-selected target folder",
				m, experimentReferences), guiSetting));
		
		return res;
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getNetworkedServers();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/transfer2c.png";
	}
	
	@Override
	public String getDefaultTitle() {
		if (experimentReferences.size() == 1)
			return "Copy";
		else
			return "Copy Set of Experiments (" + experimentReferences.size() + ")";
	}
}
