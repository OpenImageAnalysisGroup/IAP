package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Clear a rectangle inside of the image (only masks) from top, bottom, left and right sides.
 * 
 * @author Christian Klukas
 */
public class BlClearRectangle extends AbstractBlock {
	
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
		
		int background = options.getBackground();
		
		if (getBoolean("Use gray NIR background", false) && mask.getCameraType() == CameraType.NIR) {
			int gray = new Color(180, 180, 180).getRGB();
			background = gray;
		}
		
		if (getBoolean("debug", false)) {
			background = Color.BLUE.getRGB();
		}
		ImageOperation result = mask.io();
		
		double cutoffLeft = getDouble("Cut-off " + mask.getCameraType() + " from left (percent)", 0) / 100d;
		double cutoffRight = getDouble("Cut-off " + mask.getCameraType() + " from right (percent)", 0) / 100d;
		double cutoffTop = getDouble("Cut-off " + mask.getCameraType() + " from top (percent)", 0) / 100d;
		double cutoffBottom = getDouble("Cut-off " + mask.getCameraType() + " from bottom (percent)", 0) / 100d;
		
		int x = (int) (cutoffLeft * mask.getWidth());
		int y = (int) (cutoffTop * mask.getHeight());
		int x2 = mask.getWidth() - (int) (cutoffRight * mask.getWidth());
		int y2 = mask.getHeight() - (int) (cutoffBottom * mask.getHeight());
		
		result = result.canvas().fillRect(x, y, x2 - x, y2 - y, background).io();
		
		return result.getImage();
	}
	
	@Override
	protected Image processImage(Image image) {
		boolean dontCut = getBoolean("Process Only Mask Images", false);
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
