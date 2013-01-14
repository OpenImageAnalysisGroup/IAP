package de.ipk.ag_ba.server.analysis.image_analysis_tasks.roots;

import iap.pipelines.ImageProcessor;
import iap.pipelines.roots.RootsAnalysisPipeline;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class RootsAnalysisTask extends AbstractPhenotypingTask {
	
	public static final String DEFAULT_NAME = "Root Scan Analysis";
	public static final String DEFAULT_DESC = "Analyse Root Scans";
	
	public RootsAnalysisTask(PipelineDesc pd) {
		super(pd);
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new RootsAnalysisPipeline(getSystemOptions());
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
