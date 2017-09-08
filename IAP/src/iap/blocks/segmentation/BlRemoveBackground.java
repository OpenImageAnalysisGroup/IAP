/**
 * 
 */
package iap.blocks.segmentation;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import ij.gui.Roi;
import ij.process.AutoThresholder;
import ij.process.AutoThresholder.Method;

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
			String Value = optionsAndResults.getStringSettingRadio(this, "Thresholding Method (" + ct + ")",
					ct == CameraType.VIS ? "Li" : "Otsu", possibleValues);// ct == CameraType.VIS ? "Li" : "Otsu", possibleValues);
			Image image = input().images().getImage(ct).show("inp", debug);
			boolean normalize = getBoolean("Normalize " + ct + " Image", ct == CameraType.FLUO);
			boolean histogramEqualisation = getBoolean("Equalize histogram", normalize);
			double percentSaturated = getDouble("Normalize (equalisation) - percentage of saturated pixels", 35) / 100d;
			if (histogramEqualisation && normalize)
				image = image.io().copy().histogramEqualisation(normalize, percentSaturated).getImage().show("img_he", debug);
			// mask = mask.io().blurImageJ(getDouble("Blur mask percent", 1.0) / 100d * mask.getWidth()).getImage();
			// mask = mask.io().histogramEqualisation(true, 0.35).getImage().show("mask_he", debug);
			if (mask == null || image == null)
				return null;
				
			// Nir: invert mask then add to image
			// Fluo: diff = image - mask
			Image diff_image;
			if (getBoolean("Use Lab Diff Calculation " + ct + " Image", ct == CameraType.VIS))
				diff_image = mask.io().calc().colorDifferenceDeltaE2000(image,
						getDouble("Use Lab Diff Calculation " + ct + " Image (L importance)", 1),
						getDouble("Use Lab Diff Calculation " + ct + " Image (a importance)", 1),
						getDouble("Use Lab Diff Calculation " + ct + " Image (b importance)", 1));
			else
				if (ct == CameraType.NIR)
					diff_image = mask.io().invert().add(image).getImage();
				else
					diff_image = mask.io().diff(image).getImage();
				
			if (debug)
				diff_image.show("diff", debug);
			if (diff_image == null)
				return null;
			
			boolean tune = false;
			Method[] methods = Method.values();
			
			if (tune) {
				int idx = (int) optionsAndResults.getUnitTestIdx();
				Value = methods[idx].name();
			}
			
			diff_image = diff_image.io().convertFP2RGB().getImage();
			Roi bb = diff_image.io().getBoundingBox();
			Rectangle br = bb.getBounds();
			diff_image = diff_image.io().crop(bb).getImage();
			
			Image thresh_image = diff_image.io().thresholdImageJ(Value, ct == CameraType.NIR)
					.replaceColor(Color.BLACK.getRGB(), ImageOperation.BACKGROUND_COLORint).getImage()
					.show("thresh", debug);
			
			int borderSizeLeftRight = (image.getWidth() - br.width) / 2;
			int borderSizeTopBottom = (image.getHeight() - br.height) / 2;
			
			thresh_image = thresh_image.io()
					.addBorder(borderSizeLeftRight, borderSizeTopBottom, (int) br.getMinX() - borderSizeLeftRight, (int) br.getMinY() - borderSizeTopBottom,
							ImageOperation.BACKGROUND_COLORint)
					.getImage();
			
			Image workimg = input().images().copy().getImage(ct);
			if (getBoolean("Return difference image (" + ct + ")", ct == CameraType.NIR)) {
				workimg = diff_image.io()
						.addBorder(borderSizeLeftRight, borderSizeTopBottom, (int) br.getMinX() - borderSizeLeftRight, (int) br.getMinY() - borderSizeTopBottom,
								ImageOperation.BACKGROUND_COLORint)
						.getImage();
			}
			Image res = workimg.io()
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
