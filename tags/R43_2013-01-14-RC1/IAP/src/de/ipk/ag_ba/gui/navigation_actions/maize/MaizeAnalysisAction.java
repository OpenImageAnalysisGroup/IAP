package de.ipk.ag_ba.gui.navigation_actions.maize;

import org.StringManipulationTools;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.MaizeAnalysisTask;

/**
 * @author klukas
 */
public class MaizeAnalysisAction extends AbstractPhenotypeAnalysisAction {
	
	public MaizeAnalysisAction(MongoDB m, ExperimentReference experiment) {
		super(null);
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
		
		setTooltip(getImageAnalysisTask().getTaskDescription());
	}
	
	public MaizeAnalysisAction() {
		super(MaizeAnalysisTask.DEFAULT_DESC);
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		PipelineDesc pd = new PipelineDesc(
				StringManipulationTools.getFileSystemName(MaizeAnalysisTask.DEFAULT_NAME) + ".pipeline.ini",
				experiment.getIniIoProvider(),
				MaizeAnalysisTask.DEFAULT_NAME, MaizeAnalysisTask.DEFAULT_DESC);
		return new MaizeAnalysisTask(pd);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/mais.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return getImageAnalysisTask().getName();
	}
	
	@Override
	public int getCpuTargetUtilization() {
		return 1;
	}
	
	@Override
	public int getNumberOfJobs() {
		int snapshotsPerJob = 500;
		int numberOfJobs = experiment.getHeader().getNumberOfFiles() / 3 / snapshotsPerJob;
		
		return numberOfJobs;
	}
}