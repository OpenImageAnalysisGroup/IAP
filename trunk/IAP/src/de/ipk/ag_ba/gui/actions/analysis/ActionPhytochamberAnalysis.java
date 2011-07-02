package de.ipk.ag_ba.gui.actions.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.actions.ActionFileManager;
import de.ipk.ag_ba.gui.actions.ActionMongoOrLemnaTecExperimentNavigation;
import de.ipk.ag_ba.gui.actions.ActionCopyToMongo;
import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhytochamberAnalysisTask;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class ActionPhytochamberAnalysis extends AbstractNavigationAction implements RemoteCapableAnalysisAction {
	private MongoDB m;
	private ExperimentReference experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	ArrayList<ZoomedImage> zoomedImages = new ArrayList<ZoomedImage>();
	private Experiment experimentResult;
	
	// used when started as remote analysis task
	private RunnableWithMappingData resultReceiver;
	private int workOnSubset;
	private int numberOfSubsets;
	private String mongoDatasetID;
	
	public ActionPhytochamberAnalysis(MongoDB m, double epsilon, double epsilon2,
						ExperimentReference experiment) {
		super("Analyse Phytochamber Top-Images");
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
	}
	
	public ActionPhytochamberAnalysis() {
		super("Analyse phytochamber top-images");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
		if (experimentResult != null)
			return;
		
		try {
			ExperimentInterface res = experiment.getData(m);
			
			ArrayList<NumericMeasurementInterface> workload = new ArrayList<NumericMeasurementInterface>();
			
			HashSet<String> ignored = new HashSet<String>();
			
			for (SubstanceInterface m : res) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						for (Measurement md : sd3.getMeasurements(MeasurementNodeType.IMAGE)) {
							if (md instanceof ImageData) {
								ImageConfiguration config = ImageConfiguration.get(((ImageData) md).getSubstanceName());
								if (config == ImageConfiguration.Unknown)
									config = ImageConfiguration.get(((ImageData) md).getURL().getFileName());
								
								if (config == ImageConfiguration.FluoTop) {
									ImageData i = (ImageData) md;
									workload.add(i);
								} else
									if (config == ImageConfiguration.RgbTop) {
										ImageData i = (ImageData) md;
										workload.add(i);
									} else
										if (config == ImageConfiguration.NirTop) {
											ImageData i = (ImageData) md;
											workload.add(i);
										} else
											ignored.add(((ImageData) md).getSubstanceName());
							}
						}
					}
				}
			}
			
			if (status != null)
				status.setCurrentStatusText1("Experiment: " + workload.size() + " images");
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setInt(1);
			
			int pi = 1;// SystemAnalysis.getNumberOfCPUs();
			int ti = 1;// SystemAnalysis.getNumberOfCPUs() / 2;
			
			PhytochamberAnalysisTask task = new PhytochamberAnalysisTask();
			
			task.setInput(workload, m, workOnSubset, numberOfSubsets);
			task.performAnalysis(pi, ti, status);
			
			final ArrayList<MappingData3DPath> newStatisticsData = new ArrayList<MappingData3DPath>();
			Collection<NumericMeasurementInterface> statRes = task.getOutput();
			
			if (statRes == null) {
				ErrorMsg.addErrorMessage("Error: no statistics result");
			} else {
				for (NumericMeasurementInterface m : statRes) {
					if (m == null)
						System.out.println("Error: null value in statistical result set");
					else
						newStatisticsData.add(new MappingData3DPath(m));
				}
			}
			
			final Experiment statisticsResult = new Experiment(MappingData3DPath.merge(newStatisticsData, false));
			statisticsResult.getHeader().setExperimentname(statisticsResult.getName());
			statisticsResult.getHeader().setImportusergroup(getDefaultTitle());
			for (SubstanceInterface s : statisticsResult) {
				for (ConditionInterface c : s) {
					c.setExperimentInfo(statisticsResult.getHeader());
				}
			}
			
			System.out.println();
			System.out.println("Statistics results :                  " + newStatisticsData.size());
			// System.out.println("Statistics results within Experiment: " + statisticsResult.getNumberOfMeasurementValues());
			
			statisticsResult.getHeader().setDatabaseId("");
			if (resultReceiver == null) {
				if (status != null)
					status.setCurrentStatusText1("Ready");
				
				MyExperimentInfoPanel info = new MyExperimentInfoPanel();
				info.setExperimentInfo(m, statisticsResult.getHeader(), false, statisticsResult);
				mpc = new MainPanelComponent(info, true);
			} else {
				mpc = new MainPanelComponent("Running in batch-mode. Partial result is not shown at this place.");
				resultReceiver.setExperimenData(statisticsResult);
				resultReceiver.run();
			}
			this.experimentResult = statisticsResult;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
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
		
		res.add(ActionFileManager.getFileManagerEntity(m, new ExperimentReference(experimentResult),
							src.getGUIsetting()));
		
		res.add(new NavigationButton(new ActionCopyToMongo(m, new ExperimentReference(experimentResult)),
							"Save Result", "img/ext/user-desktop.png", src.getGUIsetting())); // PoweredMongoDBgreen.png"));
		
		ActionMongoOrLemnaTecExperimentNavigation.getDefaultActions(res, experimentResult, experimentResult.getHeader(),
							false, src.getGUIsetting(), m);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/phyto.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Arabidopsis Analysis";
	}
	
	@Override
	public void setWorkingSet(int workOnSubset, int numberOfSubsets, RunnableWithMappingData resultReceiver) {
		this.resultReceiver = resultReceiver;
		this.workOnSubset = workOnSubset;
		this.numberOfSubsets = numberOfSubsets;
	}
	
	@Override
	public void setParams(ExperimentReference experiment, MongoDB m, String params) {
		this.experiment = experiment;
		this.m = m;
	}
	
	@Override
	public String getMongoDatasetID() {
		return mongoDatasetID;
	}
	
	@Override
	public MongoDB getMongoDB() {
		return m;
	}
}