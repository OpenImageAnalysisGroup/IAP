package de.ipk.ag_ba.image.operations.intensity;

public class Histogram {
	public static enum Mode {
		MODE_GRAY_NIR_ANALYSIS, MODE_MULTI_LEVEL_RGB_FLUO_ANALYIS, MODE_HUE_VIS_ANALYSIS, MODE_HUE_RGB_ANALYSIS, MODE_IR_ANALYSIS
	}
	
	private final double[] freq; // freq[i] = # occurences of value i
	private double max; // max frequency of any value
	private final int n;
	
	double[] other1sum;
	double[] other2sum;
	
	// Create a new histogram.
	public Histogram(int N) {
		freq = new double[N];
		other1sum = new double[N];
		other2sum = new double[N];
		this.n = N;
	}
	
	// Add one occurrence of the value i.
	public void addDataPoint(int i, double other1, double other2) {
		freq[i]++;
		if (freq[i] > max)
			max = freq[i];
		other1sum[i] += other1;
		other2sum[i] += other2;
	}
	
	public void addDataPoint(int value, int maxValue, double other1, double other2) {
		addDataPoint((int) (value / (double) maxValue * (n - 1)), other1, other2);
	}
	
	public double getFreqAt(int i) {
		return freq[i];
	}
	
	public int getBorderLeft(int i, int maxValue) {
		return i * maxValue / n;
	}
	
	public int getBorderRight(int i, int maxValue) {
		return (i + 1) * maxValue / n;
	}
	
	public Double getOther1avg(int i) {
		if (freq[i] == 0 || Double.isNaN(other1sum[i]))
			return null;
		else
			return other1sum[i] / freq[i];
	}
	
	public Double getOther2avg(int i) {
		if (freq[i] == 0 || Double.isNaN(other2sum[i]))
			return null;
		else
			return other2sum[i] / freq[i];
	}
}
