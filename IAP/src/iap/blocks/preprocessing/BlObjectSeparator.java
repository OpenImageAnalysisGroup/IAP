package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Separate objects according to their size, up to the maximum object count,
 * and starting from the biggest to the smallest object.
 * Objects need to be larger than the specified minimum size.
 * 
 * @author klukas
 */
public class BlObjectSeparator extends AbstractBlock implements WellProcessor {
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Separate Objects (TODO, NOT WORKING)";
	}
	
	@Override
	public String getDescription() {
		return "Processes separated objects individually (from biggest to smallest, up to maximum object count, and larger than " +
				"the minimum size).";
	}
	
	@Override
	public int getDefinedWellCount(ImageProcessorOptions options) {
		return options.getIntSetting(this, "Maximum Object Count", 10);
	}
	
}
