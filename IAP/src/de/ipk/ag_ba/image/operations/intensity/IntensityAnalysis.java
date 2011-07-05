package de.ipk.ag_ba.image.operations.intensity;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

public class IntensityAnalysis {
	
	private final ImageOperation io;
	private final int n;
	
	public IntensityAnalysis(ImageOperation imageOperation, int numberOfIntervals) {
		this.io = imageOperation;
		this.n = numberOfIntervals;
	}
	
	public ResultsTable calculateHistorgram() {
		ResultsTable result = new ResultsTable();
		
		int[] pixels = io.getImageAs1array();
		
		double sumOfIntensity = 0;
		
		int background = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		
		Histogram hist = new Histogram(this.n);
		int plantImagePixelCnt = 0;
		for (int c : pixels) {
			if (c == background)
				continue;
			plantImagePixelCnt++;
			// int r = (c & 0xff0000) >> 16;
			// int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);
			
			sumOfIntensity += (255 - b);
			
			hist.addDataPoint(b, 255);
		}
		
		result.incrementCounter();
		
		result.addValue("intensity.average", sumOfIntensity / plantImagePixelCnt / 255d);
		// double realDist = 1;
		// if (markerDistHorizontal != null) {
		// double normalize = ((realDist * realDist) / (markerDistHorizontal.getValue() * markerDistHorizontal.getValue()));
		
		for (int i = 0; i < this.n; i++) {
			result.addValue("histogram.bin." + (i + 1) + "." + hist.getBorderLeft(i, 255) + "_" + hist.getBorderRight(i, 255), hist.getFreqAt(i)); // * normalize
		}
		// }
		return result;
	}
}
