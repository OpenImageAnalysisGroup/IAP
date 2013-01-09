package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import iap.pipelines.ImageProcessor;
import iap.pipelines.maize.MaizeAnalysisPipeline;
import de.ipk.ag_ba.gui.PipelineDesc;

/**
 * @author klukas
 */
public class MaizeAnalysisTask extends AbstractPhenotypingTask {
	
	public static final String DEFAULT_DESC = "Analyze Maize Phenotype";
	public static final String DEFAULT_NAME = "Maize Analysis";
	
	public MaizeAnalysisTask(PipelineDesc pMaize) {
		super(pMaize);
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new MaizeAnalysisPipeline(getSystemOptions());
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
