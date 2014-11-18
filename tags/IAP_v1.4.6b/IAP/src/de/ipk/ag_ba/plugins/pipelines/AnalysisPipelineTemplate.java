package de.ipk.ag_ba.plugins.pipelines;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.pipelines.ImageProcessorOptionsAndResults;
import de.ipk.ag_ba.gui.PipelineDesc;

/**
 * @author Christian Klukas
 */
public interface AnalysisPipelineTemplate {
	/**
	 * @return Title of the analysis template.
	 */
	public String getTitle();
	
	/**
	 * @return Description of the analysis pipeline, e.g. indication for what kind of images
	 *         the pipeline is best suited.
	 */
	public String getDescription();
	
	/**
	 * @param options
	 *           The template may initialize the settings, if needed.
	 * @return The ordered list of analysis blocks.
	 */
	public ImageAnalysisBlock[] getBlockList(ImageProcessorOptionsAndResults options);
	
	public boolean analyzeTopImages();
	
	public boolean analyzeSideImages();
	
	public PipelineDesc getDefaultPipelineDesc();
	
	public String getTestedIAPversion();
}
