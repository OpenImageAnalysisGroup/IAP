package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public interface ImageProcessor {
	
	public abstract FlexibleMaskAndImageSet pipeline(FlexibleImageSet input, FlexibleImageSet optInputMasks, int maxThreadsPerImage,
			FlexibleImageStack debugStack,
			boolean automaticParameterSearch,
			boolean cropResult)
			throws InstantiationException, IllegalAccessException, InterruptedException;
	
	public abstract BlockProperties getSettings();
	
}