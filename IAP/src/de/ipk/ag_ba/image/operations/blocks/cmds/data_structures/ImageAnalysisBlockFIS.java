package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import java.util.TreeMap;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public interface ImageAnalysisBlockFIS {
	
	public void setInputAndOptions(FlexibleMaskAndImageSet input, ImageProcessorOptions options, BlockProperties settings, int blockPositionInPipeline,
			FlexibleImageStack debugStack);
	
	public void reset();
	
	public FlexibleMaskAndImageSet process() throws InterruptedException;
	
	public BlockProperties getProperties();
	
	public int getBlockPosition();
	
	public void postProcessResultsForAllAngles(Sample3D inSample,
			TreeMap<String, ImageData> inImages,
			TreeMap<String, BlockProperties> allResultsForSnapshot, BlockProperties summaryResult);
}
