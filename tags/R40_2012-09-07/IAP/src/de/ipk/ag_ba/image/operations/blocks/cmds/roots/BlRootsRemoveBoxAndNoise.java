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
			boolean printEachStep = true;
			img = img.copy().print("1", printEachStep);
			ImageOperation io = img.io().border(2).print("2", printEachStep);
			io = io.invert().thresholdBlueHigherThan(3).print("3", printEachStep);
			// remove pure white area inside the box
			io = io.erode().removeSmallElements(50, 50).print("REMOVE WHITE AREA INSIDE THE BOX", debug).print("4", printEachStep);
			io = io.replaceColor(options.getBackground(), blue).print("5", printEachStep);
			io = io.threshold(10, options.getBackground(), blue).print("6", printEachStep);
			io = io.erode(60).print("7", printEachStep);
			io = io.erode().removeSmallElements(800 * 800, 800).print("REMOVE NOISE OUTSIDE THE BOX", debug).print("8", printEachStep);
			ImageOperation r = input().images().vis().io().applyMask(io.getImage(), options.getBackground()).print("FINAL", debug).print("9", printEachStep);
			r = r.adaptiveThresholdForGrayscaleImage(5, black, white, 0.02).print("ROOTS", debug).print("10", printEachStep);
			return r.removeSmallElements(10, 10).getImage().print("11", printEachStep);
		}
		return null;
	}
	
}
