package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import org.IoStringProvider;

import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.phytochamber.ArabidopsisAnalysisPipeline;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class PhytochamberAnalysisTask extends AbstractPhenotypingTask {
	
	public PhytochamberAnalysisTask() {
		// empty
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Arabidopsis Phenotype";
	}
	
	@Override
	public String getName() {
		return "Arabidopsis Phenotyping";
	}
	
	@Override
	public IoStringProvider getIniIo() {
		return null;
	}
	
	@Override
	protected ImageProcessor getImageProcessor() {
		return new ArabidopsisAnalysisPipeline();
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
