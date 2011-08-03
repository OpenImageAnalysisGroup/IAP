package de.ipk.ag_ba.image.analysis.maize;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public interface ImageProcessor {
	
	public abstract FlexibleMaskAndImageSet pipeline(
			ImageProcessorOptions options,
			FlexibleImageSet input, FlexibleImageSet optInputMasks, int maxThreadsPerImage,
			FlexibleImageStack debugStack)
			throws InstantiationException, IllegalAccessException, InterruptedException;
	
	public abstract BlockProperties getSettings();
	
	public abstract void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status);
	
	public abstract BackgroundTaskStatusProviderSupportingExternalCall getStatus();
}