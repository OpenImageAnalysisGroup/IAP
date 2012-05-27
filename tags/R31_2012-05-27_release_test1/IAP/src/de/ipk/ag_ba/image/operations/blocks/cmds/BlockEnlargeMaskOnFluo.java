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
public class BlockEnlargeMaskOnFluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return enlargeMask(input().masks().fluo(), options.getCameraPosition());
	}
	
	private FlexibleImage enlargeMask(FlexibleImage workImage, CameraPosition cameraTyp) {
		
		switch (cameraTyp) {
			case SIDE:
				return doEnlargeMask(workImage, options.getIntSetting(Setting.FLUO_SIDE_NUMBER_OF_ERODE_LOOPS),
						options.getIntSetting(Setting.FLUO_SIDE_NUMBER_OF_DILATE_LOOPS));
				
			case TOP:
				return doEnlargeMask(workImage, options.getIntSetting(Setting.FLUO_TOP_NUMBER_OF_ERODE_LOOPS),
						options.getIntSetting(Setting.FLUO_TOP_NUMBER_OF_DILATE_LOOPS));
		}
		
		return null;
	}
	
	private FlexibleImage doEnlargeMask(FlexibleImage workImage, int numberOfErodeLoops, int numberOfDilateLoops) {
		
		ImageOperation io = new ImageOperation(workImage);
		
		for (int i = 0; i < numberOfErodeLoops; i++)
			io.erode();
		for (int i = 0; i < numberOfDilateLoops; i++)
			io.dilate();
		
		return io.getImage();
	}
}
