package de.ipk.ag_ba.image.operations.intensity;

public class Histogram {
	public static enum Mode {
		MODE_GRAY_NIR_ANALYSIS, MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS, MODE_HUE_VIS_ANALYSIS, MODE_HUE_RGB_ANALYSIS, MODE_IR_ANALYSIS
	}
	
	private final double[] freqMain, freqOther1, freqOther2; // freq[i] = # occurences of value i
	private double maxFreqInAnyBin; // max frequency of any value
	private final int bins;
	
	double[] other1sum;
	double[] other2sum;
	
	// Create a new histogram.
	public Histogram(int bins) {
		freqMain = new double[bins];
		freqOther1 = new double[bins];
		freqOther2 = new double[bins];
		other1sum = new double[bins];
		other2sum = new double[bins];
		this.bins = bins;
	}
	
	// Add one occurrence of the value i.
	public void addDataPoint(int i, double other1, double other2) {
		addDataPoint(i, other1, other2, true, true);
	}
	
	public void addDataPoint(int i, double other1, double other2, boolean addOther1, boolean addOther2) {
		freqMain[i]++;
		if (freqMain[i] > maxFreqInAnyBin)
			maxFreqInAnyBin = freqMain[i];
		
		if (addOther1) {
			other1sum[i] += other1;
			freqOther1[i]++;
		}
		if (addOther2) {
			other2sum[i] += other2;
			freqOther2[i]++;
		}
	}
	
	public void addDataPoint(int value, int maxValue, double other1, double other2) {
		addDataPoint((int) (value / (double) maxValue * (bins - 1)), other1, other2);
	}
	
	public void addDataPoint(int value, int maxValue, double other1, double other2, boolean addOther1, boolean addOther2) {
		addDataPoint((int) (value / (double) maxValue * (bins - 1)), other1, other2, addOther1, addOther2);
	}
	
	public void addDataPoint(int value, int maxValue, int other1, int other2, boolean ignoreZeroOther1, boolean ignoreZeroOther2) {
		addDataPoint((int) (value / (double) maxValue * (bins - 1)), other1, other2, !ignoreZeroOther1 || other1 != 0, !ignoreZeroOther2 || other2 != 0);
	}
	
	public double getFreqAt(int i) {
		return freqMain[i];
	}
	
	public int getBorderLeft(int i, int maxValue) {
		return i * maxValue / bins;
	}
	
	public int getBorderRight(int i, int maxValue) {
		return (i + 1) * maxValue / bins;
	}
	
	public Double getOther1avg(int i) {
		if (freqOther1[i] == 0 || Double.isNaN(other1sum[i]))
			return null;
		else
			return other1sum[i] / freqOther1[i];
	}
	
	public Double getOther2avg(int i) {
		if (freqOther2[i] == 0 || Double.isNaN(other2sum[i]))
			return null;
		else
			return other2sum[i] / freqOther2[i];
	}
}
