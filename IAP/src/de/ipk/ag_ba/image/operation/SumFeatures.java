package de.ipk.ag_ba.image.operation;

public class SumFeatures {
	float[] sumarray;
	int n = 0;
	
	public SumFeatures(int n) {
		this.sumarray = new float[n];
	}
	
	public void sumUp(FeatureVector in) {
		for (int i = 0; i < in.numFeatures.size(); i++) {
			sumarray[i] += in.numFeatures.get(i);
		}
		n++;
		
	}
}
