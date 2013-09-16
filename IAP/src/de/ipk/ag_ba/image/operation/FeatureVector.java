package de.ipk.ag_ba.image.operation;

import java.awt.Color;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class FeatureVector {
	public ArrayList<Float> numFeatures;
	
	public int acCluster;
	
	public FeatureVector(int clu) {
		this.acCluster = clu;
		this.numFeatures = new ArrayList<Float>();
	}
	
	public FeatureVector(Color c, float xr, float yr) {
		float L;
		float a;
		float b;
		int rgb = c.getRGB();
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
	
	public double euclidianDistance(FeatureVector inp) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException {
		double dist = 0.0;
		
		for (int i = 0; i < this.numFeatures.size(); i++) {
			dist += (this.numFeatures.get(i) - inp.numFeatures.get(i)) * (this.numFeatures.get(i) - inp.numFeatures.get(i));
		}
		return dist;
	}
	
	public int getAcCluster() {
		return acCluster;
	}
	
	public void setAcCluster(int acCluster) {
		this.acCluster = acCluster;
	}
}
