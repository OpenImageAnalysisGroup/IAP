package de.ipk.ag_ba.image.operations.segmentation;

import org.Vector2i;

import de.ipk.ag_ba.image.structures.Image;

public class ClusterDetection implements Segmentation {
	
	private final int[] img;
	private final int[] clu;
	private final int w;
	private final int h;
	private final int back;
	private int[] clusterPixelCount;
	private int numberOfClusters;
	private Vector2i[] centerPoints;
	private Vector2i[] widthAndHeight;
	private int foregroundPixelCount;
	
	public ClusterDetection(Image image, int background) {
		this.img = image.getAs1A();
		this.clu = new int[img.length];
		this.w = image.getWidth();
		this.h = image.getHeight();
		// img[1 + w] = background + 1;
		this.back = background;
	}
	
	@Override
	public void detectClusters() {
		int currentClusterID = 0;
		int idx = 0;
		this.foregroundPixelCount = 0;
		
		int[] queue = new int[w * h * 2];
		for (int c : img) {
			if (c != back) {
				foregroundPixelCount++;
				if (clu[idx] == 0) {
					currentClusterID++;
					assignCluster(currentClusterID, idx, queue);
				}
			}
			idx++;
		}
		idx = 0;
		numberOfClusters = currentClusterID;
		if (numberOfClusters > 0) {
			this.clusterPixelCount = new int[numberOfClusters + 1];
			Vector2i[] clusterMinXY = new Vector2i[numberOfClusters + 1];
			for (int i = 0; i < clusterMinXY.length; i++)
				clusterMinXY[i] = new Vector2i(w, h);
			Vector2i[] clusterMaxXY = new Vector2i[numberOfClusters + 1];
			for (int i = 0; i < clusterMaxXY.length; i++)
				clusterMaxXY[i] = new Vector2i(0, 0);
			int x = 0;
			int y = 0;
			idx = 0;
			for (int cl : clu) {
				clusterPixelCount[cl]++;
				if (x < clusterMinXY[cl].x)
					clusterMinXY[cl].x = x;
				if (y < clusterMinXY[cl].y)
					clusterMinXY[cl].y = y;
				if (x > clusterMaxXY[cl].x)
					clusterMaxXY[cl].x = x;
				if (y > clusterMaxXY[cl].y)
					clusterMaxXY[cl].y = y;
				idx++;
				x++;
				if (x == w) {
					x = 0;
					y++;
				}
			}
			this.centerPoints = new Vector2i[numberOfClusters + 1];
			this.widthAndHeight = new Vector2i[numberOfClusters + 1];
			for (int i = 0; i <= numberOfClusters; i++) {
				int xd = (clusterMaxXY[i].x - clusterMinXY[i].x) / 2 + clusterMinXY[i].x;
				int yd = (clusterMaxXY[i].y - clusterMinXY[i].y) / 2 + clusterMinXY[i].y;
				int wd = clusterMaxXY[i].x - clusterMinXY[i].x;
				int hd = clusterMaxXY[i].y - clusterMinXY[i].y;
				centerPoints[i] = new Vector2i(xd, yd);
				widthAndHeight[i] = new Vector2i(wd, hd);
			}
		} else {
			this.centerPoints = new Vector2i[0];
			this.widthAndHeight = new Vector2i[0];
		}
	}
	
	private void assignCluster(int currentClusterID, int idx, int[] queue) {
		int qL = 0, qR = 0;
		queue[++qR] = idx;
		int maxQueueLength = queue.length - 1;
		// int max = 0;
		while (qR != qL) {
			// if (qR - qL > max)
			// max = qR - qL;
			idx = queue[++qL];
			if (qL == queue.length - 1)
				qL = 0;
			if (clu[idx] == currentClusterID)
				continue;
			clu[idx] = currentClusterID;
			int f = idx - 1; // left
			if (idx % w > 0 && img[f] != back && clu[f] == 0) {
				queue[++qR] = f;
				if (qR == maxQueueLength)
					qR = 0;
			}
			f = idx - w; // above
			if (idx > w && img[f] != back && clu[f] == 0) {
				queue[++qR] = f;
				if (qR == maxQueueLength)
					qR = 0;
			}
			f = idx + 1; // right
			if ((idx) % w < w - 1 && img[f] != back && clu[f] == 0) {
				queue[++qR] = f;
				if (qR == maxQueueLength)
					qR = 0;
			}
			f = idx + w; // below
			if (idx < img.length - w && img[f] != back && clu[f] == 0) {
				queue[++qR] = f;
				if (qR == maxQueueLength)
					qR = 0;
			}
		}
	}
	
	@Override
	public Vector2i[] getClusterCenterPoints() {
		return centerPoints;
	}
	
	@Override
	public Vector2i[] getClusterDimension() {
		return widthAndHeight;
	}
	
	@Override
	public int[] getClusterSize() {
		return clusterPixelCount;
	}
	
	@Override
	public int getClusterCount() {
		return numberOfClusters;
	}
	
	@Override
	public int getForegroundPixelCount() {
		return foregroundPixelCount;
	}
	
	@Override
	public int[] getClusterDimensionMinWH() {
		Vector2i[] vector2ds = getClusterDimension();
		int[] res = new int[vector2ds.length];
		for (int index = 0; index < vector2ds.length; index++) {
			int w = vector2ds[index].x;
			int h = vector2ds[index].y;
			if (w > h)
				res[index] = h;
			else
				res[index] = w;
		}
		return res;
	}
	
	@Override
	public int[] getImageClusterIdMask() {
		return clu;
	}
	
	@Override
	public int[] getImage1A() {
		return img;
	}
	
	@Override
	public void printOriginalImage() {
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				System.out.print(img[x + y * w] + " ");
			}
			System.out.println();
		}
	}
	
	@Override
	public void printClusterIds() {
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				System.out.print(clu[x + y * w] + " ");
			}
			System.out.println();
		}
		System.out.println("*************");
	}
}
