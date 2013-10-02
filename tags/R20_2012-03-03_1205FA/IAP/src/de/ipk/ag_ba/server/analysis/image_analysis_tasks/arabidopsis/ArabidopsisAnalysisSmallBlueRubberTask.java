package de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis;

import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.phytochamber.ArabidopsisAnalysisPipelineBlueSmallAndMiddle;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class ArabidopsisAnalysisSmallBlueRubberTask extends AbstractPhenotypingTask {
	
	public ArabidopsisAnalysisSmallBlueRubberTask() {
		// empty
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Arabidopsis Phenotype (small, blue rubber)";
	}
	
	@Override
	public String getName() {
		return "Arabidopsis Phenotyping (small, blue rubber)";
	}
	
	@Override
	protected ImageProcessor getImageProcessor() {
		return new ArabidopsisAnalysisPipelineBlueSmallAndMiddle();
	}
	
	@Override
	protected boolean analyzeTopImages() {
		return true;
	}
	
	@Override
	protected boolean analyzeSideImages() {
		return true;
	}
	
}