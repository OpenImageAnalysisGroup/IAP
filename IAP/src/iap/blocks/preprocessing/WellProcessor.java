package iap.blocks.preprocessing;

import iap.pipelines.ImageProcessorOptions;

public interface WellProcessor {
	
	int getDefinedWellCount(ImageProcessorOptions options);
	
}
