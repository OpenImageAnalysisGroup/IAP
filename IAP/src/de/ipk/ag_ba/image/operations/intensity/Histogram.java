package de.ipk.ag_ba.image.operations.intensity;

public class Histogram {
	public static enum Mode {
		MODE_GRAY, MODE_MULTI_LEVEL_RGB, MODE_HUE
	}
	
	private final double[] freq; // freq[i] = # occurences of value i
	private double max; // max frequency of any value
	private final int n;
	
	// Create a new histogram.
	public Histogram(int N) {
		freq = new double[N];
		this.n = N;
	}
	
	// Add one occurrence of the value i.
	public void addDataPoint(int i) {
		freq[i]++;
		if (freq[i] > max)
			max = freq[i];
	}
	
	public void addDataPoint(int value, int maxValue) {
		addDataPoint((int) (value / (double) maxValue * (n - 1)));
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
}
