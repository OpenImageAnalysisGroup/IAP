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
public class BlRootsRemoveDarkBoxBorder extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().masks().vis();
		if (img != null) {
			img = img.copy();
			int white = Color.WHITE.getRGB();
			int black = Color.BLACK.getRGB();
			int blue = Color.BLUE.getRGB();
			int background = options.getBackground();
			ImageOperation io = img.io().invert().thresholdBlueHigherThan(3);
			// remove pure white area inside the box
			io = io.erode().removeSmallElements(50, 50).print("REMOVE WHITE AREA INSIDE THE BOX", false);
			io = io.replaceColor(options.getBackground(), blue);
			io = io.threshold(10, options.getBackground(), blue);
			io = io.erode().removeSmallElements(800 * 800, 800).print("REMOVE NOISE OUTSIDE THE BOX", false);
			io.erode(55).print("BOX AREA", false);
			ImageOperation r = input().images().vis().io().applyMask(io.getImage(), options.getBackground()).print("FINAL", false);
			r = r.adaptiveThresholdForGrayscaleImage(5, black, white, 0.02).print("ROOTS", false);
			r.removeSmallElements(10, 10).skeletonize().binary(black, background).print("SKELET");
		}
		return super.processVISmask();
	}
	
}
