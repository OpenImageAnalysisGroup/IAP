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
		
		return labFilter(
				getInput().getMasks().getVis().getIO().dilate().dilate().dilate().blur(2)// .enhanceContrast().printImage("blur & multi")
						.getImage(),
				getInput().getImages().getVis(),
				new int[] { options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS) }, options.getCameraPosition());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (options.getCameraPosition() == CameraPosition.SIDE)
			return getInput().getMasks().getFluo();
		else
			return labFilter(getInput().getMasks().getFluo(), getInput().getImages().getFluo(),
					new int[] { options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO) }, options.getCameraPosition());
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int[] lowerValueOfL, int[] upperValueOfL, int[] lowerValueOfA,
			int[] upperValueOfA, int[] lowerValueOfB, int[] upperValueOfB, CameraPosition typ) {
		
		int[] workMask1D = workMask.getAs1A();
		// int[] result = new int[workMask1D.length];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.doThresholdLAB(width, height, workMask1D, workMask1D,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back, typ);
		
		// FlexibleImage mask = new FlexibleImage(result, width, height);
		FlexibleImage mask = new FlexibleImage(workMask1D, width, height);
		
		return new ImageOperation(originalImage).applyMask_ResizeSourceIfNeeded(mask, options.getBackground()).getImage();
	}
}
