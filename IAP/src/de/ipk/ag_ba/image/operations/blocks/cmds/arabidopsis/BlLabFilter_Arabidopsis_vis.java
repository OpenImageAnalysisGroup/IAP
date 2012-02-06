/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

/**
 * Uses a lab-based pixel filter for the vis and fluo images.
 * 
 * @author Entzian, Pape, Klukas
 */
public class BlLabFilter_Arabidopsis_vis extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null
				|| getInput().getImages().getVis() == null)
			return null;
		else {
			FlexibleImageStack fis = debug ?
					new FlexibleImageStack() : null;
			int dilate;
			FlexibleImage result;
			FlexibleImage mask = getInput().getMasks().getVis();
			FlexibleImage orig = getInput().getImages().getVis();
			// fis.addImage("mask", mask.copy());
			boolean side = options.getCameraPosition() == CameraPosition.SIDE;
			if (options.isMaize()) {
				dilate = 3;
				result = labFilterVis(side, mask, orig, dilate, debug);
				return result.getIO().filterGray(220, 15, 15).getImage()
						.print("Gray filtered", debug);
			} else {
				dilate = 0;
				result = mask;
				
				FlexibleImage toBeFiltered = result;// .copy().getIO().hq_thresholdLAB(
				// 10, 240,
				// 127 - 80, 500,
				// 0, 127 - 10,
				// options.getBackground(), false).print("BLUEEE", false).erode().dilate(0).getImage();// .erode().dilate(2).getImage();
				// result = result.copy().getIO().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage();
				if (fis != null)
					fis.addImage("step 1", result.copy());
				
				// filter white pot
				int an = 150, bn = 170;
				int offB = 20, offB2 = 20;
				int lowerLimit = options.isBarleyInBarleySystem() ? 0 : 0;
				if (!options.isBarleyInBarleySystem()) {
					an = 13;
					bn = 7;
					offB = 15;
					offB2 = 0;
				}
				int upperLimit = 255;
				toBeFiltered = result.copy().getIO().hq_thresholdLAB(
						lowerLimit, upperLimit,
						120 - an, 120 + an,
						120 - offB, 120 + 2 * bn + offB2,
						options.getBackground(), true).getImage();
				
				if (fis != null)
					fis.addImage("step 2", toBeFiltered.copy());
				
				int w = toBeFiltered.getWidth();
				int h = toBeFiltered.getHeight();
				
				result = result.copy().getIO().applyMask_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).
						print("unknown 1", false).getImage();
				
				// .removeSmallElements(30, 30).getImage().print("ORRR GREEN (NACH REMOVAL)", debug)
				
				if (fis != null)
					fis.addImage("step 4", result);
				
				if (fis != null)
					fis.addImage("step 6", result);
				// filter background noise
				double blueCurbWidthBarley0_1 = options.isBarleyInBarleySystem() ? 0.15 : 0.28;
				double blueCurbHeightEndBarly0_8 = options.isBarleyInBarleySystem() ? 0.71 : 0.7;
				if (options.getCameraPosition() == CameraPosition.SIDE)
					toBeFiltered = result
							.getIO()
							.hq_thresholdLAB_multi_color_or_and_not(
									// noise colors
									new int[] {
											215 - 5, 225, 146 - 5, 250, 170 - 10, 151 - 20, 188 - 20, 220 - 5, 195 - 5, 100 - 5, 197 - 5, 47 - 5, 205 - 5, 110 - 5,
											50 - 5,
											146 - 5, 184 - 5, 155 - 5, 155 - 5, 171 - 5, 153 - 5, 116 - 5, 115 - 5, 168 - 5, 0, 161 - 5,
											options.isBarleyInBarleySystem() ? 255 - 5 : 135 - 5,
											options.isBarleyInBarleySystem() ? 120 - 5 : 120 - 5,
											options.isBarleyInBarleySystem() ? 255 : 235 - 5 },
									new int[] {
											256, 256, 146 + 5, 257, 230 + 10, 151 + 4, 211 + 20, 220 + 5, 195 + 5, 218 + 5, 197 + 5, 91 + 5, 245 + 5, 144 + 5,
											50 + 5,
											146 + 5, 185 + 5, 155 + 5, 155 + 5, 199 + 5, 161 + 5, 172 + 5, 126 + 5, 168 + 5, 110 + 5, 161 + 5, 135 + 5,
											250 + 5, 255 + 5 },
									new int[] {
											120 - 5, 120 - 5, 127 - 5, 118 - 10, 129 - 5, 129 - 4, 121 - 15, 120 - 5, 123 - 5, 124 - 5, 121 - 4, 126 - 5, 117 - 5,
											120 - 5,
											138 - 5, 125 - 5, 113 - 5, 121 - 5, 118 - 5, 116 - 5, 128 - 5, 120 - 5, 130 - 5, 121 - 5, 137 - 10, 122 - 5, 127 - 5,
											105 - 5, 113 - 5 },
									new int[] {
											120 + 5, 120 + 6, 127 + 5, 118 + 10, 129 + 5, 129 + 4, 121 + 5, 120 + 5, 123 + 5, 137 + 5, 121 + 4, 132 + 5, 123 + 5,
											122 + 5,
											138 + 5, 125 + 5, 113 + 5, 121 + 5, 118 + 5, 121 + 5, 132 + 5, 136 + 5, 134 + 5, 121 + 5, 137 + 10, 122 + 5, 127 + 5,
											134 + 5, 118 + 5 },
									new int[] {
											117 - 2, 122 - 14, 144 - 5, 124 - 10, 117 - 5, 114 - 4, 100 - 5, 120 - 5, 118 - 5, 121 - 5, 123 - 4, 117 - 5, 116 - 5,
											106 - 5,
											96 - 5, 100 - 5, 116 - 5, 109 - 5, 119 - 5, 116 - 5, 107 - 5, 110 - 5, 131 - 5, 105 - 5, 118 - 10, 103 - 5, 99 - 5,
											95 - 5, 121 - 5 },
									new int[] {
											124, 122 + 5, 144 + 5, 124 + 10, 117 + 5, 114 + 4, 100 + 8, 120 + 5, 118 + 5, 126 + 5, 123 + 4, 128 + 5, 123 + 5,
											113 + 5,
											96 + 5, 100 + 5, 116 + 5, 109 + 5, 119 + 5, 119 + 5, 111 + 5, 114 + 5, 131 + 5, 105 + 5, 118 + 10, 103 + 5, 99 + 5,
											128, 133 + 5 },
									options.getBackground(), 1, false,
									// plant colors
									new int[] {}, new int[] {},
									new int[] {}, new int[] {},
									new int[] {}, new int[] {},
									blueCurbWidthBarley0_1,
									blueCurbHeightEndBarly0_8).
							border_left_right((int) (options.isBarleyInBarleySystem() ? 0 : w * 0.05), Color.red.getRGB()).
							print("removed noise", debug).getImage();
				else
					toBeFiltered = result
							.getIO()
							.hq_thresholdLAB_multi_color_or_and_not(
									// noise colors
									new int[] { 215 - 5, 225, 0, 0, 135,
											-1, 250, 170 - 10, 151 - 20, 188 - 20, 220 - 5, 195 - 5, 100 - 5, 197 - 5, 47 - 5, 205 - 5,
											110 - 5,
											50 - 5,
											146 - 5, 184 - 5, 155 - 5, 155 - 5, 171 - 5, 153 - 5, 116 - 5, 115 - 5, 168 - 5, 0, 161 - 5, 135 - 5,
											80 - 5,
											options.isBarleyInBarleySystem() ? 255 : 0, 160 },
									new int[] { 256, 256, 255, 255, 188,
											146 + 5, 257, 230 + 10, 151 + 4, 211 + 20, 220 + 5, 195 + 5, 218 + 5, 197 + 5, 91 + 5, 245 + 5,
											144 + 5,
											50 + 5,
											146 + 5, 185 + 5, 155 + 5, 155 + 5, 199 + 5, 161 + 5, 172 + 5, 126 + 5, 168 + 5, 110 + 5, 161 + 5, 135 + 5,
											255, 190 + 5, 255 },
									new int[] { 120 - 5, 120 - 5, 100, 120, 0,
											127 - 5, 118 - 10, 129 - 5, 129 - 4, 121 - 15, 120 - 5, 123 - 5, 124 - 5, 121 - 4,
											126 - 5,
											117 - 5,
											120 - 5,
											138 - 5, 125 - 5, 113 - 5, 121 - 5, 118 - 5, 116 - 5, 128 - 5, 120 - 5, 130 - 5, 121 - 5, 137 - 10, 122 - 5, 127 - 5,
											110 - 5, 115 - 5, 118 - 5 },
									new int[] { 120 + 5, 130, 110, 135, 132,
											128, 118 + 10, 129 + 5, 129 + 4, 121 + 5, 120 + 5, 123 + 5, 137 + 5, 121 + 4, 132 + 5,
											123 + 5,
											122 + 5,
											138 + 5, 125 + 5, 123 + 5, 121 + 5, 118 + 5, 121 + 5, 132 + 5, 136 + 5, 134 + 5, 121 + 5, 137 + 10, 122 + 5, 127 + 5,
											127 + 5, 134 + 5, 135 + 5 },
									new int[] { 117 - 2, 122 - 14, 100, 70, 110,
											144 - 5, 124 - 10, 117 - 5, 114 - 4, 100 - 5, 120 - 5, 118 - 5, 121 - 5, 123 - 4,
											117 - 5,
											116 - 5,
											106 - 5,
											96 - 5, 100 - 5, 116 - 5, 109 - 5, 119 - 5, 116 - 5, 107 - 5, 110 - 5, 131 - 5, 105 - 5, 118 - 10, 103 - 5, 99 - 5,
											90 - 5, 90 - 5, 80 - 5 },
									new int[] { 125, 122 + 5, 144 + 5, 90, 144,
											110, 124 + 10, 117 + 5, 114 + 4, 100 + 8, 120 + 5, 118 + 5, 126 + 5, 123 + 4, 128 + 5,
											123 + 5,
											113 + 5,
											96 + 5, 100 + 5, 120 + 5, 109 + 5, 119 + 5, 119 + 5, 111 + 5, 114 + 5, 131 + 5, 105 + 5, 118 + 10, 103 + 5, 99 + 5,
											110 + 5, 149 + 5, 102 + 5 },
									options.getBackground(), Integer.MAX_VALUE, false,
									// plant colors
									new int[] {}, // 70 - 5
									new int[] {}, // 160 + 5
									new int[] {}, // 113 - 5
									new int[] {}, // 120 + 5
									new int[] {}, // 128 - 5
									new int[] {}, // 148 + 5
									blueCurbWidthBarley0_1,
									blueCurbHeightEndBarly0_8).
							// border_left_right(
							// options.isBarleyInBarleySystem() ?
							// (int) (w * 0.01) :
							// 0,
							// Color.red.getRGB()).
							print("removed noise", false).getImage();
				if (!options.isBarleyInBarleySystem())
					toBeFiltered = toBeFiltered.getIO().medianFilter32Bit().medianFilter32Bit().getImage();
				if (fis != null)
					fis.addImage("step 7", toBeFiltered.copy());
				
				result = result.getIO().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage(); // copy().
				if (fis != null)
					fis.addImage("step 8", result);
				if (debug)
					result.copy().getIO().replaceColors(options.getBackground(), Color.black.getRGB()).print("Left-Over", false);
				if (fis != null)
					fis.addImage("step 9", result);
				
				// fis.addImage("blue1_filtered", result.copy());
				// result = result.getIO().filterGray(220, 15, 15).getImage()
				// .print("Gray filtered", debug);
				// fis.addImage("gray filtered", result);
				if (fis != null)
					fis.print("lab filter vis");
				return getInput().getImages().getVis().copy().getIO()
						.applyMask_ResizeSourceIfNeeded(
								result.getIO().erode().blur(1).getImage(), options.getBackground())
						.getImage();
			}
		}
	}
	
	private FlexibleImage labFilterVis(boolean sideImage, FlexibleImage mask,
			FlexibleImage orig, int dilate, boolean debug) {
		FlexibleImageStack fis = new FlexibleImageStack();
		
		if (!sideImage && !options.isHighResMaize()) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 10);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 80); // 98
			// //
			// 130
			// gerste
			// wegen
			// topf
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 120);// 125
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		}
		
		if (sideImage && options.isBarleyInBarleySystem())
			dilate = 0;
		
		FlexibleImage labResult = labFilter(
				// getInput().getMasks().getVis().getIO().dilate(3,
				// getInput().getImages().getVis()).blur(2).getImage(),
				mask.copy().getIO().blur((dilate > 0 ? 3 : 0)).getImage(),
				orig.copy(),
				options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS),
				options.getCameraPosition(), options.isMaize(), false, true,
				options.getBackground()).getIO().erode(dilate > 0 ? 2 : 0)
				.print("before dilate, after lab", debug)
				.dilate(dilate > 0 ? 3 : 0).getImage()
				.print("after lab", debug).getIO().erode(2).getImage();
		
		if (debug) {
			fis.addImage("mask", mask.copy());
			fis.addImage("labresult", labResult.copy());
		}
		
		FlexibleImage result = mask
				.copy()
				.getIO()
				.removePixel(labResult.copy(), options.getBackground(), 1, 105,
						120).getImage();
		
		if (debug)
			fis.addImage("without blue parts", result);
		
		if (!sideImage || !options.isHighResMaize()) {
			if (options.isHighResMaize()) {
				// remove black matts inside the holes
				return result
						.getIO()
						.thresholdLAB(0, 150, 110, 150, 100, 150,
								ImageOperation.BACKGROUND_COLORint,
								CameraPosition.TOP, false, true).getImage();
			} else
				return result;
		}
		
		FlexibleImage potFiltered = labFilter(
				// options.isMaize() ?
				result.copy(), getInput().getImages().getVis().copy(),
				100, // filter anything that is very dark
				255,
				0, // 127 - 10,
				255, // 127 + 10,
				0, // 127 - 10,
				255, // 127 + 10,
				options.getCameraPosition(), options.isMaize(), false, true,
				options.getBackground())
				.getIO()
				.clearImageAbove(mask.getHeight() * 0.6,
						options.getBackground()).erode(1)
				.print("A: " + dilate, debug)
				.threshold(127, 1, ImageOperation.BACKGROUND_COLORint)
				.print("B", debug).dilate(11).dilateHor(Integer.MAX_VALUE)
				.print("C", debug).debug(fis, "before hor dilate", debug)
				.dilateHor(2 + dilate * 3)
				.debug(fis, "after hor dilate", debug).// blur(4).
				getImage();// : null;
		
		if (debug) {
			fis.addImage("removed black", potFiltered);
			fis.print("debug lab filter");
		}
		
		return result = result
				.getIO()
				.removePixel(
						potFiltered.print(
								"black parts removed from blue parts removal",
								false), options.getBackground())// , 50, 110, 1)
				.getImage();
	}
	
	// @Override
	// protected FlexibleImage processFLUOmask() {
	// if (getInput().getMasks().getFluo() == null &&
	// getInput().getImages().getFluo() == null)
	// return null;
	//
	// if (options.getCameraPosition() == CameraPosition.SIDE)
	// return getInput().getMasks().getFluo();
	// else
	// return labFilter(getInput().getMasks().getFluo(),
	// getInput().getImages().getFluo(),
	// options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO),
	// options.getCameraPosition(),
	// options.isMaize(), false, false, options.getBackground());
	// }
	
	static FlexibleImage labFilter(FlexibleImage workMask,
			FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL,
			int lowerValueOfA, int upperValueOfA, int lowerValueOfB,
			int upperValueOfB, CameraPosition typ, boolean maize,
			boolean blueStick, boolean blueBasket, int back) {
		if (workMask == null)
			return null;
		int[] workMask1D = workMask.getAs1A();
		// int[] result = new int[workMask1D.length];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		FlexibleImage mod = ImageOperation.thresholdLAB3(width, height,
				workMask1D, workMask1D, lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA, lowerValueOfB, upperValueOfB,
				back, typ, maize, blueStick, originalImage.getAs2A(),
				blueBasket);
		
		return new ImageOperation(mod).applyMask_ResizeSourceIfNeeded(
				workMask1D, width, height, back).getImage();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages,
			FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		// processedImages.setVis(mod);
	}
}
