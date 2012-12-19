package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import org.IniIoProvider;

import de.ipk.ag_ba.image.analysis.ImageProcessor;
import de.ipk.ag_ba.image.analysis.arabidopsis.ArabidopsisAnalysisPipelineBlueSmallAndMiddle;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class PhytochamberAnalysisBlueRubberTask extends AbstractPhenotypingTask {
	
	public PhytochamberAnalysisBlueRubberTask() {
		// empty
	}
	
	@Override
	public String getTaskDescription() {
		return "Analyse Arabidopsis Phenotype (for small to middle single plants with blue rubber mat)";
	}
	
	@Override
	public String getName() {
		return "Arabidopsis Phenotyping<br>(Small/Middle, Blue Rubber)";
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
