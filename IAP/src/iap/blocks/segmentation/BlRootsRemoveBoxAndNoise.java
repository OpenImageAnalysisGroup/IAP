package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Detect root pixels by removing the larger root plastic box border, background and noise from the image.
 * 
 * @author klukas
 */
public class BlRootsRemoveBoxAndNoise extends AbstractSnapshotAnalysisBlock {
	
	int white = Color.WHITE.getRGB();
	int black = Color.BLACK.getRGB();
	int blue = Color.BLUE.getRGB();
	
	@Override
	protected Image processVISmask() {
		boolean debug = getBoolean("debug", false);
		Image img = input().masks().vis();
		if (img == null)
			return null;
		
		ImageOperation io = img.copy().io().invert().thresholdBlueHigherThan(getInt("minimum blue intensity level", 6))
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
		ImageOperation removeBlack = img.copy().io().blur(20).thresholdGrayClearLowerThan(150).show("large black area", debug);
		io = io.erode(getInt("Reduce-area-ignore-border of box_erode-cnt", 70))
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
		
		r.removeSmallElements(
				getInt("minimum root area (noise reduction)", 20),
				getInt("minimum root dimension (noise reduction)", 20)).getImage()
				.show("removed small elements", debug);
		
		r = r.applyMask(removeBlack.getImage()).show("remove large black (final image)", debug);
		
		// ImageOperation detectThickElements = r.copy()
		// .erode(4).show("thick 1 erode")
		// // .dilate(5).show("thick 1 dilate")
		// .dilate(15).show("thick 2 final area", debug);
		// r = r.applyMaskInversed_ResizeMaskIfNeeded(detectThickElements.getImage()).show("REMOVED THICK ELEMENTS (result image)", debug);
		
		return r.getImage();
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
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
}
