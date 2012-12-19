/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
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
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null
				|| input().images().vis() == null)
			return null;
		else {
			FlexibleImageStack fis = debug ?
					new FlexibleImageStack() : null;
			int dilate;
			FlexibleImage result;
			FlexibleImage mask = input().masks().vis();
			FlexibleImage orig = input().images().vis();
			// fis.addImage("mask", mask.copy());
			boolean side = options.getCameraPosition() == CameraPosition.SIDE;
			
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
			toBeFiltered = result.copy().io().filterRemoveLAB(
					lowerLimit, upperLimit,
					120 - an, 120 + an,
					120 - offB, 120 + 2 * bn + offB2,
					options.getBackground(), true).getImage();
			
			if (fis != null)
				fis.addImage("step 2", toBeFiltered.copy());
			
			int w = toBeFiltered.getWidth();
			int h = toBeFiltered.getHeight();
			
			result = result.copy().io().applyMask_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).
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
						.io()
						.hq_thresholdLAB_multi_color_or_and_not(
								// noise colors
								new Integer[] {
										215 - 5, 225, 146 - 5, 250, 170 - 10, 151 - 20, 188 - 20, 220 - 5, 195 - 5, 100 - 5, 197 - 5, 47 - 5, 205 - 5, 110 - 5,
										50 - 5,
										146 - 5, 184 - 5, 155 - 5, 155 - 5, 171 - 5, 153 - 5, 116 - 5, 115 - 5, 168 - 5, 0, 161 - 5,
										options.isBarleyInBarleySystem() ? 255 - 5 : 135 - 5,
										options.isBarleyInBarleySystem() ? 120 - 5 : 120 - 5,
										options.isBarleyInBarleySystem() ? 255 : 235 - 5 },
								new Integer[] {
										256, 256, 146 + 5, 257, 230 + 10, 151 + 4, 211 + 20, 220 + 5, 195 + 5, 218 + 5, 197 + 5, 91 + 5, 245 + 5, 144 + 5,
										50 + 5,
										146 + 5, 185 + 5, 155 + 5, 155 + 5, 199 + 5, 161 + 5, 172 + 5, 126 + 5, 168 + 5, 110 + 5, 161 + 5, 135 + 5,
										250 + 5, 255 + 5 },
								new Integer[] {
										120 - 5, 120 - 5, 127 - 5, 118 - 10, 129 - 5, 129 - 4, 121 - 15, 120 - 5, 123 - 5, 124 - 5, 121 - 4, 126 - 5, 117 - 5,
										120 - 5,
										138 - 5, 125 - 5, 113 - 5, 121 - 5, 118 - 5, 116 - 5, 128 - 5, 120 - 5, 130 - 5, 121 - 5, 137 - 10, 122 - 5, 127 - 5,
										105 - 5, 113 - 5 },
								new Integer[] {
										120 + 5, 120 + 6, 127 + 5, 118 + 10, 129 + 5, 129 + 4, 121 + 5, 120 + 5, 123 + 5, 137 + 5, 121 + 4, 132 + 5, 123 + 5,
										122 + 5,
										138 + 5, 125 + 5, 113 + 5, 121 + 5, 118 + 5, 121 + 5, 132 + 5, 136 + 5, 134 + 5, 121 + 5, 137 + 10, 122 + 5, 127 + 5,
										134 + 5, 118 + 5 },
								new Integer[] {
										117 - 2, 122 - 14, 144 - 5, 124 - 10, 117 - 5, 114 - 4, 100 - 5, 120 - 5, 118 - 5, 121 - 5, 123 - 4, 117 - 5, 116 - 5,
										106 - 5,
										96 - 5, 100 - 5, 116 - 5, 109 - 5, 119 - 5, 116 - 5, 107 - 5, 110 - 5, 131 - 5, 105 - 5, 118 - 10, 103 - 5, 99 - 5,
										95 - 5, 121 - 5 },
								new Integer[] {
										124, 122 + 5, 144 + 5, 124 + 10, 117 + 5, 114 + 4, 100 + 8, 120 + 5, 118 + 5, 126 + 5, 123 + 4, 128 + 5, 123 + 5,
										113 + 5,
										96 + 5, 100 + 5, 116 + 5, 109 + 5, 119 + 5, 119 + 5, 111 + 5, 114 + 5, 131 + 5, 105 + 5, 118 + 10, 103 + 5, 99 + 5,
										128, 133 + 5 },
								options.getBackground(), 1, false,
								// plant colors
								new Integer[] {}, new Integer[] {},
								new Integer[] {}, new Integer[] {},
								new Integer[] {}, new Integer[] {},
								blueCurbWidthBarley0_1,
								blueCurbHeightEndBarly0_8).
						border_left_right((int) (options.isBarleyInBarleySystem() ? 0 : w * 0.05), Color.red.getRGB()).
						print("removed noise", debug).getImage();
			else
				toBeFiltered = result
						.io()
						.hq_thresholdLAB_multi_color_or_and_not(
								// noise colors
								new Integer[] { 215 - 5, 225, 0, 0, 135,
										-1, 250, 170 - 10, 151 - 20, 188 - 20, 220 - 5, 195 - 5, 100 - 5, 197 - 5, 47 - 5, 205 - 5,
										110 - 5,
										50 - 5,
										146 - 5, 184 - 5, 155 - 5, 155 - 5, 171 - 5, 153 - 5, 116 - 5, 115 - 5, 168 - 5, 0, 161 - 5, 0,
										80 - 5,
										options.isBarleyInBarleySystem() ? 255 : 0, 160 },
								new Integer[] { 256, 256, 255, 255, 188,
										146 + 5, 257, 230 + 10, 151 + 4, 211 + 20, 220 + 5, 195 + 5, 218 + 5, 197 + 5, 91 + 5, 245 + 5,
										144 + 5,
										50 + 5,
										146 + 5, 185 + 5, 155 + 5, 155 + 5, 199 + 5, 161 + 5, 172 + 5, 126 + 5, 168 + 5, 110 + 5, 161 + 5, 135 + 5,
										255, 255,
										255 },
								new Integer[] { 120 - 5, 120 - 5, 100, 120, 0,
										127 - 5, 118 - 10, 129 - 5, 129 - 4, 121 - 15, 120 - 5, 123 - 5, 124 - 5, 121 - 4,
										126 - 5,
										117 - 5,
										120 - 5,
										138 - 5, 125 - 5, 113 - 5, 121 - 5, 118 - 5, 116 - 5, 128 - 5, 120 - 5, 130 - 5, 121 - 5, 137 - 10, 122 - 5, 127 - 5,
										110 - 5, 115 - 5,
										118 - 5 },
								new Integer[] { 120 + 5, 130, 110, 135, 132,
										128, 118 + 10, 129 + 5, 129 + 4, 121 + 5, 120 + 5, 123 + 5, 137 + 5, 121 + 4, 132 + 5,
										123 + 5,
										122 + 5,
										138 + 5, 125 + 5, 123 + 5, 121 + 5, 118 + 5, 121 + 5, 132 + 5, 136 + 5, 134 + 5, 121 + 5, 137 + 10, 122 + 5, 127 + 5,
										127 + 5, 115 + 5,
										135 + 5 },
								new Integer[] { 117 - 2, 122 - 14, 100, 70, 110,
										144 - 5, 124 - 10, 117 - 5, 114 - 4, 100 - 5, 120 - 5, 118 - 5, 121 - 5, 123 - 4,
										117 - 5,
										116 - 5,
										106 - 5,
										96 - 5, 100 - 5, 116 - 5, 109 - 5, 119 - 5, 116 - 5, 107 - 5, 110 - 5, 131 - 5, 105 - 5, 118 - 10, 103 - 5, 99 - 5,
										90 - 5, 90 - 5,
										80 - 5 },
								new Integer[] { 125, 122 + 5, 139, 90, 120,
										110, 124 + 10, 117 + 5, 114 + 4, 100 + 8, 120 + 5, 118 + 5, 126 + 5, 123 + 4, 128 + 5,
										123 + 5,
										113 + 5,
										96 + 5, 100 + 5, 120 + 5, 109 + 5, 119 + 5, 119 + 5, 111 + 5, 114 + 5, 131 + 5, 105 + 5, 118 + 10, 103 + 5, 99 + 5,
										110 + 5, 129 + 5,
										102 + 5 },
								options.getBackground(), Integer.MAX_VALUE, false,
								// plant colors
								new Integer[] {}, // 70 - 5
								new Integer[] {}, // 160 + 5
								new Integer[] {}, // 113 - 5
								new Integer[] {}, // 120 + 5
								new Integer[] {}, // 128 - 5
								new Integer[] {}, // 148 + 5
								blueCurbWidthBarley0_1,
								blueCurbHeightEndBarly0_8).
						// border_left_right(
						// options.isBarleyInBarleySystem() ?
						// (int) (w * 0.01) :
						// 0,
						// Color.red.getRGB()).
						print("removed noise", false).getImage();
			if (!options.isBarleyInBarleySystem())
				toBeFiltered = toBeFiltered.io().medianFilter32Bit().medianFilter32Bit().getImage();
			if (fis != null)
				fis.addImage("step 7", toBeFiltered.copy());
			
			result = result.io().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage(); // copy().
			if (fis != null)
				fis.addImage("step 8", result);
			if (debug)
				result.copy().io().replaceColor(options.getBackground(), Color.black.getRGB()).print("Left-Over", false);
			if (fis != null)
				fis.addImage("step 9", result);
			
			// fis.addImage("blue1_filtered", result.copy());
			// result = result.getIO().filterGray(220, 15, 15).getImage()
			// .print("Gray filtered", debug);
			// fis.addImage("gray filtered", result);
			if (fis != null)
				fis.print("lab filter vis");
			return input().images().vis().copy().io()
					.applyMask_ResizeSourceIfNeeded(
							result.io().erode().blur(1).getImage(), options.getBackground())
					.getImage();
		}
	}
	
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
