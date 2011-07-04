package de.ipk.ag_ba.image.operations.segmentation;

import org.Vector2d;
import org.Vector2i;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

public class ClusterDetection implements Segmentation {
	
	private final int[] img;
	private final int[] clu;
	private final int w;
	private final int h;
	private final int back;
	private int[] clusterPixelCount;
	private int numberOfClusters;
	private Vector2d[] centerPoints;
	private Vector2d[] widthAndHeight;
	private int foregroundPixelCount;
	
	public ClusterDetection(FlexibleImage image) {
		this.img = image.getAs1A();
		this.clu = new int[img.length];
		this.w = image.getWidth();
		this.h = image.getHeight();
		this.back = PhenotypeAnalysisTask.BACKGROUND_COLORint;
	}
	
	@Override
	public void detectClusters() {
		int x = 0, y = 0;
		int currentClusterID = 0;
		int idx = 0;
		this.foregroundPixelCount = 0;
		for (int c : img) {
			if (c != back)
				foregroundPixelCount++;
		}
		
		int[] queue = new int[w * h];
		for (int c : img) {
			if (c != back && clu[idx] == 0) {
				currentClusterID++;
				assignCluster(currentClusterID, x, y, queue);
			}
			idx++;
			x++;
			if (x == w) {
				x = 0;
				y++;
			}
		}
		idx = 0;
		for (int cl : clu)
			clu[idx++] = cl - 1;
		numberOfClusters = currentClusterID;
		if (currentClusterID > 0) {
			this.clusterPixelCount = new int[currentClusterID];
			Vector2i[] clusterMinXY = new Vector2i[currentClusterID];
			for (int i = 0; i < clusterMinXY.length; i++)
				clusterMinXY[i] = new Vector2i(w, h);
			Vector2i[] clusterMaxXY = new Vector2i[currentClusterID];
			for (int i = 0; i < clusterMaxXY.length; i++)
				clusterMaxXY[i] = new Vector2i(0, 0);
			x = 0;
			y = 0;
			idx = 0;
			for (int cl : clu) {
				if (cl > 0) {
					clusterPixelCount[cl]++;
					if (x < clusterMinXY[cl].x)
						clusterMinXY[cl].x = x;
					if (x < clusterMinXY[cl].y)
						clusterMinXY[cl].y = y;
					if (x > clusterMaxXY[cl].x)
						clusterMaxXY[cl].x = x;
					if (x > clusterMaxXY[cl].y)
						clusterMaxXY[cl].y = y;
				}
				idx++;
				x++;
				if (x == w) {
					x = 0;
					y++;
				}
			}
			this.centerPoints = new Vector2d[numberOfClusters];
			this.widthAndHeight = new Vector2d[numberOfClusters];
			for (int i = 0; i < numberOfClusters; i++) {
				double xd = (clusterMaxXY[i].x - clusterMinXY[i].x) / 2d + clusterMinXY[i].x;
				double yd = (clusterMaxXY[i].x - clusterMinXY[i].x) / 2d + clusterMinXY[i].x;
				double wd = clusterMaxXY[i].x - clusterMinXY[i].x;
				double hd = clusterMaxXY[i].y - clusterMinXY[i].y;
				centerPoints[i] = new Vector2d(xd, yd);
				widthAndHeight[i] = new Vector2d(wd, hd);
			}
		} else {
			this.centerPoints = new Vector2d[0];
			this.widthAndHeight = new Vector2d[0];
		}
	}
	
	private void assignCluster(int currentClusterID, int x, int y, int[] queue) {
		// LinkedList<Integer> queue = new LinkedList<Integer>();
		int qL = 0, qR = 0;
		Integer idx = x + y * w;
		// queue.add(idx);
		queue[++qR] = idx;
		// while (!queue.isEmpty()) {
		while (qR > qL) {
			// idx = queue.poll();
			idx = queue[qL++];
			clu[idx] = currentClusterID;
			if (idx > 0 && img[idx - 1] != back && clu[idx - 1] == 0)
				queue[++qR] = idx - 1;
			// queue.add(idx - 1);
			if (idx > w && img[idx - w] != back && clu[idx - w] == 0)
				queue[++qR] = idx - w;
			// queue.add(idx - w);
			if (idx + 1 < img.length && img[idx + 1] != back && clu[idx + 1] == 0)
				queue[++qR] = idx + 1;
			// queue.add(idx + 1);
			if (idx < img.length - w && img[idx + w] != back && clu[idx + w] == 0)
				queue[++qR] = idx + w;
			// queue.add(idx + w);
		}
	}
	
	@Override
	public Vector2d[] getClusterCenterPoints() {
		return centerPoints;
	}
	
	@Override
	public Vector2d[] getClusterDimension() {
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
		Vector2d[] vector2ds = getClusterDimension();
		int[] res = new int[vector2ds.length];
		for (int index = 0; index < vector2ds.length; index++) {
			double w = vector2ds[index].x;
			double h = vector2ds[index].y;
			if (w > h)
				res[index] = (int) h;
			else
				res[index] = (int) w;
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
}
