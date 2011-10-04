package de.ipk.ag_ba.gui.navigation_actions.maize;

import java.util.HashSet;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.BarleyAnalysisTask;

/**
 * @author klukas
 */
public class BarleyAnalysisAction extends AbstractPhenotypeAnalysisAction {
	
	public BarleyAnalysisAction(MongoDB m, ExperimentReference experiment) {
		super("Analyze Phenotype (Barley)");
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
	}
	
	public BarleyAnalysisAction() {
		super("Analyze Phenotype (Barley)");
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		return new BarleyAnalysisTask();
	}
	
	@Override
	protected HashSet<ImageConfiguration> getValidImageTypes() {
		HashSet<ImageConfiguration> res = new HashSet<ImageConfiguration>();
		res.addAll(ImageConfiguration.getTopImageTypes());
		res.addAll(ImageConfiguration.getSideImageTypes());
		return res;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/000Grad.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Barley Analysis";
	}
	
	@Override
	public int getCpuTargetUtilization() {
		return 1;
	}
	
	@Override
	public int getNumberOfJobs() {
		int snapshotsPerJob = 1000;
		int numberOfJobs = experiment.getHeader().getNumberOfFiles() / 3 / snapshotsPerJob;
		
		return numberOfJobs;
	}
	
}