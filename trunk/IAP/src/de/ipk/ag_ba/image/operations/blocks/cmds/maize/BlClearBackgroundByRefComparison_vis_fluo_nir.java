/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * Clears the background by comparison of foreground and background.
 * Additionally the border around the masks is cleared (width 2 pixels).
 * 
 * @author pape, klukas
 */
public class BlClearBackgroundByRefComparison_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	int back = ImageOperation.BACKGROUND_COLORint;
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		// getInput().getImages().getVis().copy().saveToFile(ReleaseInfo.getDesktopFolder() + File.separator + "MaizeVisMask2.png");
		if (input().images().vis() != null && input().masks().vis() == null) {
			// no reference image create dummy image
			FlexibleImage in = input().images().vis();
			FlexibleImage simulatedGreen = in.io().copy().filterByHSV(0.1, Color.GREEN.getRGB()).
					print("simulated background green", debug).getImage();
			FlexibleImage simulatedGreen2 = in.io().copy().filterByHSV(0.1, new Color(94, 118, 50).getRGB()).
					print("simulated background green 2", debug).getImage();
			FlexibleImage simulatedBlue = in.io().copy().print("mist", debug).filterByHSV(0.1, new Color(20, 36, 76).getRGB()).
					print("simulated background blue", debug).getImage();
			FlexibleImage simBlueGreen = simulatedBlue.io().or(simulatedGreen).or(simulatedGreen2).print("simulated green and blue", debug).getImage();
			input().masks().setVis(in.io().xor(simBlueGreen).print("sim xor", debug).getImage());
		}
		
		if (input().images().vis() != null && input().masks().vis() != null) {
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				FlexibleImage visImg = input().images().vis().print("In VIS", false);
				FlexibleImage visMsk = input().masks().vis().print("In Mask", false);
				FlexibleImage cleared = visImg.io().compare() // medianFilter32Bit().
						.compareImages("vis", visMsk.io().blur(2).print("Blurred Mask", false).getImage(),
								options.getIntSetting(Setting.L_Diff_VIS_SIDE),
								options.getIntSetting(Setting.L_Diff_VIS_SIDE),
								options.getIntSetting(Setting.abDiff_VIS_SIDE),
								back, true).
						// protect blue: (will be removed later)
						or(visMsk.copy().io().filterRemainHSV(0.02, 0.62).getImage()).
						border(2).getImage(); //
				return input().images().vis().io().applyMask_ResizeMaskIfNeeded(cleared, options.getBackground())
						.print("CLEAR RESULT", debug).getImage();
			}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				// double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
				FlexibleImage visX = input().images().vis().copy();
				// visX = visX.resize((int) (scaleFactor * visX.getWidth()), (int) (scaleFactor * visX.getHeight()));
				FlexibleImage cleared = new ImageOperation(visX).blur(2)
						// .blur(3).printImage("median", false)
						.compare()
						.compareImages("vis", input().masks().vis().io().blur(2).print("medianb", debug).getImage(),
								options.getIntSetting(Setting.L_Diff_VIS_TOP),
								options.getIntSetting(Setting.L_Diff_VIS_TOP),
								options.getIntSetting(Setting.abDiff_VIS_TOP),
								back, debug).print("comparison result", debug).
						// protect blue: (will be removed later)
						or(visX.copy().io().filterRemainHSV(0.02, 0.62).getImage()).
						// .dilate().dilate().dilate()
						border(1).
						getImage();
				return input().images().vis().io().applyMask_ResizeMaskIfNeeded(cleared, options.getBackground())
						.print("CLEAR RESULT", debug).getImage();
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		// return input().images().fluo();
		if (input().images().fluo() != null && input().masks().fluo() != null) {
			if ((options.isBarley() && options.isHighResMaize()))
				return input().images().fluo();
			else
				if (options.getCameraPosition() == CameraPosition.SIDE) {
					double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
					FlexibleImage fluo = input().images().fluo();
					fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()),
							(int) (scaleFactor * fluo.getHeight()));
					
					double leftRightBorder = 0.1;
					
					if (options.isArabidopsis())
						leftRightBorder = 0;
					
					FlexibleImage result = new ImageOperation(fluo.io().copy()
							.blur(1d).print("Blurred 1.5 fluo image", false)
							.medianFilter32Bit()
							.getImage()).compare()
							.compareImages("fluo", input().masks().fluo().io()
									// .blur(/* 2 */options.getUnitTestIdx() / 2d).print("Blurred 1.5 fluo mask", false)
									.medianFilter32Bit()
									.getImage(),
									options.getIntSetting(Setting.L_Diff_FLUO) * 0.1d,
									options.getIntSetting(Setting.L_Diff_FLUO) * 0.1d,
									options.getIntSetting(Setting.abDiff_FLUO) * 0.1d,
									back).border(2).border_left_right((int) (fluo.getWidth() * leftRightBorder), options.getBackground()).getImage();
					double blueCurbWidthBarley0_1 = 0;
					double blueCurbHeightEndBarly0_8 = 1;
					FlexibleImage toBeFiltered = result.io().hq_thresholdLAB_multi_color_or_and_not(
							// black background and green pot (fluo of white pot)
							new int[] { -1, 200 - 40, 50 - 4, 0 }, new int[] { 115, 200 + 20, 50 + 4, 50 },
							new int[] { 80 - 5, 104 - 15, 169 - 4, 0 }, new int[] { 140 + 5, 104 + 15, 169 + 4, 250 },
							new int[] { 116 - 5, 206 - 20, 160 - 4, 0 }, new int[] { 175 + 5, 206 + 20, 160 + 4, 250 },
							options.getBackground(), Integer.MAX_VALUE, false,
							new int[] {}, new int[] {},
							new int[] {}, new int[] {},
							new int[] {}, new int[] {},
							blueCurbWidthBarley0_1,
							blueCurbHeightEndBarly0_8).
							print("removed noise", debug).getImage();
					
					result = result.copy().io().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage();
					
					if (debug)
						result.copy().io().replaceColor(options.getBackground(), Color.YELLOW.getRGB()).print("Left-Over");
					
					return result;
				}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
				FlexibleImage fluo = input().images().fluo();
				fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
				double f = 0.2d;
				return new ImageOperation(fluo).compare()
						.compareImages("fluo", input().masks().fluo()
								// .io().
								// copyImagesParts(0.26, 0.3).print("cut out", true).getImage()
								,
								options.getIntSetting(Setting.L_Diff_FLUO) * f,
								options.getIntSetting(Setting.L_Diff_FLUO) * f,
								options.getIntSetting(Setting.abDiff_FLUO) * f,
								back).border(2).getImage();
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		FlexibleImage nir = input().images().nir();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (options.isBarleyInBarleySystem()) {
				// remove horizontal bar
				if (nir != null)
					nir = filterHorBar(nir);
			}
		}
		return nir;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().images().nir() != null && input().masks().nir() == null) {
			// create simulated nir background
			int w = input().images().nir().getWidth();
			int h = input().images().nir().getHeight();
			input().masks().setNir(ImageOperation.createColoredImage(w, h, new Color(180, 180, 180)));
		}
		if (input().images().nir() != null && input().masks().nir() != null) {
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				FlexibleImage nir = input().masks().nir();
				if (options.isBarleyInBarleySystem()) {
					// remove horizontal bar
					nir = filterHorBar(nir);
				}
				if (options.isMaize()) {
					int blackDiff = options.getIntSetting(Setting.B_Diff_NIR_TOP) / 3;
					int whiteDiff = options.getIntSetting(Setting.W_Diff_NIR_TOP) / 3;
					FlexibleImage msk = new ImageOperation(nir.print("NIR MSK", debug)).compare()
							.compareGrayImages(input().images().nir(), blackDiff, whiteDiff, options.getBackground())
							.print("result nir", debug).getImage();
					// .thresholdClearBlueBetween(150 - 10, 169 + 10).thresholdBlueHigherThan(240).border(2).getImage();
					
					return msk; // 150 169 240
				}
				return nir;
			}
			if (options.getCameraPosition() == CameraPosition.TOP) {
				FlexibleImage nir = input().images().nir();
				
				if (options.isMaize()) {
					int blackDiff = options.getIntSetting(Setting.B_Diff_NIR_TOP);
					int whiteDiff = options.getIntSetting(Setting.W_Diff_NIR_TOP);
					return new ImageOperation(nir).compare()
							.compareGrayImages(input().masks().nir(), blackDiff, whiteDiff, options.getBackground())
							.print("result nir", debug).thresholdClearBlueBetween(150, 169).thresholdBlueHigherThan(240).border(2).getImage(); // 150 169 240
				} else {
					if (options.isHighResMaize())
						return new ImageOperation(nir).compare()
								.compareGrayImages(input().masks().nir(), 10, 23, options.getBackground())
								.print("result nir", debug).thresholdBlueHigherThan(240).border(2).getImage();
					else
						return new ImageOperation(nir).compare()
								.compareGrayImages(input().masks().nir(), 10, 23, options.getBackground())
								.print("result nir", debug).thresholdBlueHigherThan(240).border(2).getImage();
					
				}
			}
			throw new UnsupportedOperationException("Unknown camera setting.");
		} else {
			return null;
		}
	}
	
	private FlexibleImage filterHorBar(FlexibleImage nirImage) {
		int[][] in = nirImage.getAs2A();
		int width = nirImage.getWidth();
		int height = nirImage.getHeight();
		int gray = new Color(180, 180, 180).getRGB();
		for (int y = (int) (height * 0.4); y < height * 0.6; y++) {
			double sum = 0;
			int n = 0;
			for (int x = 0; x < width; x++) {
				float i = (float) ((in[x][y] & 0x0000ff) / 255.0); // B 0..1
				sum = sum + i;
				n++;
			}
			double avg = sum / n;
			double differenceDistanceSum = 0;
			for (int x = 0; x < width; x++) {
				float i = (float) ((in[x][y] & 0x0000ff) / 255.0); // B 0..1
				differenceDistanceSum += Math.abs(i - avg);
			}
			if (avg < 0.6) {
				for (int x = 0; x < width; x++) {
					in[x][y] = gray;
				}
			}
		}
		FlexibleImage res = new FlexibleImage(in).print("DEBUG", false);
		return res;
	}
	
	@Override
	protected FlexibleImage processIRmask() {
		if (input().images().ir() != null)
			return input().images().ir().copy();
		else
			return input().masks().ir();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage i = processedImages.nir();
			FlexibleImage m = processedMasks.nir();
			if (i != null && m != null)
				i = i.io().applyMask_ResizeMaskIfNeeded(m.io().getImage(), options.getBackground()).getImage();
			processedImages.setNir(i);
		}
	}
}
