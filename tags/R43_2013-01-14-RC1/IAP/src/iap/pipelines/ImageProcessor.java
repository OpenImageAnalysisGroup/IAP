package iap.pipelines;

import java.util.HashMap;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public interface ImageProcessor {
	
	public abstract HashMap<Integer, StringAndFlexibleMaskAndImageSet> pipeline(
			ImageProcessorOptions options,
			FlexibleImageSet input, FlexibleImageSet optInputMasks, int maxThreadsPerImage,
			HashMap<Integer, FlexibleImageStack> debugStack)
			throws Exception;
	
	public abstract HashMap<Integer, BlockResultSet> getSettings();
	
	public abstract BlockPipeline getPipeline(ImageProcessorOptions options);
	
	public abstract void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status);
	
	public abstract BackgroundTaskStatusProviderSupportingExternalCall getStatus();
	
	public abstract TreeMap<Long, HashMap<Integer, BlockResultSet>> postProcessPipelineResults(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData2,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> analysisResults,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			ImageProcessorOptions options)
			throws Exception;
	
	public abstract void setValidTrays(int[] debugValidTrays);
}