package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.MaizeAnalysisPipelineProcessor;

/**
 * @author klukas
 */
public class MaizeAnalysisTask extends AbstractMaizePhenotypingTask {
	
	public MaizeAnalysisTask() {
		// empty
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Maize Phenotype";
	}
	
	@Override
	public String getName() {
		return "Maize Phenotyping";
	}
	
	@Override
	protected ImageProcessor getImageProcessor(ImageProcessorOptions options) {
		return new MaizeAnalysisPipelineProcessor(options);
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
