package de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis;

import iap.pipelines.ImageProcessor;
import iap.pipelines.arabidopsis.ArabidopsisAnalysisPipelineBlue;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class ArabidopsisAnalysisSmallBlueRubberTask extends AbstractPhenotypingTask {
	
	public final static String DEFAULT_NAME = "Arabidopsis Phenotyping (blue background)";
	public final static String DEFAULT_DESC = "Analyse Arabidopsis Phenotype";
	
	public ArabidopsisAnalysisSmallBlueRubberTask(PipelineDesc pd) {
		super(pd);
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new ArabidopsisAnalysisPipelineBlue(getSystemOptions());
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
