package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockMatchBrightness extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		return match(input().images().vis(), input().masks().vis());
	}
	
	private FlexibleImage match(FlexibleImage img, FlexibleImage mask) {
		if (img == null || mask == null)
			return null;
		double[] edgeLabImage = getLab(img, 0.05);
		double[] edgeLabMasks = getLab(mask, 0.05);
		int nearestIdx = getNearestIndex(edgeLabImage, edgeLabMasks);
		double i = edgeLabImage[nearestIdx];
		double m = edgeLabMasks[nearestIdx];
		double r = m / i;
		if (r < 1)
			r = r * 0.9;
		else
			r = r * 1.1;
		return new ImageOperation(img).gamma(r).getImage();
	}
	
	private int getNearestIndex(double[] edgeLabImage, double[] edgeLabMasks) {
		int li = 0;
		double ldiff = Double.MAX_VALUE;
		for (int i = 0; i < edgeLabImage.length; i++) {
			double diff = Math.abs(edgeLabImage[i] - edgeLabMasks[i]);
			if (diff < ldiff)
				li = i;
		}
		return li;
	}
	
	private double[] getLab(FlexibleImage vis, double d) {
		ImageOperation io = new ImageOperation(vis);
		int s = (int) (vis.getWidth() * d);
		int w = vis.getWidth();
		int h = vis.getHeight();
		int[][] i = io.getImageAs2array();
		return new double[] {
				io.getLABAverage(i, 0, 0, s, s).getAverageA(), io.getLABAverage(i, w - s, 0, s, s).getAverageA(),
				io.getLABAverage(i, 0, h - s, s, s).getAverageA(), io.getLABAverage(i, w - s, h - s, s, s).getAverageA() };
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		return match(input().images().fluo(), input().masks().fluo());
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		return match(input().images().nir(), input().masks().nir());
	}
}
