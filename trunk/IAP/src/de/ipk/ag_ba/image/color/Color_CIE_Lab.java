package de.ipk.ag_ba.image.color;

import de.ipk.ag_ba.mongo.IAPservice;

/*
 * Created on Feb 24, 2010 by Christian Klukas
 */

/**
 * @author klukas
 */
public class Color_CIE_Lab {
	private double l;
	private double a;
	private double b;
	
	public Color_CIE_Lab(double l, double a, double b) {
		this.l = l;
		this.a = a;
		this.b = b;
	}
	
	public Color_CIE_Lab(int rgb, boolean exactAndSlow) {
		int red = (rgb >> 16) & 0xff;
		int green = (rgb >> 8) & 0xff;
		int blue = (rgb) & 0xff;
		
		Color_CIE_Lab c = ColorUtil.colorXYZ2CIELAB(ColorUtil.colorRGB2XYZ(red, green, blue, exactAndSlow));
		l = c.l;
		a = c.a;
		b = c.b;
	}
	
	public Color_CIE_Lab(int rgb) {
		int red = (rgb >> 16) & 0xff;
		int green = (rgb >> 8) & 0xff;
		int blue = (rgb) & 0xff;
		
		l = IAPservice.labCube[red][green][blue][0];
		a = IAPservice.labCube[red][green][blue][1];
		b = IAPservice.labCube[red][green][blue][2];
	}
	
	public void setL(double l) {
		this.l = l;
	}
	
	public double getL() {
		return l;
	}
	
	public void setA(double a) {
		this.a = a;
	}
	
	public double getA() {
		return a;
	}
	
	public void setB(double b) {
		this.b = b;
	}
	
	public double getB() {
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
		
		double ref_X = 95.047; // Observer= 2°, Illuminant= D65
		double ref_Y = 100.000;
		double ref_Z = 108.883;
		
		double X = ref_X * var_X; // ref_X = 95.047 Observer= 2°, Illuminant= D65
		double Y = ref_Y * var_Y; // ref_Y = 100.000
		double Z = ref_Z * var_Z; // ref_Z = 108.883
		
		return new ColorXYZ(X, Y, Z);
	}
	
	public int getRGB() {
		return getColorXYZ().getColor().getRGB();
	}
	
	@Override
	public String toString() {
		return "[" + l + "/" + a + "/" + b + "]";
	}
}
