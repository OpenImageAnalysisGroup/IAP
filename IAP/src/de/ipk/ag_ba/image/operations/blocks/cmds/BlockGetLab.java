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
 * @author Entzian
 */
public class BlockGetLab extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		
		return labFilter(getInput().getMasks().getVis(), getInput().getImages().getVis(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS), options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS), options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS), options.getCameraPosition());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		return labFilter(getInput().getMasks().getFluo(), getInput().getImages().getFluo(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO), options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO), options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO), options.getCameraPosition());
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		
		return labFilter(getInput().getMasks().getNir(), getInput().getImages().getNir(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_NIR),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_NIR), options.getIntSetting(Setting.LAB_MIN_A_VALUE_NIR),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_NIR), options.getIntSetting(Setting.LAB_MIN_B_VALUE_NIR),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_NIR), options.getCameraPosition());
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB, CameraPosition typ) {
		
		int[][] image = originalImage.getAs2A();
		int[][] result = new int[workMask.getWidth()][workMask.getHeight()];
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.doThresholdLAB(width, height, image, result,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back, typ);
		
		for (int i = 0; i < workMask.getWidth(); i++) {
			for (int j = 0; j < workMask.getHeight(); j++) {
				
				if (result[i][j] != back)
					result[i][j] = image[i][j];
				
			}
		}
		
		return new FlexibleImage(result);
		
	}
}
