package iap.blocks.preprocessing;

import iap.pipelines.ImageProcessorOptions;

/**
 * A well processor should remove parts, that do not correspond to the current
 * "well" (image object). The actual image analysis pipeline is executed as often
 * as the getDefinedWellCount says.
 * 
 * @author klukas
 */
public interface WellProcessor {
	
	public int getDefinedWellCount(ImageProcessorOptions options);
	
}
