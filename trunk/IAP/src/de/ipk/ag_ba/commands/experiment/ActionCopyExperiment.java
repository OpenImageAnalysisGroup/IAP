package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.MeasurementFilter;
import org.SettingsHelperDefaultIsFalse;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.hsm.ActionDataUdpBroadcast;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.mongodb.ActionCopyListOfExperimentsToMongo;
import de.ipk.ag_ba.commands.settings.ActionToggle;
import de.ipk.ag_ba.commands.vfs.ActionDataExportToVfs;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabAglet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ActionCopyExperiment extends AbstractNavigationAction implements NavigationAction, ActionDataProcessing {
	
	private MongoDB m;
	private ArrayList<ExperimentReferenceInterface> experimentReferences;
	private NavigationButton src;
	private ArrayList<MongoDB> ml;
	// private boolean addHSMcopy;
	private ArrayList<VirtualFileSystem> vl;
	private boolean addUDPcopy;
	private int outlierCountNumerics;
	private int outlierCountBinary;
	private long expSize = -1;
	
	public ActionCopyExperiment(String tooltip) {
		super(tooltip);
	}
	
	public ActionCopyExperiment() {
		this("Copy dataset");
	}
	
	public ActionCopyExperiment(MongoDB m, ArrayList<ExperimentHeaderInterface> experimentHeaderList, GUIsetting guiSetting) {
		this("Copy dataset");
		this.m = m;
		this.experimentReferences = new ArrayList<ExperimentReferenceInterface>();
		expSize = 0;
		for (ExperimentHeaderInterface eh : experimentHeaderList) {
			ExperimentReference er = new ExperimentReference(eh, m);
			this.experimentReferences.add(er);
			if (eh.getSizekb() > 0)
				expSize += eh.getSizekb() * 1024;
		}
		this.guiSetting = guiSetting;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ml = MongoDB.getMongos();
		this.addUDPcopy = new SettingsHelperDefaultIsFalse().isEnabled(TabAglet.ENABLE_BROADCAST_SETTING);
		// this.addHSMcopy = new SettingsHelperDefaultIsTrue().isEnabled("ARCHIVE|enabled");
		this.vl = VirtualFileSystemFolderStorage.getKnown(true);
		this.outlierCountNumerics = 0;
		this.outlierCountBinary = 0;
		if (experimentReferences.size() == 1) {
			MeasurementFilter mf = IAPservice.getMeasurementFilter(experimentReferences.get(0).getHeader());
			
			ExperimentInterface e = experimentReferences.get(0).getData(getStatusProvider());
			for (SampleInterface s : Substance3D.getAllSamples(e)) {
				for (NumericMeasurementInterface binary : ((Sample3D) s).getMeasurements(MeasurementNodeType.binaryTypes())) {
					if (mf.isGlobalOutlierOrSpecificOutlier(binary))
						outlierCountBinary++;
				}
				for (NumericMeasurementInterface numeric : ((Sample3D) s).getMeasurements(MeasurementNodeType.OMICS)) {
					if (mf.isGlobalOutlierOrSpecificOutlier(numeric))
						outlierCountNumerics++;
				}
			}
		}
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
		ThreadSafeOptions includeOutliers = new ThreadSafeOptions();
		if (experimentReferences.size() == 1 && (outlierCountBinary > 0 || outlierCountNumerics > 0)) {
			String name = "Skipping Outliers";
			if (outlierCountNumerics > 0 && outlierCountBinary > 0)
				name = name + "<br>(" + outlierCountNumerics + " numerics, " + outlierCountBinary + " files)";
			else
				if (outlierCountNumerics > 0)
					name = name + "<br>(" + outlierCountNumerics + " numeric value" + (outlierCountNumerics > 1 ? "s" : "") + ")";
				else
					name = name + "<br>(" + outlierCountBinary + " file" + (outlierCountBinary > 1 ? "s" : "") + ")";
			ActionToggle removeOutliers =
					new ActionToggle("Remove outliers during copy?", "Complete Copy<br>(outliers included)", name, includeOutliers);
			
			res.add(new NavigationButton(removeOutliers, guiSetting));
		}
		for (MongoDB m : ml)
			res.add(new NavigationButton(new ActionCopyListOfExperimentsToMongo(m, experimentReferences, false, !includeOutliers.getBval(0, false)), guiSetting));
		for (VirtualFileSystem vx : vl) {
			if (vx instanceof VirtualFileSystemVFS2) {
				VirtualFileSystemVFS2 v = (VirtualFileSystemVFS2) vx;
				res.add(new NavigationButton(new ActionDataExportToVfs(m,
						experimentReferences, v, !includeOutliers.getBval(0, false), null), guiSetting));
			}
		}
		
		if (experimentReferences.size() == 1)
			if (addUDPcopy) {
				res.add(new NavigationButton(new ActionDataUdpBroadcast(m, experimentReferences.iterator().next()), guiSetting));
			}
		
		// if (experimentReferences.size() == 1)
		// if (addHSMcopy) {
		// String hsmf = IAPmain.getHSMfolder();
		// if (hsmf != null)
		// res.add(new NavigationButton(new ActionDataExportToHsmFolder(m, experimentReferences.iterator().next(), hsmf), guiSetting));
		// }
		
		res.add(new NavigationButton(new ActionDataExportToUserSelectedFileSystemFolder(
				"Copy dataset to a user-selected target folder",
				m, experimentReferences, !includeOutliers.getBval(0, false)), guiSetting));
		
		res.add(new NavigationButton(new ActionDataExportToUserSelectedVFStarget(
				"Copy dataset to a user-selected remote target",
				m, experimentReferences, !includeOutliers.getBval(0, false)), guiSetting));
		
		return res;
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getNetworkedServers();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Folder-move2.png";// transfer2c.png";
	}
	
	@Override
	public String getDefaultTitle() {
		if (experimentReferences.size() == 1)
			return "Copy";
		else
			return "Copy Set of Experiments (" + experimentReferences.size() + " e., ~" + SystemAnalysis.getDataAmountString(expSize) + ")";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.m = experimentReference.getM();
		this.experimentReferences = new ArrayList<ExperimentReferenceInterface>();
		this.experimentReferences.add(experimentReference);
	}
}
