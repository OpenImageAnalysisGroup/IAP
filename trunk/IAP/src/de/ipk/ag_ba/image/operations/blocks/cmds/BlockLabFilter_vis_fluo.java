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
		else
			return labFilter(
					// getInput().getMasks().getVis().getIO().dilate(3, getInput().getImages().getVis()).blur(2).getImage(),
					getInput().getMasks().getVis().getIO().blur(1).getImage(),
					getInput().getImages().getVis(),
					options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS),
					options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS),
					options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS),
					options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS),
						options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS),
						options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS),
						options.getCameraPosition(),
						options.isMaize(), true).print("after lab", false);
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
						options.isMaize(), false);
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB, CameraPosition typ,
			boolean maize, boolean blueStick) {
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
				back, typ, maize, blueStick, originalImage.getAs2A());
		
		return new ImageOperation(mod).applyMask_ResizeSourceIfNeeded(workMask1D, width, height, options.getBackground()).getImage();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		processedImages.setVis(mod);
	}
}
