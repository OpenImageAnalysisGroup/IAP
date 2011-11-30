/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.barley;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockApplyMaskButNotOnVIS;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosing_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCopyVisContentOnFluoMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCopyVisMaskToNirMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCropMasks;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockEnlargeVisAndFluoMasks;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMorphologicalOperations;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClustersOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters_vis_fluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockResizeMasksToLargest;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockTransferImageSet;
import de.ipk.ag_ba.image.operations.blocks.cmds.debug.BlockLoadImagesIfNeeded_images_masks;
import de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search.BlockAutomaticParameterSearchRotation;
import de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search.BlockAutomaticParameterSearchScaling;
import de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search.BlockAutomaticParameterSearchTranslation;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockPostProcessEdgeErodeEnlarge;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockPostProcessEdgeErodeEnlargeOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockPostProcessEdgeErodeReduce;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockPostProcessEdgeErodeReduceOnFluo;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * @author entzian, klukas
 */
public class BarleyTopImageProcessor {
	
	private final ImageProcessorOptions options;
	private final BlockResultSet settings;
	
	public BarleyTopImageProcessor(ImageProcessorOptions options) {
		this(options, new BlockResults());
	}
	
	public BarleyTopImageProcessor(ImageProcessorOptions options, BlockResults settings) {
		this.options = options;
		this.settings = settings;
	}
	
	public void setValuesToStandard(double scale) {
		options.initStandardValues(scale);
	}
	
	public FlexibleMaskAndImageSet pipeline(FlexibleImageSet input, int maxThreadsPerImage, FlexibleImageStack debugStack,
			boolean automaticParameterSearch,
			boolean cropResult)
			throws Exception {
		// if (debugStack != null)
		// options.setDebugTakeTimes(true);
		
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, input.copy());
		
		FlexibleMaskAndImageSet result = null;
		
		BlockPipeline p = new BlockPipeline();
		
		if (automaticParameterSearch) {
			p.add(BlockLoadImagesIfNeeded_images_masks.class);
			// p.add(BlockClearBackground.class);
			p.add(BlockClosing_fluo.class);
			p.add(BlockRemoveSmallClusters_vis_fluo.class);
			// p.add(BlockDataAnalysis.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockMorphologicalOperations.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockResizeMasksToLargest.class);
			// p.add(BlockPrintInfos.class);
			// #################
			p.add(BlockAutomaticParameterSearchTranslation.class);
			p.add(BlockAutomaticParameterSearchScaling.class);
			p.add(BlockAutomaticParameterSearchRotation.class);
			// #################
			// p.add(BlockPrintInfos.class);
			// p.add(BlockMorphologicalOperations.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockEnlargeVisAndFluoMasks.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockCopyVisMaskToNirMask.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockCopyVisContentOnFluoMask.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockPostProcessEdgeErodeReduceOnFluo.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockRemoveSmallClustersOnFluo.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockPostProcessEdgeErodeEnlargeOnFluo.class);
			p.add(BlockApplyMaskButNotOnVIS.class);
			p.add(BlockLoadImagesIfNeeded_images_masks.class);
			p.add(BlockTransferImageSet.class);
			// p.add(BlockPrintInfosEND.class);
			
		} else {
			// p.add(BlockClearBackground.class);
			p.add(BlockClosing_fluo.class);
			p.add(BlockRemoveSmallClusters_vis_fluo.class);
			// p.add(BlockDataAnalysis.class);
			p.add(BlockResizeMasksToLargest.class);
			p.add(BlockEnlargeVisAndFluoMasks.class);
			p.add(BlockCopyVisMaskToNirMask.class);
			p.add(BlockApplyMaskButNotOnVIS.class);
			p.add(BlockPostProcessEdgeErodeReduce.class);
			// p.add(BlockRemoveSmallClusters.class);
			p.add(BlockPostProcessEdgeErodeEnlarge.class);
			p.add(BlockTransferImageSet.class);
		}
		
		boolean cropWorking = true;
		
		if (cropResult && cropWorking)
			p.add(BlockCropMasks.class);
		
		result = p.execute(options, workset, debugStack, settings, null);
		
		if (debugStack != null) {
			debugStack.addImage("RESULT", result.getOverviewImage(options.getIntSetting(Setting.DEBUG_STACK_WIDTH)));
			// debugStack.print("Debug Result Overview");
		}
		
		return result;
	}
	
	public BlockResultSet getSettings() {
		return settings;
	}
}
