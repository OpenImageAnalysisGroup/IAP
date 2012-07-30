package de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley;

import de.ipk.ag_ba.image.analysis.barley.BarleyAnalysisPipeline;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class BarleyAnalysisTask extends AbstractPhenotypingTask {
	
	public BarleyAnalysisTask() {
		// empty
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Barley Phenotype";
	}
	
	@Override
	public String getName() {
		return "Barley Phenotyping";
	}
	
	@Override
	protected ImageProcessor getImageProcessor() {
		return new BarleyAnalysisPipeline();
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
