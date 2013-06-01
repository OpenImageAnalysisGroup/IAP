package de.ipk.ag_ba.commands.analysis;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;
import java.util.HashMap;

import org.ErrorMsg;
import org.IniIoProvider;
import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ActionViewExportData;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ThreeDreconstruction;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.VolumeStatistics;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.UserDefinedImageAnalysisPipelineTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import display.DisplayHistogram;

/**
 * @author klukas
 */
public class ActionThreeDreconstruction extends AbstractNavigationAction {
	
	private final MongoDB m;
	private final ExperimentReference experiment;
	
	NavigationButton src = null;
	MainPanelComponent mpc;
	ArrayList<ZoomedImage> zoomedImages = new ArrayList<ZoomedImage>();
	DisplayHistogram histogram, histogramG, histogramB;
	ArrayList<NavigationButton> storedActions = new ArrayList<NavigationButton>();
	private int voxelresolution = 500;
	private int widthFactor = 40;
	
	private boolean saveVolume = false;
	private final IniIoProvider ioStringProvider;
	
	public ActionThreeDreconstruction(IniIoProvider ioStringProvider, ExperimentReference experiment) {
		super("Create 3-D Volumes using Space Carving Technology");
		this.ioStringProvider = ioStringProvider;
		this.experiment = experiment;
		this.m = experiment.m;
	}
	
	@Override
	public String getDefaultTitle() {
		return "3D-Reconstruction";
	}

	@Override
	public String getDefaultImage() {
		return "img/RotationReconstruction.png";
	}

	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		if (storedActions.size() > 0)
			return;
		storedActions = new ArrayList<NavigationButton>();
		this.src = src;
		
		Object[] inp = MyInputHelper.getInput("Please specify the cube resolution:", "3-D Reconstruction", new Object[] {
				"Resolution (X=Y=Z)", voxelresolution, "Trim Width? (0..100)", widthFactor ,
				"Save Volume in MongoDB", saveVolume});
		if (inp == null)
			return;
		voxelresolution = (Integer) inp[0];
		widthFactor = (Integer) inp[1];
		saveVolume = (Boolean) inp[2];
		ArrayList<Sample3D> workset = new ArrayList<Sample3D>();
		try {
			ExperimentInterface res = experiment.getData().clone();
			
			// src.title = src.title + ": processing";
			
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
			
			ArrayList<MappingData3DPath> newStatisticsData = new ArrayList<MappingData3DPath>();
			
			VolumeStatistics volumeStatistics = new VolumeStatistics();
			
			DatabaseTarget saveVolumesToDB = new DataBaseTargetMongoDB(saveVolume, m, m.getColls());
			
			
			ThreeDreconstruction threeDreconstructionTask = new ThreeDreconstruction(saveVolumesToDB);
			threeDreconstructionTask.setInput(
					AbstractPhenotypingTask.getWateringInfo(res),
					workset, null, m, 0, 1);
			threeDreconstructionTask.setResolution(voxelresolution, widthFactor);
			threeDreconstructionTask.addResultProcessor(volumeStatistics);
			
			threeDreconstructionTask.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 2, status);
			
			HashMap<ImageAnalysisTask, ArrayList<NumericMeasurementInterface>> volumeStatisticsResults =
					threeDreconstructionTask.getAdditionalResults();
			
			ArrayList<NumericMeasurementInterface> statRes = volumeStatisticsResults.get(volumeStatistics);
			if (statRes == null) {
				// ErrorMsg.addErrorMessage("Error: no statistics result");
			} else {
				// add volumes (if available)
				statRes.addAll(threeDreconstructionTask.getOutput());
				for (NumericMeasurementInterface m : statRes) {
					newStatisticsData.add(new MappingData3DPath(m));
				}
			}
			
			Experiment statisticsResult = new Experiment(MappingData3DPath.merge(newStatisticsData, false));
			statisticsResult.getHeader().setDatabaseId("");
			
			MyExperimentInfoPanel ip = new MyExperimentInfoPanel();
			ip.setExperimentInfo(experiment.m, statisticsResult.getHeader(), true, statisticsResult);
			mpc = new MainPanelComponent(ip, true);
			
			ActionViewExportData viewAction = new ActionViewExportData();
			viewAction.setExperimentReference(new ExperimentReference(statisticsResult));
			storedActions.add(new NavigationButton(viewAction, src.getGUIsetting()));
			
			storedActions.add(new NavigationButton(new ActionCopyToMongo(m, new ExperimentReference(
					statisticsResult)), "Save Result", "img/ext/user-desktop.png", src.getGUIsetting())); // PoweredMongoDBgreen.png"));
			
			for (ActionDataProcessing adp : IAPpluginManager.getInstance()
					.getExperimentProcessingActions(new ExperimentReference(statisticsResult), true))
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
		if (!ImageAnalysis3D.isSaveInDatabase()) {
			NavigationButton imageHistogram = new NavigationButton(TableLayout.get3Split(histogram, histogramG,
					histogramB, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED), src.getGUIsetting());
			res.add(imageHistogram);
			
			NavigationButton imageZoom = new NavigationButton(ImageAnalysis3D.getImageZoomSlider(zoomedImages), src
					.getGUIsetting());
			res.add(imageZoom);
		}
		res.addAll(storedActions);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
}