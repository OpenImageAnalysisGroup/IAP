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
	
	public static float[] get1d(float[][] img) {
		int w = img.length;
		int h = img[0].length;
		float[] image = new float[w * h];
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
	
	public static double[][][] half(double[][][] in) {
		int len = in[0][0].length;
		int newlen = len / 2;
		double[][][] res = new double[newlen][newlen][newlen];
		for (int x = 0; x < len; x++)
			for (int y = 0; y < len; y++)
				for (int z = 0; z < len; z++) {
					res[x / 2][y / 2][z / 2] = res[x / 2][y / 2][z / 2] + in[x][y][z];
				}
		
		for (int x = 0; x < newlen; x++)
			for (int y = 0; y < newlen; y++)
				for (int z = 0; z < newlen; z++) {
					res[x][y][z] = res[x][y][z] / 4;
				}
		
		return res;
	}
}
