/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Clears the background by comparison of foreground and background.
 * Additionally the border around the masks is cleared (width 2 pixels).
 * 
 * @author pape, klukas
 */
public class BlockClearBackgroundByRefComparison_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	int back = ImageOperation.BACKGROUND_COLORint;
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getImages().getVis() != null && getInput().getMasks().getVis() == null) {
			// no reference image create dummy image
			FlexibleImage in = getInput().getImages().getVis();
			FlexibleImage simulatedGreen = in.getIO().copy().filterByHSV(0.1, Color.GREEN.getRGB()).
					print("simulated background green", debug).getImage();
			FlexibleImage simulatedGreen2 = in.getIO().copy().filterByHSV(0.1, new Color(94, 118, 50).getRGB()).
					print("simulated background green 2", debug).getImage();
			FlexibleImage simulatedBlue = in.getIO().copy().print("mist", debug).filterByHSV(0.1, new Color(20, 36, 76).getRGB()).
					print("simulated background blue", debug).getImage();
			FlexibleImage simBlueGreen = simulatedBlue.getIO().or(simulatedGreen).or(simulatedGreen2).print("simulated green and blue", debug).getImage();
			getInput().getMasks().setVis(in.getIO().xor(simBlueGreen).print("sim xor", debug).getImage());
		}
		
		if (getInput().getImages().getVis() != null && getInput().getMasks().getVis() != null) {
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				FlexibleImage visImg = getInput().getImages().getVis().print("In VIS", false);
				FlexibleImage visMsk = getInput().getMasks().getVis().print("In Mask", false);
				FlexibleImage cleared = visImg.getIO().compare() // medianFilter32Bit().
						.compareImages("vis", visMsk.getIO().blur(2).print("Blurred Mask", false).getImage(),
								options.getIntSetting(Setting.L_Diff_VIS_SIDE) * 0.5,
								options.getIntSetting(Setting.L_Diff_VIS_SIDE) * 0.5,
								options.getIntSetting(Setting.abDiff_VIS_SIDE) * 0.5,
								back, true).border(2).getImage(); //
				return getInput().getImages().getVis().getIO().applyMask_ResizeMaskIfNeeded(cleared, options.getBackground())
						.print("CLEAR RESULT", false).getImage();
			}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
				FlexibleImage visX = getInput().getImages().getVis().copy();
				visX = visX.resize((int) (scaleFactor * visX.getWidth()), (int) (scaleFactor * visX.getHeight()));
				FlexibleImage cleared = new ImageOperation(visX)
						// .blur(3).printImage("median", false)
						.compare()
						.compareImages("vis", getInput().getMasks().getVis().getIO().blur(3).print("medianb", false).getImage(),
								options.getIntSetting(Setting.L_Diff_VIS_TOP) * 0.5d,
								options.getIntSetting(Setting.L_Diff_VIS_TOP) * 0.5d,
								options.getIntSetting(Setting.abDiff_VIS_TOP) * 0.5d,
								back, false)
						// .dilate().dilate().dilate()
						.border(2).getImage();
				return getInput().getImages().getVis().getIO().applyMask_ResizeMaskIfNeeded(cleared, options.getBackground())
						.print("CLEAR RESULT", false).getImage();
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getImages().getFluo() != null && getInput().getMasks().getFluo() != null) {
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
				FlexibleImage fluo = getInput().getImages().getFluo();
				fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
				FlexibleImage result = new ImageOperation(fluo.getIO()
						// .blur(1.5).print("Blurred 1.5 fluo image", true)
						.medianFilter32Bit()
						.getImage()).compare()
						.compareImages("fluo", getInput().getMasks().getFluo().getIO()
								.blur(1.1).print("Blurred 1.5 fluo mask", false)
								.medianFilter32Bit()
								.getImage(),
								options.getIntSetting(Setting.L_Diff_FLUO) * 0.5d,
								options.getIntSetting(Setting.L_Diff_FLUO) * 0.5d,
								options.getIntSetting(Setting.abDiff_FLUO) * 0.5d,
								back).border(2).getImage();
				
				FlexibleImage toBeFiltered = result.getIO().hq_thresholdLAB_multi_color_or(
						// black background and green pot (fluo of white pot)
						new int[] { -1, 200 - 40 }, new int[] { 115, 200 + 20 },
						new int[] { 90 - 5, 104 - 15 }, new int[] { 150 + 5, 104 + 15 },
						new int[] { 116 - 5, 206 - 20 }, new int[] { 175 + 5, 206 + 20 },
						options.getBackground(), false).
						print("removed noise", debug).getImage();
				
				result = result.copy().getIO().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage();
				
				if (debug)
					result.copy().getIO().replaceColors(options.getBackground(), Color.YELLOW.getRGB()).print("Left-Over");
				
				return result;
			}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
				FlexibleImage fluo = getInput().getImages().getFluo();
				fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
				return new ImageOperation(fluo).compare()
						.compareImages("fluo", getInput().getMasks().getFluo()
								// .getIO().
								// copyImagesParts(0.26, 0.3).print("cut out", true).getImage()
								,
								options.getIntSetting(Setting.L_Diff_FLUO) * 2d,
								options.getIntSetting(Setting.L_Diff_FLUO) * 2d,
								options.getIntSetting(Setting.abDiff_FLUO) * 2d,
								back).border(2).getImage();
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getImages().getNir() != null && getInput().getMasks().getNir() == null) {
			// create simulated nir background
			int w = getInput().getImages().getNir().getWidth();
			int h = getInput().getImages().getNir().getHeight();
			getInput().getMasks().setNir(ImageOperation.createColoredImage(w, h, new Color(180, 180, 180)));
		}
		if (getInput().getImages().getNir() != null && getInput().getMasks().getNir() != null) {
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				return getInput().getMasks().getNir();
			}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				FlexibleImage nir = getInput().getImages().getNir();
				if (options.isMaize()) {
					int blackDiff = options.getIntSetting(Setting.B_Diff_NIR_TOP);
					int whiteDiff = options.getIntSetting(Setting.W_Diff_NIR_TOP);
					return new ImageOperation(nir).compare()
							.compareGrayImages(getInput().getMasks().getNir(), blackDiff, whiteDiff, options.getBackground())
							.print("result nir", debug).thresholdClearBlueBetween(150, 169).thresholdBlueHigherThan(240).border(2).getImage(); // 150 169 240
				} else {
					if (options.isHighResMaize())
						return new ImageOperation(nir).compare()
								.compareGrayImages(getInput().getMasks().getNir(), 10, 23, options.getBackground())
								.print("result nir", debug).thresholdBlueHigherThan(240).border(2).getImage();
					else
						return new ImageOperation(nir).compare()
								.compareGrayImages(getInput().getMasks().getNir(), 10, 23, options.getBackground())
								.print("result nir", debug).thresholdBlueHigherThan(240).border(2).getImage();
					
				}
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
}
