package iap.blocks;

import iap.blocks.data_structures.AbstractBlock;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageSide;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Dijun Chen
 *         Clear the image (only masks) from top, bottom, left and right sides.
 */
public class BlockCutFromSide extends AbstractBlock {
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> inputTypes = new HashSet<FlexibleImageType>();
		for (FlexibleImageType ft : FlexibleImageType.values()) {
			inputTypes.add(ft);
		}
		return inputTypes;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> outputTypes = new HashSet<FlexibleImageType>();
		for (FlexibleImageType ft : FlexibleImageType.values()) {
			outputTypes.add(ft);
		}
		return outputTypes;
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask == null) {
			return null;
		}
		
		boolean doCut = getBoolean("Cut " + mask.getType(), false);
		
		if (!doCut)
			return mask;
		
		double cutoffLeft = getDouble("Cut-off " + mask.getType() + " from left (percent)", 0) / 100d;
		double cutoffRight = getDouble("Cut-off " + mask.getType() + "from right (percent)", 0) / 100d;
		double cutoffTop = getDouble("Cut-off " + mask.getType() + " from top (percent)", 0) / 100d;
		double cutoffBottom = getDouble("Cut-off " + mask.getType() + " from bottom (percent)", 0) / 100d;
		
		int background = options.getBackground();
		
		if (mask.getType() == FlexibleImageType.NIR) {
			int gray = new Color(180, 180, 180).getRGB();
			background = gray;
		}
		
		if (getBoolean("debug", false)) {
			background = Color.BLUE.getRGB();
		}
		FlexibleImage result = mask.io()
				.clearImage(ImageSide.Left, cutoffLeft, background)
				.clearImage(ImageSide.Right, cutoffRight, background)
				.clearImage(ImageSide.Top, cutoffTop, background)
				.clearImage(ImageSide.Bottom, cutoffBottom, background)
				.getImage();
		return result;
	}
	
}
