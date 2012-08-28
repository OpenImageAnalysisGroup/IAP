package de.ipk.ag_ba.image.operations;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

public class SkeletonizeProcessor {
	
	private final ImageOperation image;
	
	public SkeletonizeProcessor(ImageOperation image) {
		this.image = image;
	}
	
	/**
	 * @author klukas
	 *         The "fire" burns down each solid voxel with fixed speed.
	 * @return A image where each int value is either BACKGROUND_COLORint (no skeleton pixel here) or a number greater or equal to 1, which denotes
	 *         the minimum distance of the skeleton pixel to the free space in the input cube. Therefore, each pixel denotes a width at this
	 *         particular part of the input voxel.
	 */
	public ImageOperation calculateThicknessSkeleton(boolean calcDistanceTrueOrNormalColoredSkeletonFalse) {
		return calculateThicknessSkeleton(calcDistanceTrueOrNormalColoredSkeletonFalse, ImageOperation.BACKGROUND_COLORint);
	}
	
	public ImageOperation calculateThicknessSkeleton(boolean calcDistanceTrueOrNormalColoredSkeletonFalse, int back) {
		
		int[][] cube = image.getImageAs2dArray();
		
		int fire = back;
		HashMap<Integer, HashMap<Integer, Integer>> x2y2colorSkeleton = new HashMap<Integer, HashMap<Integer, Integer>>();
		boolean foundBorderVoxel = false;
		int loop = 1;
		
		int voxelresolutionX = cube.length;
		int voxelresolutionY = cube[0].length;
		
		boolean debug = true;
		
		FlexibleImageStack fis = debug ? new FlexibleImageStack() : null;
		
		do {
			foundBorderVoxel = false;
			LinkedList<Integer> borderX = new LinkedList<Integer>();
			LinkedList<Integer> borderY = new LinkedList<Integer>();
			
			for (int x = 1; x < voxelresolutionX - 1; x++) {
				for (int y = 1; y < voxelresolutionY - 1; y++) {
					int c = cube[x][y];
					boolean filled = c != fire;
					if (filled) {
						boolean left = cube[x - 1][y] != fire;
						boolean right = cube[x + 1][y] != fire;
						boolean above = cube[x][y - 1] != fire;
						boolean below = cube[x][y + 1] != fire;
						
						boolean tl = cube[x - 1][y - 1] != fire;
						boolean tr = cube[x + 1][y - 1] != fire;
						boolean bl = cube[x - 1][y + 1] != fire;
						boolean br = cube[x + 1][y + 1] != fire;
						
						if (!left || !right || !above || !below || !tl || !tr || !bl || !br) {
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
							if (tl)
								filledSurrounding++;
							if (tr)
								filledSurrounding++;
							if (bl)
								filledSurrounding++;
							if (br)
								filledSurrounding++;
							
							if (filledSurrounding <= 4) {
								if (!calcDistanceTrueOrNormalColoredSkeletonFalse)
									addSkeleton(x2y2colorSkeleton, x, y, c);
								else {
									addSkeleton(x2y2colorSkeleton, x, y, new Color(loop, loop, loop).getRGB());
								}
							}
						}
						if (!(left && right && above && below)) { // && tl && tr && bl && br)) {
							borderX.add(x);
							borderY.add(y);
						}
					}
				}
			}
			for (int i = 0; i < borderX.size(); i++) {
				cube[borderX.get(i)][borderY.get(i)] = fire;
			}
			borderX.clear();
			borderY.clear();
			if (fis != null)
				fis.addImage("Loop " + loop, new FlexibleImage(cube).copy());
			loop++;
		} while (foundBorderVoxel);
		if (fis != null)
			fis.print("LOOP");
		for (int x = 1; x < voxelresolutionX - 1; x++) {
			if (x2y2colorSkeleton.containsKey(x)) {
				HashMap<Integer, Integer> y2c = x2y2colorSkeleton.get(x);
				for (int y = 1; y < voxelresolutionY - 1; y++) {
					if (y2c.containsKey(y)) {
						Integer c = y2c.get(y);
						cube[x][y] = c;
					}
				}
			}
		}
		return new ImageOperation(cube);
	}
	
	private void addSkeleton(HashMap<Integer, HashMap<Integer, Integer>> x2y2colorSkeleton, int x, int y, int c) {
		if (!x2y2colorSkeleton.containsKey(x))
			x2y2colorSkeleton.put(x, new HashMap<Integer, Integer>());
		x2y2colorSkeleton.get(x).put(y, c);
	}
}
