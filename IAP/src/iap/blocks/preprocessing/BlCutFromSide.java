package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.ImageSide;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
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
		
		boolean doCutMarker = getBoolean("Crop (Marker-based)", true);
		
		boolean doCutFixed = getBoolean("Cut " + mask.getCameraType(), false);
		
		if (!doCutMarker && !doCutFixed)
			return mask;
		
		int background = options.getBackground();
		
		if (getBoolean("Use gray NIR background", false) && mask.getCameraType() == CameraType.NIR) {
			int gray = new Color(180, 180, 180).getRGB();
			background = gray;
		}
		
		if (getBoolean("debug", false)) {
			background = Color.BLUE.getRGB();
		}
		ImageOperation result = mask.io();
		
		boolean cropMarker = true;
		if (!cropMarker)
			if (doCutMarker) {
				Rectangle2D.Double r = ((BlockResults) getProperties()).getRelativeBlueMarkerRectangle(options);
				if (r != null) {
					double cutoffMarkerSide = getDouble("Marker cut left and right offset (percent)", 10) / 100d;
					double cutoffMarkerTop = getDouble("Marker cut top offset (percent)", 30) / 100d;
					double cutoffMarkerBottom = getDouble("Marker cut bottom offset (percent)", 0) / 100d;
					
					double cutLeft = r.x - cutoffMarkerSide;
					double cutRight = 1 - (r.x + r.width + cutoffMarkerSide);
					double cutTop = r.y - cutoffMarkerTop;
					double cutBottom = 1 - (r.y + r.height + cutoffMarkerBottom);
					
					result = result
							.clearImage(ImageSide.Left, cutLeft, background)
							.clearImage(ImageSide.Right, cutRight, background)
							.clearImage(ImageSide.Top, cutTop, background)
							.clearImage(ImageSide.Bottom, cutBottom, background);
				}
			}
		
		if (doCutFixed) {
			double cutoffLeft = getDouble("Cut-off " + mask.getCameraType() + " from left (percent)", 0) / 100d;
			double cutoffRight = getDouble("Cut-off " + mask.getCameraType() + " from right (percent)", 0) / 100d;
			double cutoffTop = getDouble("Cut-off " + mask.getCameraType() + " from top (percent)", 0) / 100d;
			double cutoffBottom = getDouble("Cut-off " + mask.getCameraType() + " from bottom (percent)", 0) / 100d;
			
			result = result
					.clearImage(ImageSide.Left, cutoffLeft, background)
					.clearImage(ImageSide.Right, cutoffRight, background)
					.clearImage(ImageSide.Top, cutoffTop, background)
					.clearImage(ImageSide.Bottom, cutoffBottom, background);
		}
		
		if (cropMarker)
			if (doCutMarker) {
				Rectangle2D.Double r = ((BlockResults) getProperties()).getRelativeBlueMarkerRectangle(options);
				
				if (r != null) {
					// crop according to markers
					double cutoffMarkerSide = getDouble("Marker cut left and right offset (percent)", 10) / 100d;
					double cutoffMarkerTop = getDouble("Marker cut top offset (percent)", 30) / 100d;
					double cutoffMarkerBottom = getDouble("Marker cut bottom offset (percent)", 0) / 100d;
					
					double cutLeft = r.x - cutoffMarkerSide;
					double cutRight = 1 - (r.x + r.width + cutoffMarkerSide);
					double cutTop = r.y - cutoffMarkerTop;
					double cutBottom = 1 - (r.y + r.height + cutoffMarkerBottom);
					
					cutLeft = Math.max(0, Math.min(1, cutLeft));
					cutRight = Math.max(0, Math.min(1, cutRight));
					cutTop = Math.max(0, Math.min(1, cutTop));
					cutBottom = Math.max(0, Math.min(1, cutBottom));
					
					result = result.crop(cutLeft, cutRight, cutTop, cutBottom);
				}
			}
		return result.getImage();
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
	
	@Override
	public String getName() {
		return "Crop Sides";
	}
	
	@Override
	public String getDescription() {
		return "Clear the image (only masks) from top, bottom, left and right sides.";
	}
}
