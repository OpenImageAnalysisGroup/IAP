/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 26, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

/**
 * @author klukas
 */
public class ColorHistogramEntry {

	private int pixels = 0;
	private final int hueMiddle;

	public ColorHistogramEntry(int hueMiddle) {
		this.hueMiddle = hueMiddle;
	}

	public int getNumberOfPixels() {
		return pixels;
	}

	public String getColorDisplayName() {
		return "hue=" + hueMiddle;
	}

	public void addPixelCount(int i) {
		pixels += i;
	}

	public int getHue() {
		return hueMiddle;
	}

}
