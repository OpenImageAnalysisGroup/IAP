package de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis;

import org.IniIoProvider;

import de.ipk.ag_ba.image.analysis.ImageProcessor;
import de.ipk.ag_ba.image.analysis.arabidopsis.ArabidopsisAnalysisPipelineBlueSmallAndMiddle;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class ArabidopsisAnalysisSmallBlueRubberTask extends AbstractPhenotypingTask {
	
	public ArabidopsisAnalysisSmallBlueRubberTask() {
		// empty
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Arabidopsis Phenotype (small, blue rubber)";
	}
	
	@Override
	public String getName() {
		return "Arabidopsis Phenotyping (small, blue rubber)";
	}
	
	@Override
	public IniIoProvider getIniIo() {
		return null;
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new ArabidopsisAnalysisPipelineBlueSmallAndMiddle();
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
