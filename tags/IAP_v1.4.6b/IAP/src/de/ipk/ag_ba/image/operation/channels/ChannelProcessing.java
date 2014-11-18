package de.ipk.ag_ba.image.operation.channels;

import java.awt.Color;

import de.ipk.ag_ba.image.operation.ColorSpaceConverter;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Provides easy to use channel splitting operations.
 * 
 * @author klukas
 */
public class ChannelProcessing {
	
	private final int[] imageAs1dArray;
	private final int BACKGROUND_COLORint = ImageOperation.BACKGROUND_COLORint;
	int width;
	int height;
	
	public ChannelProcessing(int[] imageAs1dArray, int width, int height) {
		this.imageAs1dArray = imageAs1dArray;
		this.width = width;
		this.height = height;
	}
	
	public ImageOperation get(Channel c) {
		switch (c) {
			case HSV_H:
				return getH();
			case HSV_S:
				return getS();
			case HSV_V:
				return getV();
			case LAB_B:
				return getLabB();
			case LAB_A:
				return getLabA();
			case LAB_L:
				return getLabL();
			case RGB_B:
				return getB();
			case RGB_G:
				return getG();
			case RGB_R:
				return getR();
			case XYZ_X:
				return getX();
			case XYZ_Y:
				return getY();
			case XYZ_Z:
				return getZ();
			default:
				break;
		}
		throw new UnsupportedOperationException("Channel not implemented.");
	}
	
	private ImageOperation getZ() {
		int[] in = imageAs1dArray;
		int[] out = new int[in.length];
		int c, r, g, b;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < in.length; i++) {
			c = in[i];
			if (c == BACKGROUND_COLORint) {
				out[i] = BACKGROUND_COLORint;
				continue;
			}
			r = (c & 0xff0000) >> 16;
			g = (c & 0x00ff00) >> 8;
			b = c & 0x0000ff;
			
			double[] xyz = cspc.RGBtoXYZ(r, g, b);
			r = (int) (xyz[2] * 2.55);
			
			out[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((r & 0xFF) << 8) | ((r & 0xFF) << 0);
		}
		return new Image(width, height, out).io();
	}
	
	private ImageOperation getY() {
		int[] in = imageAs1dArray;
		int[] out = new int[in.length];
		int c, r, g, b;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < in.length; i++) {
			c = in[i];
			if (c == BACKGROUND_COLORint) {
				out[i] = BACKGROUND_COLORint;
				continue;
			}
			r = (c & 0xff0000) >> 16;
			g = (c & 0x00ff00) >> 8;
			b = c & 0x0000ff;
			
			double[] xyz = cspc.RGBtoXYZ(r, g, b);
			r = (int) (xyz[1] * 2.55);
			
			out[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((r & 0xFF) << 8) | ((r & 0xFF) << 0);
		}
		return new Image(width, height, out).io();
	}
	
	private ImageOperation getX() {
		int[] in = imageAs1dArray;
		int[] out = new int[in.length];
		int c, r, g, b;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < in.length; i++) {
			c = in[i];
			if (c == BACKGROUND_COLORint) {
				out[i] = BACKGROUND_COLORint;
				continue;
			}
			r = (c & 0xff0000) >> 16;
			g = (c & 0x00ff00) >> 8;
			b = c & 0x0000ff;
			
			double[] xyz = cspc.RGBtoXYZ(r, g, b);
			r = (int) (xyz[0] * 2.55);
			
			out[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((r & 0xFF) << 8) | ((r & 0xFF) << 0);
		}
		return new Image(width, height, out).io();
	}
	
	/**
	 * @return A gray image composed from the R channel.
	 */
	public ImageOperation getR() {
		int[] in = imageAs1dArray;
		int[] out = new int[in.length];
		int c, r, g, b;
		for (int i = 0; i < in.length; i++) {
			c = in[i];
			if (c == BACKGROUND_COLORint) {
				out[i] = BACKGROUND_COLORint;
				continue;
			}
			r = (c & 0xff0000) >> 16;
			g = r;
			b = r;
			
			out[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new Image(width, height, out).io();
	}
	
	/**
	 * @return A gray image composed from the G channel.
	 */
	public ImageOperation getG() {
		int[] in = imageAs1dArray;
		int[] out = new int[in.length];
		int c, r, g, b;
		for (int i = 0; i < in.length; i++) {
			c = in[i];
			if (c == BACKGROUND_COLORint) {
				out[i] = BACKGROUND_COLORint;
				continue;
			}
			g = (c & 0x00ff00) >> 8;
			
			r = g;
			b = g;
			
			out[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new Image(width, height, out).io();
	}
	
	/**
	 * @return A gray image composed from the B channel.
	 */
	public ImageOperation getB() {
		int[] in = imageAs1dArray;
		int[] out = new int[in.length];
		int c, r, g, b;
		for (int i = 0; i < in.length; i++) {
			c = in[i];
			if (c == BACKGROUND_COLORint) {
				out[i] = BACKGROUND_COLORint;
				continue;
			}
			b = c & 0x0000ff;
			
			r = b;
			g = b;
			
			out[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new Image(width, height, out).io();
	}
	
	public ImageOperation getLabL() {
		int[] out = new int[imageAs1dArray.length];
		float[][][] lab = ImageOperation.getLabCubeInstance();
		for (int px = 0; px < imageAs1dArray.length; px++) {
			int c = imageAs1dArray[px];
			if (c == BACKGROUND_COLORint) {
				out[px] = BACKGROUND_COLORint;
				continue;
			}
			
			int r = ((c & 0xff0000) >> 16); // R 0..1
			int g = ((c & 0x00ff00) >> 8); // G 0..1
			int b = (c & 0x0000ff); // B 0..1
			
			float Li = (int) lab[r][g][b];
			
			r = (int) Li;
			g = r;
			b = r;
			
			out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new ImageOperation(out, width, height);
	}
	
	public ImageOperation getLabA() {
		int[] out = new int[imageAs1dArray.length];
		float[][][] lab = ImageOperation.getLabCubeInstance();
		for (int px = 0; px < imageAs1dArray.length; px++) {
			int c = imageAs1dArray[px];
			if (c == BACKGROUND_COLORint) {
				out[px] = BACKGROUND_COLORint;
				continue;
			}
			
			int r = ((c & 0xff0000) >> 16); // R 0..1
			int g = ((c & 0x00ff00) >> 8); // G 0..1
			int b = (c & 0x0000ff); // B 0..1
			
			float ai = (int) lab[r][g][b + 256];
			
			r = (int) ai;
			g = r;
			b = r;
			
			out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new ImageOperation(out, width, height);
	}
	
	public ImageOperation getLabB() {
		int[] out = new int[imageAs1dArray.length];
		float[][][] lab = ImageOperation.getLabCubeInstance();
		for (int px = 0; px < imageAs1dArray.length; px++) {
			int c = imageAs1dArray[px];
			if (c == BACKGROUND_COLORint) {
				out[px] = BACKGROUND_COLORint;
				continue;
			}
			
			int r = ((c & 0xff0000) >> 16); // R 0..1
			int g = ((c & 0x00ff00) >> 8); // G 0..1
			int b = (c & 0x0000ff); // B 0..1
			
			float bi = (int) lab[r][g][b + 512];
			
			r = (int) bi;
			g = r;
			b = r;
			
			out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new ImageOperation(out, width, height);
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
	
	public ImageOperation getH() {
		int[] out = new int[imageAs1dArray.length];
		float[] hsv = new float[3];
		for (int px = 0; px < imageAs1dArray.length; px++) {
			int c = imageAs1dArray[px];
			if (c == BACKGROUND_COLORint) {
				out[px] = BACKGROUND_COLORint;
				continue;
			}
			
			int r = ((c & 0xff0000) >> 16); // R 0..1
			int g = ((c & 0x00ff00) >> 8); // G 0..1
			int b = (c & 0x0000ff); // B 0..1
			
			Color.RGBtoHSB(r, g, b, hsv);
			
			float h = hsv[0];
			r = (int) (h * 255);
			g = r;
			b = r;
			
			out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new ImageOperation(out, width, height);
	}
	
	public ImageOperation getS() {
		int[] out = new int[imageAs1dArray.length];
		float[] hsv = new float[3];
		for (int px = 0; px < imageAs1dArray.length; px++) {
			int c = imageAs1dArray[px];
			if (c == BACKGROUND_COLORint) {
				out[px] = BACKGROUND_COLORint;
				continue;
			}
			
			int r = ((c & 0xff0000) >> 16); // R 0..1
			int g = ((c & 0x00ff00) >> 8); // G 0..1
			int b = (c & 0x0000ff); // B 0..1
			
			Color.RGBtoHSB(r, g, b, hsv);
			
			float s = hsv[1];
			r = (int) (s * 255);
			g = r;
			b = r;
			
			out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new ImageOperation(out, width, height);
	}
	
	public ImageOperation getV() {
		int[] out = new int[imageAs1dArray.length];
		float[] hsv = new float[3];
		for (int px = 0; px < imageAs1dArray.length; px++) {
			int c = imageAs1dArray[px];
			if (c == BACKGROUND_COLORint) {
				out[px] = BACKGROUND_COLORint;
				continue;
			}
			
			int r = ((c & 0xff0000) >> 16); // R 0..1
			int g = ((c & 0x00ff00) >> 8); // G 0..1
			int b = (c & 0x0000ff); // B 0..1
			
			Color.RGBtoHSB(r, g, b, hsv);
			
			float v = hsv[2];
			r = (int) (v * 255);
			g = r;
			b = r;
			
			out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new ImageOperation(out, width, height);
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
