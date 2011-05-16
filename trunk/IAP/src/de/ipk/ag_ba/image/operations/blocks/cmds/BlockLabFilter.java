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
public class BlockLabFilter extends AbstractSnapshotAnalysisBlockFIS {
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
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	//
	// return labFit(getInput().getMasks().getNir(), getInput().getImages().getNir(), options.getIntSetting(Setting.LAB_MIN_L_VALUE_NIR),
	// options.getIntSetting(Setting.LAB_MAX_L_VALUE_NIR), options.getIntSetting(Setting.LAB_MIN_A_VALUE_NIR),
	// options.getIntSetting(Setting.LAB_MAX_A_VALUE_NIR), options.getIntSetting(Setting.LAB_MIN_B_VALUE_NIR),
	// options.getIntSetting(Setting.LAB_MAX_B_VALUE_NIR));
	// }
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB) {
		
		int[][] originalArray = originalImage.getAs2A();
		int[][] resultMask = new int[workMask.getWidth()][workMask.getHeight()];
		
		ImageOperation getLab = new ImageOperation(workMask);
		resultMask = getLab.thresholdLAB(lowerValueOfL, upperValueOfL, lowerValueOfA, upperValueOfA, lowerValueOfB, upperValueOfB, options.getBackground())
				.getImageAs2array();
		
		for (int i = 0; i < workMask.getWidth(); i++) {
			for (int j = 0; j < workMask.getHeight(); j++) {
				
				if (resultMask[i][j] != options.getBackground())
					resultMask[i][j] = originalArray[i][j];
				
			}
		}
		
		return new FlexibleImage(resultMask);
		
	}
}
