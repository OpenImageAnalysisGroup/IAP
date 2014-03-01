package de.ipk.ag_ba.image.operation.channels;

import java.awt.Color;

import de.ipk.ag_ba.image.operation.ImageOperation;

/**
 * Provides easy to use channel splitting operations.
 * 
 * @author klukas
 */
public class ChannelProcessing {
	
	private final int[] imageAs1dArray;
	@SuppressWarnings("unused")
	private final int width;
	@SuppressWarnings("unused")
	private final int height;
	
	public ChannelProcessing(int[] imageAs1dArray, int width, int height) {
		this.imageAs1dArray = imageAs1dArray;
		this.width = width;
		this.height = height;
	}
	
	public float[][] getLAB() {
		float[][] res = new float[3][imageAs1dArray.length];
		float[][][] lab = ImageOperation.getLabCubeInstance();
		for (int px = 0; px < imageAs1dArray.length; px++) {
			int c = imageAs1dArray[px];
			
			int r = ((c & 0xff0000) >> 16); // R 0..1
			int g = ((c & 0x00ff00) >> 8); // G 0..1
			int b = (c & 0x0000ff); // B 0..1
			
			float Li = (int) lab[r][g][b];
			float ai = (int) lab[r][g][b + 256];
			float bi = (int) lab[r][g][b + 512];
			
			res[0][px] = Li;
			res[1][px] = ai;
			res[2][px] = bi;
		}
		return res;
	}
	
	public float[][] getHSV() {
		float[][] res = new float[3][imageAs1dArray.length];
		float[] hsv = new float[3];
		for (int px = 0; px < imageAs1dArray.length; px++) {
			int c = imageAs1dArray[px];
			
			int r = ((c & 0xff0000) >> 16); // R 0..1
			int g = ((c & 0x00ff00) >> 8); // G 0..1
			int b = (c & 0x0000ff); // B 0..1
			
			Color.RGBtoHSB(r, g, b, hsv);
			
			res[0][px] = hsv[0];
			res[1][px] = hsv[1];
			res[2][px] = hsv[2];
		}
		return res;
	}
	
	public static float[] getLAB(Color color) {
		int c = color.getRGB();
		
		int r = ((c & 0xff0000) >> 16); // R 0..1
		int g = ((c & 0x00ff00) >> 8); // G 0..1
		int b = (c & 0x0000ff); // B 0..1
		
		float[][][] lab = ImageOperation.getLabCubeInstance();
		
		float Li = (int) lab[r][g][b];
		float ai = (int) lab[r][g][b + 256];
		float bi = (int) lab[r][g][b + 512];
		
		return new float[] { Li, ai, bi };
	}
	
	public static float[] getHSV(Color color) {
		int c = color.getRGB();
		
		int r = ((c & 0xff0000) >> 16); // R 0..1
		int g = ((c & 0x00ff00) >> 8); // G 0..1
		int b = (c & 0x0000ff); // B 0..1
		
		float[] hsv = new float[3];
		
		Color.RGBtoHSB(r, g, b, hsv);
		
		return hsv;
	}
}
