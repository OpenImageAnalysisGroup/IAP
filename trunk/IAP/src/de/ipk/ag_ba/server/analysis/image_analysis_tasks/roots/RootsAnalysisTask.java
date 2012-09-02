package de.ipk.ag_ba.server.analysis.image_analysis_tasks.roots;

import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.roots.RootsAnalysisPipeline;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class RootsAnalysisTask extends AbstractPhenotypingTask {
	
	public RootsAnalysisTask() {
		// empty
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Root Scans";
	}
	
	@Override
	public String getName() {
		return "Root Scan Analysis";
	}
	
	@Override
	protected ImageProcessor getImageProcessor() {
		return new RootsAnalysisPipeline();
	}
	
	@Override
	protected boolean analyzeTopImages() {
		return true;
	}
	
	@Override
	protected boolean analyzeSideImages() {
		return false;
	}
}
