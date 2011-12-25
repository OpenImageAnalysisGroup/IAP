/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.maize;

import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
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
	
	private final BlockResultSet settings;
	
	public AbstractImageProcessor() {
		this(new BlockResults());
	}
	
	public AbstractImageProcessor(BlockResults settings) {
		this.settings = settings;
	}
	
	public FlexibleMaskAndImageSet pipeline(
			ImageProcessorOptions options,
			FlexibleImageSet input,
			int maxThreadsPerImage,
			FlexibleImageStack debugStack)
			throws Exception {
		return pipeline(options, input, null, maxThreadsPerImage, debugStack);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.image.analysis.maize.ImageProcessor#pipeline(de.ipk.ag_ba.image.structures.FlexibleImageSet,
	 * de.ipk.ag_ba.image.structures.FlexibleImageSet, int, de.ipk.ag_ba.image.structures.FlexibleImageStack, boolean, boolean)
	 */
	@Override
	public FlexibleMaskAndImageSet pipeline(
			ImageProcessorOptions options,
			FlexibleImageSet input,
			FlexibleImageSet optInputMasks,
			int maxThreadsPerImage,
			FlexibleImageStack debugStack)
			throws Exception {
		
		BlockPipeline pipeline = getPipeline(options);
		
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, optInputMasks != null ? optInputMasks : input);
		
		FlexibleMaskAndImageSet result = pipeline.execute(options, workset, debugStack, settings, getStatus());
		
		if (debugStack != null)
			debugStack.addImage("RESULT", result.getOverviewImage(options.getIntSetting(Setting.DEBUG_STACK_WIDTH)));
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.image.analysis.maize.ImageProcessor#getSettings()
	 */
	@Override
	public BlockResultSet getSettings() {
		return settings;
	}
	
	protected abstract BlockPipeline getPipeline(ImageProcessorOptions options);
	
	@Override
	public TreeMap<Long, BlockResultSet> postProcessPipelineResults(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData2,
			TreeMap<Long, Sample3D> inSample,
			TreeMap<Long, TreeMap<String, ImageData>> inImages,
			TreeMap<Long, TreeMap<String, BlockResultSet>> analysisResults,
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
}
