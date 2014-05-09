package de.ipk.ag_ba.image.operations.segmentation;

import org.Vector2i;

public interface Segmentation {
	
	public abstract void detectClusters();
	
	public abstract Vector2i[] getClusterCenterPoints();
	
	public abstract Vector2i[] getClusterDimension();
	
	public abstract int[] getClusterSize();
	
	public abstract int getClusterCount();
	
	public abstract int getForegroundPixelCount();
	
	int[] getClusterDimensionMinWH();
	
	public abstract int[] getImageClusterIdMask();
	
	public abstract int[] getImage1A();
	
	public abstract void printOriginalImage();
	
	public abstract void printClusterIds();
}