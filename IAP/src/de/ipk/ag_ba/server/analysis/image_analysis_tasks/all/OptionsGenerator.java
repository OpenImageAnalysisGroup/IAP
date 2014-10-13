package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import iap.pipelines.ImageProcessorOptionsAndResults;

/**
 * @author Christian Klukas
 */
public interface OptionsGenerator {
	
	ImageProcessorOptionsAndResults getOptions();
	
}
