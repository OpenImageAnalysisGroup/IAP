/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 26, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import de.ipk.ag_ba.image.operation.ImageOperation;

/**
 * @author klukas
 */
public class ColorHistogram {

	private final ArrayList<ColorHistogramEntry> colorEntries;
	private final int numberOfColors;

	public ColorHistogram(int colors) {
		this.colorEntries = new ArrayList<ColorHistogramEntry>();
		this.numberOfColors = colors;
		double hueStepSize = 360d / colors;
		double hueMiddle = hueStepSize / 2;
		for (int i = 0; i < colors; i++) {
			colorEntries.add(new ColorHistogramEntry((int) hueMiddle));
			hueMiddle += hueStepSize;
		}
	}

	public Collection<ColorHistogramEntry> getColorEntries() {
		return colorEntries;
	}

	public void countColorPixels(int[] rgbArray) {
		int red, green, blue;
		Color backgroundFill = ImageOperation.BACKGROUND_COLOR;
		int iBackgroundFill = backgroundFill.getRGB();
		int[] count = new int[numberOfColors];
		for (int i = 0; i < numberOfColors; i++)
			count[i] = 0;
		for (int rgb : rgbArray) {
			if (rgb == iBackgroundFill)
				continue;
			red = (rgb >> 16) & 0xFF;
			green = (rgb >> 8) & 0xFF;
			blue = rgb & 0xFF;
			float[] hsb = Color.RGBtoHSB(red, green, blue, null);
			int hue = (int) (hsb[0] * numberOfColors);
			count[hue] = count[hue] + 1;
		}
		for (int i = 0; i < numberOfColors; i++)
			colorEntries.get(i).addPixelCount(count[i]);
	}

	public long getNumberOfFilledPixels() {
		long res = 0;
		for (ColorHistogramEntry e : colorEntries)
			res += e.getNumberOfPixels();
		return res;
	}
}
