package de.ipk.ag_ba.commands.analysis;

import info.clearthought.layout.TableLayout;

import java.util.ArrayList;
import java.util.HashMap;

import org.ErrorMsg;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ActionViewExportData;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ThreeDreconstruction;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.VolumeStatistics;
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
	
	public ActionThreeDreconstruction(MongoDB m, ExperimentReference experiment) {
		super("Create 3-D Volumes using Space Carving Technology");
		this.m = m;
		this.experiment = experiment;
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		if (storedActions.size() > 0)
			return;
		storedActions = new ArrayList<NavigationButton>();
		this.src = src;
		
		Object[] inp = MyInputHelper.getInput("Please specify the cube resolution:", "3-D Reconstruction", new Object[] {
				"Resolution (X=Y=Z)", voxelresolution, "Trim Width? (0..100)", widthFactor });
		if (inp == null)
			return;
		voxelresolution = (Integer) inp[0];
		widthFactor = (Integer) inp[1];
		ArrayList<Sample3D> workset = new ArrayList<Sample3D>();
		try {
			ExperimentInterface res = experiment.getData(m).clone();
			
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
			
			DatabaseTarget saveVolumesToDB = new DataBaseTargetMongoDB(true, m, m.getColls());
			
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
			ip.setExperimentInfo(m, statisticsResult.getHeader(), true, statisticsResult);
			mpc = new MainPanelComponent(ip, true);
			
			storedActions.add(ActionViewExportData.getFileManagerEntity(m,
					new ExperimentReference(statisticsResult), src.getGUIsetting()));
			
			storedActions.add(new NavigationButton(new ActionCopyToMongo(m, new ExperimentReference(
					statisticsResult)), "Save Result", "img/ext/user-desktop.png", src.getGUIsetting())); // PoweredMongoDBgreen.png"));
			
			ActionMongoOrLTexperimentNavigation.getDefaultActions(storedActions,
					new ExperimentReference(statisticsResult),
					statisticsResult
							.getHeader(), false, src.getGUIsetting(), m);
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
	
	public static NavigationButton getThreeDreconstructionTaskEntity(MongoDB m,
			final ExperimentReference experiment, String title, final double epsilon, final double epsilon2,
			GUIsetting guiSetting) {
		
		NavigationAction threeDreconstructionAction = new ActionThreeDreconstruction(m, experiment);
		NavigationButton resultTaskButton = new NavigationButton(threeDreconstructionAction, title,
				"img/RotationReconstruction.png", guiSetting);
		return resultTaskButton;
	}
}