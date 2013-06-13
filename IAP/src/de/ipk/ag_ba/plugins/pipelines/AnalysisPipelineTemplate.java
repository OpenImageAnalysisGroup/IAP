package de.ipk.ag_ba.plugins.pipelines;

import iap.blocks.data_structures.ImageAnalysisBlock;

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
	 * @return The ordered list of analysis blocks.
	 */
	public ImageAnalysisBlock[] getBlockList();
	
	public boolean analyzeTopImages();
	
	public boolean analyzeSideImages();
}
