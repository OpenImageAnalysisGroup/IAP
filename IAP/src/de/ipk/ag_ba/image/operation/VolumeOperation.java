package de.ipk.ag_ba.image.operation;

import info.StopWatch;

import java.util.HashMap;

import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d.ThreeDmodelGenerator;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.ByteShortIntArray;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;

public class VolumeOperation {
	
	private final LoadedVolume volume;
	
	public VolumeOperation(LoadedVolume volume) {
		this.volume = volume;
	}
	
	/**
	 * @author klukas
	 *         The "fire" burns down each solid voxel with fixed speed.
	 *         Warning: changes this VolumeOperation instance and modifes actual instance, and returns it.
	 */
	public VolumeOperation createSkeletonUsingUniformBurningSpeed(BlockResultSet summaryResult, int voxelresolution, ThreeDmodelGenerator mg,
			BlockProperty distHorizontal,
			double realMarkerDistHorizontal) {
		return calculateThicknessSkeleton(summaryResult, voxelresolution, mg, distHorizontal, realMarkerDistHorizontal, false);
	}
	
	/**
	 * @author klukas
	 *         The "fire" burns down each solid voxel with fixed speed.
	 *         Warning: changes this VolumeOperation instance and modifes actual instance, and returns it.
	 * @return A int cube, where each int value is either BACKGROUND_COLORint (no skeleton pixel here) or a number greater or equal to 1, which denotes
	 *         the minimum distance of the skeleton pixel to the free space in the input cube. Therefore, each pixel denotes a width at this
	 *         particular part of the input voxel.
	 */
	public VolumeOperation calculateThicknessSkeleton(BlockResultSet summaryResult, int voxelresolution, ThreeDmodelGenerator mg,
			BlockProperty distHorizontal,
			double realMarkerDistHorizontal, boolean calcDistanceTrueOrNormalColoredSkeletonFalse) {
		
		int[][][] cube = copy(volume.getLoadedVolume().getIntArray());
		
		int fire = ImageOperation.BACKGROUND_COLORint;
		StopWatch s = new StopWatch(SystemAnalysis.getCurrentTime() + ">Create simple 3D skeleton", false);
		HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> x2y2z2colorSkeleton = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
		boolean foundBorderVoxel = false;
		int loop = 1;
		do {
			foundBorderVoxel = false;
			for (int x = 1; x < voxelresolution - 1; x++) {
				for (int y = 1; y < voxelresolution - 1; y++) {
					for (int z = 1; z < voxelresolution - 1; z++) {
						int c = cube[x][y][z];
						boolean filled = c != fire;
						if (filled) {
							boolean left = cube[x - 1][y][z] != fire;
							boolean right = cube[x + 1][y][z] != fire;
							boolean above = cube[x][y - 1][z] != fire;
							boolean below = cube[x][y + 1][z] != fire;
							boolean behind = cube[x][y][z + 1] != fire;
							boolean before = cube[x][y][z - 1] != fire;
							if (!left || !right || !above || !below || !behind || !before) {
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
								if (behind)
									filledSurrounding++;
								if (before)
									filledSurrounding++;
								if (filledSurrounding <= 2) {
									if (!calcDistanceTrueOrNormalColoredSkeletonFalse)
										addSkeleton(x2y2z2colorSkeleton, x, y, z, c);
									else
										addSkeleton(x2y2z2colorSkeleton, x, y, z, loop);
								}
							}
							cube[x][y][z] = fire;
						}
					}
				}
			}
			loop++;
		} while (foundBorderVoxel);
		long skeletonLength = 0;
		for (int x = 1; x < voxelresolution - 1; x++) {
			if (x2y2z2colorSkeleton.containsKey(x)) {
				HashMap<Integer, HashMap<Integer, Integer>> y2z = x2y2z2colorSkeleton.get(x);
				for (int y = 1; y < voxelresolution - 1; y++) {
					if (y2z.containsKey(y)) {
						HashMap<Integer, Integer> z2c = y2z.get(y);
						for (int z : z2c.keySet()) {
							Integer c = z2c.get(z);
							cube[x][y][z] = c;
							skeletonLength++;
						}
					}
				}
			}
		}
		summaryResult.setNumericProperty(0, 
				"RESULT_plant3d.skeleton.length", skeletonLength, "px");
		if (distHorizontal != null) {
			double corr = realMarkerDistHorizontal / distHorizontal.getValue();
			summaryResult.setNumericProperty(0, "RESULT_plant3d.skeleton.length.norm",
					skeletonLength * corr, "mm");
		}
		
		LoadedVolumeExtension lve = new LoadedVolumeExtension(volume);
		lve.setVolume(new ByteShortIntArray(cube));
		String n = lve.getURL().getFileName();
		if (n == null)
			n = SystemAnalysis.getCurrentTime() + " (NO VOLUME NAME, NULL ERROR 1)";
		n = StringManipulationTools.stringReplace(n, ".argb_volume", "");
		lve.getURL().setFileName(n + ".(plant skeleton).argb_volume");
		summaryResult.setVolume("RESULT_plant_skeleton", lve);
		
		s.printTime();
		
		volume.getLoadedVolume().setIntArray(cube);
		
		return this;
	}
	
	private int[][][] copy(int[][][] rgbCube) {
		int maxVoxelPerSideX = rgbCube.length;
		int maxVoxelPerSideY = rgbCube[0].length;
		int maxVoxelPerSideZ = rgbCube[0][0].length;
		int[][][] res = new int[maxVoxelPerSideX][maxVoxelPerSideY][maxVoxelPerSideZ];
		for (int x = 0; x < maxVoxelPerSideX; x++)
			for (int y = 0; y < maxVoxelPerSideY; y++)
				for (int z = 0; z < maxVoxelPerSideZ; z++)
					res[x][y][z] = rgbCube[x][y][z];
		return res;
	}
	
	private void addSkeleton(HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> x2y2z2colorSkeleton, int x, int y, int z, int c) {
		if (!x2y2z2colorSkeleton.containsKey(x))
			x2y2z2colorSkeleton.put(x, new HashMap<Integer, HashMap<Integer, Integer>>());
		if (!x2y2z2colorSkeleton.get(x).containsKey(y))
			x2y2z2colorSkeleton.get(x).put(y, new HashMap<Integer, Integer>());
		x2y2z2colorSkeleton.get(x).get(y).put(z, c);
	}
	
}
