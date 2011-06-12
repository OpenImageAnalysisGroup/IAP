/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertiesImpl;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * @author pape, klukas, entzian
 */
public abstract class AbstractImageProcessor implements ImageProcessor {
	
	protected final ImageProcessorOptions options;
	private final BlockProperties settings;
	private final BlockPipeline pipeline;
	
	public AbstractImageProcessor(ImageProcessorOptions options) {
		this(options, new BlockPropertiesImpl());
	}
	
	public AbstractImageProcessor(ImageProcessorOptions options, BlockPropertiesImpl settings) {
		this.options = options;
		this.settings = settings;
		this.pipeline = getPipeline();
	}
	
	public void setValuesToStandard(double scale) {
		options.initStandardValues(scale);
	}
	
	public FlexibleMaskAndImageSet pipeline(FlexibleImageSet input, int maxThreadsPerImage, FlexibleImageStack debugStack,
			boolean automaticParameterSearch,
			boolean cropResult)
			throws InstantiationException, IllegalAccessException, InterruptedException {
		return pipeline(input, null, maxThreadsPerImage, debugStack, automaticParameterSearch, cropResult);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.image.analysis.maize.ImageProcessor#pipeline(de.ipk.ag_ba.image.structures.FlexibleImageSet,
	 * de.ipk.ag_ba.image.structures.FlexibleImageSet, int, de.ipk.ag_ba.image.structures.FlexibleImageStack, boolean, boolean)
	 */
	@Override
	public FlexibleMaskAndImageSet pipeline(FlexibleImageSet input, FlexibleImageSet optInputMasks, int maxThreadsPerImage, FlexibleImageStack debugStack,
			boolean automaticParameterSearch,
			boolean cropResult)
			throws InstantiationException, IllegalAccessException, InterruptedException {
		
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, optInputMasks != null ? optInputMasks : input);
		
		FlexibleMaskAndImageSet result = pipeline.execute(workset, debugStack, settings, getStatus());
		
		if (debugStack != null)
			debugStack.addImage("RESULT", result.getOverviewImage(options.getIntSetting(Setting.DEBUG_STACK_WIDTH)));
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.image.analysis.maize.ImageProcessor#getSettings()
	 */
	@Override
	public BlockProperties getSettings() {
		return settings;
	}
	
	protected abstract BlockPipeline getPipeline();
}
