package de.ipk.ag_ba.gui.navigation_actions.maize;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.UserDefinedImageAnalysisPipelineTask;

/**
 * @author klukas
 */
public class ImageAnalysisAction extends AbstractPhenotypeAnalysisAction {
	private PipelineDesc pd;
	
	public ImageAnalysisAction() {
		super(null);
	}
	
	public ImageAnalysisAction(PipelineDesc pd, MongoDB m, ExperimentReference experiment) {
		super(pd.getTooltip());
		this.pd = pd;
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		return new UserDefinedImageAnalysisPipelineTask(pd);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/000Grad.png";
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