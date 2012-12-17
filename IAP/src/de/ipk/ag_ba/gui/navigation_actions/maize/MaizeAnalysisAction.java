package de.ipk.ag_ba.gui.navigation_actions.maize;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.MaizeAnalysisTask;

/**
 * @author klukas
 */
public class MaizeAnalysisAction extends AbstractPhenotypeAnalysisAction {
	
	public MaizeAnalysisAction(MongoDB m, ExperimentReference experiment) {
		super("Analyze Phenotype (Maize)");
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
	}
	
	public MaizeAnalysisAction() {
		super("Analyze Phenotype (Maize)");
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		return new MaizeAnalysisTask();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/mais.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Maize Analysis";
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