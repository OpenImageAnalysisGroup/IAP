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
		
		DescriptiveStatistics statsHueValuesOverall = new DescriptiveStatistics();
		DescriptiveStatistics statsSatValuesOverall = new DescriptiveStatistics();
		DescriptiveStatistics statsValValuesOverall = new DescriptiveStatistics();
		
		DescriptiveStatistics statsLabL = new DescriptiveStatistics();
		DescriptiveStatistics statsLabA = new DescriptiveStatistics();
		DescriptiveStatistics statsLabB = new DescriptiveStatistics();
		
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
				
				int r = r_intensityClassic;
				int g = g_intensityChlorophyl;
				int b = b_intensityPhenol;
				
				int Li = (int) ImageOperation.labCube[r][g][b];
				int ai = (int) ImageOperation.labCube[r][g][b + 256];
				int bi = (int) ImageOperation.labCube[r][g][b + 512];
				statsLabL.addValue(Li);
				statsLabA.addValue(ai);
				statsLabB.addValue(bi);
				
				Color.RGBtoHSB(r_intensityClassic, g_intensityChlorophyl, b_intensityPhenol, hsb);
				{
					double h = hsb[0];
					double s = hsb[1];
					double v = hsb[2];
					if ((int) (v * 255) > 0) {
						{
							histHue.addDataPoint((int) (h * 255), 255, s, v);
							sumOfHue += h;
							statsHueValuesOverall.addValue(h);
						}
						{
							histSat.addDataPoint((int) (s * 255), 255, h, v);
							sumOfSat += s;
							statsSatValuesOverall.addValue(s);
						}
						{
							histVal.addDataPoint((int) (v * 255), 255, h, s);
							sumOfVal += v;
							statsValValuesOverall.addValue(v);
						}
					}
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
				histHue.addDataPoint((int) h, 255, Double.NaN, Double.NaN);
				sumOfHue += h;
				statsHueValuesOverall.addValue(h);
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
					histChlorophyl.addDataPoint(g_intensityChlorophyl, 255, Double.NaN, Double.NaN);
				if (b_intensityPhenol > 0)
					histPhenol.addDataPoint(b_intensityPhenol, 255, Double.NaN, Double.NaN);
				// if (intensityPhenol + intensityChloro > 0)
				// histRatio.addDataPoint((int) Math.round(intensityPhenol / (intensityPhenol + intensityChloro) * 255d), 255);
			}
		}
		
		result.incrementCounter();
		
		boolean addNormalizedHistogramValues = true;
		if (mode == Mode.MODE_HUE_VIS_ANALYSIS) {
			if (optDistHorizontal != null && optRealMarkerDistance != null && addNormalizedHistogramValues) {
				double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
				for (int i = 0; i < this.n; i++) {
					result.addValue("hsv.normalized.h.histogram.bin." + (i + 1) + "." + histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
							histHue.getFreqAt(i) * normalize);
					result.addValue("hsv.normalized.s.histogram.bin." + (i + 1) + "." + histSat.getBorderLeft(i, 255) + "_" + histSat.getBorderRight(i, 255),
							histSat.getFreqAt(i) * normalize);
					result.addValue("hsv.normalized.v.histogram.bin." + (i + 1) + "." + histVal.getBorderLeft(i, 255) + "_" + histVal.getBorderRight(i, 255),
							histVal.getFreqAt(i) * normalize);
				}
			} else {
				for (int i = 0; i < this.n; i++) {
					result.addValue("hsv.h.histogram.bin." + (i + 1) + "." + histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
							histHue.getFreqAt(i));
					result.addValue("hsv.s.histogram.bin." + (i + 1) + "." + histSat.getBorderLeft(i, 255) + "_" + histSat.getBorderRight(i, 255),
							histSat.getFreqAt(i));
					result.addValue("hsv.v.histogram.bin." + (i + 1) + "." + histVal.getBorderLeft(i, 255) + "_" + histVal.getBorderRight(i, 255),
							histVal.getFreqAt(i));
				}
			}
			for (int i = 0; i < this.n; i++) {
				if (histHue.getOther1avg(i) != null)
					result.addValue("hsv.normalized.h.histogram.s_avg.bin." + (i + 1) + "." + histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
							histHue.getOther1avg(i));
				if (histHue.getOther2avg(i) != null)
					result.addValue("hsv.normalized.h.histogram.v_avg.bin." + (i + 1) + "." + histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
							histHue.getOther2avg(i));
				
				if (histSat.getOther1avg(i) != null)
					result.addValue("hsv.normalized.s.histogram.h_avg.bin." + (i + 1) + "." + histSat.getBorderLeft(i, 255) + "_" + histSat.getBorderRight(i, 255),
							histSat.getOther1avg(i));
				if (histSat.getOther2avg(i) != null)
					result.addValue("hsv.normalized.s.histogram.v_avg.bin." + (i + 1) + "." + histSat.getBorderLeft(i, 255) + "_" + histSat.getBorderRight(i, 255),
							histSat.getOther2avg(i));
				
				if (histVal.getOther1avg(i) != null)
					result.addValue("hsv.normalized.v.histogram.h_avg.bin." + (i + 1) + "." + histVal.getBorderLeft(i, 255) + "_" + histVal.getBorderRight(i, 255),
							histVal.getOther1avg(i));
				if (histVal.getOther2avg(i) != null)
					result.addValue("hsv.normalized.v.histogram.s_avg.bin." + (i + 1) + "." + histVal.getBorderLeft(i, 255) + "_" + histVal.getBorderRight(i, 255),
							histVal.getOther2avg(i));
			}
			result.addValue("hsv.h.average", sumOfHue / plantImagePixelCnt);
			result.addValue("hsv.s.average", sumOfSat / plantImagePixelCnt);
			result.addValue("hsv.v.average", sumOfVal / plantImagePixelCnt);
			
			boolean addStressIndicatorHueValues = true;
			if (addStressIndicatorHueValues) {
				double green5to8 = histHue.getFreqAt(5 - 1) + histHue.getFreqAt(6 - 1) + histHue.getFreqAt(7 - 1) + histHue.getFreqAt(8 - 1);
				if (green5to8 > 0) {
					double yello4togreen5to8 = histHue.getFreqAt(4 - 1) / green5to8;
					double red1and2togreen5to8 = (histHue.getFreqAt(1 - 1) + histHue.getFreqAt(2 - 1)) / green5to8;
					double brown3togreen5to8 = histHue.getFreqAt(3 - 1) / green5to8;
					
					result.addValue("stress.hue.yellow2green", yello4togreen5to8);
					result.addValue("stress.hue.red2green", red1and2togreen5to8);
					result.addValue("stress.hue.brown2green", brown3togreen5to8);
				}
			}
			
			if (statsHueValuesOverall.getN() > 0) {
				result.addValue("hsv.h.stddev", statsHueValuesOverall.getStandardDeviation());
				result.addValue("hsv.s.stddev", statsSatValuesOverall.getStandardDeviation());
				result.addValue("hsv.v.stddev", statsValValuesOverall.getStandardDeviation());
				
				result.addValue("hsv.h.skewness", statsHueValuesOverall.getSkewness());
				result.addValue("hsv.s.skewness", statsSatValuesOverall.getSkewness());
				result.addValue("hsv.v.skewness", statsValValuesOverall.getSkewness());
				
				result.addValue("hsv.h.kurtosis", statsHueValuesOverall.getKurtosis());
				result.addValue("hsv.s.kurtosis", statsSatValuesOverall.getKurtosis());
				result.addValue("hsv.v.kurtosis", statsValValuesOverall.getKurtosis());
				
				result.addValue("lab.l.mean", statsLabL.getMean());
				result.addValue("lab.a.mean", statsLabA.getMean());
				result.addValue("lab.b.mean", statsLabB.getMean());
				
				result.addValue("lab.l.stddev", statsLabL.getStandardDeviation());
				result.addValue("lab.a.stddev", statsLabA.getStandardDeviation());
				result.addValue("lab.b.stddev", statsLabB.getStandardDeviation());
				
				result.addValue("lab.l.skewness", statsLabL.getSkewness());
				result.addValue("lab.a.skewness", statsLabA.getSkewness());
				result.addValue("lab.b.skewness", statsLabB.getSkewness());
				
				result.addValue("lab.l.kurtosis", statsLabL.getKurtosis());
				result.addValue("lab.a.kurtosis", statsLabA.getKurtosis());
				result.addValue("lab.b.kurtosis", statsLabB.getKurtosis());
			}
		} else {
			if (mode == Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS) {
				result.addValue("intensity.phenol.plant_weight", weightOfPlant);
				result.addValue("intensity.phenol.plant_weight_drought_loss", plantImagePixelCnt - weightOfPlant);
			}
			
			if (mode != Mode.MODE_IR_ANALYSIS) {
				result.addValue("filled.pixels", plantImagePixelCnt);
				result.addValue("filled.percent", (100d * plantImagePixelCnt) / pixels.length);
				
				if (statsHueValuesOverall.getN() > 0) {
					result.addValue("intensity.stddev", statsHueValuesOverall.getStandardDeviation());
					result.addValue("intensity.skewness", statsHueValuesOverall.getSkewness());
					result.addValue("intensity.kurtosis", statsHueValuesOverall.getKurtosis());
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
