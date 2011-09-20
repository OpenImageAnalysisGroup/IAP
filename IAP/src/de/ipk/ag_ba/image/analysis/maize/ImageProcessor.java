package de.ipk.ag_ba.image.analysis.maize;

import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public interface ImageProcessor {
	
	public abstract FlexibleMaskAndImageSet pipeline(
			ImageProcessorOptions options,
			FlexibleImageSet input, FlexibleImageSet optInputMasks, int maxThreadsPerImage,
			FlexibleImageStack debugStack)
			throws InstantiationException, IllegalAccessException, InterruptedException;
	
	public abstract BlockProperties getSettings();
	
	public abstract void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status);
	
	public abstract BackgroundTaskStatusProviderSupportingExternalCall getStatus();
	
	public abstract BlockProperties postProcessPipelineResults(Sample3D inSample,
			TreeMap<String, ImageData> inImages,
			TreeMap<String, BlockProperties> analysisResults)
			throws InstantiationException,
			IllegalAccessException;
}