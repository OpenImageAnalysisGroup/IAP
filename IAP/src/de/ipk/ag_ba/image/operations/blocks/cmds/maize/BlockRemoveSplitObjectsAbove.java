package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveSplitObjectsAbove extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null || getInput().getMasks().getFluo() == null)
			return null;
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getFluo();
			int background = options.getBackground();
			
			int cut = searchSplitObjectsAbove(input, 10, background);
			if (cut > 0)
				return new ImageOperation(getInput().getMasks().getFluo()).clearImageAbove(cut, background).getImage();
			else
				return getInput().getMasks().getFluo();
		}
		return getInput().getMasks().getFluo();
	}
	
	private int searchSplitObjectsAbove(FlexibleImage input, int tolerance, int background) {
		int[][] imgArray = input.getAs2A();
		int width = input.getWidth();
		int height = input.getHeight();
		int result = -1;
		int count = 0;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (imgArray[x][y] != background) {
					result = -1;
					count = 0;
				}
			}
			result = y;
			count++;
			if (count > tolerance)
				return result;
		}
		return result;
	}
}
