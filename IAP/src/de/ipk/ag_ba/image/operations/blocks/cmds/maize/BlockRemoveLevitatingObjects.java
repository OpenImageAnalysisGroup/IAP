package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveLevitatingObjects extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null || getInput().getMasks().getVis() == null)
			return null;
		if (options.getCameraTyp() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getMasks().getVis();
			int background = options.getBackground();
			int cut = searchSplitObjectsInYDirection(input, 10, background);
			if (cut > 0)
				return new ImageOperation(getInput().getMasks().getVis()).clearImageAbove(cut, background).getImage();
			else
				return getInput().getMasks().getVis();
		}
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null || getInput().getMasks().getFluo() == null)
			return null;
		if (options.getCameraTyp() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getMasks().getFluo();
			int background = options.getBackground();
			int cut = searchSplitObjectsInYDirection(input, 10, background);
			if (cut > 0)
				return new ImageOperation(getInput().getMasks().getFluo()).clearImageAbove(cut, background).getImage();
			else
				return getInput().getMasks().getFluo();
		}
		return getInput().getMasks().getFluo();
	}
	
	private int searchSplitObjectsInYDirection(FlexibleImage input, int tolerance, int background) {
		int[][] imgArray = input.getAs2A();
		int width = input.getWidth();
		int height = input.getHeight();
		int result = -1;
		int count = 0;
		
		int begin = searchBeginOfPlant(imgArray, height, width, background);
		boolean plant = false;
		
		for (int y = begin; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				if (imgArray[x][y] != background) {
					result = -1;
					count = 0;
					plant = true;
				}
			}
			if (!plant) {
				result = y;
				count++;
			}
			if (count > tolerance)
				return result + count;
			plant = false;
		}
		return result;
	}
	
	private int searchBeginOfPlant(int[][] imgArray, int height, int width, int background) {
		int count = 0;
		int res = 0;
		int tolerance = 3;
		boolean plant = false;
		
		for (int y = height - 1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				if (imgArray[x][y] != background) {
					plant = true;
				}
			}
			if (plant) {
				res = y;
				count++;
			} else {
				res = 0;
				count = 0;
			}
			if (count > tolerance) {
				res = res + (count - 1);
				return res;
			}
		}
		return 0; // no plant
	}
	
}