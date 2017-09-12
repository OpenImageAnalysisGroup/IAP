package iap.blocks.preprocessing;

import java.util.HashSet;

import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ColorSpace;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

/**
 * @author klukas
 */
public class BlCalculator extends AbstractBlock {
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		for (CameraType ct : CameraType.values())
			res.add(ct);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Mathematical operation";
	}
	
	@Override
	public String getDescription() {
		return "Perform a mathmeatical operation on image and reference image";
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask != null && getBoolean("Process " + mask.getCameraType(), false)) {
			int w = mask.getWidth();
			int h = mask.getHeight();
			
			float[][] iLabValues = input().images().getImage(mask.getCameraType()).io().channels().getLabArrays();
			float[][] mLabValues = mask.io().channels().getLabArrays();
			
			String deltaE = "Delta-E";
			String imgMmask = "Image-Mask";
			String maskMimg = "Mask-Image";
			String absMaskMimg = "Abs(Mask-Image)";
			String invert = "Invert";
			
			String operation = getStringRadioSelection("Operation", deltaE, org.StringManipulationTools.getStringListFromArray(
					new String[] { deltaE, imgMmask, maskMimg, absMaskMimg }));
			
			int[] img = mask.getAs1A();
			
			double[] rLchannel = new double[img.length];
			double[] rAchannel = new double[img.length];
			double[] rBchannel = new double[img.length];
			
			if (operation.equals(deltaE))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						rLchannel[i] = ColorUtil.deltaE2000(iLabValues[0][i], iLabValues[1][i], iLabValues[2][i],
								mLabValues[0][i], mLabValues[1][i], mLabValues[2][i]);
						rAchannel[i] = 0;
						rBchannel[i] = 0;
					}
				}
			
			if (operation.equals(imgMmask))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						rLchannel[i] = iLabValues[0][i] - mLabValues[0][i];
						rAchannel[i] = iLabValues[1][i] - mLabValues[1][i];
						rBchannel[i] = iLabValues[2][i] - mLabValues[2][i];
					}
				}
			
			if (operation.equals(maskMimg))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						rLchannel[i] = mLabValues[0][i] - iLabValues[0][i];
						rAchannel[i] = mLabValues[1][i] - iLabValues[1][i];
						rBchannel[i] = mLabValues[2][i] - iLabValues[2][i];
					}
				}
			
			if (operation.equals(absMaskMimg))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						rLchannel[i] = Math.abs(iLabValues[0][i] - mLabValues[0][i]);
						rAchannel[i] = Math.abs(iLabValues[1][i] - mLabValues[1][i]);
						rBchannel[i] = Math.abs(iLabValues[2][i] - mLabValues[2][i]);
					}
				}
			
			if (operation.equals(invert))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						rLchannel[i] = 100 - mLabValues[0][i];
						rAchannel[i] = -mLabValues[1][i];
						rBchannel[i] = -mLabValues[2][i];
					}
				}
			
			return new Image(w, h, rLchannel, rAchannel, rBchannel, ColorSpace.LAB_UNSHIFTED, img);
		} else
			return mask;
	}
}
