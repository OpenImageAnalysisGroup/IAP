/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

/**
 * Clears the background by comparison of foreground and background.
 * Additionally the border around the masks is cleared (width 2 pixels).
 * 
 * @author pape, klukas
 */
public class BlockClearBackgroundByComparingNullImageAndImage extends AbstractSnapshotAnalysisBlockFIS {
	
	int back = PhenotypeAnalysisTask.BACKGROUND_COLORint;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			// double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			FlexibleImage visImg = getInput().getImages().getVis();
			FlexibleImage visMsk = getInput().getMasks().getVis();
			
			// visImg.getIO().subtractImages(visMsk).printImage("LAB difference", true);
			
			// vis = vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight()));
			// unsharpedMask(1.0f, 3.0).printImage("UNSHARPEN").
			FlexibleImage cleared = visImg.getIO().compare() // medianFilter32Bit().
					.compareImages(visMsk.getIO().blur(2).getImage(), //
							options.getIntSetting(Setting.L_Diff_VIS_SIDE) * 0.5,
							options.getIntSetting(Setting.L_Diff_VIS_SIDE) * 0.5,
							options.getIntSetting(Setting.abDiff_VIS_SIDE) * 0.5,
							back, true).border(2).getImage(); //
			return getInput().getImages().getVis().getIO().applyMask_ResizeMaskIfNeeded(cleared, options.getBackground())
					.printImage("CLEAR RESULT", false).getImage();
		}
		if (options.getCameraPosition() == CameraPosition.TOP) {
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			FlexibleImage visX = getInput().getImages().getVis().copy();
			visX = visX.resize((int) (scaleFactor * visX.getWidth()), (int) (scaleFactor * visX.getHeight()));
			FlexibleImage cleared = new ImageOperation(visX)
					// .blur(3).printImage("median", false)
					.compare()
					.compareImages(getInput().getMasks().getVis().getIO().blur(3).printImage("medianb", false).getImage(),
							options.getIntSetting(Setting.L_Diff_VIS) * 0.5d,
							options.getIntSetting(Setting.L_Diff_VIS) * 0.5d,
							options.getIntSetting(Setting.abDiff_VIS) * 0.5d,
							back, false)
							// .dilate().dilate().dilate()
					.border(2).getImage();
			return getInput().getImages().getVis().getIO().applyMask_ResizeMaskIfNeeded(cleared, options.getBackground())
					.printImage("CLEAR RESULT", false).getImage();
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			FlexibleImage fluo = getInput().getImages().getFluo();
			fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
			return new ImageOperation(fluo.getIO().medianFilter32Bit().getImage()).compare()
					.compareImages(getInput().getMasks().getFluo().getIO().medianFilter32Bit().getImage(),
							options.getIntSetting(Setting.L_Diff_FLOU) * 0.5d,
							options.getIntSetting(Setting.L_Diff_FLOU) * 0.5d,
							options.getIntSetting(Setting.abDiff_FLOU) * 0.5d,
							back).border(2).getImage();
		}
		if (options.getCameraPosition() == CameraPosition.TOP) {
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			FlexibleImage fluo = getInput().getImages().getFluo();
			fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
			return new ImageOperation(fluo).compare()
					.compareImages(getInput().getMasks().getFluo().getIO().copyImagesParts(0.26, 0.3).printImage("cut out", false).getImage(),
							options.getIntSetting(Setting.L_Diff_FLOU) * 0.5d,
							options.getIntSetting(Setting.L_Diff_FLOU) * 0.5d,
							options.getIntSetting(Setting.abDiff_FLOU) * 0.5d,
							back).border(2).getImage();
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			return getInput().getMasks().getNir();
		}
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage nir = getInput().getImages().getNir();
			return new ImageOperation(nir).compare()
					.compareGrayImages(getInput().getMasks().getNir(), 25, 15, options.getBackground())
					.printImage("result nir", true).thresholdBlueHigherThan(200).border(2).getImage();
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
}
