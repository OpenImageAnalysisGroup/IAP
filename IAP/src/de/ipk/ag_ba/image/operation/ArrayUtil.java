package de.ipk.ag_ba.image.operation;

/**
 * @author Christian Klukas
 */
public class ArrayUtil {
	
	public static int[] get1d(int[][] img) {
		int h = img.length, w = img[0].length;
		int[] res = new int[(h * w)];
		for (int i = 0; i < h; i++)
			System.arraycopy(img[i], 0, res, (i * w), w);
		return res;
	}
	
	public static int[][] get2d(int width, int height, int[] as1a) {
		int[][] res = new int[width][height];
		for (int i = 0; i < height; i++)
			System.arraycopy(as1a, (i * width), as1a[i], 0, width);
		
		return res;
	}
	
}
