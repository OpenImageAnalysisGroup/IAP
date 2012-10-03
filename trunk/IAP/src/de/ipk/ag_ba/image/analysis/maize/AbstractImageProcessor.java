/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.maize;

import java.util.HashMap;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author pape, klukas, entzian
 */
public abstract class AbstractImageProcessor implements ImageProcessor {
	
	private final HashMap<Integer, BlockResultSet> settings;
	private int[] debugValidTrays;
	
	public AbstractImageProcessor() {
		this(new HashMap<Integer, BlockResultSet>());
	}
	
	public AbstractImageProcessor(HashMap<Integer, BlockResultSet> settings) {
		this.settings = settings;
	}
	
	public HashMap<Integer, FlexibleMaskAndImageSet> pipeline(
			ImageProcessorOptions options,
			FlexibleImageSet input,
			int maxThreadsPerImage,
			HashMap<Integer, FlexibleImageStack> debugStack)
			throws Exception {
		return pipeline(options, input, null, maxThreadsPerImage, debugStack);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.image.analysis.maize.ImageProcessor#pipeline(de.ipk.ag_ba.image.structures.FlexibleImageSet,
	 * de.ipk.ag_ba.image.structures.FlexibleImageSet, int, de.ipk.ag_ba.image.structures.FlexibleImageStack, boolean, boolean)
	 */
	@Override
	public HashMap<Integer, FlexibleMaskAndImageSet> pipeline(
			ImageProcessorOptions options,
			FlexibleImageSet input,
			FlexibleImageSet optInputMasks,
			int maxThreadsPerImage,
			HashMap<Integer, FlexibleImageStack> debugStack)
			throws Exception {
		BlockPipeline pipeline = getPipeline(options);
		pipeline.setValidTrays(debugValidTrays);
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, optInputMasks != null ? optInputMasks : input);
		
		HashMap<Integer, FlexibleMaskAndImageSet> result = pipeline.execute(options, workset, debugStack, settings, getStatus());
		
		if (debugStack != null)
			for (Integer key : debugStack.keySet()) {
				debugStack.get(key).addImage("RESULT", result.get(key).getOverviewImage(options.getIntSetting(Setting.DEBUG_STACK_WIDTH)));
			}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.image.analysis.maize.ImageProcessor#getSettings()
	 */
	@Override
	public HashMap<Integer, BlockResultSet> getSettings() {
		return settings;
	}
	
	public abstract BlockPipeline getPipeline(ImageProcessorOptions options);
	
	@Override
	public TreeMap<Long, HashMap<Integer, BlockResultSet>> postProcessPipelineResults(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData2,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> analysisResults,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws InstantiationException,
			IllegalAccessException, InterruptedException {
		BlockPipeline pipeline = getPipeline(null);
		return pipeline.postProcessPipelineResultsForAllAngles(
				plandID2time2waterData2,
				inSample,
				inImages,
				analysisResults,
				optStatus);
	}
	
	@Override
	public void setValidTrays(int[] debugValidTrays) {
		this.debugValidTrays = debugValidTrays;
	}
	
}
