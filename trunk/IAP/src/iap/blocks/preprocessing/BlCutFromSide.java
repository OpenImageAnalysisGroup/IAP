package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageSide;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Clear the image (only masks) from top, bottom, left and right sides.
 * 
 * @author Dijun Chen, Christian Klukas
 */
public class BlCutFromSide extends AbstractBlock {
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> inputTypes = new HashSet<CameraType>();
		for (CameraType ft : CameraType.values()) {
			inputTypes.add(ft);
		}
		return inputTypes;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> outputTypes = new HashSet<CameraType>();
		for (CameraType ft : CameraType.values()) {
			outputTypes.add(ft);
		}
		return outputTypes;
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null) {
			return null;
		}
		
		boolean doCut = getBoolean("Cut " + mask.getCameraType(), false);
		
		if (!doCut)
			return mask;
		
		double cutoffLeft = getDouble("Cut-off " + mask.getCameraType() + " from left (percent)", 0) / 100d;
		double cutoffRight = getDouble("Cut-off " + mask.getCameraType() + " from right (percent)", 0) / 100d;
		double cutoffTop = getDouble("Cut-off " + mask.getCameraType() + " from top (percent)", 0) / 100d;
		double cutoffBottom = getDouble("Cut-off " + mask.getCameraType() + " from bottom (percent)", 0) / 100d;
		
		int background = options.getBackground();
		
		if (mask.getCameraType() == CameraType.NIR) {
			int gray = new Color(180, 180, 180).getRGB();
			background = gray;
		}
		
		if (getBoolean("debug", false)) {
			background = Color.BLUE.getRGB();
		}
		Image result = mask.io()
				.clearImage(ImageSide.Left, cutoffLeft, background)
				.clearImage(ImageSide.Right, cutoffRight, background)
				.clearImage(ImageSide.Top, cutoffTop, background)
				.clearImage(ImageSide.Bottom, cutoffBottom, background)
				.getImage();
		return result;
	}
	
	@Override
	protected Image processImage(Image image) {
		boolean dontCut = getBoolean("Cut Only Mask Images", false);
		if (dontCut)
			return super.processImage(image);
		else
			return processMask(image);
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
}
