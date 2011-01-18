package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.phytochamber.PhytoTopImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public interface ImageAnalysisBlockFIS {
	
	public void setInputAndOptions(FlexibleMaskAndImageSet input, PhytoTopImageProcessorOptions options, BlockProperties settings, int blockPositionInPipeline,
			FlexibleImageStack debugStack);
	
	public void reset();
	
	public FlexibleMaskAndImageSet process();
	
	public BlockProperties getProperties();
	
	public int getBlockPosition();
}
