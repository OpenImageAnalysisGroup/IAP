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
			
			double[][] iLabValues = input().images().getImage(mask.getCameraType()).io().channels().getLabArrays();
			double[][] mLabValues = mask.io().channels().getLabArrays();
			
			String deltaE = "Delta-E";
			String imgMmask = "Image-Mask";
			String maskMimg = "Mask-Image";
			String absMaskMimg = "Abs(Mask-Image)";
			String invert = "Invert";
			
			String operation = getStringRadioSelection("Operation", deltaE, org.StringManipulationTools.getStringListFromArray(
					new String[] { deltaE, imgMmask, maskMimg, absMaskMimg }));
			
			int[] img = mask.getAs1A();
			double[] ilChannel = iLabValues[0];
			double[] iaChannel = iLabValues[1];
			double[] ibChannel = iLabValues[2];
			
			double[] mlChannel = mLabValues[0];
			double[] maChannel = mLabValues[1];
			double[] mbChannel = mLabValues[2];
			
			if (operation.equals(deltaE))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						mlChannel[i] = ColorUtil.deltaE2000(ilChannel[i], iaChannel[i], ibChannel[i], mlChannel[i], maChannel[i], mbChannel[i]);
						maChannel[i] = 0;
						mbChannel[i] = 0;
					}
				}
			
			if (operation.equals(imgMmask))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						mlChannel[i] = ilChannel[i] - mlChannel[i];
						maChannel[i] = iaChannel[i] - maChannel[i];
						mbChannel[i] = ibChannel[i] - mbChannel[i];
					}
				}
			
			if (operation.equals(maskMimg))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						mlChannel[i] = mlChannel[i] - ilChannel[i];
						maChannel[i] = maChannel[i] - iaChannel[i];
						mbChannel[i] = mbChannel[i] - ibChannel[i];
					}
				}
			
			if (operation.equals(absMaskMimg))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						mlChannel[i] = Math.abs(mlChannel[i] - ilChannel[i]);
						maChannel[i] = Math.abs(maChannel[i] - iaChannel[i]);
						mbChannel[i] = Math.abs(mbChannel[i] - ibChannel[i]);
					}
				}
			
			if (operation.equals(invert))
				for (int i = 0; i < w * h; i++) {
					if (img[i] != ImageOperation.BACKGROUND_COLORint) {
						mlChannel[i] = 127 - mlChannel[i];
						maChannel[i] = -maChannel[i];
						mbChannel[i] = -mbChannel[i];
					}
				}
			
			Image res = new Image(w, h, mLabValues[0], mLabValues[1], mLabValues[2], ColorSpace.LAB_UNSHIFTED);
			int[] resI = res.getAs1A();
			for (int i = 0; i < w * h; i++) {
				if (img[i] == ImageOperation.BACKGROUND_COLORint) {
					resI[i] = ImageOperation.BACKGROUND_COLORint;
				}
			}
			return new Image(w, h, resI);
		} else
			return mask;
	}
}
