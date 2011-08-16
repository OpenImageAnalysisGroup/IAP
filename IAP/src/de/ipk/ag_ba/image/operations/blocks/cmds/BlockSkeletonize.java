package de.ipk.ag_ba.image.operations.blocks.cmds;

import ij.measure.ResultsTable;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

/**
 * calculate the skeleton to detect the leafs and the clade
 * 
 * @author pape
 */
public class BlockSkeletonize extends AbstractSnapshotAnalysisBlockFIS {
	
	private boolean debug = false;
	private boolean debug2 = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = getInput().getMasks().getVis();
		FlexibleImage fluo = getInput().getMasks().getFluo() != null ? getInput().getMasks().getFluo().copy() : null;
		FlexibleImage res = vis;
		if (options.getCameraPosition() == CameraPosition.SIDE && vis != null && fluo != null) {
			FlexibleImage viswork = vis.copy().getIO()// .medianFilter32Bit()
					// .closing(3, 3)
					// .erode()
					.dilateHorizontal(5)
					.blur(1)
					.getImage().print("vis", debug);
			
			if (viswork != null)
				if (options.isMaize() && vis != null && fluo != null) {
					getProperties().setImage("skeleton", calcSkeleton(viswork, vis, fluo));
					res = getProperties().getImage("beforeBloomEnhancement");
				}
		}
		return res;
	}
	
	public FlexibleImage calcSkeleton(FlexibleImage inp, FlexibleImage vis, FlexibleImage fluo) {
		// ***skeleton calculations***
		SkeletonProcessor2d skel2d = new SkeletonProcessor2d(getInvert(inp.getIO().skeletonize().getImage()));
		skel2d.findEndpointsAndBranches2();
		skel2d.print("endpoints and branches", debug);
		
		double xf = fluo.getWidth() / (double) vis.getWidth();
		double yf = fluo.getHeight() / (double) vis.getHeight();
		int h = vis.getHeight();
		
		skel2d.deleteShortEndLimbs(10, true, new HashSet<Point>());
		FlexibleImage probablyBloomFluo = skel2d.calcProbablyBloomImage(fluo.getIO().blur(10).getImage().print("blurf", false), 0.075f, h, 20).getIO().// blur(3).
				thresholdGrayClearLowerThan(10, Color.BLACK.getRGB()).getImage();
		
		probablyBloomFluo = probablyBloomFluo.getIO().print("BEFORE", debug2).medianFilter32Bit().invert().removeSmallClusters(true, null).
				erode().erode().erode().erode().invert().
				getImage();
		
		if (debug2) {
			FlexibleImageStack fis = new FlexibleImageStack();
			fis.addImage("PROB", probablyBloomFluo);
			fis.addImage("FLUO", fluo);
			fis.print("CHECK THIS");
		}
		
		HashSet<Point> knownBloompoints = skel2d.detectBloom(probablyBloomFluo, xf, yf);
		int bloomLimbCount = knownBloompoints.size();
		skel2d.deleteShortEndLimbs(10, false, knownBloompoints);
		skel2d.detectBloom(probablyBloomFluo, xf, yf);
		
		int leafcount = skel2d.endlimbs.size();
		FlexibleImage skelres = skel2d.getAsFlexibleImage();
		int leaflength = skelres.getIO().countFilledPixels(SkeletonProcessor2d.background);
		leafcount -= bloomLimbCount;
		
		// ***Out***
		// System.out.println("leafcount: " + leafcount + " leaflength: " + leaflength + " numofendpoints: " + skel2d.endpoints.size());
		FlexibleImage result = MapOriginalOnSkelUseingMedian(skelres, vis, Color.BLACK.getRGB());
		result.print("res", debug);
		FlexibleImage result2 = skel2d.copyONOriginalImage(vis);
		result2.print("res2", debug);
		
		// ***Saved***
		BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
		double normFactor = distHorizontal != null ? options.getIntSetting(Setting.REAL_MARKER_DISTANCE) / distHorizontal.getValue() : 1;
		ResultsTable rt = new ResultsTable();
		rt.incrementCounter();
		
		rt.addValue("bloom.area.size", probablyBloomFluo.getIO().print("BLOOM AREA", false).countFilledPixels());
		
		rt.addValue("bloom.count", bloomLimbCount);
		rt.addValue("leaf.count", leafcount);
		if (leafcount > 0) {
			if (distHorizontal != null)
				rt.addValue("leaf.length.sum.norm", leaflength * normFactor);
			rt.addValue("leaf.length.sum", leaflength);
		}
		if (bloomLimbCount > 0)
			rt.addValue("bloom", 1);
		else
			rt.addValue("bloom", 0);
		
		if (leafcount > 0) {
			if (distHorizontal != null)
				rt.addValue("leaf.length.avg.norm", leaflength * normFactor / leafcount);
			rt.addValue("leaf.length.avg", leaflength / leafcount);
		}
		
		if (options.getCameraPosition() == CameraPosition.SIDE && rt != null)
			getProperties().storeResults(
					"RESULT_side.", rt,
					getBlockPosition());
		if (options.getCameraPosition() == CameraPosition.TOP && rt != null)
			getProperties().storeResults(
					"RESULT_top.", rt,
					getBlockPosition());
		
		return skel2d.getAsFlexibleImage();
	}
	
	private FlexibleImage MapOriginalOnSkelUseingMedian(FlexibleImage skeleton, FlexibleImage original, int back) {
		int w = skeleton.getWidth();
		int h = skeleton.getHeight();
		int[] img = skeleton.getAs1A().clone();
		int[] oi = original.getAs1A().clone();
		int last = img.length - w;
		for (int i = 0; i < img.length; i++) {
			if (i > w && i < last && img[i] != back) {
				int center = oi[i];
				int above = oi[i - w];
				int left = oi[i - 1];
				int right = oi[i + 1];
				int below = oi[i + w];
				img[i] = median(center, above, left, right, below);
			} else
				img[i] = img[i];
		}
		return new FlexibleImage(w, h, img);
	}
	
	private int median(int center, int above, int left, int right, int below) {
		int[] temp = { center, above, left, right, below };
		java.util.Arrays.sort(temp);
		return temp[2];
	}
	
	private int avg(int mask, int plant) {
		int r, g, b, r2, g2, b2;
		r = (mask & 0xff0000) >> 16;
		g = (mask & 0x00ff00) >> 8;
		b = (mask & 0x0000ff);
		
		r2 = (plant & 0xff0000) >> 16;
		g2 = (plant & 0x00ff00) >> 8;
		b2 = (plant & 0x0000ff);
		
		float ll = ImageOperation.labCube[r][g][b];
		float la = ImageOperation.labCube[r][g][b + 256];
		float lb = ImageOperation.labCube[r][g][b + 512];
		
		float l2l = ImageOperation.labCube[r2][g2][b2];
		float l2a = ImageOperation.labCube[r2][g2][b2 + 256];
		float l2b = ImageOperation.labCube[r2][g2][b2 + 512];
		double of = 0.7;
		double off = 0.3;
		return new Color_CIE_Lab(Math.min(ll, l2l), la * of + l2a * off, lb * of + l2b * off).getRGB();
	}
	
	/**
	 * Function to invert skeleton image, invert from class imageoperation does not work
	 * 
	 * @param input
	 * @return
	 */
	private FlexibleImage getInvert(FlexibleImage input) {
		int[][] img = input.getAs2A();
		int width = img.length;
		int height = img[0].length;
		int[][] res = new int[width][height];
		int black = Color.BLACK.getRGB();
		int white = Color.WHITE.getRGB();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				if (img[x][y] == -768) // -768 Background should be added, depends on the values of the imagej skeleton, color.White dont work
					res[x][y] = black;
				else {
					res[x][y] = white;
				}
			}
		}
		return new FlexibleImage(res);
	}
	
	@Override
	public void postProcessResultsForAllAngles(TreeMap<Double, BlockProperties> allResultsForSnapshot, BlockProperties summaryResult) {
		Double maxLeafcount = -1d;
		Double maxLeaflength = -1d;
		Double maxLeaflengthNorm = -1d;
		ArrayList<Double> lc = new ArrayList<Double>();
		
		Integer a = null;
		searchLoop: for (Double key : allResultsForSnapshot.keySet()) {
			BlockProperties rt = allResultsForSnapshot.get(key);
			for (BlockPropertyValue v : rt.getProperties("RESULT_top.main.axis.rotation")) {
				if (v.getValue() != null) {
					a = v.getValue().intValue();
					// System.out.println("main.axis.rotation: " + a);
					break searchLoop;
				}
			}
		}
		
		Double bestAngle = null;
		if (a != null) {
			a = a % 180;
			Double bestDiff = Double.MAX_VALUE;
			for (Double d : allResultsForSnapshot.keySet()) {
				if (d >= 0) {
					double dist = Math.abs(a - d);
					if (dist < bestDiff) {
						bestAngle = d;
						bestDiff = dist;
					}
				}
			}
		}
		// System.out.println("ANGLES WITHIN SNAPSHOT: " + allResultsForSnapshot.size());
		for (Double key : allResultsForSnapshot.keySet()) {
			BlockProperties rt = allResultsForSnapshot.get(key);
			
			if (bestAngle != null && key == bestAngle) {
				// System.out.println("Best side angle: " + bestAngle);
				Double cnt = null;
				for (BlockPropertyValue v : rt.getProperties("RESULT_side.leaf.count")) {
					if (v.getValue() != null)
						cnt = v.getValue();
				}
				if (cnt != null) {
					summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.leaf.count.best", cnt);
					// System.out.println("Leaf count for best side image: " + cnt);
				}
			}
			
			for (BlockPropertyValue v : rt.getProperties("RESULT_side.leaf.count")) {
				if (v.getValue() != null) {
					if (v.getValue() > maxLeafcount)
						maxLeafcount = v.getValue();
					lc.add(v.getValue());
				}
			}
			for (BlockPropertyValue v : rt.getProperties("RESULT_side.leaf.length.sum")) {
				if (v.getValue() != null) {
					if (v.getValue() > maxLeaflength)
						maxLeaflength = v.getValue();
				}
			}
			for (BlockPropertyValue v : rt.getProperties("RESULT_side.leaf.length.sum.norm")) {
				if (v.getValue() != null) {
					if (v.getValue() > maxLeaflengthNorm)
						maxLeaflengthNorm = v.getValue();
				}
			}
		}
		
		if (maxLeafcount > 0) {
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.leaf.count.max", maxLeafcount);
			// System.out.println("MAX leaf count: " + maxLeafcount);
			Double[] lca = lc.toArray(new Double[] {});
			Arrays.sort(lca);
			Double median = lca[lca.length / 2];
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.leaf.count.median", median);
		}
		if (maxLeaflength > 0)
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.leaf.length.sum.max", maxLeaflength);
		if (maxLeaflengthNorm > 0)
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.leaf.length.sum.norm.max", maxLeaflengthNorm);
	}
}
