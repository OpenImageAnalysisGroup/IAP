package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import org.IniIoProvider;

import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.MaizeAnalysisPipeline;

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
	public IniIoProvider getIniIo() {
		return null;
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new MaizeAnalysisPipeline("Maize Phenotyping", null);
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
