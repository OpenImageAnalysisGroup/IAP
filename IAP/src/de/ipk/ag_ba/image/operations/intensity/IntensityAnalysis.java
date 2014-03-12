package de.ipk.ag_ba.image.operations.intensity;

import java.awt.Color;

import org.StringManipulationTools;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.intensity.Histogram.Mode;

public class IntensityAnalysis {
	
	private final ImageOperation io;
	private final int n;
	
	public IntensityAnalysis(ImageOperation imageOperation, int numberOfIntervals) {
		this.io = imageOperation;
		this.n = numberOfIntervals;
	}
	
	public ResultsTableWithUnits calculateHistorgram(Double optDistHorizontal, Double optRealMarkerDistance,
			Histogram.Mode mode, boolean addHistogramValues, boolean calcCurtosis) {
		
		ResultsTableWithUnits result = new ResultsTableWithUnits();
		
		int[] pixels = io.getAs1D();
		
		double sumOfIntensityChlorophyl = 0;
		double sumOfIntensityPhenol = 0;
		double sumOfIntensityClassic = 0;
		
		double sumOfHue = 0;
		double sumOfSat = 0;
		double sumOfVal = 0;
		
		double sumOfDGCIs2 = 0;
		
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
		int plantImagePixelCntVgreater0 = 0;
		float[] hsb = new float[3];
		float[][][] lab = ImageOperation.getLabCubeInstance();
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
				
				int Li = (int) lab[r][g][b];
				int ai = (int) lab[r][g][b + 256];
				int bi = (int) lab[r][g][b + 512];
				statsLabL.addValue(Li);
				statsLabA.addValue(ai);
				statsLabB.addValue(bi);
				
				Color.RGBtoHSB(r_intensityClassic, g_intensityChlorophyl, b_intensityPhenol, hsb);
				{
					double h = hsb[0];
					double s = hsb[1];
					double v = hsb[2];
					if ((int) (v * 255) > 0) {
						plantImagePixelCntVgreater0++;
						{
							histHue.addDataPoint((int) (h * 255), 255, s, v);
							sumOfHue += h;
							double hLimit = h;
							if (hLimit < 60d / 360d)
								hLimit = 60d / 360d;
							else
								if (hLimit > 120d / 360d)
									hLimit = 120d / 360d;
							
							double pH1 = (hLimit - 60d / 360d) / (60d / 360d);
							sumOfDGCIs2 += (pH1 + (1 - s) + (1 - v)) / 3d;
							
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
			if (addHistogramValues) {
				if (optDistHorizontal != null && optRealMarkerDistance != null && addNormalizedHistogramValues) {
					double normalize = optRealMarkerDistance / optDistHorizontal;
					for (int i = 0; i < this.n; i++) {
						result.addValue(
								"hsv.normalized.h.histogram.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histHue.getBorderLeft(i, 255)
										+ "_" + histHue.getBorderRight(i, 255),
								histHue.getFreqAt(i) * normalize);
						result.addValue(
								"hsv.normalized.s.histogram.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histSat.getBorderLeft(i, 255)
										+ "_" + histSat.getBorderRight(i, 255),
								histSat.getFreqAt(i) * normalize);
						result.addValue(
								"hsv.normalized.v.histogram.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histVal.getBorderLeft(i, 255)
										+ "_" + histVal.getBorderRight(i, 255),
								histVal.getFreqAt(i) * normalize);
					}
				} else {
					for (int i = 0; i < this.n; i++) {
						result.addValue("hsv.h.histogram.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histHue.getBorderLeft(i, 255)
								+ "_" + histHue.getBorderRight(i, 255),
								histHue.getFreqAt(i));
						result.addValue("hsv.s.histogram.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histSat.getBorderLeft(i, 255)
								+ "_" + histSat.getBorderRight(i, 255),
								histSat.getFreqAt(i));
						result.addValue("hsv.v.histogram.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histVal.getBorderLeft(i, 255)
								+ "_" + histVal.getBorderRight(i, 255),
								histVal.getFreqAt(i));
					}
				}
				for (int i = 0; i < this.n; i++) {
					if (histHue.getOther1avg(i) != null)
						result.addValue(
								"hsv.normalized.h.histogram.s_avg.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "."
										+ histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
								histHue.getOther1avg(i));
					if (histHue.getOther2avg(i) != null)
						result.addValue(
								"hsv.normalized.h.histogram.v_avg.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "."
										+ histHue.getBorderLeft(i, 255) + "_" + histHue.getBorderRight(i, 255),
								histHue.getOther2avg(i));
					
					if (histSat.getOther1avg(i) != null)
						result.addValue(
								"hsv.normalized.s.histogram.h_avg.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "."
										+ histSat.getBorderLeft(i, 255) + "_" + histSat.getBorderRight(i, 255),
								histSat.getOther1avg(i));
					if (histSat.getOther2avg(i) != null)
						result.addValue(
								"hsv.normalized.s.histogram.v_avg.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "."
										+ histSat.getBorderLeft(i, 255) + "_" + histSat.getBorderRight(i, 255),
								histSat.getOther2avg(i));
					
					if (histVal.getOther1avg(i) != null)
						result.addValue(
								"hsv.normalized.v.histogram.h_avg.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "."
										+ histVal.getBorderLeft(i, 255) + "_" + histVal.getBorderRight(i, 255),
								histVal.getOther1avg(i));
					if (histVal.getOther2avg(i) != null)
						result.addValue(
								"hsv.normalized.v.histogram.s_avg.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "."
										+ histVal.getBorderLeft(i, 255) + "_" + histVal.getBorderRight(i, 255),
								histVal.getOther2avg(i));
				}
			}
			
			if (plantImagePixelCntVgreater0 > 0) {
				double havg = sumOfHue / plantImagePixelCntVgreater0;
				double savg = sumOfSat / plantImagePixelCntVgreater0;
				double vavg = sumOfVal / plantImagePixelCntVgreater0;
				result.addValue("hsv.h.average", havg);
				result.addValue("hsv.s.average", savg);
				result.addValue("hsv.v.average", vavg);
				
				if (mode == Mode.MODE_HUE_VIS_ANALYSIS) {
					result.addValue("hsv.dgci.average", sumOfDGCIs2 / plantImagePixelCntVgreater0);
				}
			}
			
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
				
				if (calcCurtosis) {
					result.addValue("hsv.h.kurtosis", statsHueValuesOverall.getKurtosis());
					result.addValue("hsv.s.kurtosis", statsSatValuesOverall.getKurtosis());
					result.addValue("hsv.v.kurtosis", statsValValuesOverall.getKurtosis());
				}
				
				result.addValue("lab.l.mean", statsLabL.getMean());
				result.addValue("lab.a.mean", statsLabA.getMean());
				result.addValue("lab.b.mean", statsLabB.getMean());
				
				result.addValue("lab.l.stddev", statsLabL.getStandardDeviation());
				result.addValue("lab.a.stddev", statsLabA.getStandardDeviation());
				result.addValue("lab.b.stddev", statsLabB.getStandardDeviation());
				
				result.addValue("lab.l.skewness", statsLabL.getSkewness());
				result.addValue("lab.a.skewness", statsLabA.getSkewness());
				result.addValue("lab.b.skewness", statsLabB.getSkewness());
				
				if (calcCurtosis) {
					result.addValue("lab.l.kurtosis", statsLabL.getKurtosis());
					result.addValue("lab.a.kurtosis", statsLabA.getKurtosis());
					result.addValue("lab.b.kurtosis", statsLabB.getKurtosis());
				}
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
					if (calcCurtosis)
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
			
			if (plantImagePixelCnt > 0)
				result.addValue("intensity.average", sumOfIntensityChlorophyl / plantImagePixelCnt / 255d);
			
			if (addHistogramValues)
				if (addNormalizedHistogramValues && optDistHorizontal != null && optRealMarkerDistance != null) {
					double normalize = optRealMarkerDistance / optDistHorizontal;
					for (int i = 0; i < this.n; i++) {
						result.addValue(
								"normalized.histogram.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histChlorophyl.getBorderLeft(i, 255)
										+ "_" + histChlorophyl.getBorderRight(i, 255),
								histChlorophyl.getFreqAt(i) * normalize);
					}
				} else
					for (int i = 0; i < this.n; i++) {
						result.addValue(
								"histogram.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histChlorophyl.getBorderLeft(i, 255) + "_"
										+ histChlorophyl.getBorderRight(i, 255),
								histChlorophyl.getFreqAt(i));
					}
			
			if (addHistogramValues)
				if (mode == Mode.MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS) {
					if (addNormalizedHistogramValues && optDistHorizontal != null && optRealMarkerDistance != null) {
						double normalize = optRealMarkerDistance / optDistHorizontal;
						for (int i = 0; i < this.n; i++) {
							result.addValue(
									"normalized.histogram.phenol.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "."
											+ histPhenol.getBorderLeft(i, 255) + "_" + histPhenol.getBorderRight(i, 255),
									histPhenol.getFreqAt(i) * normalize);
						}
					} else
						for (int i = 0; i < this.n; i++) {
							result.addValue(
									"histogram.phenol.bin." + StringManipulationTools.formatNumberAddZeroInFront(i + 1, 2) + "." + histPhenol.getBorderLeft(i, 255)
											+ "_" + histPhenol.getBorderRight(i, 255),
									histPhenol.getFreqAt(i));
						}
				}
		}
		return result;
	}
}
