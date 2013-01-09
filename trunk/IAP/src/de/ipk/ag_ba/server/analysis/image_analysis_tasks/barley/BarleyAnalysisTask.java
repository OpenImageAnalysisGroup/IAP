package de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley;

import iap.pipelines.ImageProcessor;
import iap.pipelines.barley.BarleyAnalysisPipeline;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

public class BarleyAnalysisTask extends AbstractPhenotypingTask {
	
	public static final String DEFAULT_NAME = "Barley Analysis";
	public static final String DEFAULT_DESC = "Analyse Barley Phenotype";
	
	public BarleyAnalysisTask(PipelineDesc pd) {
		super(pd);
	}
	
	@Override
	public ImageProcessor getImageProcessor() throws Exception {
		return new BarleyAnalysisPipeline(getSystemOptions());
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
