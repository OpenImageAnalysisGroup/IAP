package de.ipk.ag_ba.gui.navigation_actions.maize;

import java.util.HashSet;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.Maize3DanalysisTask;

/**
 * @author klukas
 */
public class Maize3DanalysisAction extends AbstractPhenotypeAnalysisAction {
	
	public Maize3DanalysisAction(MongoDB m, ExperimentReference experiment) {
		super("Analyze 3D-Phenotype (Maize)");
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
	}
	
	public Maize3DanalysisAction() {
		super("Analyze 3D-Phenotype (Maize)");
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		return new Maize3DanalysisTask();
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
		return "img/mais.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Maize 3D-Analysis";
	}
	
	@Override
	public int getCpuTargetUtilization() {
		// by returning a very high number, this task will be the only one running
		// on the cloud execution server
		return 2;// Integer.MAX_VALUE;
	}
	
	@Override
	public int getNumberOfJobs() {
		int snapshotsPerJob = 100;
		int numberOfJobs = experiment.getHeader().getNumberOfFiles() / 3 / snapshotsPerJob;
		
		return numberOfJobs;
	}
	
}