package de.ipk.ag_ba.image.operations.segmentation;

import org.Vector2d;

public interface Segmentation {
	
	public abstract void detectClusters();
	
	public abstract Vector2d[] getClusterCenterPoints();
	
	public abstract Vector2d[] getClusterDimension();
	
	public abstract int[] getClusterSize();
	
	public abstract int getClusterCount();
	
	public abstract int getForegroundPixelCount();
	
	int[] getClusterDimensionMinWH();
	
	public abstract int[] getImageClusterIdMask();
	
	public abstract int[] getImage1A();
}