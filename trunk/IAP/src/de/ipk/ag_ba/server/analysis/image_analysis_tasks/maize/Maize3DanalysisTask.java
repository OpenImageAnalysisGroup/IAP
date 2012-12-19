package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import org.IoStringProvider;

import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.maize.MaizeAnalysisPipelineWith3D;

/**
 * @author klukas
 */
public class Maize3DanalysisTask extends AbstractPhenotypingTask {
	
	public Maize3DanalysisTask() {
		// empty
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Maize 3D-Phenotype";
	}
	
	@Override
	public String getName() {
		return "Maize 3D-Phenotyping";
	}
	
	@Override
	public IoStringProvider getIniIo() {
		return null;
	}
	
	@Override
	protected ImageProcessor getImageProcessor() {
		return new MaizeAnalysisPipelineWith3D("Maize 3D-Phenotyping", null);
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
