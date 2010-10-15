package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoOrLemnaTecExperimentNavigationAction;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.VolumeSegmentation;
import de.ipk.ag_ba.rmi_server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.rmi_server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;

/**
 * @author klukas
 */
public class ThreeDsegmentationAction extends AbstractNavigationAction {
	private final String login;
	private final String pass;
	private final ExperimentReference experiment;

	NavigationGraphicalEntity src = null;
	MainPanelComponent mpc;
	ArrayList<NavigationGraphicalEntity> storedActions = new ArrayList<NavigationGraphicalEntity>();

	public ThreeDsegmentationAction(String login, String pass, ExperimentReference experiment) {
		super("Perform Color-based Volume Segmentation based on SOM");
		this.login = login;
		this.pass = pass;
		this.experiment = experiment;
	}

	@Override
	public void performActionCalculateResults(final NavigationGraphicalEntity src) {
		if (storedActions.size() > 0)
			return;
		storedActions = new ArrayList<NavigationGraphicalEntity>();
		this.src = src;

		try {
			ExperimentInterface res = experiment.getData();
			res = res.clone();

			// src.title = src.title + ": processing";

			Collection<NumericMeasurementInterface> workset = new ArrayList<NumericMeasurementInterface>();

			for (SubstanceInterface m : res) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						for (Measurement md : sd3.getAllMeasurements()) {
							if (md instanceof VolumeData) {
								VolumeData i = (VolumeData) md;
								workset.add(i);
							}
						}
					}
				}
			}

			DatabaseTarget saveVolumesToDB = new DataBaseTargetMongoDB(true);
			VolumeSegmentation segmentationTask = new VolumeSegmentation(saveVolumesToDB);
			segmentationTask.setInput(workset, login, pass);

			segmentationTask.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 1, status);

			for (NumericMeasurementInterface nmi : segmentationTask.getOutput()) {
				nmi.getParentSample().add(nmi);
				nmi.getParentSample().recalculateSampleAverage();
			}

			for (SubstanceInterface si : res)
				for (ConditionInterface ci : si)
					ci.setExperimentName(ci.getExperimentName() + " (segmented)");

			MyExperimentInfoPanel ip = new MyExperimentInfoPanel();
			ip.setExperimentInfo(login, pass, res.getHeader(), true, res);
			mpc = new MainPanelComponent(ip, true);

			storedActions.add(FileManagerExp.getFileManagerEntity(login, pass, new ExperimentReference(res)));

			storedActions.add(new NavigationGraphicalEntity(new CloudUploadEntity(login, pass,
					new ExperimentReference(res)), "Store Dataset", "img/ext/user-desktop.png")); // PoweredMongoDBgreen.png"));

			MongoOrLemnaTecExperimentNavigationAction.getDefaultActions(storedActions, res, res.getHeader(), false);
			// TODO: create show with VANTED action with these action commands:
			// AIPmain.showVANTED();
			// ExperimentDataProcessingManager.getInstance().processIncomingData(statisticsResult);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
		// src.title = src.title.split("\\:")[0];
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		res.add(src);
		return res;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>();
		res.addAll(storedActions);
		return res;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}

	public static NavigationGraphicalEntity getThreeDsegmentationTaskEntity(final String login, final String pass,
			final ExperimentReference experiment, String title, final double epsilon, final double epsilon2) {

		NavigationAction segmentationAction = new ThreeDsegmentationAction(login, pass, experiment);
		NavigationGraphicalEntity resultTaskButton = new NavigationGraphicalEntity(segmentationAction, title,
				"img/RotationReconstructionSegmentation.png");
		return resultTaskButton;
	}
}