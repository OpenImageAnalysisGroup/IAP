package de.ipk.ag_ba.image.operation.skeleton;

import java.util.HashMap;
import java.util.LinkedList;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

public class SkeletonizeProcessor {
	
	private final ImageOperation image;
	
	public SkeletonizeProcessor(ImageOperation image) {
		this.image = image;
	}
	
	/**
	 * @author klukas The "fire" burns down each solid voxel with fixed speed.
	 * @return A image where each int value is either BACKGROUND_COLORint (no
	 *         skeleton pixel here) or a number greater or equal to 1, which
	 *         denotes the minimum distance of the skeleton pixel to the free
	 *         space in the input cube. Therefore, each pixel denotes a width at
	 *         this particular part of the input voxel.
	 */
	public ImageOperation calculateDistanceToBorder(
			boolean calcDistanceTrueOrNormalColoredSkeletonFalse, int back) {
		
		int[][] img = image.getAs2D();
		
		int fire = back;
		HashMap<Integer, HashMap<Integer, Integer>> x2y2colorSkeleton = new HashMap<Integer, HashMap<Integer, Integer>>();
		boolean foundBorderVoxel = false;
		int loop = 1;
		
		int voxelresolutionX = img.length;
		int voxelresolutionY = img[0].length;
		
		boolean debug = false;
		
		ImageStack fis = debug ? new ImageStack() : null;
		
		do {
			foundBorderVoxel = false;
			LinkedList<Integer> borderX = new LinkedList<Integer>();
			LinkedList<Integer> borderY = new LinkedList<Integer>();
			
			for (int x = 1; x < voxelresolutionX - 1; x++) {
				for (int y = 1; y < voxelresolutionY - 1; y++) {
					int c = img[x][y];
					boolean filled = c != fire;
					if (filled) {
						boolean left = img[x - 1][y] != fire;
						boolean right = img[x + 1][y] != fire;
						boolean above = img[x][y - 1] != fire;
						boolean below = img[x][y + 1] != fire;
						
						// boolean tl = img[x - 1][y - 1] != fire;
						// boolean tr = img[x + 1][y - 1] != fire;
						// boolean bl = img[x - 1][y + 1] != fire;
						// boolean br = img[x + 1][y + 1] != fire;
						
						int filledSurrounding = 0;
						// border voxel
						if (left) {
							filledSurrounding++;
						}
						if (right) {
							filledSurrounding++;
						}
						if (above) {
							filledSurrounding++;
						}
						if (below) {
							filledSurrounding++;
						}
						if (filledSurrounding < 4) {
							if (!calcDistanceTrueOrNormalColoredSkeletonFalse) {
								addSkeleton(x2y2colorSkeleton, x, y, c);
							} else {
								addSkeleton(x2y2colorSkeleton, x, y, loop);
							}
							foundBorderVoxel = true;
							borderX.add(x);
							borderY.add(y);
						}
					}
				}
			}
			for (int i = 0; i < borderX.size(); i++) {
				int x = borderX.get(i);
				int y = borderY.get(i);
				img[x][y] = fire;
			}
			borderX.clear();
			borderY.clear();
			if (fis != null) {
				fis.addImage("Loop " + loop, new Image(img).copy());
			}
			loop++;
		} while (foundBorderVoxel);
		if (fis != null) {
			fis.show("LOOP");
		}
		for (int x = 1; x < voxelresolutionX - 1; x++) {
			if (x2y2colorSkeleton.containsKey(x)) {
				HashMap<Integer, Integer> y2c = x2y2colorSkeleton.get(x);
				for (int y = 1; y < voxelresolutionY - 1; y++) {
					if (y2c.containsKey(y)) {
						Integer c = y2c.get(y);
						img[x][y] = c;
					}
				}
			}
		}
		return new ImageOperation(img);
	}
	
	private void addSkeleton(
			HashMap<Integer, HashMap<Integer, Integer>> x2y2colorSkeleton,
			int x, int y, int c) {
		if (!x2y2colorSkeleton.containsKey(x))
			x2y2colorSkeleton.put(x, new HashMap<Integer, Integer>());
		x2y2colorSkeleton.get(x).put(y, c);
	}
	
	static int[] table =
	// 0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1
	{ 0, 0, 0, 1, 0, 0, 1, 3, 0, 0, 3, 1, 1, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0,
			2, 0, 2, 0, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 2, 2, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0,
			3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 2, 0, 0, 0, 3, 1, 0, 0,
			1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 1, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 1, 3, 0, 0, 1, 3,
			0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 2, 3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 1,
			0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0 };
	
	/**
	 * Uses a lookup table to repeatably removes pixels from the edges of
	 * objects in a binary image, reducing them to single pixel wide skeletons.
	 * Based on an a thinning algorithm by by Zhang and Suen (CACM, March 1984,
	 * 236-239). There is an entry in the table for each of the 256 possible 3x3
	 * neighborhood configurations. An entry of '1' means delete pixel on first
	 * pass, '2' means delete pixel on second pass, and '3' means delete on
	 * either pass. A graphical representation of the 256 neighborhoods indexed
	 * by the table is available at
	 * "http://imagej.nih.gov/ij/images/skeletonize-table.gif".
	 */
	
	public ImageOperation skeletonize(int bgColor) {
		int pass = 0;
		int pixelsRemoved;
		int[] pixels2 = image.getAs1D();
		do {
			pixelsRemoved = thin(pass++, table, pixels2, bgColor);
			pixelsRemoved = thin(pass++, table, pixels2, bgColor);
		} while (pixelsRemoved > 0);
		return new ImageOperation(pixels2, image.getWidth(), image.getHeight());
	}
	
	int thin(int pass, int[] table, int[] pixels2, int bgColor) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		
		int v, index, code;
		int offset, rowOffset = image.getWidth();
		int pixelsRemoved = 0;
		int yMax = image.getHeight() - 2;
		int yMin = 1;
		int width = image.getWidth();
		int xMin = 1;
		int xMax = image.getWidth() - 2;
		for (int y = yMin; y <= yMax; y++) {
			for (int x = xMin; x <= xMax; x++) {
				offset = x + y * width;
				p5 = pixels2[offset];
				v = p5;
				if (v != bgColor) {
					p1 = pixels2[offset - rowOffset - 1];
					p2 = pixels2[offset - rowOffset];
					p3 = pixels2[offset - rowOffset + 1];
					p4 = pixels2[offset - 1];
					p6 = pixels2[offset + 1];
					p7 = pixels2[offset + rowOffset - 1];
					p8 = pixels2[offset + rowOffset];
					if (offset + rowOffset + 1 >= pixels2.length) {
						System.out.println("Problem");
					}
					p9 = pixels2[offset + rowOffset + 1];
					index = 0;
					if (p1 != bgColor)
						index |= 1;
					if (p2 != bgColor)
						index |= 2;
					if (p3 != bgColor)
						index |= 4;
					if (p6 != bgColor)
						index |= 8;
					if (p9 != bgColor)
						index |= 16;
					if (p8 != bgColor)
						index |= 32;
					if (p7 != bgColor)
						index |= 64;
					if (p4 != bgColor)
						index |= 128;
					code = table[index];
					if ((pass & 1) == 1) { // odd pass
						if (code == 2 || code == 3) {
							v = bgColor;
							pixelsRemoved++;
						}
					} else { // even pass
						if (code == 1 || code == 3) {
							v = bgColor;
							pixelsRemoved++;
						}
					}
				}
				pixels2[offset++] = v;
			}
		}
		return pixelsRemoved;
	}
	
}
