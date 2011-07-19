/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Uses a lab-based pixel filter for the vis and fluo images.
 * 
 * @author Entzian, Pape
 */
public class BlockLabFilter extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null)
			return null;
		else
			return labFilter(
					getInput().getMasks().getVis().getIO().dilateNG(3, getInput().getImages().getVis()).blur(2).getImage(),
					getInput().getImages().getVis(),
					options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS),
					options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS),
					options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS),
					options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS),
						options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS),
						options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS), options.getCameraPosition());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (options.getCameraPosition() == CameraPosition.SIDE)
			return getInput().getMasks().getFluo();
		else
			return labFilter(getInput().getMasks().getFluo(), getInput().getImages().getFluo(),
						options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO),
					options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO),
						options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO),
						options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO),
						options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO),
						options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO), options.getCameraPosition());
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB, CameraPosition typ) {
		
		int[] workMask1D = workMask.getAs1A();
		// int[] result = new int[workMask1D.length];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.thresholdLAB3(width, height, workMask1D, workMask1D,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back, typ);
		
		return new ImageOperation(originalImage).applyMask_ResizeSourceIfNeeded(workMask1D, width, height, options.getBackground()).getImage();
	}
}
