package de.ipk.ag_ba.commands.experiment.process;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_actions.maize.AbstractPhenotypeAnalysisAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.UserDefinedImageAnalysisPipelineTask;

/**
 * @author klukas
 */
public class ActionPerformGridAnalysis extends AbstractPhenotypeAnalysisAction {
	private final PipelineDesc pd;
	private int numberOfJobs;
	
	public ActionPerformGridAnalysis() {
		super(null);
		pd = null;
	}
	
	public ActionPerformGridAnalysis(PipelineDesc pd, MongoDB m, ExperimentReference experimentReference) {
		super(pd.getTooltip());
		this.pd = pd;
		this.m = m;
		this.experiment = experimentReference;
		this.experimentResult = null;
		if (experimentReference != null && experimentReference.getHeader() != null)
			this.mongoDatasetID = experimentReference.getHeader().getDatabaseId();
		int snapshotsPerJob = 500;
		this.numberOfJobs = experimentReference.getHeader().getNumberOfFiles() / 3 / snapshotsPerJob;
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		if (pd == null)
			return new UserDefinedImageAnalysisPipelineTask(
					new PipelineDesc(null, experiment.getIniIoProvider(), null, null));
		else
			return new UserDefinedImageAnalysisPipelineTask(pd);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/network-workgroup-power.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Submit " + numberOfJobs + " analysis jobs to " + (m != null ? m.getDatabaseName() : "(database instance is null)") + "";
	}
	
	@Override
	public int getCpuTargetUtilization() {
		return 1;
	}
	
	@Override
	public int getNumberOfJobs() {
		return numberOfJobs;
	}
	
}