/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */
public class BlockLabFilterMaskToMask extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		
		return labFilter(getInput().getMasks().getVis(), getInput().getImages().getVis(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS), options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS), options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS));
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		return labFilter(getInput().getMasks().getFluo(), getInput().getImages().getFluo(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO), options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO), options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO));
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getMasks().getNir() != null) {
			return labFilter(getInput().getMasks().getNir(), getInput().getImages().getNir(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_NIR),
					options.getIntSetting(Setting.LAB_MAX_L_VALUE_NIR), options.getIntSetting(Setting.LAB_MIN_A_VALUE_NIR),
					options.getIntSetting(Setting.LAB_MAX_A_VALUE_NIR), options.getIntSetting(Setting.LAB_MIN_B_VALUE_NIR),
					options.getIntSetting(Setting.LAB_MAX_B_VALUE_NIR));
		} else
			return null;
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB) {
		
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
