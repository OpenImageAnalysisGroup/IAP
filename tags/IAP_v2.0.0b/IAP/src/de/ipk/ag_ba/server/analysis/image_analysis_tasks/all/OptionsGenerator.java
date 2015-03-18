package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import iap.pipelines.ImageProcessorOptionsAndResults;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author Christian Klukas
 */
public interface OptionsGenerator {
	
	ImageProcessorOptionsAndResults getOptions();
	
	ImageSet getImageSet();
	
	ImageSet getMaskSet();
	
}
