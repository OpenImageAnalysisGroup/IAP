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
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.MongoOrLemnaTecExperimentNavigationAction;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ThreeDreconstruction;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.VolumeStatistics;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import display.DisplayHistogram;

/**
 * @author klukas
 */
public class ThreeDreconstructionAction extends AbstractNavigationAction {
	
	private final MongoDB m;
	private final ExperimentReference experiment;
	
	NavigationButton src = null;
	MainPanelComponent mpc;
	ArrayList<ZoomedImage> zoomedImages = new ArrayList<ZoomedImage>();
	DisplayHistogram histogram, histogramG, histogramB;
	ArrayList<NavigationButton> storedActions = new ArrayList<NavigationButton>();
	private int voxelresolution = 200;
	private int widthFactor = 40;
	
	public ThreeDreconstructionAction(MongoDB m, ExperimentReference experiment) {
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
		
		try {
			ExperimentInterface res = experiment.getData(m).clone();
			
			// src.title = src.title + ": processing";
			
			HashMap<Sample3D, ArrayList<NumericMeasurementInterface>> workset = new HashMap<Sample3D, ArrayList<NumericMeasurementInterface>>();
			
			for (SubstanceInterface m : res) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						for (Measurement md : sd3.getMeasurements(MeasurementNodeType.IMAGE)) {
							if (md instanceof ImageData) {
								ImageData i = (ImageData) md;
								ImageConfiguration ic = ImageConfiguration.get(i.getSubstanceName());
								if (!(ic == ImageConfiguration.RgbSide || ic == ImageConfiguration.FluoSide || ic == ImageConfiguration.NirSide))
									continue;
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
				
				VolumeStatistics volumeStatistics = new VolumeStatistics();
				
				DatabaseTarget saveVolumesToDB = new DataBaseTargetMongoDB(true, m);
				
				ThreeDreconstruction threeDreconstructionTask = new ThreeDreconstruction(saveVolumesToDB);
				threeDreconstructionTask.setInput(workload, m, 0, 1);
				threeDreconstructionTask.setResolution(voxelresolution, widthFactor);
				threeDreconstructionTask.addResultProcessor(volumeStatistics);
				
				threeDreconstructionTask.performAnalysis(SystemAnalysis.getNumberOfCPUs(), 2, status);
				
				System.out.println("Process Sample: " + s3d.toString() + " Substance: " + s3d.getParentCondition().getParentSubstance().getName());
				
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
			}
			
			Experiment statisticsResult = new Experiment(MappingData3DPath.merge(newStatisticsData));
			statisticsResult.getHeader().setExcelfileid("");
			
			MyExperimentInfoPanel ip = new MyExperimentInfoPanel();
			ip.setExperimentInfo(m, statisticsResult.getHeader(), true, statisticsResult);
			mpc = new MainPanelComponent(ip, true);
			
			storedActions.add(FileManagerAction.getFileManagerEntity(m,
								new ExperimentReference(statisticsResult), src.getGUIsetting()));
			
			storedActions.add(new NavigationButton(new CloudUploadEntity(m, new ExperimentReference(
								statisticsResult)), "Save Result", "img/ext/user-desktop.png", src.getGUIsetting())); // PoweredMongoDBgreen.png"));
			
			MongoOrLemnaTecExperimentNavigationAction.getDefaultActions(storedActions, statisticsResult, statisticsResult
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
		
		NavigationAction threeDreconstructionAction = new ThreeDreconstructionAction(m, experiment);
		NavigationButton resultTaskButton = new NavigationButton(threeDreconstructionAction, title,
							"img/RotationReconstruction.png", guiSetting);
		return resultTaskButton;
	}
}