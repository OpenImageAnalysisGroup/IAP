package de.ipk.ag_ba.image.operations;

import java.util.HashMap;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.ThreeDmodelGenerator;

public class SkeletonizeProcessor {
	
	private final ImageOperation image;
	
	public SkeletonizeProcessor(ImageOperation image) {
		this.image = image;
	}
	
	/**
	 * @author klukas
	 *         The "fire" burns down each solid voxel with fixed speed.
	 *         Warning: changes this VolumeOperation instance and modifes actual instance, and returns it.
	 * @return A int cube, where each int value is either BACKGROUND_COLORint (no skeleton pixel here) or a number greater or equal to 1, which denotes
	 *         the minimum distance of the skeleton pixel to the free space in the input cube. Therefore, each pixel denotes a width at this
	 *         particular part of the input voxel.
	 */
	public ImageOperation calculateThicknessSkeleton(BlockResultSet summaryResult, int voxelresolution, ThreeDmodelGenerator mg,
			BlockProperty distHorizontal,
			double realMarkerDistHorizontal, boolean calcDistanceTrueOrNormalColoredSkeletonFalse) {
		
		int[][] cube = image.getImageAs2dArray();
		
		int fire = ImageOperation.BACKGROUND_COLORint;
		HashMap<Integer, HashMap<Integer, Integer>> x2y2colorSkeleton = new HashMap<Integer, HashMap<Integer, Integer>>();
		boolean foundBorderVoxel = false;
		int loop = 1;
		do {
			foundBorderVoxel = false;
			for (int x = 1; x < voxelresolution - 1; x++) {
				for (int y = 1; y < voxelresolution - 1; y++) {
					int c = cube[x][y];
					boolean filled = c != fire;
					if (filled) {
						boolean left = cube[x - 1][y] != fire;
						boolean right = cube[x + 1][y] != fire;
						boolean above = cube[x][y - 1] != fire;
						boolean below = cube[x][y + 1] != fire;
						
						if (!left || !right || !above || !below) {
							// border voxel
							foundBorderVoxel = true;
							int filledSurrounding = 0;
							if (left)
								filledSurrounding++;
							if (right)
								filledSurrounding++;
							if (above)
								filledSurrounding++;
							if (below)
								filledSurrounding++;
							
							if (filledSurrounding <= 2) {
								if (!calcDistanceTrueOrNormalColoredSkeletonFalse)
									addSkeleton(x2y2colorSkeleton, x, y, c);
								else
									addSkeleton(x2y2colorSkeleton, x, y, loop);
							}
						}
						cube[x][y] = fire;
					}
				}
			}
			loop++;
		} while (foundBorderVoxel);
		long skeletonLength = 0;
		for (int x = 1; x < voxelresolution - 1; x++) {
			if (x2y2colorSkeleton.containsKey(x)) {
				HashMap<Integer, Integer> y2c = x2y2colorSkeleton.get(x);
				for (int y = 1; y < voxelresolution - 1; y++) {
					if (y2c.containsKey(y)) {
						Integer c = y2c.get(y);
						cube[x][y] = c;
						skeletonLength++;
					}
				}
			}
		}
		summaryResult.setNumericProperty(0, "RESULT_plant3d.skeleton.length", skeletonLength);
		if (distHorizontal != null) {
			double corr = realMarkerDistHorizontal / distHorizontal.getValue();
			summaryResult.setNumericProperty(0, "RESULT_plant3d.skeleton.length.norm",
					skeletonLength * corr);
		}
		
		return new ImageOperation(cube);
	}
	
	private void addSkeleton(HashMap<Integer, HashMap<Integer, Integer>> x2y2colorSkeleton, int x, int y, int c) {
		if (!x2y2colorSkeleton.containsKey(x))
			x2y2colorSkeleton.put(x, new HashMap<Integer, Integer>());
		x2y2colorSkeleton.get(x).put(y, c);
	}
}
