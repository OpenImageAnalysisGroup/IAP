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
public class BlockLabFilterMaskToMaskBlack extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		
		return labFilter(getInput().getMasks().getVis(), getInput().getImages().getVis(), 110, 255, 0, 255, 0, 255);
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB) {
		
		int[][] image = workMask.getAs2A();
		int[][] result = new int[workMask.getWidth()][workMask.getHeight()];
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.doThresholdLAB(width, height, image, result,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back);
		
		for (int i = 0; i < workMask.getWidth(); i++) {
			for (int j = 0; j < workMask.getHeight(); j++) {
				
				if (result[i][j] != back)
					result[i][j] = image[i][j];
				
			}
		}
		
		return new FlexibleImage(result);
		
	}
}
