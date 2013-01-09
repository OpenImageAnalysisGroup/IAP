package de.ipk.ag_ba.commands.analysis;

import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_actions.maize.AbstractPhenotypeAnalysisAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis.ArabidopsisAnalysisTask;

/**
 * @author klukas
 */
public class ActionPhytochamberAnalysis extends AbstractPhenotypeAnalysisAction {
	public ActionPhytochamberAnalysis(MongoDB m, ExperimentReference experiment) {
		super("Analyze Phenotype (Arabidopsis middle to large, without soil cover)");
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
	}
	
	public ActionPhytochamberAnalysis() {
		super("Analyze Phenotype (Arabidopsis middle to large, without soil cover)");
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		PipelineDesc pd = new PipelineDesc(
				StringManipulationTools.getFileSystemName(ArabidopsisAnalysisTask.DEFAULT_NAME) + ".pipeline.ini",
				experiment.getIniIoProvider(),
				ArabidopsisAnalysisTask.DEFAULT_NAME, ArabidopsisAnalysisTask.DEFAULT_DESC);
		return new ArabidopsisAnalysisTask(pd);
	}
	
	@Override
	public int getCpuTargetUtilization() {
		return 1;
	}
	
	@Override
	public int getNumberOfJobs() {
		int snapshotsPerJob = 500;
		try {
			snapshotsPerJob = snapshotsPerJob / IAPservice.getMaxTrayCount(experiment.getData(m));
		} catch (Exception e) {
			System.err.println(SystemAnalysis.getCurrentTime() + ">Could not get experiment data for analysis of tray-count. Error is ignored at this stage.");
			e.printStackTrace();
		}
		int numberOfJobs = experiment.getHeader().getNumberOfFiles() / 3 / snapshotsPerJob;
		
		return numberOfJobs;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/arabidopsis.large.no.cover.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Arabidopsis Analysis<br>(no cover)";
	}
}