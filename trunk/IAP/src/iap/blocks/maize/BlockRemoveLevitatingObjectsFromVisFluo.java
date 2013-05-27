package iap.blocks.maize;

import java.util.HashSet;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

public class BlockRemoveLevitatingObjectsFromVisFluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null || input().masks().vis() == null)
			return null;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			Image input = input().masks().vis();
			int background = options.getBackground();
			int cut = searchSplitObjectsInYDirection(input, 20, background);
			if (cut > 0 && cut < input().masks().vis().getHeight() * 0.5)
				return new ImageOperation(input().masks().vis()).clearImageAbove(cut, background).getImage();
			else
				return input().masks().vis();
		}
		return input().masks().vis();
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null || input().masks().fluo() == null)
			return null;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			Image input = input().masks().fluo();
			int background = options.getBackground();
			int cut = searchSplitObjectsInYDirection(input, 20, background);
			if (cut > 0 && cut < input().masks().fluo().getHeight() * 0.5)
				return new ImageOperation(input().masks().fluo()).clearImageAbove(cut, background).getImage();
			else
				return input().masks().fluo();
		}
		return input().masks().fluo();
	}
	
	private int searchSplitObjectsInYDirection(Image input, int tolerance, int background) {
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

	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}

}