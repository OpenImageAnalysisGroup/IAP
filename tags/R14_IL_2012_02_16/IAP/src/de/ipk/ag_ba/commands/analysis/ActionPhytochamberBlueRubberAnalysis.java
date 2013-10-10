package de.ipk.ag_ba.commands.analysis;

import java.util.HashSet;

import de.ipk.ag_ba.commands.ImageConfiguration;
import de.ipk.ag_ba.gui.navigation_actions.maize.AbstractPhenotypeAnalysisAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhytochamberAnalysisBlueRubberTask;

/**
 * @author klukas
 */
public class ActionPhytochamberBlueRubberAnalysis extends AbstractPhenotypeAnalysisAction {
	public ActionPhytochamberBlueRubberAnalysis(MongoDB m, ExperimentReference experiment) {
		super("Analyze Phenotype (Arabidopsis small to middle, with blue rubber mat)");
		this.m = m;
		this.experiment = experiment;
		this.experimentResult = null;
		if (experiment != null && experiment.getHeader() != null)
			this.mongoDatasetID = experiment.getHeader().getDatabaseId();
	}
	
	public ActionPhytochamberBlueRubberAnalysis() {
		super("Analyze Phenotype (Arabidopsis small to middle, with blue rubber mat)");
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		return new PhytochamberAnalysisBlueRubberTask();
	}
	
	@Override
	protected HashSet<ImageConfiguration> getValidImageTypes() {
		HashSet<ImageConfiguration> res = new HashSet<ImageConfiguration>();
		res.addAll(ImageConfiguration.getTopImageTypes());
		res.addAll(ImageConfiguration.getSideImageTypes());
		return res;
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
	
	@Override
	public String getDefaultImage() {
		return "img/ext/arabidopsis.middle.blue.cover.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Arabidopsis Analysis<br>(blue cover)";
	}
	
}