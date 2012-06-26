package de.ipk.ag_ba.image.operations.intensity;

import ij.measure.ResultsTable;

import java.awt.Color;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.gui.util.IAPservice;
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
		
		ResultsTable result = new ResultsTable();
		
		int[] pixels = io.getImageAs1array();
		
		double sumOfIntensityChlorophyl = 0;
		double sumOfIntensityPhenol = 0;
		double sumOfIntensityClassic = 0;
		
		double sumOfHue = 0;
		double sumOfSat = 0;
		double sumOfVal = 0;
		
		DescriptiveStatistics statsHueValues = new DescriptiveStatistics();
		DescriptiveStatistics statsSatValues = new DescriptiveStatistics();
		DescriptiveStatistics statsValValues = new DescriptiveStatistics();
		
		double weightOfPlant = 0;
		
		int background = ImageOperation.BACKGROUND_COLORint;
		
		Histogram histHue = new Histogram(this.n);
		Histogram histSat = new Histogram(this.n);
		Histogram histVal = new Histogram(this.n);
		
		Histogram histChlorophyl = new Histogram(this.n);
		Histogram histPhenol = new Histogram(this.n);
		// Histogram histRatio = new Histogram(this.n);
		int plantImagePixelCnt = 0;
		float[] hsb = new float[3];
		for (int c : pixels) {
			if (c == background)
				continue;
			plantImagePixelCnt++;
			
			int r_intensityClassic = (c & 0xff0000) >> 16;
			int g_intensityChlorophyl = (c & 0x00ff00) >> 8;
			int b_intensityPhenol = (c & 0x0000ff);
			
			if (mode == Mode.MODE_HUE_VIS_ANALYSIS) {
				Color.RGBtoHSB(r_intensityClassic, g_intensityChlorophyl, b_intensityPhenol, hsb);
				{
					double h = hsb[0];
					if ((int) (h * 255) > 0)
						histHue.addDataPoint((int) (h * 255), 255);
					sumOfHue += h;
					statsHueValues.addValue(h);
				}
				{
					double s = hsb[1];
					if ((int) (s * 255) > 0)
						histSat.addDataPoint((int) (s * 255), 255);
					sumOfSat += s;
					statsSatValues.addValue(s);
				}
				{
					double v = hsb[0];
					if ((int) (v * 255) > 0)
						histVal.addDataPoint((int) (v * 255), 255);
					sumOfVal += v;
					statsValValues.addValue(v);
				}
			}
			if (mode == Mode.MODE_IR_ANALYSIS) {
				boolean absoluteIR = false;
				double h;
				if (absoluteIR) {
					h = IAPservice.getIRintenstityFromRGB(r_intensityClassic, g_intensityChlorophyl, b_intensityPhenol) * 255d;
				} else {
					h = (255d - r_intensityClassic);
				}
				sumOfIntensityChlorophyl += h;
				histHue.addDataPoint((int) h, 255);
				sumOfHue += h;
				statsHueValues.addValue(h);
			} else {
				// double intensityChloro = (255d - g_intensityChlorophyl) / 255d;
				double intensityPhenol = (255d - b_intensityPhenol) / 255d;
				weightOfPlant += (1d - (1 - 1 / 7d) * intensityPhenol);
				// if (intensityPhenol > 0.1)
				// System.out.println("PHENOL: " + intensityPhenol + ", pixel weight: " + (1d - (1 - 1 / 7d) * intensityPhenol));
				
				sumOfIntensityChlorophyl += (255 - g_intensityChlorophyl);
				if (mode == Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS) {
					sumOfIntensityPhenol += (255 - b_intensityPhenol);
					sumOfIntensityClassic += (255 - r_intensityClassic);
				}
				if (g_intensityChlorophyl > 0)
					histChlorophyl.addDataPoint(g_intensityChlorophyl, 255);
				if (b_intensityPhenol > 0)
					histPhenol.addDataPoint(b_intensityPhenol, 255);
				// if (intensityPhenol + intensityChloro > 0)
				// histRatio.addDataPoint((int) Math.round(intensityPhenol / (intensityPhenol + intensityChloro) * 255d), 255);
			}
		}
		
		result.incrementCounter();
		
		boolean addNormalizedHistogramValues = true;
		if (mode == Mode.MODE_HUE_VIS_ANALYSIS) {
			if (addNormalizedHistogramValues) {
				if (optDistHorizontal != null && optRealMarkerDistance != null) {
					double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
					for (int i = 0; i < this.n; i++) {
						result.addValue("hsv.normalized.hue.histogram.bin." + (i + 1) + "." + histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
								histHue.getFreqAt(i) * normalize);
						result.addValue("hsv.normalized.sat.histogram.bin." + (i + 1) + "." + histSat.getBorderLeft(i, 255) + "_" + histSat.getBorderRight(i, 255),
								histSat.getFreqAt(i) * normalize);
						result.addValue("hsv.normalized.val.histogram.bin." + (i + 1) + "." + histVal.getBorderLeft(i, 255) + "_" + histVal.getBorderRight(i, 255),
								histVal.getFreqAt(i) * normalize);
					}
				}
			}
			for (int i = 0; i < this.n; i++) {
				result.addValue("hsv.hue.histogram.bin." + (i + 1) + "." + histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
						histHue.getFreqAt(i));
				result.addValue("hsv.sat.histogram.bin." + (i + 1) + "." + histSat.getBorderLeft(i, 255) + "_" + histSat.getBorderRight(i, 255),
						histSat.getFreqAt(i));
				result.addValue("hsv.val.histogram.bin." + (i + 1) + "." + histVal.getBorderLeft(i, 255) + "_" + histVal.getBorderRight(i, 255),
						histVal.getFreqAt(i));
			}
			result.addValue("hsv.hue.average", sumOfHue / plantImagePixelCnt);
			result.addValue("hsv.sat.average", sumOfSat / plantImagePixelCnt);
			result.addValue("hsv.val.average", sumOfVal / plantImagePixelCnt);
			
			if (statsHueValues.getN() > 0) {
				result.addValue("hsv.hue.stddev", statsHueValues.getStandardDeviation());
				result.addValue("hsv.sat.stddev", statsSatValues.getStandardDeviation());
				result.addValue("hsv.val.stddev", statsValValues.getStandardDeviation());
				
				result.addValue("hsv.hue.skewess", statsHueValues.getSkewness());
				result.addValue("hsv.sat.skewness", statsSatValues.getSkewness());
				result.addValue("hsv.val.skewness", statsValValues.getSkewness());
				
				result.addValue("hsv.hue.kurtosis", statsHueValues.getKurtosis());
				result.addValue("hsv.sat.kurtosis", statsSatValues.getKurtosis());
				result.addValue("hsv.val.kurtosis", statsValValues.getKurtosis());
			}
		} else {
			if (mode == Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS) {
				result.addValue("intensity.phenol.plant_weight", weightOfPlant);
				result.addValue("intensity.phenol.plant_weight_drought_loss", plantImagePixelCnt - weightOfPlant);
			}
			
			if (mode != Mode.MODE_IR_ANALYSIS) {
				result.addValue("filled.pixels", plantImagePixelCnt);
				result.addValue("filled.percent", (100d * plantImagePixelCnt) / pixels.length);
				
				if (statsHueValues.getN() > 0) {
					result.addValue("intensity.stddev", statsHueValues.getStandardDeviation());
					result.addValue("intensity.skewness", statsHueValues.getSkewness());
					result.addValue("intensity.kurtosis", statsHueValues.getKurtosis());
				}
			}
			if (mode == Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS) {
				if (plantImagePixelCnt > 0) {
					result.addValue("intensity.chlorophyl.sum", sumOfIntensityChlorophyl);
					result.addValue("intensity.chlorophyl.average", sumOfIntensityChlorophyl / plantImagePixelCnt / 255d);
					result.addValue("intensity.phenol.sum", sumOfIntensityPhenol);
					result.addValue("intensity.phenol.average", sumOfIntensityPhenol / plantImagePixelCnt / 255d);
					result.addValue("intensity.classic.sum", sumOfIntensityClassic);
					result.addValue("intensity.classic.average", sumOfIntensityClassic / plantImagePixelCnt / 255d);
				}
				if (sumOfIntensityChlorophyl > 0)
					result.addValue("intensity.phenol.chlorophyl.ratio", sumOfIntensityPhenol / sumOfIntensityChlorophyl);
			} else
				result.addValue("intensity.sum", sumOfIntensityChlorophyl);
			
			result.addValue("intensity.average", sumOfIntensityChlorophyl / plantImagePixelCnt / 255d);
			
			if (addNormalizedHistogramValues) {
				if (optDistHorizontal != null && optRealMarkerDistance != null) {
					double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
					for (int i = 0; i < this.n; i++) {
						result.addValue(
								"normalized.histogram.bin." + (i + 1) + "." + histChlorophyl.getBorderLeft(i, 255) + "_" + histChlorophyl.getBorderRight(i, 255),
								histChlorophyl.getFreqAt(i) * normalize);
					}
				}
			}
			for (int i = 0; i < this.n; i++) {
				result.addValue(
						"histogram.bin." + (i + 1) + "." + histChlorophyl.getBorderLeft(i, 255) + "_" + histChlorophyl.getBorderRight(i, 255),
						histChlorophyl.getFreqAt(i));
			}
			
			if (mode == Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS) {
				if (addNormalizedHistogramValues) {
					if (optDistHorizontal != null && optRealMarkerDistance != null) {
						double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
						for (int i = 0; i < this.n; i++) {
							result.addValue(
									"normalized.histogram.phenol.bin." + (i + 1) + "." + histPhenol.getBorderLeft(i, 255) + "_" + histPhenol.getBorderRight(i, 255),
									histPhenol.getFreqAt(i) * normalize);
						}
					}
				}
				for (int i = 0; i < this.n; i++) {
					result.addValue(
							"histogram.phenol.bin." + (i + 1) + "." + histPhenol.getBorderLeft(i, 255) + "_" + histPhenol.getBorderRight(i, 255),
							histPhenol.getFreqAt(i));
				}
			}
		}
		return result;
	}
}
