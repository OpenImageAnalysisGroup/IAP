package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.MaizeImageProcessor;

/**
 * @author klukas
 */
public class MaizeAnalysisTask extends AbstractPhenotypingTask {
	
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
		return new MaizeImageProcessor(options);
	}
	
	@Override
	protected boolean analyzeTopImages() {
		return false;
	}
	
	@Override
	protected boolean analyzeSideImages() {
		return true;
	}
	
}
