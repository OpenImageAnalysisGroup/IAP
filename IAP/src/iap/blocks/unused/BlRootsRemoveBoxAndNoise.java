package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Detect root pixels by removing the larger root plastic box border, background and noise from the image.
 * 
 * @author klukas
 */
public class BlRootsRemoveBoxAndNoise extends AbstractSnapshotAnalysisBlockFIS {
	
	int white = Color.WHITE.getRGB();
	int black = Color.BLACK.getRGB();
	int blue = Color.BLUE.getRGB();
	
	@Override
	protected Image processVISmask() {
		boolean debug = getBoolean("debug", false);
		Image img = input().masks().vis();
		if (img == null)
			return null;
		
		ImageOperation io = img.copy().io().invert().thresholdBlueHigherThan(getInt("minimum blue intensity level", 3))
				.show("result of blue threshold", debug);
		// remove pure white area inside the box
		io = io.erode()
				.removeSmallElements(
						getInt("Remove-Pure-White_Noise-Size-Area", 50),
						getInt("Remove-Pure-White_Noise-Size-Dimension", 50))
				.show("REMOVED WHITE AREA INSIDE THE BOX", debug);
		io = io.replaceColor(options.getBackground(), blue);
		io = io.threshold(getInt("minimum brightness", 10), options.getBackground(), blue)
				.show("minimum brightness filtered", debug);
		io = io.erode(getInt("Reduce-area-ignore-border of box_erode-cnt", 60))
				.show("reduced area to ignore box border", debug);
		io = io.erode().removeSmallElements(
				getInt("Remove-All-Outside-Box_Noise-Size-Area", 800 * 800),
				getInt("Remove-All-Outside-Box_Noise-Size-Dimension", 800))
				.show("REMOVED NOISE OUTSIDE THE BOX ", debug);
		ImageOperation r = input().images().vis().io().applyMask(io.getImage(), options.getBackground())
				.show("input for adaptive thresholding for edge detection", debug);
		r = r.adaptiveThresholdForGrayscaleImage(
				getInt("Adaptive_Thresholding_Region_Size", 5),
				black, white,
				getDouble("Adaptive_Thresholding_K", 0.02))
				.show("result of adaptive thresholding", debug);
		return r.removeSmallElements(
				getInt("minimum root area (noise reduction)", 10),
				getInt("minimum root dimension (noise reduction)", 10)).getImage()
				.show("result image", debug);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
}
