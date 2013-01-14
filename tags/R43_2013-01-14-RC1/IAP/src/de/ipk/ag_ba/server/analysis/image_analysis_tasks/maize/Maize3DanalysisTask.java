package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import iap.pipelines.ImageProcessor;
import iap.pipelines.maize.MaizeAnalysisPipelineWith3D;
import de.ipk.ag_ba.gui.PipelineDesc;

/**
 * @author klukas
 */
public class Maize3DanalysisTask extends AbstractPhenotypingTask {
	
	public static final String DEFAULT_DESC = "Analyse Maize 3D-Phenotype";
	public static final String DEFAULT_NAME = "Maize 3D-Phenotyping";
	
	public Maize3DanalysisTask(PipelineDesc pd) {
		super(pd);
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new MaizeAnalysisPipelineWith3D(getSystemOptions());
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
