package de.ipk.ag_ba.image.operation;

import java.awt.Color;
import java.util.ArrayList;

import de.ipk.ag_ba.image.color.ColorUtil;

public class FeatureVector {
	public ArrayList<Float> numFeatures;
	
	public int acCluster;
	
	public FeatureVector(int clu) {
		this.acCluster = clu;
		this.numFeatures = new ArrayList<Float>();
	}
	
	public FeatureVector(Color c) {
		new FeatureVector(c.getRGB(), 0f, 0f);
	}
	
	public FeatureVector(int c, float xr, float yr) {
		float L;
		float a;
		float b;
		int rgb = c;
		this.numFeatures = new ArrayList<Float>();
		
		int red = ((rgb >> 16) & 0xff);
		int green = ((rgb >> 8) & 0xff);
		int blue = (rgb & 0xff);
		
		float[] hsb = new float[3];
		Color.RGBtoHSB(red, green, blue, hsb);
		
		L = ImageOperation.getLabCubeInstance()[red][green][blue];
		a = ImageOperation.getLabCubeInstance()[red][green][blue + 256];
		b = ImageOperation.getLabCubeInstance()[red][green][blue + 512];
		
		// addNumericFeature(xr);
		// addNumericFeature(yr);
		// addNumericFeature(hsb[0]);
		// addNumericFeature(hsb[1]);
		// addNumericFeature(hsb[2]);
		addNumericFeature((float) (L / 255.0));
		addNumericFeature((float) (a / 255.0));
		addNumericFeature((float) (b / 255.0));
		
	}
	
	public FeatureVector copy() {
		FeatureVector feat = new FeatureVector(this.acCluster);
		for (Float f : numFeatures) {
			feat.addNumericFeature(f);
		}
		return feat;
	}
	
	public void addNumericFeature(float a) {
		numFeatures.add(a);
	}
	
	public FeatureVector(SumFeatures in) {
		this.numFeatures = new ArrayList<Float>();
		
		for (float f : in.sumarray) {
			if (in.n > 0)
				numFeatures.add(f / in.n);
			else
				numFeatures.add(Float.NaN);
		}
	}
	
	public FeatureVector() {
		this.numFeatures = new ArrayList<Float>();
	}
	
	public float euclidianDistance(FeatureVector inp) {
		float dist = 0.0f;
		
		for (int i = 0; i < this.numFeatures.size(); i++) {
			float a = this.numFeatures.get(i) - inp.numFeatures.get(i);
			dist += a * a;
		}
		return dist;
	}
	
	public double colorDistance(FeatureVector inp) {
		return ColorUtil.deltaE2000(
				numFeatures.get(0), numFeatures.get(1), numFeatures.get(2),
				inp.numFeatures.get(0), inp.numFeatures.get(1), inp.numFeatures.get(2));
	}
	
	public int getAcCluster() {
		return acCluster;
	}
	
	public void setAcCluster(int acCluster) {
		this.acCluster = acCluster;
	}
}
