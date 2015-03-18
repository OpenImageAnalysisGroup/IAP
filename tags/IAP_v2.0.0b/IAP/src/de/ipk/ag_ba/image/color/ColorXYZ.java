package de.ipk.ag_ba.image.color;

import java.awt.Color;

/**
 * @author klukas
 */
public class ColorXYZ {
	
	public float x, y, z;
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public ColorXYZ(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * @param c1
	 */
	public ColorXYZ(Color c) {
		int r, g, b;
		r = c.getRed();
		g = c.getGreen();
		b = c.getBlue();
		ColorXYZ xyz = ColorUtil.colorRGB2XYZ(r, g, b);
		x = xyz.x;
		y = xyz.y;
		z = xyz.z;
	}
	
	/**
	 * @return
	 */
	public float getX() {
		return x;
	}
	
	/**
	 * @return
	 */
	public float getY() {
		return y;
	}
	
	/**
	 * @return
	 */
	public float getZ() {
		return z;
	}
	
	public Color getColor() {
		float var_X = x / 100f; // X from 0 to 95.047 (Observer = 2°, Illuminant
										// = D65)
		float var_Y = y / 100f; // Y from 0 to 100.000
		float var_Z = z / 100f; // Z from 0 to 108.883
		
		float var_R = var_X * 3.2406f + var_Y * -1.5372f + var_Z * -0.4986f;
		float var_G = var_X * -0.9689f + var_Y * 1.8758f + var_Z * 0.0415f;
		float var_B = var_X * 0.0557f + var_Y * -0.2040f + var_Z * 1.0570f;
		
		if (var_R > 0.0031308)
			var_R = (float) (1.055f * (Math.pow(var_R, (1 / 2.4f))) - 0.055f);
		else
			var_R = 12.92f * var_R;
		if (var_G > 0.0031308)
			var_G = (float) (1.055f * (Math.pow(var_G, (1 / 2.4f))) - 0.055f);
		else
			var_G = 12.92f * var_G;
		if (var_B > 0.0031308)
			var_B = (float) (1.055f * (Math.pow(var_B, (1 / 2.4f))) - 0.055f);
		else
			var_B = 12.92f * var_B;
		
		if (var_R > 1)
			var_R = 1;
		if (var_G > 1)
			var_G = 1;
		if (var_B > 1)
			var_B = 1;
		
		if (var_R < 0)
			var_R = 0;
		if (var_G < 0)
			var_G = 0;
		if (var_B < 0)
			var_B = 0;
		
		return new Color(var_R, var_G, var_B);
	}
}
