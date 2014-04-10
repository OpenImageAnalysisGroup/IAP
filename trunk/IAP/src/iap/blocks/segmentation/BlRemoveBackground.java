/**
 * 
 */
package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import ij.process.AutoThresholder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Clears the background by comparison of foreground and background.
 * Additionally the border around the masks is cleared (width 2 pixels).
 * 
 * @author pape, klukas
 */
public class BlRemoveBackground extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null)
			return null;
		boolean debug = debugValues;
		CameraType ct = mask.getCameraType();
		if (input().images().getImage(ct) != null) {
			ArrayList<String> possibleValues = new ArrayList<String>(Arrays.asList(AutoThresholder.getMethods()));
			String Value = optionsAndResults.getStringSettingRadio(this, "Thresholding Method (" + ct + ")", ct == CameraType.VIS ? "Li" : "Otsu", possibleValues);
			Image image = input().images().getImage(ct).show("inp", debug);
			if (getBoolean("Normalize " + ct + " Image", ct == CameraType.FLUO))
				image = image.io().copy().histogramEqualisation(true, 0.35).getImage().show("img_he", debug);
			// mask = mask.io().histogramEqualisation(true, 0.35).getImage().show("mask_he", debug);
			if (mask == null || image == null)
				return null;
			Image diff_image = mask.io().diff(image).getImage();
			if (debug)
				diff_image.show("diff", debug);
			if (diff_image == null)
				return null;
			Image thresh_image = diff_image.io().thresholdImageJ(Value, false).replaceColor(Color.BLACK.getRGB(), ImageOperation.BACKGROUND_COLORint).getImage()
					.show("thresh", debug);
			Image res = input().images().copy().getImage(ct).io()
					.applyMask(thresh_image)
					.getImage();
			return res;
		} else
			return null;
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
		return "Compare Images and Reference Images";
	}
	
	@Override
	public String getDescription() {
		return " Clears the background by comparison of foreground and background.";
	}
}
