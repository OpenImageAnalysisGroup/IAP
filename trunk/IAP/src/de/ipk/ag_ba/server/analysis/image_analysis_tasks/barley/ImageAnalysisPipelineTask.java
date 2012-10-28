package de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley;

import de.ipk.ag_ba.image.analysis.Pipeline;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class ImageAnalysisPipelineTask extends AbstractPhenotypingTask {
	
	private final String name;
	private final String desc;
	
	public ImageAnalysisPipelineTask(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}
	
	@Override
	public String getTaskDescription() {
		return desc;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	protected ImageProcessor getImageProcessor() {
		return new Pipeline(name);
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
