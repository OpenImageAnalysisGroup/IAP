/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Feb 24, 2010 by Christian Klukas
 */

package org.color;

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
	
	/**
	 * @return the l
	 */
	public double getL() {
		return l;
	}
	
	public void setL(double l) {
		this.l = l;
	}
	
	/**
	 * @param a
	 *           the a to set
	 */
	public void setA(double a) {
		this.a = a;
	}
	
	/**
	 * @return the a
	 */
	public double getA() {
		return a;
	}
	
	/**
	 * @param b
	 *           the b to set
	 */
	public void setB(double b) {
		this.b = b;
	}
	
	/**
	 * @return the b
	 */
	public double getB() {
		return b;
	}
	
	public ColorXYZ getColorXYZ() {
		double var_Y = (l + 16) / 116d;
		double var_X = a / 500d + var_Y;
		double var_Z = var_Y - b / 200d;
		
		if (Math.pow(var_Y, 3d) > 0.008856)
			var_Y = Math.pow(var_Y, 3);
		else
			var_Y = (var_Y - 16d / 116d) / 7.787d;
		if (Math.pow(var_X, 3d) > 0.008856)
			var_X = Math.pow(var_X, 3);
		else
			var_X = (var_X - 16 / 116) / 7.787;
		if (Math.pow(var_Z, 3) > 0.008856)
			var_Z = Math.pow(var_Z, 3);
		else
			var_Z = (var_Z - 16 / 116) / 7.787d;
		
		double ref_X = 95.047; // Observer= 2°, Illuminant= D65
		double ref_Y = 100.000;
		double ref_Z = 108.883;
		
		double X = ref_X * var_X; // ref_X = 95.047 Observer= 2°, Illuminant= D65
		double Y = ref_Y * var_Y; // ref_Y = 100.000
		double Z = ref_Z * var_Z; // ref_Z = 108.883
		
		return new ColorXYZ(X, Y, Z);
	}
}
