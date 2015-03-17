/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Copyright (c) 2003 IPK Gatersleben
 * $Id: Colors.java,v 1.1 2011-01-31 09:04:49 klukas Exp $
 */

/*
 * Created on 07.04.2004
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Colors implements HelperClass {
	
	/**
	 * creates the specified number of color objects which can be
	 * discriminated as good as possible
	 * 
	 * @param numberOfColors
	 *           number of different colors to create
	 * @return Collection of Color objects
	 */
	public static ArrayList<Color> get(int numberOfColors) {
		return get(numberOfColors, 0.2f); // 0.8f); // 0.5f);
	}
	
	public static ArrayList<Color> get(int numberOfColors, double saturation) {
		return get(numberOfColors, (float) saturation);
	}
	
	/**
	 * creates the specified number of color objects which can be
	 * discriminated as good as possible
	 * 
	 * @param numberOfColors
	 *           number of different colors to create
	 * @return Collection of Color objects
	 * @author HS, modified by C. Klukas (hue value calculation modified)
	 */
	public static ArrayList<Color> get(int numberOfColors, float saturation) {
		ArrayList<Color> colors = new ArrayList<Color>();
		
		// define how much the Hue will change between steps
		float incr = 1f / numberOfColors;
		float s = saturation;
		float b = 1;
		
		for (int i = 0; i < numberOfColors; i++) {
			float h = incr * i;
			// create a new color using the HSB parameters
			colors.add(Color.getHSBColor(h, s, b));
		}
		
		return colors;
	}
	
	public static ArrayList<Color> getGrayColors(int numberOfColors) {
		ArrayList<Color> colors = new ArrayList<Color>();
		
		final Color c1 = Color.LIGHT_GRAY;
		final Color c2 = Color.BLACK;
		
		for (int i = 0; i < numberOfColors; i++) {
			float f;
			if (numberOfColors > 1)
				f = ((float) (numberOfColors - i - 1)) / (numberOfColors - 1);
			else
				f = 0.5f;
			Color cc = getColor(f, 1, c1, c2);
			colors.add(cc);
		}
		
		return colors;
	}
	
	public static Color getColor(float maxOrMinR, double gamma,
						Color col__1, Color col_1) {
		Color col1 = col__1;
		Color col2 = col_1;
		maxOrMinR = Math.abs(maxOrMinR);
		maxOrMinR = (float) Math.pow(maxOrMinR, gamma);
		float red = (col2.getRed() - col1.getRed()) * maxOrMinR + col1.getRed();
		float green = (col2.getGreen() - col1.getGreen()) * maxOrMinR + col1.getGreen();
		float blue = (col2.getBlue() - col1.getBlue()) * maxOrMinR + col1.getBlue();
		float alpha = (col2.getAlpha() - col1.getAlpha()) * maxOrMinR + col1.getAlpha();
		return new Color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
	}
	
	public static ArrayList<Color> getGrayColorsInverse(int numberOfColors) {
		ArrayList<Color> colors = getGrayColors(numberOfColors);
		
		ArrayList<Color> result = new ArrayList<Color>();
		for (int i = colors.size() - 1; i >= 0; i--) {
			result.add(colors.get(i));
		}
		return result;
	}
	
	public static Color[] getAlphaColors(int numberOfColors, int alpha) {
		Collection<Color> r = get(numberOfColors);
		Color[] result = new Color[r.size()];
		int i = 0;
		for (Iterator<Color> it = r.iterator(); it.hasNext();) {
			Color t = (Color) it.next();
			Color c = new Color(t.getRed(), t.getGreen(), t.getBlue(), alpha);
			result[i++] = c;
		}
		return result;
	}
	
	public static Color[] getColors(int numberOfColors) {
		Collection<Color> r = get(numberOfColors);
		Color[] result = new Color[r.size()];
		int i = 0;
		for (Iterator<Color> it = r.iterator(); it.hasNext();) {
			Color t = (Color) it.next();
			Color c = new Color(t.getRed(), t.getGreen(), t.getBlue());
			result[i++] = c;
		}
		return result;
	}
	
	/**
	 * @author klukas
	 * @param numberOfColors
	 * @param alpha
	 * @param index
	 * @return
	 */
	public static Color getAlphaColor(int numberOfColors, int alpha, int index) {
		Color[] result = getAlphaColors(numberOfColors, alpha);
		return result[index];
	}
	
	/**
	 * @param newColor
	 * @return
	 * @author klukas
	 */
	public static Color getOppositeColor(Color color) {
		// float[] hsb = new float[3];
		// Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		// hsb[2] = hsb[2] > 0.5 ? 0f : 1f;
		// hsb[0] = hsb[0] > 0.5 ? hsb[0]-0.5f : hsb[0]+0.5f;
		// hsb[1] = hsb[1] > 0.5 ? hsb[1]-0.5f : hsb[1]+0.5f;
		// Color r = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		// return r;
		return new Color(color.getRGB() ^ 0x00ffffff);
	}
	
	public static Color brighten(Color color, double saturation, double value) {
		float[] hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		hsb[1] = (float) (hsb[1] * saturation);
		if (hsb[1] > 1)
			hsb[1] = 1;
		hsb[2] = (float) (hsb[2] * value);
		if (hsb[2] > 1)
			hsb[2] = 1;
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}
}
