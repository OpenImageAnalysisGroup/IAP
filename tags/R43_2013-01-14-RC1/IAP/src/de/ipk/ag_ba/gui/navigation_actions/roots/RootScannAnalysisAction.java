package de.ipk.ag_ba.gui.navigation_actions.roots;

import org.StringManipulationTools;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_actions.maize.AbstractPhenotypeAnalysisAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.roots.RootsAnalysisTask;

/**
 * @author klukas
 */
public class RootScannAnalysisAction extends AbstractPhenotypeAnalysisAction {
	public RootScannAnalysisAction(MongoDB m, ExperimentReference experiment) {
		super("Analyze Root Scanns");
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
	}
	
	public RootScannAnalysisAction() {
		super("Analyze Root Scanns");
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		PipelineDesc pd = new PipelineDesc(
				StringManipulationTools.getFileSystemName(RootsAnalysisTask.DEFAULT_NAME) + ".pipeline.ini",
				experiment.getIniIoProvider(),
				RootsAnalysisTask.DEFAULT_NAME, RootsAnalysisTask.DEFAULT_DESC);
		return new RootsAnalysisTask(pd);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/root.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Root Scan Analysis";
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