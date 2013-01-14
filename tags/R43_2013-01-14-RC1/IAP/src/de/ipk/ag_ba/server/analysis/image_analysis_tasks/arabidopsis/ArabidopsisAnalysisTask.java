package de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis;

import iap.pipelines.ImageProcessor;
import iap.pipelines.arabidopsis.ArabidopsisAnalysisPipeline;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class ArabidopsisAnalysisTask extends AbstractPhenotypingTask {
	
	public static final String DEFAULT_DESC = "Analyse Arabidopsis Phenotype";
	public static final String DEFAULT_NAME = "Arabidopsis Phenotyping";
	
	public ArabidopsisAnalysisTask(PipelineDesc pd) {
		super(pd);
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new ArabidopsisAnalysisPipeline(getSystemOptions());
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
