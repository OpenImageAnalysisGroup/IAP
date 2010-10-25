package de.ipk.ag_ba.gui.navigation_actions;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;
import java.util.HashMap;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.ImageAnalysis3D;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoOrLemnaTecExperimentNavigationAction;
import de.ipk.ag_ba.rmi_server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.ThreeDreconstruction;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.VolumeStatistics;
import de.ipk.ag_ba.rmi_server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.rmi_server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import display.DisplayHistogram;

/**
 * @author klukas
 */
public class ThreeDreconstructionAction extends AbstractNavigationAction {

	private final String login;
	private final String pass;
	private final ExperimentReference experiment;

	NavigationGraphicalEntity src = null;
	MainPanelComponent mpc;
	ArrayList<ZoomedImage> zoomedImages = new ArrayList<ZoomedImage>();
	DisplayHistogram histogram, histogramG, histogramB;
	ArrayList<NavigationGraphicalEntity> storedActions = new ArrayList<NavigationGraphicalEntity>();

	public ThreeDreconstructionAction(String login, String pass, ExperimentReference experiment) {
		super("Create 3-D Volumes using Space Carving Technology");
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

		histogram = null;
		histogramG = null;
		histogramB = null;

		try {
			ExperimentInterface res = experiment.getData();

			// src.title = src.title + ": processing";

			HashMap<Sample3D, ArrayList<NumericMeasurementInterface>> workset = new HashMap<Sample3D, ArrayList<NumericMeasurementInterface>>();

			for (SubstanceInterface m : res) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						for (Measurement md : sd3.getAllMeasurements()) {
							if (md instanceof ImageData) {
								ImageData i = (ImageData) md;
								if (workset.get(sd3) == null)
									workset.put(sd3, new ArrayList<NumericMeasurementInterface>());
								workset.get(sd3).add(i);
							}
						}
					}
				}
			}

			ArrayList<MappingData3DPath> newStatisticsData = new ArrayList<MappingData3DPath>();

			for (Sample3D s3d : workset.keySet()) {

				ArrayList<NumericMeasurementInterface> workload = workset.get(s3d);

				if (workload.size() < 1)
					continue;

				// DatabaseTarget saveClearedImagesToDB = null;
				// ClearAndCalcStatsBackground clearTask = new
				// ClearAndCalcStatsBackground(epsilon, epsilon2,
				// saveClearedImagesToDB);
				// clearTask.setInput(workload, login, pass);
				// clearTask.performAnalysis(SystemAnalysis.getNumberOfCPUs(),
				// status);
				//
				// Collection<NumericMeasurementInterface> clearedImages =
				// clearTask.getOutput();

				VolumeStatistics volumeStatistics = new VolumeStatistics();

				DatabaseTarget saveVolumesToDB = new DataBaseTargetMongoDB(true);

				ThreeDreconstruction threeDreconstructionTask = new ThreeDreconstruction(saveVolumesToDB);
				threeDreconstructionTask.setInput(workload, login, pass);

				threeDreconstructionTask.addResultProcessor(volumeStatistics);

				threeDreconstructionTask.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 1, status);

				HashMap<ImageAnalysisTask, ArrayList<NumericMeasurementInterface>> volumeStatisticsResults = threeDreconstructionTask
						.getAdditionalResults();

				ArrayList<NumericMeasurementInterface> statRes = volumeStatisticsResults.get(volumeStatistics);
				if (statRes == null) {
					ErrorMsg.addErrorMessage("Error: no statistics result");
				} else {
					// add volumes (if available)
					statRes.addAll(threeDreconstructionTask.getOutput());
					for (NumericMeasurementInterface m : statRes) {
						newStatisticsData.add(new MappingData3DPath(m));
					}
				}
			}

			Experiment statisticsResult = new Experiment(MappingData3DPath.merge(newStatisticsData));

			System.out.println(statisticsResult.toString());

			MyExperimentInfoPanel ip = new MyExperimentInfoPanel();
			ip.setExperimentInfo(login, pass, statisticsResult.getHeader(), true, statisticsResult);
			mpc = new MainPanelComponent(ip, true);

			storedActions.add(FileManagerAction.getFileManagerEntity(login, pass, new ExperimentReference(statisticsResult),
					src.getGUIsetting()));

			storedActions.add(new NavigationGraphicalEntity(new CloudUploadEntity(login, pass, new ExperimentReference(
					statisticsResult)), "Store Dataset", "img/ext/user-desktop.png", src.getGUIsetting())); // PoweredMongoDBgreen.png"));

			MongoOrLemnaTecExperimentNavigationAction.getDefaultActions(storedActions, statisticsResult,
					statisticsResult.getHeader(), false, src.getGUIsetting());
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
		if (!ImageAnalysis3D.isSaveInDatabase()) {
			NavigationGraphicalEntity imageHistogram = new NavigationGraphicalEntity(TableLayout.get3Split(histogram,
					histogramG, histogramB, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED),
					src.getGUIsetting());
			res.add(imageHistogram);

			NavigationGraphicalEntity imageZoom = new NavigationGraphicalEntity(
					ImageAnalysis3D.getImageZoomSlider(zoomedImages), src.getGUIsetting());
			res.add(imageZoom);
		}
		res.addAll(storedActions);
		return res;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}

	public static NavigationGraphicalEntity getThreeDreconstructionTaskEntity(final String login, final String pass,
			final ExperimentReference experiment, String title, final double epsilon, final double epsilon2,
			GUIsetting guiSetting) {

		NavigationAction clearBackgroundAction = new ThreeDreconstructionAction(login, pass, experiment);
		NavigationGraphicalEntity resultTaskButton = new NavigationGraphicalEntity(clearBackgroundAction, title,
				"img/RotationReconstruction.png", guiSetting);
		return resultTaskButton;
	}
}