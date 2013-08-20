package iap.blocks.data_structures;

import iap.pipelines.ImageProcessorOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public interface ImageAnalysisBlock {
	
	public void setInputAndOptions(MaskAndImageSet input, ImageProcessorOptions options, BlockResultSet settings, int blockPositionInPipeline,
			ImageStack debugStack);
	
	public MaskAndImageSet process() throws InterruptedException;
	
	public HashSet<CameraType> getCameraInputTypes();
	
	public HashSet<CameraType> getCameraOutputTypes();
	
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws InterruptedException;
	
	public BlockType getBlockType();
	
	public void setPreventDebugValues(boolean b);
}
