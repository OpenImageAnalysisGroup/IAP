/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */
@Deprecated
public class BlockGetLab extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		boolean maize = false;
		return labFilter(input().masks().vis(), input().images().vis(),
				options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS), options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS), options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS), options.getCameraPosition(),
				maize);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		boolean maize = false;
		return labFilter(input().masks().fluo(), input().images().fluo(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO), options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO), options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO), options.getCameraPosition(), maize);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		boolean maize = false;
		return labFilter(input().masks().nir(), input().images().nir(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_NIR),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_NIR), options.getIntSetting(Setting.LAB_MIN_A_VALUE_NIR),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_NIR), options.getIntSetting(Setting.LAB_MIN_B_VALUE_NIR),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_NIR), options.getCameraPosition(), maize);
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB, CameraPosition typ, boolean maize) {
		
		int[] image = originalImage.getAs1A();
		int[] result = new int[image.length];
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.thresholdLAB(width, height, image, result,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back, typ, maize);
		
		int idx = 0;
		for (int c : result) {
			if (c != back)
				result[idx] = image[idx++];
		}
		
		return new FlexibleImage(width, height, result);
	}
}
