package de.ipk.ag_ba.image.operations.intensity;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;

public class IntensityAnalysis {
	
	private final ImageOperation io;
	private final int n;
	
	public IntensityAnalysis(ImageOperation imageOperation, int numberOfIntervals) {
		this.io = imageOperation;
		this.n = numberOfIntervals;
	}
	
	public ResultsTable calculateHistorgram(BlockProperty optDistHorizontal, Integer optRealMarkerDistance, boolean multiLevel) {
		ResultsTable result = new ResultsTable();
		
		int[] pixels = io.getImageAs1array();
		
		double sumOfIntensityChlorophyl = 0;
		double sumOfIntensityPhenol = 0;
		double sumOfIntensityClassic = 0;
		
		int background = ImageOperation.BACKGROUND_COLORint;
		
		Histogram histChlorophyl = new Histogram(this.n);
		int plantImagePixelCnt = 0;
		for (int c : pixels) {
			if (c == background)
				continue;
			plantImagePixelCnt++;
			int r_intensityClassic = (c & 0xff0000) >> 16;
			int g_intensityChlorophyl = (c & 0x00ff00) >> 8;
			int b_intensityPhenol = (c & 0x0000ff);
			
			sumOfIntensityChlorophyl += (255 - g_intensityChlorophyl);
			if (multiLevel) {
				sumOfIntensityPhenol += (255 - b_intensityPhenol);
				sumOfIntensityClassic += (255 - r_intensityClassic);
			}
			histChlorophyl.addDataPoint(g_intensityChlorophyl, 255);
		}
		
		result.incrementCounter();
		
		if (multiLevel) {
			if (plantImagePixelCnt > 0) {
				result.addValue("intensity.chlorophyl.average", sumOfIntensityChlorophyl / plantImagePixelCnt / 255d);
				result.addValue("intensity.phenol.average", sumOfIntensityPhenol / plantImagePixelCnt / 255d);
			}
			if (sumOfIntensityChlorophyl > 0)
				result.addValue("intensity.phenol.chlorophyl.ratio", sumOfIntensityPhenol / sumOfIntensityChlorophyl);
			
		}
		result.addValue("intensity.average", sumOfIntensityChlorophyl / plantImagePixelCnt / 255d);
		
		if (optDistHorizontal != null && optRealMarkerDistance != null) {
			double normalize = optRealMarkerDistance / optDistHorizontal.getValue();
			for (int i = 0; i < this.n; i++) {
				result.addValue("histogram.bin." + (i + 1) + "." + histChlorophyl.getBorderLeft(i, 255) + "_" + histChlorophyl.getBorderRight(i, 255),
						histChlorophyl.getFreqAt(i) * normalize);
			}
		}
		for (int i = 0; i < this.n; i++) {
			result.addValue(
					"chlorophyl.normalized.histogram.bin." + (i + 1) + "." + histChlorophyl.getBorderLeft(i, 255) + "_" + histChlorophyl.getBorderRight(i, 255),
					histChlorophyl.getFreqAt(i));
		}
		return result;
	}
}
