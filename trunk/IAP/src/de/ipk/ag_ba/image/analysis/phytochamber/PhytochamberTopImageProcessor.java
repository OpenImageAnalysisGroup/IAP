/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.phytochamber;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockApplyMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockAutomaticParameterSearchRotation;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockAutomaticParameterSearchScaling;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockAutomaticParameterSearchTranslation;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearBackground;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockDataAnalysis;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockEnlargeMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockEqualize;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMergeMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMorphologicalOperations;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockOpeningClosing;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockPostProcessEdgeErodeEnlarge;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockPostProcessEdgeErodeReduce;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockTransferImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * @author entzian, klukas
 */
public class PhytochamberTopImageProcessor {
	
	private final PhytoTopImageProcessorOptions options;
	
	public PhytochamberTopImageProcessor(PhytoTopImageProcessorOptions options) {
		this.options = options;
	}
	
	public void setValuesToStandard(double scale) {
		options.initStandardValues(scale);
	}
	
	public FlexibleMaskAndImageSet pipeline(FlexibleImageSet input, int maxThreadsPerImage, FlexibleImageStack debugStack, boolean automaticParameterSearch)
			throws InstantiationException, IllegalAccessException {
		if (debugStack != null)
			options.setDebugTakeTimes(true);
		
		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, input.copy());
		
		FlexibleMaskAndImageSet result = null;
		
		BlockPipeline p = new BlockPipeline(options);
		
		if (automaticParameterSearch) {
			p.add(BlockClearBackground.class);
			p.add(BlockOpeningClosing.class);
			p.add(BlockRemoveSmallClusters.class);
			p.add(BlockDataAnalysis.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockMorphologicalOperations.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockEqualize.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockAutomaticParameterSearchTranslation.class);
			p.add(BlockAutomaticParameterSearchScaling.class);
			p.add(BlockAutomaticParameterSearchRotation.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockMorphologicalOperations.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockEnlargeMask.class);
			// p.add(BlockPrintInfos.class);
			p.add(BlockMergeMask.class);
			p.add(BlockApplyMask.class);
			p.add(BlockPostProcessEdgeErodeReduce.class);
			p.add(BlockRemoveSmallClusters.class);
			p.add(BlockPostProcessEdgeErodeEnlarge.class);
			p.add(BlockTransferImageSet.class);
		} else {
			p.add(BlockClearBackground.class);
			p.add(BlockOpeningClosing.class);
			p.add(BlockRemoveSmallClusters.class);
			p.add(BlockDataAnalysis.class);
			p.add(BlockEqualize.class);
			p.add(BlockEnlargeMask.class);
			p.add(BlockMergeMask.class);
			p.add(BlockApplyMask.class);
			p.add(BlockPostProcessEdgeErodeReduce.class);
			// p.add(BlockRemoveSmallClusters.class);
			p.add(BlockPostProcessEdgeErodeEnlarge.class);
			p.add(BlockTransferImageSet.class);
		}
		
		result = p.execute(workset, debugStack);
		
		if (debugStack != null) {
			debugStack.addImage("RESULT", result.getOverviewImage(options.getDebugStackWidth()));
			debugStack.print("Debug Result Overview");
		}
		
		return result;
	}
}
