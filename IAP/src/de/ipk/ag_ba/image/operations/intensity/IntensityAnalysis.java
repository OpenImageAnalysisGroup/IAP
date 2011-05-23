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
	
	public ResultsTable calcualteHistorgram(int plantArea) {
		ResultsTable result = new ResultsTable();
		
		int[] pixels = io.getImageAs1array();
		
		double sumOfIntensity = 0;
		
		int back = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		
		Histogram hist = new Histogram(this.n);
		
		for (int c : pixels) {
			if (c == back)
				continue;
			// int r = (c & 0xff0000) >> 16;
			// int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);
			
			sumOfIntensity += (255 - b);
			
			hist.addDataPoint(b, 255);
		}
		
		result.incrementCounter();
		
		result.addValue("intensity.average", sumOfIntensity / plantArea);
		
		for (int i = 0; i < this.n; i++) {
			result.addValue("histogram.bin." + i, hist.getFreqAt(i));
		}
		
		return result;
	}
}
