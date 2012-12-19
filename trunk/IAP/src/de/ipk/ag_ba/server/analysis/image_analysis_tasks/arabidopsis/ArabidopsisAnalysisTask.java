package de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis;

import org.IniIoProvider;

import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.phytochamber.ArabidopsisAnalysisPipeline;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class ArabidopsisAnalysisTask extends AbstractPhenotypingTask {
	
	public ArabidopsisAnalysisTask() {
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
	public IniIoProvider getIniIo() {
		return null;
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
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
