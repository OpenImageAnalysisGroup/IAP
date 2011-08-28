/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.phytochamber;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertiesImpl;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockApplyMasksToImages;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosing_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCopyVisMaskToNirMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCropAllFixedPhytoOne;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCrop_images_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockEnlargeVisAndFluoMasks;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockFilterFluoMaskByValue30;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMoveNirMaskAndImageFixedUp;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClustersOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockResizeMasksToLargest;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockSetMasksToNull;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockSetVisAndFluoMaskFromMergedVisAndFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search.BlockAutomaticParameterSearchRotationOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search.BlockAutomaticParameterSearchScalingOnFluo;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * @author entzian, klukas
 */
public class PhytochamberTopImageProcessor {
	
	private final ImageProcessorOptions options;
	private final BlockProperties settings;
	
	public PhytochamberTopImageProcessor(ImageProcessorOptions options) {
		this(options, new BlockPropertiesImpl());
	}
	
	public PhytochamberTopImageProcessor(ImageProcessorOptions options, BlockPropertiesImpl settings) {
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
		
		BlockPipeline p = new BlockPipeline();
		p.add(BlockCropAllFixedPhytoOne.class);
		// p.add(BlockClearBackground.class);
		p.add(BlockFilterFluoMaskByValue30.class);
		p.add(BlockRemoveSmallClusters_vis_fluo.class);
		p.add(BlockClosing_fluo.class);
		p.add(BlockResizeMasksToLargest.class);
		p.add(BlockEnlargeVisAndFluoMasks.class);
		if (automaticParameterSearch) {
			p.add(BlockAutomaticParameterSearchRotationOnFluo.class);
			p.add(BlockAutomaticParameterSearchScalingOnFluo.class);
			p.add(BlockAutomaticParameterSearchRotationOnFluo.class);
			p.add(BlockAutomaticParameterSearchScalingOnFluo.class);
		}
		p.add(BlockSetVisAndFluoMaskFromMergedVisAndFluo.class);
		p.add(BlockMoveNirMaskAndImageFixedUp.class);
		p.add(BlockCopyVisMaskToNirMask.class);
		p.add(BlockRemoveSmallClustersOnFluo.class);
		p.add(BlockApplyMasksToImages.class);
		p.add(BlockSetMasksToNull.class);
		if (cropResult)
			p.add(BlockCrop_images_vis_fluo_nir.class);
		
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, input);
		FlexibleMaskAndImageSet result = p.execute(options, workset, debugStack, settings, null);
		
		if (debugStack != null)
			debugStack.addImage("RESULT", result.getOverviewImage(options.getIntSetting(Setting.DEBUG_STACK_WIDTH)));
		
		return result;
	}
	
	public BlockProperties getSettings() {
		return settings;
	}
}
