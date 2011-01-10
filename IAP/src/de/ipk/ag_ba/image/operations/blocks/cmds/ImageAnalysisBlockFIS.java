package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.phytochamber.PhytoTopImageProcessorOptions;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public interface ImageAnalysisBlockFIS {
	
	public void setInputAndOptions(FlexibleMaskAndImageSet input, PhytoTopImageProcessorOptions options, FlexibleImageStack debugStack);
	
	public void reset();
	
	public FlexibleMaskAndImageSet process();
	
}
