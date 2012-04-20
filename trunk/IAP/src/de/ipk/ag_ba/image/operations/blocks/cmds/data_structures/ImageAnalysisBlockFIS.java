package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import java.util.HashMap;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public interface ImageAnalysisBlockFIS {
	
	public void setInputAndOptions(FlexibleMaskAndImageSet input, ImageProcessorOptions options, BlockResultSet settings, int blockPositionInPipeline,
			FlexibleImageStack debugStack);
	
	public FlexibleMaskAndImageSet process() throws InterruptedException;
	
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws InterruptedException;
	
	/**
	 * Returns a list of <code>Parameter</code> that are set for this
	 * algorithm.
	 * 
	 * @return a collection of <code>Parameter</code> that are needed by the <code>Algorithm</code>.
	 */
	public Parameter[] getParameters();
	
	/**
	 * Sets the parameters for this algorithm. Must have the same types and
	 * order as the array returned by <code>getParameter</code>.
	 */
	public void setParameters(Parameter[] params);
}
