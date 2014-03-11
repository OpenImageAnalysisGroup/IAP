package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Analysis block for morphological image operations. 3 Steps are performed, first erode, then dilate, and then erode again. Each step can be performed multiple
 * times, according
 * to user preference. This way this block can perform the Opening or Closing operation, depending on its settings, or only Erode or only Dilate.
 * 
 * @author Christian Klukas
 */
public class BlMorphologicalOperations extends AbstractBlock {
	
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
	protected Image processMask(Image mask) {
		if (mask != null && getBoolean("Process " + mask.getCameraType(), mask.getCameraType() == CameraType.VIS)) {
			ImageOperation binaryMask = mask.io();
			binaryMask = binaryMask.bm().erode(getRoundMask(getInt(mask.getCameraType() + " Step 1 Erode Count", 0)))
					.dilate(getRoundMask(getInt(mask.getCameraType() + " Step 2 Dilate Count", 10)))
					.erode(getRoundMask(getInt(mask.getCameraType() + " Step 3 Erode Count", 10))).io();
			Image orig = input().images().vis();
			Image img = orig.io().applyMask(binaryMask.getImage(), ImageOperation.BACKGROUND_COLORint).getImage();
			return img;
		} else
			return mask;
	}
	
	public static int[][] getRoundMask(int size) {
		int[][] kernel = new int[size][size];
		if (size == 0)
			return kernel;
		double m = size / 2d;
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++) {
				double cX = x - m + 0.5;
				double cY = y - m + 0.5;
				double d = Math.sqrt(cX * cX + cY * cY);
				boolean inside = d <= m - 0.25;
				int insideCircle = inside ? 1 : 0xffffffff;
				kernel[x][y] = insideCircle;
			}
		return kernel;
	}
	
	@Override
	public String getName() {
		return "Morphological Operations";
	}
	
	@Override
	public String getDescription() {
		return "Analysis block for morphological image operations. 3 Steps are performed, first erode, then dilate, " +
				"and then erode again. " +
				"Each step can be performed multiple times, according to user preference. " +
				"This way this block can perform the Opening or Closing operation, depending on its settings, " +
				"or only Erode or only Dilate.";
	}
}
