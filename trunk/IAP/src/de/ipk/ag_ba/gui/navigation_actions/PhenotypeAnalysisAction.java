package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.MongoOrLemnaTecExperimentNavigationAction;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk.ag_ba.rmi_server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.rmi_server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class PhenotypeAnalysisAction extends AbstractNavigationAction implements RemoteCapableAnalysisAction {
	
	private double epsilon;
	private double epsilon2;
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
	private MongoDB m;
	
	public PhenotypeAnalysisAction(MongoDB m, double epsilon, double epsilon2,
						ExperimentReference experiment) {
		super("Create phenotype data set");
		this.m = m;
		this.epsilon = epsilon;
		this.epsilon2 = epsilon2;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getExcelfileid();
	}
	
	public PhenotypeAnalysisAction() {
		super("Create phenotype data set");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
		if (experimentResult != null)
			return;
		
		try {
			ExperimentInterface res = experiment.getData(m).clone();
			experiment = null;
			
			ArrayList<NumericMeasurementInterface> workload = new ArrayList<NumericMeasurementInterface>();
			
			int workIndex = 0;
			for (SubstanceInterface m : res) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						for (Measurement md : sd3.getMeasurements(MeasurementNodeType.IMAGE)) {
							workIndex++;
							if (resultReceiver == null || workIndex % numberOfSubsets == workOnSubset)
								if (md instanceof ImageData) {
									ImageData i = (ImageData) md;
									workload.add(i);
								}
						}
					}
				}
			}
			
			if (status != null)
				status.setCurrentStatusText1("Workload: " + workload.size() + " images");
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setInt(1);
			
			PhenotypeAnalysisTask task = new PhenotypeAnalysisTask(epsilon, epsilon2, new DataBaseTargetMongoDB(true, m));
			
			// task.addPreprocessor(new CutImagePreprocessor());
			
			TreeMap<Long, String> times = new TreeMap<Long, String>();
			// for (int r = 1; r <= 3; r++)
			// for (int pi = SystemAnalysis.getNumberOfCPUs(); pi >= 1; pi -= 4)
			// for (int ti = SystemAnalysis.getNumberOfCPUs(); ti >= 1; ti -= 4) {
			long t1 = System.currentTimeMillis();
			task.setInput(workload, m);
			int pi = SystemAnalysis.getNumberOfCPUs();
			if (pi < 1)
				pi = 1;
			int ti = 4;
			task.performAnalysis(pi, ti, status);
			long t2 = System.currentTimeMillis();
			// String ss = "T(s)/IMG/T(s)/PI/TI\t" + Math.round(((t2 - t1) / 100d / workload.size())) / 10d + "\t"
			// + ((t2 - t1) / 1000) + "\t" + pi + "\t" + ti;
			// times.put((t2 - t1), ss);
			// System.out.println("------------------------------------------------------------");
			// System.out.println("--- " + ss);
			// System.out.println("------------------------------------------------------------");
			// for (String s : times.values()) {
			// System.out.println(s);
			// }
			// }
			
			final ArrayList<MappingData3DPath> newStatisticsData = new ArrayList<MappingData3DPath>();
			Collection<NumericMeasurementInterface> statRes = task.getOutput();
			// for (NumericMeasurementInterface id : statRes) {
			// if (id instanceof ImageData)
			// System.out.println("Output: " + (((ImageData)
			// id).getURL()).getDetail());
			// }
			
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
			
			final Experiment statisticsResult = new Experiment(MappingData3DPath.merge(newStatisticsData));
			statisticsResult.getHeader().setExperimentname(statisticsResult.getName());
			statisticsResult.getHeader().setImportusergroup(getDefaultTitle());
			// Substance.addAndMerge(statisticsResult, experiment.getData());
			
			// SupplementaryFilePanelMongoDB sfp = new
			// SupplementaryFilePanelMongoDB(login, pass, statisticsResult,
			// statisticsResult.getName());
			statisticsResult.getHeader().setExcelfileid("");
			if (resultReceiver == null) {
				// if (status != null)
				// status.setCurrentStatusText1("Store Result");
				
				// new MongoDB().storeExperiment("dbe3", null, null, null,
				// statisticsResult, status);
				
				if (status != null)
					status.setCurrentStatusText1("Ready");
				
				MyExperimentInfoPanel info = new MyExperimentInfoPanel();
				info.setExperimentInfo(m, statisticsResult.getHeader(), false, statisticsResult);
				// mpc = new MainPanelComponent(TableLayout.getSplit(info, sfp,
				// TableLayout.PREFERRED, TableLayout.FILL));
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
		// src.title = src.title.split("\\:")[0];
	}
	
	// private void loadAndShowResultImages(final ArrayList<JComponent> results,
	// final HashMap<JComponent, String> names,
	// final HashMap<String, Histogram> histosR, final HashMap<String, Histogram>
	// histosG,
	// final HashMap<String, Histogram> histosB,
	// Collection<NumericMeasurementInterface> statRes) throws Exception {
	// for (Measurement md : statRes) {
	// if (!(md instanceof ImageData))
	// continue;
	// ImageData id = (ImageData) md;
	// if (ImageAnalysis.isSaveInDatabase() == null) {
	// LoadedImage limg;
	// if (id instanceof LoadedImage)
	// limg = (LoadedImage) id;
	// else
	// limg = IOmodule.loadImageFromFileOrMongo(id, login, pass);
	//
	// BufferedImage img = limg.getLoadedImage();
	//
	// ZoomedImage zoomedImage = new ZoomedImage(img);
	// zoomedImages.add(zoomedImage);
	// results.add(zoomedImage);
	// names.put(zoomedImage, limg.getURL().getFileName());
	// {
	// Histogram histoR1 = ImageAnalysis.getHistogram(img, 1);
	// histosR.put(limg.getURL().getFileName(), histoR1);
	// Histogram histoG1 = ImageAnalysis.getHistogram(img, 2);
	// histosG.put(limg.getURL().getFileName(), histoG1);
	// Histogram histoB1 = ImageAnalysis.getHistogram(img, 3);
	// histosB.put(limg.getURL().getFileName(), histoB1);
	// if (histogram == null) {
	// DisplayHistogram dhR1 = new DisplayHistogram(histoR1, histoG1, histoB1,
	// "");
	// dhR1.setBG(new Color(0, 0, 0));
	// dhR1.setBinWidth(2);
	// dhR1.setHeight(80);
	// dhR1.setIndexMultiplier(1);
	// histogram = dhR1;
	// histogram.setBorder(BorderFactory.createLoweredBevelBorder());
	// }
	// }
	// }
	// }
	//
	// final JTabbedPane jtp = new JTabbedPane(JTabbedPane.TOP,
	// JTabbedPane.SCROLL_TAB_LAYOUT);
	// jtp.addChangeListener(new ChangeListener() {
	// public void stateChanged(ChangeEvent e) {
	// if (histogram != null && histogram.isVisible()) {
	// histogram.init(histosR.get(jtp.getTitleAt(jtp.getSelectedIndex())),
	// histosG.get(jtp.getTitleAt(jtp
	// .getSelectedIndex())),
	// histosB.get(jtp.getTitleAt(jtp.getSelectedIndex())));
	// histogram.repaint();
	// }
	// }
	// });
	// for (JComponent jc : results)
	// jtp.add(names.get(jc), new JScrollPane(jc));
	// mpc = new MainPanelComponent(jtp);
	// }
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		
		res.add(FileManagerAction.getFileManagerEntity(m, new ExperimentReference(experimentResult),
							src.getGUIsetting()));
		
		res.add(new NavigationButton(new CloudUploadEntity(m, new ExperimentReference(experimentResult)),
							"Save Result", "img/ext/user-desktop.png", src.getGUIsetting())); // PoweredMongoDBgreen.png"));
		
		MongoOrLemnaTecExperimentNavigationAction.getDefaultActions(res, experimentResult, experimentResult.getHeader(),
							false, src.getGUIsetting(), m);
		//
		// for (NavigationButton ne :
		// ImageAnalysisCommandManager.getCommands(login, pass, new
		// ExperimentReference(
		// experimentResult), false, src.getGUIsetting()))
		// res.add(ne);
		//
		// for (NavigationButton ne :
		// Other.getProcessExperimentDataWithVantedEntities(null, null, new
		// ExperimentReference(
		// experimentResult), src.getGUIsetting())) {
		// if (ne.getTitle().contains("Put data")) {
		// ne.setTitle("View in VANTED");
		// res.add(ne);
		// }
		// }
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/000Grad_3.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Barley/Maize Analysis";
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
		this.epsilon = 10;
		this.epsilon2 = 15;
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