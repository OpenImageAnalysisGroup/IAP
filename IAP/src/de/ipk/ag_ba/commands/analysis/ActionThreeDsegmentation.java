package de.ipk.ag_ba.commands.analysis;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ActionViewExportData;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.VolumeSegmentation;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ActionThreeDsegmentation extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReference experiment;
	
	NavigationButton src = null;
	MainPanelComponent mpc;
	ArrayList<NavigationButton> storedActions = new ArrayList<NavigationButton>();
	
	public ActionThreeDsegmentation(MongoDB m, ExperimentReference experiment) {
		super("Perform Color-based Volume Segmentation based on SOM");
		this.m = m;
		this.experiment = experiment;
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		if (storedActions.size() > 0)
			return;
		storedActions = new ArrayList<NavigationButton>();
		this.src = src;
		
		try {
			ExperimentInterface res = experiment.getData();
			res = res.clone();
			
			// src.title = src.title + ": processing";
			
			Collection<Sample3D> workset = new ArrayList<Sample3D>();
			
			for (SubstanceInterface m : res) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						workset.add(sd3);
					}
				}
			}
			// for (Measurement md : sd3.getMeasurements(MeasurementNodeType.VOLUME)) {
			// if (md instanceof VolumeData) {
			// VolumeData i = (VolumeData) md;
			// workset.add(i);
			// }
			// }
			
			DatabaseTarget saveVolumesToDB = new DataBaseTargetMongoDB(true, m, m.getColls());
			VolumeSegmentation segmentationTask = new VolumeSegmentation(saveVolumesToDB);
			segmentationTask.setInput(
					AbstractPhenotypingTask.getWateringInfo(res),
					workset, null, m, 0, 1);
			
			segmentationTask.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 1, status);
			
			for (NumericMeasurementInterface nmi : segmentationTask.getOutput()) {
				nmi.getParentSample().add(nmi);
				nmi.getParentSample().recalculateSampleAverage();
			}
			
			for (SubstanceInterface si : res)
				for (ConditionInterface ci : si)
					ci.setExperimentName(ci.getExperimentName() + " (segmented)");
			
			MyExperimentInfoPanel ip = new MyExperimentInfoPanel();
			ip.setExperimentInfo(experiment.m, res.getHeader(), true, res);
			mpc = new MainPanelComponent(ip, true);
			
			ActionViewExportData viewAction = new ActionViewExportData();
			viewAction.setExperimentReference(new ExperimentReference(res));
			storedActions.add(new NavigationButton(viewAction, src.getGUIsetting()));
			
			storedActions.add(new NavigationButton(new ActionCopyToMongo(m,
					new ExperimentReference(res)), "Store Dataset", "img/ext/user-desktop.png", src.getGUIsetting())); // PoweredMongoDBgreen.png"));
			
			for (ActionDataProcessing adp : IAPpluginManager.getInstance()
					.getExperimentProcessingActions(new ExperimentReference(res), true))
				storedActions.add(new NavigationButton(adp, guiSetting));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
		// src.title = src.title.split("\\:")[0];
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
		res.addAll(storedActions);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	public static NavigationButton getThreeDsegmentationTaskEntity(final MongoDB m,
			final ExperimentReference experiment, String title, final double epsilon, final double epsilon2,
			GUIsetting guiSetting) {
		
		NavigationAction segmentationAction = new ActionThreeDsegmentation(m, experiment);
		NavigationButton resultTaskButton = new NavigationButton(segmentationAction, title,
				"img/RotationReconstructionSegmentation.png", guiSetting);
		return resultTaskButton;
	}
}