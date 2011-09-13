/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * Uses a lab-based pixel filter for the vis and fluo images.
 * 
 * @author Entzian, Pape
 */
public class BlockLabFilter_vis_fluo extends AbstractSnapshotAnalysisBlockFIS {
	FlexibleImage mod = null;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null || getInput().getImages().getVis() == null)
			return null;
		else {
			boolean blueStick;
			int dilate;
			FlexibleImage result;
			FlexibleImage mask = getInput().getMasks().getVis();
			FlexibleImage orig = getInput().getImages().getVis();
			boolean debug = false;
			if (options.isMaize()) {
				blueStick = false;
				dilate = 2;
				result = labFilterVis(mask, orig, dilate, debug);
			} else {
				blueStick = false;
				dilate = 2;
				result = labFilterVis(mask, orig, dilate, debug);
			}
			return result;
		}
	}
	
	private FlexibleImage labFilterVis(FlexibleImage mask, FlexibleImage orig, int dilate, boolean debug) {
		
		FlexibleImage labResult = labFilter(
				// getInput().getMasks().getVis().getIO().dilate(3, getInput().getImages().getVis()).blur(2).getImage(),
				mask.copy().getIO().blur(1).getImage(),
				orig.copy(),
				options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS),
				options.getCameraPosition(),
				options.isMaize(), false, true).getIO().dilate(dilate).getImage().print("after lab", false);
		
		FlexibleImage result = mask.copy().print("mask", false).getIO()
				.removePixel(labResult.copy().print("rm", false), options.getBackground(), 1, 105, 120)
				.getImage().print("without blue parts", false);
		
		FlexibleImage potFiltered = labFilter(
				result.copy(),
				getInput().getImages().getVis().copy(),
				100,
				255,
				0, // 127 - 10,
				255, // 127 + 10,
				0, // 127 - 10,
				255, // 127 + 10,
				options.getCameraPosition(),
				options.isMaize(), false, true).getIO().dilate(6).blur(3).getImage()
				.print("after lab (removed black)", true);
		
		return result = result.getIO().removePixel(potFiltered.print("black parts removed from blue parts removal", debug), options.getBackground(), 50, 110, 1)
				.getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null && getInput().getImages().getFluo() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.SIDE)
			return getInput().getMasks().getFluo();
		else
			return labFilter(getInput().getMasks().getFluo(), getInput().getImages().getFluo(),
						options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO),
					options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO),
						options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO),
						options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO),
						options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO),
						options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO),
						options.getCameraPosition(),
						options.isMaize(), false, false);
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB, CameraPosition typ,
			boolean maize, boolean blueStick, boolean blueBasket) {
		if (workMask == null)
			return null;
		int[] workMask1D = workMask.getAs1A();
		// int[] result = new int[workMask1D.length];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		int back = options.getBackground();
		
		mod = ImageOperation.thresholdLAB3(width, height, workMask1D, workMask1D,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back, typ, maize, blueStick, originalImage.getAs2A(), blueBasket);
		
		return new ImageOperation(mod).applyMask_ResizeSourceIfNeeded(workMask1D, width, height, options.getBackground()).getImage();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		processedImages.setVis(mod);
	}
}
