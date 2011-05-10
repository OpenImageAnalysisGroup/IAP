/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */
public class BlockLabFit extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		
		return labFit(getInput().getMasks().getVis(), getInput().getImages().getVis(), 0, 255, 0, 255, 132, 255);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		return labFit(getInput().getMasks().getFluo(), getInput().getImages().getFluo(), 0, 255, 0, 255, 132, 255);
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	//
	// return labFit(getInput().getMasks().getFluo(), getInput().getImages().getFluo(), ImageTyp.FLUO, 0, 255, 0, 255, 132, 255);
	// }
	//
	private FlexibleImage labFit(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
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
