package iap.blocks.roots;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Remove the dark border around the root box.
 * 
 * @author klukas, entzian
 */
public class BlRootsRemoveBoxAndNoise extends AbstractSnapshotAnalysisBlockFIS {
	
	int white = Color.WHITE.getRGB();
	int black = Color.BLACK.getRGB();
	int blue = Color.BLUE.getRGB();
	
	@Override
	protected FlexibleImage processVISmask() {
		boolean debug = getBoolean("debug", false);
		FlexibleImage img = input().masks().vis();
		if (img == null)
			return null;
		
		img = img.copy().show("1", debug);
		ImageOperation io = img.io().border(getInt("BORDER_WIDTH", 2)).show("2", debug);
		io = io.invert().thresholdBlueHigherThan(getInt("TRESHOLD_BLUE", 3)).show("3", debug);
		// remove pure white area inside the box
		io = io
				.erode()
				.removeSmallElements(
						getInt("Remove-Pure-White_Noise-Size-Area", 50),
						getInt("Remove-Pure-White_Noise-Size-Dimension", 50))
				.show("REMOVED WHITE AREA INSIDE THE BOX - 4", debug);
		io = io.replaceColor(options.getBackground(), blue).show("5", debug);
		io = io.threshold(getInt("Background-Threshold", 10), options.getBackground(), blue).show("6", debug);
		io = io.erode(getInt("Reduce-Area-Ignore-Border_erode-cnt", 60)).show("7", debug);
		io = io.erode().removeSmallElements(
				getInt("Remove-All-Outside-Box_Noise-Size-Area", 800 * 800),
				getInt("Remove-All-Outside-Box_Noise-Size-Dimension", 800))
				.show("REMOVED NOISE OUTSIDE THE BOX - 8", debug);
		ImageOperation r = input().images().vis().io().applyMask(io.getImage(), options.getBackground()).show("FINAL - 9", debug);
		r = r.adaptiveThresholdForGrayscaleImage(
				getInt("Adaptive_Threshold_Region_Size", 5),
				black, white,
				getDouble("Adaptive_Threshold_K", 0.02)).show("10", debug);
		return r.removeSmallElements(
				getInt("Final_Noise-Size-Area", 10),
				getInt("Final_Noise-Size-Dimension", 10))
				.getImage().show("11", debug);
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
}
