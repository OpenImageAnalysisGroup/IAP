package iap.blocks.data_structures;

import iap.pipelines.ImageProcessorOptionsAndResults;

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

public interface ImageAnalysisBlock extends Comparable<ImageAnalysisBlock> {
	
	public void setInputAndOptions(int well, MaskAndImageSet input, ImageProcessorOptionsAndResults options, BlockResultSet settings, int blockPositionInPipeline,
			ImageStack debugStack);
	
	public MaskAndImageSet process() throws InterruptedException;
	
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandId2time2waterData,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws InterruptedException;
	
	public void setPreventDebugValues(boolean preventSecondShowingOfDebugWindows);
	
	/**
	 * @return List of (possible) input camera types the block will process and (possibly) change.
	 */
	public HashSet<CameraType> getCameraInputTypes();
	
	/**
	 * @return List of (possible) output camera types the block will process and (possibly) change.
	 */
	public HashSet<CameraType> getCameraOutputTypes();
	
	/**
	 * @return Block processing type for categorization.
	 */
	public BlockType getBlockType();
	
	/**
	 * @return User friendly block name.
	 */
	public String getName();
	
	/**
	 * @return Extended block description.
	 */
	public String getDescription();
	
	/**
	 * @return Extended parameter description help.
	 */
	public String getDescriptionForParameters();
}
