package de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley;

import org.IoStringProvider;

import de.ipk.ag_ba.image.analysis.Pipeline;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;

/**
 * @author klukas
 */
public class ImageAnalysisPipelineTask extends AbstractPhenotypingTask {
	
	private final String pipelineFileName;
	private final String desc;
	private final IoStringProvider iniIO;
	
	public ImageAnalysisPipelineTask(String name, IoStringProvider iniIO, String desc) {
		this.pipelineFileName = name;
		this.iniIO = iniIO;
		this.desc = desc;
	}
	
	@Override
	public String getTaskDescription() {
		return desc;
	}
	
	@Override
	public String getName() {
		return pipelineFileName;
	}
	
	@Override
	public IoStringProvider getIniIo() {
		return iniIO;
	}
	
	@Override
	protected ImageProcessor getImageProcessor() {
		try {
			return new Pipeline(pipelineFileName, iniIO);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
