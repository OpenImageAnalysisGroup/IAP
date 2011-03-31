/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.barley;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertiesImpl;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockApplyMaskButNotOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockApplyMaskOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearBackground;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClosing;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockCropOnMasks;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockDataAnalysis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockEnlargeMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockEqualize;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMergeMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMorphologicalOperations;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClustersOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockTransferImageSet;
import de.ipk.ag_ba.image.operations.blocks.cmds.debug.BlockImageInfo;
import de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search.BlockAutomaticParameterSearchRotation;
import de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search.BlockAutomaticParameterSearchScaling;
import de.ipk.ag_ba.image.operations.blocks.cmds.parameter_search.BlockAutomaticParameterSearchTranslation;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockPostProcessEdgeErodeEnlarge;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockPostProcessEdgeErodeEnlargeOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockPostProcessEdgeErodeReduce;
import de.ipk.ag_ba.image.operations.blocks.cmds.post_process.BlockPostProcessEdgeErodeReduceOnFluo;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * @author entzian, klukas
 */
public class BarleyTopImageProcessor {
	
	private final ImageProcessorOptions options;
	private final BlockProperties settings;
	
	public BarleyTopImageProcessor(ImageProcessorOptions options) {
		this(options, new BlockPropertiesImpl());
	}
	
	public BarleyTopImageProcessor(ImageProcessorOptions options, BlockPropertiesImpl settings) {
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
		// if (debugStack != null)
		// options.setDebugTakeTimes(true);
		
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, input.copy());
		
		FlexibleMaskAndImageSet result = null;
		
		BlockPipeline p = new BlockPipeline(options);
		
		if (automaticParameterSearch) {
			p.add(BlockImageInfo.class);
			p.add(BlockClearBackground.class);
			p.add(BlockClosing.class);
			p.add(BlockRemoveSmallClusters.class);
			p.add(BlockDataAnalysis.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockMorphologicalOperations.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockEqualize.class);
			// p.add(BlockPrintInfos.class);
			// #################
			p.add(BlockAutomaticParameterSearchTranslation.class);
			p.add(BlockAutomaticParameterSearchScaling.class);
			p.add(BlockAutomaticParameterSearchRotation.class);
			// #################
			// p.add(BlockPrintInfos.class);
			// p.add(BlockMorphologicalOperations.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockEnlargeMask.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockMergeMask.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockApplyMaskOnFluo.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockPostProcessEdgeErodeReduceOnFluo.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockRemoveSmallClustersOnFluo.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockPostProcessEdgeErodeEnlargeOnFluo.class);
			p.add(BlockApplyMaskButNotOnFluo.class);
			p.add(BlockImageInfo.class);
			p.add(BlockTransferImageSet.class);
			// p.add(BlockPrintInfosEND.class);
			
		} else {
			p.add(BlockClearBackground.class);
			p.add(BlockClosing.class);
			p.add(BlockRemoveSmallClusters.class);
			p.add(BlockDataAnalysis.class);
			p.add(BlockEqualize.class);
			p.add(BlockEnlargeMask.class);
			p.add(BlockMergeMask.class);
			p.add(BlockApplyMaskButNotOnFluo.class);
			p.add(BlockPostProcessEdgeErodeReduce.class);
			// p.add(BlockRemoveSmallClusters.class);
			p.add(BlockPostProcessEdgeErodeEnlarge.class);
			p.add(BlockTransferImageSet.class);
		}
		
		boolean cropWorking = true;
		
		if (cropResult && cropWorking)
			p.add(BlockCropOnMasks.class);
		
		result = p.execute(workset, debugStack, settings);
		
		if (debugStack != null) {
			debugStack.addImage("RESULT", result.getOverviewImage(options.getIntSetting(Setting.DEBUG_STACK_WIDTH)));
			// debugStack.print("Debug Result Overview");
		}
		
		return result;
	}
	
	public BlockProperties getSettings() {
		return settings;
	}
}
