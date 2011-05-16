/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertiesImpl;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockCalculateMainAxis;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockClearBackgroundByComparingNullImageAndImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.maize.BlockFindBlueMarkers;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * @author pape, klukas, entzian
 */
public class MaizeImageProcessor {
	
	private final ImageProcessorOptions options;
	private final BlockProperties settings;
	
	public MaizeImageProcessor(ImageProcessorOptions options) {
		this(options, new BlockPropertiesImpl());
	}
	
	public MaizeImageProcessor(ImageProcessorOptions options, BlockPropertiesImpl settings) {
		this.options = options;
		this.settings = settings;
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
	
	public FlexibleMaskAndImageSet pipeline(FlexibleImageSet input, FlexibleImageSet optInputMasks, int maxThreadsPerImage, FlexibleImageStack debugStack,
			boolean automaticParameterSearch,
			boolean cropResult)
			throws InstantiationException, IllegalAccessException, InterruptedException {
		
		BlockPipeline p = new BlockPipeline(options);
		
		p.add(BlockClearBackgroundByComparingNullImageAndImage.class);
		p.add(BlockCalculateMainAxis.class);
		p.add(BlockFindBlueMarkers.class);
		
		// p.add(BlockCropAllFixedPhytoOne.class);
		// p.add(BlockClearBackground.class);
		// p.add(BlockFilterFluoMaskByValue30.class);
		// p.add(BlockRemoveSmallClusters.class);
		// p.add(BlockClosing.class);
		// p.add(BlockResizeMasksToLargest.class);
		// p.add(BlockEnlargeVisAndFluoMasks.class);
		// if (automaticParameterSearch) {
		// p.add(BlockAutomaticParameterSearchRotationOnFluo.class);
		// p.add(BlockAutomaticParameterSearchScalingOnFluo.class);
		// p.add(BlockAutomaticParameterSearchRotationOnFluo.class);
		// p.add(BlockAutomaticParameterSearchScalingOnFluo.class);
		// }
		// p.add(BlockSetVisAndFluoMaskFromMergedVisAndFluo.class);
		// p.add(BlockMoveNirMaskAndImageFixedUp.class);
		// p.add(BlockCopyVisMaskToNirMask.class);
		// p.add(BlockRemoveSmallClustersOnFluo.class);
		// p.add(BlockApplyMasksToImages.class);
		// p.add(BlockSetMasksToNull.class);
		// if (cropResult)
		// p.add(BlockCropImages.class);
		
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, optInputMasks != null ? optInputMasks : input);
		FlexibleMaskAndImageSet result = p.execute(workset, debugStack, settings);
		
		if (debugStack != null)
			debugStack.addImage("RESULT", result.getOverviewImage(options.getIntSetting(Setting.DEBUG_STACK_WIDTH)));
		
		return result;
	}
	
	public BlockProperties getSettings() {
		return settings;
	}
}
