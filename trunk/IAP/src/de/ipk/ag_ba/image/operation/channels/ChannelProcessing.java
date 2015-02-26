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
	private final float[] floatR, floatG, floatB;
	private final int BACKGROUND_COLORint = ImageOperation.BACKGROUND_COLORint;
	int width;
	int height;
	
	public ChannelProcessing(int[] imageAs1dArray, int width, int height) {
		this.imageAs1dArray = imageAs1dArray;
		this.floatR = null;
		this.floatG = null;
		this.floatB = null;
		this.width = width;
		this.height = height;
	}
	
	public ChannelProcessing(float[] floatR, float[] floatG, float[] floatB, int width, int height, float divistorFor8bitRangeTarget) {
		this.imageAs1dArray = null;
		this.floatR = new float[floatR.length];
		this.floatG = new float[floatG.length];
		this.floatB = new float[floatB.length];
		
		assert floatR.length == floatG.length;
		assert floatG.length == floatB.length;
		
		for (int idx = 0; idx < floatR.length; idx++) {
			this.floatR[idx] = floatR[idx] / divistorFor8bitRangeTarget;
			this.floatG[idx] = floatG[idx] / divistorFor8bitRangeTarget;
			this.floatB[idx] = floatB[idx] / divistorFor8bitRangeTarget;
		}
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
		if (imageAs1dArray != null) {
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
		} else {
			return getXYZ(Channel.XYZ_Z);
		}
	}
	
	private ImageOperation getXYZ(Channel c) {
		float[] out = getXYZfloatArray(c);
		return new Image(width, height, out).io();
	}
	
	private float[] getXYZfloatArray(Channel c) {
		float[] out = new float[floatR.length];
		float r, g, b, res;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < floatR.length; i++) {
			r = floatR[i];
			g = floatG[i];
			b = floatB[i];
			if (Float.isNaN(r) || Float.isNaN(g) || Float.isNaN(b)) {
				out[i] = Float.NaN;
				continue;
			}
			
			double[] xyz = cspc.RGBtoXYZ(r, g, b);
			int idx = -1;
			switch (c) {
				case XYZ_X:
					idx = 0;
					break;
				case XYZ_Y:
					idx = 1;
					break;
				case XYZ_Z:
					idx = 2;
					break;
				default:
					idx = -1;
					break;
			}
			
			res = (float) xyz[idx];
			
			out[i] = res;
		}
		return out;
	}
	
	private ImageOperation getY() {
		if (imageAs1dArray != null) {
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
		} else {
			return getXYZ(Channel.XYZ_Y);
		}
		
	}
	
	private ImageOperation getX() {
		if (imageAs1dArray != null) {
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
		} else {
			return getXYZ(Channel.XYZ_X);
		}
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
		if (imageAs1dArray != null) {
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
		} else {
			return getLab(Channel.LAB_L);
		}
	}
	
	private ImageOperation getLab(Channel labC) {
		return new Image(width, height, getLabFloatArray(labC)).io();
	}
	
	private float[] getLabFloatArray(Channel labC) {
		float[] x = getXYZfloatArray(Channel.XYZ_X);
		float[] y = getXYZfloatArray(Channel.XYZ_X);
		float[] z = getXYZfloatArray(Channel.XYZ_X);
		
		float[] out = new float[x.length];
		float xv, yv, zv, res;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < x.length; i++) {
			xv = x[i];
			yv = y[i];
			zv = z[i];
			if (Float.isNaN(xv) || Float.isNaN(yv) || Float.isNaN(zv)) {
				out[i] = Float.NaN;
				continue;
			}
			
			double[] lab = cspc.XYZtoLAB(xv, yv, zv);
			int idx = -1;
			switch (labC) {
				case LAB_L:
					idx = 0;
					break;
				case LAB_A:
					idx = 1;
					break;
				case LAB_B:
					idx = 2;
					break;
				default:
					idx = -1;
					break;
			}
			
			res = (float) lab[idx];
			
			out[i] = res;
		}
		return out;
	}
	
	public ImageOperation getLabA() {
		if (imageAs1dArray != null) {
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
				
				float ai = lab[r][g][b + 256];
				
				r = (int) ai;
				g = r;
				b = r;
				
				out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
			}
			return new ImageOperation(out, width, height);
		} else {
			return getLab(Channel.LAB_A);
		}
	}
	
	public ImageOperation getLabB() {
		if (imageAs1dArray != null) {
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
		} else {
			return getLab(Channel.LAB_B);
		}
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
		if (imageAs1dArray != null) {
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
		} else {
			return getHSV(Channel.HSV_H);
		}
	}
	
	private ImageOperation getHSV(Channel c) {
		return new Image(width, height, getHSVarray(c)).io();
	}
	
	private float[] getHSVarray(Channel c) {
		float[] out = new float[floatR.length];
		float[] hsv = new float[3];
		for (int px = 0; px < imageAs1dArray.length; px++) {
			float rv = floatR[px];
			float gv = floatR[px];
			float bv = floatR[px];
			if (Float.isNaN(rv) || Float.isNaN(gv) || Float.isNaN(bv)) {
				out[px] = Float.NaN;
				continue;
			}
			
			ColorFloatRGB.RGBtoHSB(rv, gv, bv, hsv);
			int idx;
			switch (c) {
				case HSV_H:
					idx = 0;
					break;
				case HSV_S:
					idx = 1;
					break;
				case HSV_V:
					idx = 2;
					break;
				default:
					idx = -1;
					break;
			}
			out[px] = hsv[idx];
		}
		return out;
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
