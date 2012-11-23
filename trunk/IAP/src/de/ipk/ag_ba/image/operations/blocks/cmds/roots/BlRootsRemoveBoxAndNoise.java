package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Remove the dark border around the root box.
 * 
 * @author klukas, entzian
 */
public class BlRootsRemoveBoxAndNoise extends AbstractSnapshotAnalysisBlockFIS {
	// boolean debug = false;
	
	// int white = Color.WHITE.getRGB();
	// int black = Color.BLACK.getRGB();
	int blue = Color.BLUE.getRGB();
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().masks().vis();
		if (img != null) {
			// boolean printEachStep = debug;
			img = img.copy(); // .print("1", printEachStep);
			ImageOperation io = img.io().border(options.getIntSetting(Setting.ROOT_BORDER_WIDTH)); // .print("2", printEachStep);
			io = io.invert().thresholdBlueHigherThan(options.getIntSetting(Setting.ROOT_TRESHOLD_BLUE)); // .print("3", printEachStep);
			// remove pure white area inside the box
			io = io
					.erode()
					.removeSmallElements(options.getIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_AREA),
							options.getIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_DIM)); // .print("REMOVE WHITE AREA INSIDE THE BOX", debug).print("4",
																													// printEachStep);
			io = io.replaceColor(options.getBackground(), blue); // .print("5", printEachStep);
			io = io.threshold(10, options.getBackground(), blue); // .print("6", printEachStep);
			io = io.erode(options.getIntSetting(Setting.ROOT_NUMBER_OF_RUNS_ERODE)); // .print("7", printEachStep);
			io = io.erode().removeSmallElements(options.getIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_AREA),
					options.getIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_DIM)); // .print("REMOVE NOISE OUTSIDE THE BOX", debug).print("8", printEachStep);
			ImageOperation r = input().images().vis().io().applyMask(io.getImage(), options.getBackground()); // .print("FINAL", debug).print("9", printEachStep);
			r = r.adaptiveThresholdForGrayscaleImage(options.getIntSetting(Setting.ROOT_ADAPTIVE_TRESHOLD_SIZE_OF_REGION),
					options.getIntSetting(Setting.ROOT_ADAPTIVE_TRESHOLD_ASSUMED_BACKGROUND_COLOR),
					options.getIntSetting(Setting.ROOT_ADAPTIVE_TRESHOLD_NEW_FORGROUND_COLOR), options.getDoubleSetting(Setting.ROOT_ADAPTIVE_TRESHOLD_K)); // .print("ROOTS",
																																																			// debug).print("10",
																																																			// printEachStep);
			return r.removeSmallElements(options.getIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_AREA),
					options.getIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_DIM)).getImage(); // .print("11", printEachStep);
		}
		return null;
	}
	
}
