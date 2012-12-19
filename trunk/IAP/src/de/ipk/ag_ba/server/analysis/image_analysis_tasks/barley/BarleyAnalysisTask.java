package de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley;

import org.IniIoProvider;

import de.ipk.ag_ba.image.analysis.BarleyAnalysisPipeline;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

public class BarleyAnalysisTask extends AbstractPhenotypingTask {
	
	@Override
	public ImageProcessor getImageProcessor() throws Exception {
		return new BarleyAnalysisPipeline(getName(), null);
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Barley Phenotype";
	}
	
	@Override
	public String getName() {
		return "Barley Analysis";
	}
	
	@Override
	public IniIoProvider getIniIo() {
		return null;
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
