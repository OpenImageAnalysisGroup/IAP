package iap.pipelines;

import java.util.HashMap;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public interface ImageProcessor {
	
	public abstract HashMap<Integer, StringAndFlexibleMaskAndImageSet> execute(
			ImageProcessorOptions options,
			ImageSet input, ImageSet optInputMasks, int maxThreadsPerImage,
			HashMap<Integer, ImageStack> debugStack)
			throws Exception;
	
	public abstract HashMap<Integer, BlockResultSet> getNumericResults();
	
	public abstract BlockPipeline getPipeline(ImageProcessorOptions options);
	
	public abstract void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status);
	
	public abstract BackgroundTaskStatusProviderSupportingExternalCall getStatus();
	
	public abstract TreeMap<Long, HashMap<Integer, BlockResultSet>> postProcessPlantResults(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData2,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> analysisResults,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			ImageProcessorOptions options)
			throws Exception;
	
	public abstract void setValidTrays(int[] debugValidTrays);
}