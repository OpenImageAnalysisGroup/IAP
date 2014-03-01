package de.ipk.ag_ba.image.operation;

/**
 * @author Christian Klukas
 */
public class ArrayUtil {
	
	public static int[] get1d(int[][] img) {
		int w = img.length;
		int h = img[0].length;
		int[] image = new int[w * h];
		int idx = 0;
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				image[idx++] = img[x][y];
		return image;
	}
	
	public static int[][] get2d(int w, int h, int[] as1a) {
		if (w * h != as1a.length)
			throw new IllegalArgumentException("width * height not equal to source length");
		
		int[][] image = new int[w][h];
		int idx = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				image[x][y] = as1a[idx++];
			}
		}
		return image;
	}
	
}
