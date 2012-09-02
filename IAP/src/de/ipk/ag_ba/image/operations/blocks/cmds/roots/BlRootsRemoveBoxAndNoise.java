package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Remove the dark border around the root box.
 * 
 * @author klukas
 */
public class BlRootsRemoveBoxAndNoise extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	int white = Color.WHITE.getRGB();
	int black = Color.BLACK.getRGB();
	int blue = Color.BLUE.getRGB();
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().masks().vis();
		if (img != null) {
			img = img.copy();
			ImageOperation io = img.io().border(2);
			io = io.invert().thresholdBlueHigherThan(3);
			// remove pure white area inside the box
			io = io.erode().removeSmallElements(50, 50).print("REMOVE WHITE AREA INSIDE THE BOX", debug);
			io = io.replaceColor(options.getBackground(), blue);
			io = io.threshold(10, options.getBackground(), blue);
			io = io.erode(50);
			io = io.erode().removeSmallElements(800 * 800, 800).print("REMOVE NOISE OUTSIDE THE BOX", debug);
			ImageOperation r = input().images().vis().io().applyMask(io.getImage(), options.getBackground()).print("FINAL", debug);
			r = r.adaptiveThresholdForGrayscaleImage(5, black, white, 0.02).print("ROOTS", false);
			return r.removeSmallElements(10, 10).getImage();
		}
		return null;
	}
	
}
