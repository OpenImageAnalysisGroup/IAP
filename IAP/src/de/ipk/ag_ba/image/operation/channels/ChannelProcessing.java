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
	private float[] floatR;
	private float[] floatG;
	private float[] floatB;
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
	
	public ChannelProcessing(float[] floatR, float[] floatG, float[] floatB, int width, int height, float divisorFor8bitRangeTarget) {
		this.imageAs1dArray = null;
		this.floatR = new float[floatR.length];
		this.floatG = new float[floatG.length];
		this.floatB = new float[floatB.length];
		
		assert floatR.length == floatG.length;
		assert floatG.length == floatB.length;
		
		for (int idx = 0; idx < floatR.length; idx++) {
			this.floatR[idx] = floatR[idx] / divisorFor8bitRangeTarget;
			this.floatG[idx] = floatG[idx] / divisorFor8bitRangeTarget;
			this.floatB[idx] = floatB[idx] / divisorFor8bitRangeTarget;
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
			case xyY_x:
				return get_xyY_x();
			case xyY_y:
				return get_xyY_y();
			case xyY_Y:
				return getY();
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
	
	private double[] getXYZarray(Channel c) {
		if (floatR == null)
			setFloatFromInt();
		double[] out = new double[floatR.length];
		float r, g, b, res;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < floatR.length; i++) {
			r = floatR[i];
			g = floatG[i];
			b = floatB[i];
			if (r == Float.MAX_VALUE || g == Float.MAX_VALUE || b == Float.MAX_VALUE) {
				out[i] = Float.MAX_VALUE;
				continue;
			}
			
			double[] xyz = cspc.RGBtoXYZ(r / 255d, g / 255d, b / 255d);
			double[] xyz_xyYx_xyYy = new double[xyz.length + 2];
			xyz_xyYx_xyYy[0] = xyz[0];
			xyz_xyYx_xyYy[1] = xyz[1];
			xyz_xyYx_xyYy[2] = xyz[2];
			xyz_xyYx_xyYy[3] = xyz[0] / (xyz[0] + xyz[1] + xyz[2]);
			xyz_xyYx_xyYy[4] = xyz[1] / (xyz[0] + xyz[1] + xyz[2]);
			
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
				case xyY_x:
					idx = 3;
					break;
				case xyY_y:
					idx = 4;
					break;
				default:
					idx = -1;
					break;
			}
			
			res = (float) xyz_xyYx_xyYy[idx];
			
			out[i] = res;
		}
		return out;
	}
	
	private float[] getXYZfloatArray(Channel c) {
		if (floatR == null)
			setFloatFromInt();
		float[] out = new float[floatR.length];
		float r, g, b, res;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < floatR.length; i++) {
			r = floatR[i];
			g = floatG[i];
			b = floatB[i];
			if (r == Float.MAX_VALUE || g == Float.MAX_VALUE || b == Float.MAX_VALUE) {
				out[i] = Float.MAX_VALUE;
				continue;
			}
			
			double[] xyz = cspc.RGBtoXYZ(r / 255d, g / 255d, b / 255d);
			double[] xyz_xyYx_xyYy = new double[xyz.length + 2];
			xyz_xyYx_xyYy[0] = xyz[0];
			xyz_xyYx_xyYy[1] = xyz[1];
			xyz_xyYx_xyYy[2] = xyz[2];
			xyz_xyYx_xyYy[3] = xyz[0] / (xyz[0] + xyz[1] + xyz[2]);
			xyz_xyYx_xyYy[4] = xyz[1] / (xyz[0] + xyz[1] + xyz[2]);
			
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
				case xyY_x:
					idx = 3;
					break;
				case xyY_y:
					idx = 4;
					break;
				default:
					idx = -1;
					break;
			}
			
			res = (float) xyz_xyYx_xyYy[idx];
			
			out[i] = res;
		}
		return out;
	}
	
	public void setFloatFromInt() {
		this.floatR = getR().getImage().getAs1float(false);
		this.floatG = getG().getImage().getAs1float(false);
		this.floatB = getB().getImage().getAs1float(false);
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
	
	private ImageOperation get_xyY_x() {
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
				double x = xyz[0];
				double y = xyz[1];
				double z = xyz[2];
				double xyY_x = x / (x + y + z);
				r = (int) (xyY_x * 256d);
				if (r == 256)
					r = 255;
				
				out[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((r & 0xFF) << 8) | ((r & 0xFF) << 0);
			}
			return new Image(width, height, out).io();
		} else {
			return getXYZ(Channel.xyY_x);
		}
	}
	
	private ImageOperation get_xyY_y() {
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
				double x = xyz[0];
				double y = xyz[1];
				double z = xyz[2];
				double xyY_y = y / (x + y + z);
				r = (int) (xyY_y * 256d);
				if (r == 256)
					r = 255;
				
				out[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((r & 0xFF) << 8) | ((r & 0xFF) << 0);
			}
			return new Image(width, height, out).io();
		} else {
			return getXYZ(Channel.xyY_y);
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
	
	public float[] getLabFloatArray(Channel labC) {
		float[] x = getXYZfloatArray(Channel.XYZ_X);
		float[] y = getXYZfloatArray(Channel.XYZ_Y);
		float[] z = getXYZfloatArray(Channel.XYZ_Z);
		
		float[] out = new float[x.length];
		float xv, yv, zv, res;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < x.length; i++) {
			xv = x[i];
			yv = y[i];
			zv = z[i];
			if (xv == Float.MAX_VALUE || yv == Float.MAX_VALUE || zv == Float.MAX_VALUE) {
				out[i] = Float.MAX_VALUE;
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
	
	public double[][] getLabArrays() {
		double[] x = getXYZarray(Channel.XYZ_X);
		double[] y = getXYZarray(Channel.XYZ_Y);
		double[] z = getXYZarray(Channel.XYZ_Z);
		
		double xv, yv, zv;
		ColorSpaceConverter cspc = new ColorSpaceConverter();
		for (int i = 0; i < x.length; i++) {
			xv = x[i];
			yv = y[i];
			zv = z[i];
			if (xv == Float.MAX_VALUE || yv == Float.MAX_VALUE || zv == Float.MAX_VALUE)
				continue;
			
			double[] lab = cspc.XYZtoLAB(xv, yv, zv);
			
			x[i] = lab[0];
			y[i] = lab[1];
			z[i] = lab[2];
			
		}
		return new double[][] { x, y, z };
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
		for (int px = 0; px < floatR.length; px++) {
			float rv = floatR[px];
			float gv = floatG[px];
			float bv = floatB[px];
			if (rv == Float.MAX_VALUE || gv == Float.MAX_VALUE || bv == Float.MAX_VALUE) {
				out[px] = Float.MAX_VALUE;
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
				
				float s = hsv[1];
				r = (int) (s * 255);
				g = r;
				b = r;
				
				out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
			}
			return new ImageOperation(out, width, height);
		} else {
			return getHSV(Channel.HSV_S);
		}
	}
	
	public ImageOperation getV() {
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
				
				float v = hsv[2];
				r = (int) (v * 255);
				g = r;
				b = r;
				
				out[px] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
			}
			return new ImageOperation(out, width, height);
		} else {
			return getHSV(Channel.HSV_V);
		}
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
	
	public int[] getGrayImageAs1dArray() {
		return getGrayImageAs1dArray(false);
	}
	
	public int[] getGrayImageAs1dArray(boolean useMaxOfRgbForIntensity) {
		int[] img1d = imageAs1dArray;
		int c, r = 0;
		int[] res = new int[width * height];
		
		if (!useMaxOfRgbForIntensity) {
			for (int idx = 0; idx < img1d.length; idx++) {
				c = img1d[idx];
				r = ((c & 0xff0000) >> 16);
				res[idx] = r;
			}
		} else {
			int g, b;
			for (int idx = 0; idx < img1d.length; idx++) {
				c = img1d[idx];
				r = ((c & 0xff0000) >> 16);
				g = ((c & 0x00ff00) >> 8);
				b = ((c & 0x0000ff) >> 0);
				res[idx] = Math.max(Math.max(r, g), b);
			}
			
		}
		return res;
	}
	
	public static int[][] getGrayImageAs2dArray(Image grayImage) {
		int[] img1d = grayImage.getAs1A();
		int c, r, y = 0;
		int w = grayImage.getWidth();
		int h = grayImage.getHeight();
		int[][] res = new int[w][h];
		
		for (int idx = 0; idx < img1d.length; idx++) {
			c = img1d[idx];
			r = ((c & 0xff0000) >> 16);
			if (idx % w == 0 && idx > 0)
				y++;
			if (c == ImageOperation.BACKGROUND_COLORint)
				res[idx % w][y] = c;
			else
				res[idx % w][y] = r;
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
