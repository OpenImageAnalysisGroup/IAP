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
		
		double weightOfPlant = 0;
		
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
			
			double intensityPhenol = (255d - b_intensityPhenol) / 255d;
			weightOfPlant += (1d - (1 - 1 / 7d) * intensityPhenol);
			// if (intensityPhenol > 0.1)
			// System.out.println("PHENOL: " + intensityPhenol + ", pixel weight: " + (1d - (1 - 1 / 7d) * intensityPhenol));
			
			sumOfIntensityChlorophyl += (255 - g_intensityChlorophyl);
			if (multiLevel) {
				sumOfIntensityPhenol += (255 - b_intensityPhenol);
				sumOfIntensityClassic += (255 - r_intensityClassic);
			}
			histChlorophyl.addDataPoint(g_intensityChlorophyl, 255);
		}
		
		result.incrementCounter();
		
		if (multiLevel) {
			result.addValue("intensity.phenol.plant_weight", weightOfPlant);
			result.addValue("intensity.phenol.plant_weight_drought_loss", plantImagePixelCnt - weightOfPlant);
		}
		
		result.addValue("filled.pixels", plantImagePixelCnt);
		result.addValue("filled.percent", (100d * pixels.length) / plantImagePixelCnt);
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
				result.addValue("histogram.bin." + (i + 1) + "." + histChlorophyl.getBorderLeft(i, 255) + "_" + histChlorophyl.getBorderRight(i, 255),
						histChlorophyl.getFreqAt(i) * normalize);
			}
		}
		for (int i = 0; i < this.n; i++) {
			result.addValue(
					"normalized.histogram.bin." + (i + 1) + "." + histChlorophyl.getBorderLeft(i, 255) + "_" + histChlorophyl.getBorderRight(i, 255),
					histChlorophyl.getFreqAt(i));
		}
		return result;
	}
}
