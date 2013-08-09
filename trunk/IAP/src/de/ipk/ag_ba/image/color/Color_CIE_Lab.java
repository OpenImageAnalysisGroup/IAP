package de.ipk.ag_ba.image.color;

import java.awt.Color;

import de.ipk.ag_ba.image.operation.ImageOperation;

/*
 * Created on Feb 24, 2010 by Christian Klukas
 */

/**
 * @author klukas
 */
public class Color_CIE_Lab {
	private float l;
	private float a;
	private float b;
	
	public Color_CIE_Lab(double l, double a, double b) {
		this.l = (float) l;
		this.a = (float) a;
		this.b = (float) b;
	}
	
	public Color_CIE_Lab(float l, float a, float b) {
		this.l = l;
		this.a = a;
		this.b = b;
	}
	
	public Color_CIE_Lab(int rgb, boolean exactAndSlow) {
		int red = (rgb & 0xff0000) >> 16;
		int green = (rgb & 0x00ff00) >> 8;
		int blue = (rgb & 0x0000ff);
		
		Color_CIE_Lab c = ColorUtil.colorXYZ2CIELAB(ColorUtil.colorRGB2XYZ(red, green, blue, exactAndSlow));
		l = c.l;
		a = c.a;
		b = c.b;
	}
	
	/**
	 * attention may not work correctly
	 * 
	 * @param rgb
	 */
	public Color_CIE_Lab(int rgb) {
		int red = (rgb & 0xff0000) >> 16;
		int green = (rgb & 0x00ff00) >> 8;
		int blue = (rgb & 0x0000ff);
		float[][][] lab = ImageOperation.getLabCubeInstance();
		l = lab[red][green][blue];
		a = lab[red][green][blue + 256];
		b = lab[red][green][blue + 512];
	}
	
	public void setL(float l) {
		this.l = l;
	}
	
	public float getL() {
		return l;
	}
	
	public void setA(float a) {
		this.a = a;
	}
	
	public float getA() {
		return a;
	}
	
	public void setB(float b) {
		this.b = b;
	}
	
	public float getB() {
		return b;
	}
	
	public ColorXYZ getColorXYZ() {
		double var_Y = (l + 16) / 116d;
		double var_X = a / 500d + var_Y;
		double var_Z = var_Y - b / 200d;
		
		double varYYY = var_Y * var_Y * var_Y;
		double varXXX = var_X * var_X * var_X;
		double varZZZ = var_Z * var_Z * var_Z;
		
		if (varYYY > 0.008856)
			var_Y = varYYY;
		else
			var_Y = (var_Y - 16d / 116d) / 7.787d;
		if (varXXX > 0.008856)
			var_X = varXXX;
		else
			var_X = (var_X - 16d / 116d) / 7.787d;
		if (varZZZ > 0.008856)
			var_Z = varZZZ;
		else
			var_Z = (var_Z - 16d / 116d) / 7.787d;
		
		float ref_X = 95.047f; // Observer= 2°, Illuminant= D65
		float ref_Y = 100.000f;
		float ref_Z = 108.883f;
		
		float X = (float) (ref_X * var_X); // ref_X = 95.047 Observer= 2°, Illuminant= D65
		float Y = (float) (ref_Y * var_Y); // ref_Y = 100.000
		float Z = (float) (ref_Z * var_Z); // ref_Z = 108.883
		
		return new ColorXYZ(X, Y, Z);
	}
	
	public int getRGB() {
		return getColorXYZ().getColor().getRGB();
	}
	
	public Color getColor() {
		return getColorXYZ().getColor();
	}
	
	@Override
	public String toString() {
		return "[" + l + "/" + a + "/" + b + "]";
	}
}
