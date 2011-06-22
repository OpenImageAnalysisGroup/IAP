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
 * calculates lab filter for vis and fluo image, the original pixels were copy at the end
 * 
 * @author Entzian, Pape
 */
public class BlockLabFilter extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		return labFilter(getInput().getMasks().getVis(), getInput().getImages().getVis(),
				new int[] { options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS) },
				new int[] { options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS) });
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (options.getCameraTyp() == CameraPosition.SIDE)
			return getInput().getMasks().getFluo();
		else
			return labFilter(getInput().getMasks().getFluo(), getInput().getImages().getFluo(),
					new int[] { options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO) },
					new int[] { options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO) });
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int[] lowerValueOfL, int[] upperValueOfL, int[] lowerValueOfA,
			int[] upperValueOfA, int[] lowerValueOfB, int[] upperValueOfB) {
		
		int[][] image = workMask.getAs2A();
		int[][] result = new int[workMask.getWidth()][workMask.getHeight()];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.doThresholdLAB(width, height, image, result,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back);
		
		FlexibleImage mask = new FlexibleImage(result);
		
		return new ImageOperation(originalImage).applyMask_ResizeSourceIfNeeded(mask, options.getBackground()).getImage();
	}
}
