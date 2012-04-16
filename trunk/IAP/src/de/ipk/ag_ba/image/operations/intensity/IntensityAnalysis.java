package de.ipk.ag_ba.image.operations.intensity;

import ij.measure.ResultsTable;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.intensity.Histogram.Mode;

public class IntensityAnalysis {
	
	private final ImageOperation io;
	private final int n;
	
	public IntensityAnalysis(ImageOperation imageOperation, int numberOfIntervals) {
		this.io = imageOperation;
		this.n = numberOfIntervals;
	}
	
	public ResultsTable calculateHistorgram(BlockProperty optDistHorizontal, Integer optRealMarkerDistance,
			Histogram.Mode mode) {
		
		boolean calcHue = false, multiLevel = false;
		if (mode == Mode.MODE_MULTI_LEVEL_RGB)
			multiLevel = true;
		if (mode == Mode.MODE_HUE)
			calcHue = true;
		
		ResultsTable result = new ResultsTable();
		
		int[] pixels = io.getImageAs1array();
		
		double sumOfIntensityChlorophyl = 0;
		double sumOfIntensityPhenol = 0;
		double sumOfIntensityClassic = 0;
		
		double sumOfHue = 0;
		Double minHue = null, maxHue = null;
		
		double weightOfPlant = 0;
		
		int background = ImageOperation.BACKGROUND_COLORint;
		
		Histogram histHue = new Histogram(this.n);
		
		Histogram histChlorophyl = new Histogram(this.n);
		Histogram histPhenol = new Histogram(this.n);
		Histogram histRatio = new Histogram(this.n);
		int plantImagePixelCnt = 0;
		float[] hsb = new float[3];
		for (int c : pixels) {
			if (c == background)
				continue;
			plantImagePixelCnt++;
			
			int r_intensityClassic = (c & 0xff0000) >> 16;
			int g_intensityChlorophyl = (c & 0x00ff00) >> 8;
			int b_intensityPhenol = (c & 0x0000ff);
			
			if (calcHue) {
				Color.RGBtoHSB(r_intensityClassic, g_intensityChlorophyl, b_intensityPhenol, hsb);
				double h = hsb[0];
				if ((int) (h * 255) > 0)
					histHue.addDataPoint((int) (h * 255), 255);
				sumOfHue += h;
				if (minHue == null || h < minHue)
					minHue = h;
				if (maxHue == null || h > maxHue)
					maxHue = h;
			} else {
				double intensityChloro = (255d - g_intensityChlorophyl) / 255d;
				double intensityPhenol = (255d - b_intensityPhenol) / 255d;
				weightOfPlant += (1d - (1 - 1 / 7d) * intensityPhenol);
				// if (intensityPhenol > 0.1)
				// System.out.println("PHENOL: " + intensityPhenol + ", pixel weight: " + (1d - (1 - 1 / 7d) * intensityPhenol));
				
				sumOfIntensityChlorophyl += (255 - g_intensityChlorophyl);
				if (multiLevel) {
					sumOfIntensityPhenol += (255 - b_intensityPhenol);
					sumOfIntensityClassic += (255 - r_intensityClassic);
				}
				if (g_intensityChlorophyl > 0)
					histChlorophyl.addDataPoint(g_intensityChlorophyl, 255);
				if (b_intensityPhenol > 0)
					histPhenol.addDataPoint(b_intensityPhenol, 255);
				if (intensityPhenol + intensityChloro > 0)
					histRatio.addDataPoint((int) Math.round(intensityPhenol / (intensityPhenol + intensityChloro) * 255d), 255);
			}
		}
		
		result.incrementCounter();
		
		if (calcHue) {
			if (optDistHorizontal != null && optRealMarkerDistance != null) {
				double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
				for (int i = 0; i < this.n; i++) {
					result.addValue("normalized.histogram.ratio.bin." + (i + 1) + "." + histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
							histHue.getFreqAt(i) * normalize);
				}
			}
			for (int i = 0; i < this.n; i++) {
				result.addValue(
						"hue.histogram.ratio.bin." + (i + 1) + "." + histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255), histHue.getFreqAt(i));
			}
			result.addValue("hue.average", sumOfHue / plantImagePixelCnt);
			// if (maxHue != null)
			// result.addValue("hue.max", maxHue);
			// if (minHue != null)
			// result.addValue("hue.min", minHue);
		} else {
			if (multiLevel) {
				result.addValue("intensity.phenol.plant_weight", weightOfPlant);
				result.addValue("intensity.phenol.plant_weight_drought_loss", plantImagePixelCnt - weightOfPlant);
			}
			
			result.addValue("filled.pixels", plantImagePixelCnt);
			result.addValue("filled.percent", (100d * plantImagePixelCnt) / pixels.length);
			if (multiLevel) {
				if (plantImagePixelCnt > 0) {
					result.addValue("intensity.chlorophyl.average", sumOfIntensityChlorophyl / plantImagePixelCnt / 255d);
					result.addValue("intensity.phenol.average", sumOfIntensityPhenol / plantImagePixelCnt / 255d);
					result.addValue("intensity.chlorophyl.sum", sumOfIntensityChlorophyl);
					result.addValue("intensity.phenol.sum", sumOfIntensityPhenol);
					result.addValue("intensity.classic.sum", sumOfIntensityClassic);
				}
				if (sumOfIntensityChlorophyl > 0)
					result.addValue("intensity.phenol.chlorophyl.ratio", sumOfIntensityPhenol / sumOfIntensityChlorophyl);
				
			} else
				result.addValue("intensity.sum", sumOfIntensityChlorophyl);
			result.addValue("intensity.average", sumOfIntensityChlorophyl / plantImagePixelCnt / 255d);
			
			if (optDistHorizontal != null && optRealMarkerDistance != null) {
				double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
				for (int i = 0; i < this.n; i++) {
					result.addValue(
							"normalized.histogram.bin." + (i + 1) + "." + histChlorophyl.getBorderLeft(i, 255) + "_" + histChlorophyl.getBorderRight(i, 255),
							histChlorophyl.getFreqAt(i) * normalize);
				}
			}
			for (int i = 0; i < this.n; i++) {
				result.addValue(
						"histogram.bin." + (i + 1) + "." + histChlorophyl.getBorderLeft(i, 255) + "_" + histChlorophyl.getBorderRight(i, 255),
						histChlorophyl.getFreqAt(i));
			}
			
			if (multiLevel) {
				if (optDistHorizontal != null && optRealMarkerDistance != null) {
					double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
					for (int i = 0; i < this.n; i++) {
						result.addValue(
								"normalized.histogram.phenol.bin." + (i + 1) + "." + histPhenol.getBorderLeft(i, 255) + "_" + histPhenol.getBorderRight(i, 255),
								histPhenol.getFreqAt(i) * normalize);
					}
				}
				for (int i = 0; i < this.n; i++) {
					result.addValue(
							"histogram.phenol.bin." + (i + 1) + "." + histPhenol.getBorderLeft(i, 255) + "_" + histPhenol.getBorderRight(i, 255),
							histPhenol.getFreqAt(i));
				}
				if (optDistHorizontal != null && optRealMarkerDistance != null) {
					double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
					for (int i = 0; i < this.n; i++) {
						result.addValue("normalized.histogram.ratio.bin." + (i + 1) + "." + histRatio.getBorderLeft(i, 255) + "_" + histRatio.getBorderRight(i, 255),
								histRatio.getFreqAt(i) * normalize);
					}
				}
				for (int i = 0; i < this.n; i++) {
					result.addValue(
							"histogram.ratio.bin." + (i + 1) + "." + histRatio.getBorderLeft(i, 255) + "_" + histRatio.getBorderRight(i, 255),
							histRatio.getFreqAt(i));
				}
			}
		}
		return result;
	}
}
