package de.ipk.ag_ba.image.operation;

import java.awt.Color;

import de.ipk.ag_ba.image.color.ColorUtil;

public class FeatureVector {
	public float[] numFeatures = new float[2];
	
	public int acCluster;
	
	public FeatureVector(int clu) {
		this.acCluster = clu;
	}
	
	public FeatureVector(Color c, float[][][] lc) {
		this(c.getRGB(), 0f, 0f, lc);
	}
	
	public FeatureVector(int c, float xr, float yr, float[][][] lc) {
		float L;
		float a;
		float b;
		int rgb = c;
		
		int red = ((rgb >> 16) & 0xff);
		int green = ((rgb >> 8) & 0xff);
		int blue = (rgb & 0xff);
		
		// float[] hsb = new float[3];
		// Color.RGBtoHSB(red, green, blue, hsb);
		
		// L = lc[red][green][blue];
		a = lc[red][green][blue + 256];
		b = lc[red][green][blue + 512];
		
		// addNumericFeature(xr);
		// addNumericFeature(yr);
		// addNumericFeature(hsb[0]);
		// addNumericFeature(hsb[1]);
		// addNumericFeature(hsb[2]);
		// numFeatures[0] = L / 255f;
		numFeatures[0] = a / 255f;
		numFeatures[1] = b / 255f;
	}
	
	public FeatureVector copy() {
		FeatureVector feat = new FeatureVector(this.acCluster);
		for (int idx = 0; idx < numFeatures.length; idx++) {
			feat.numFeatures[idx] = numFeatures[idx];
		}
		return feat;
	}
	
	public FeatureVector(SumFeatures in) {
		for (int idx = 0; idx < numFeatures.length; idx++) {
			if (in.n > 0)
				numFeatures[idx] = in.sumarray[idx] / in.n;
			else
				numFeatures[idx] = Float.NaN;
		}
	}
	
	public FeatureVector() {
		// empty
	}
	
	public float euclidianDistance(FeatureVector inp) {
		float dist = 0.0f;
		
		for (int i = 0; i < numFeatures.length; i++) {
			float a = numFeatures[i] - inp.numFeatures[i];
			dist += a * a;
		}
		return dist;
	}
	
	public double colorDistance(FeatureVector inp) {
		return ColorUtil.deltaE2000(
				numFeatures[0], numFeatures[1], numFeatures[2],
				inp.numFeatures[0], inp.numFeatures[1], inp.numFeatures[2]);
	}
	
	public int getAcCluster() {
		return acCluster;
	}
	
	public void setAcCluster(int acCluster) {
		this.acCluster = acCluster;
	}
}
