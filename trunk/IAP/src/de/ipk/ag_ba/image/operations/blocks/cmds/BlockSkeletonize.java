package de.ipk.ag_ba.image.operations.blocks.cmds;

import ij.measure.ResultsTable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * calculate the skeleton to detect the leafs and the clade
 * 
 * @author pape
 */
public class BlockSkeletonize extends AbstractSnapshotAnalysisBlockFIS {
	
	private boolean debug = true;
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = getInput().getMasks().getVis();
		FlexibleImage res = vis;
		FlexibleImage viswork = vis.copy().getIO().medianFilter32Bit().closing(3, 3).getImage().print("vis", debug);
		
		if (viswork != null)
			if (options.isMaize())
				if (options.getCameraPosition() == CameraPosition.SIDE)
					res = calcSkeleton(viswork, vis);
		
		return res;
	}
	
	public FlexibleImage calcSkeleton(FlexibleImage inp, FlexibleImage vis) {
		// ***skeleton calculations***
		SkeletonProcessor2d skel2d = new SkeletonProcessor2d(getInvert(inp.getIO().skeletonize().getImage()));
		skel2d.findEndpointsAndBranches();
		skel2d.print("endpoints and branches", debug);
		// skel2d.removeBurls();
		skel2d.deleteShortEndLimbs(20);
		skel2d.calculateEndlimbsRecursive();
		
		int leafcount = skel2d.endlimbs.size();
		FlexibleImage fires = skel2d.getAsFlexibleImage();
		int leaflength = fires.getIO().countFilledPixels(skel2d.background);
		
		// ***Out***
		// System.out.println("leafcount: " + leafcount + " leaflength: " + leaflength + " numofendpoints: " + skel2d.endpoints.size());
		FlexibleImage result = MapOriginalOnSkelUseingMedian(fires, vis, Color.BLACK.getRGB());
		result.print("res", debug);
		FlexibleImage result2 = copyONOriginalImage(fires, vis, Color.BLACK.getRGB());
		result2.print("res2", debug);
		
		// ***Saved***
		BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
		double normFactor = distHorizontal != null ? options.getIntSetting(Setting.REAL_MARKER_DISTANCE) / distHorizontal.getValue() : 1;
		ResultsTable rt = new ResultsTable();
		rt.incrementCounter();
		rt.addValue("leaf.count", leafcount);
		if (leafcount > 0) {
			if (distHorizontal != null)
				rt.addValue("leaf.length.sum.norm", leaflength * normFactor);
			rt.addValue("leaf.length.sum", leaflength);
		}
		
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
		
		return result2;
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
	
	/**
	 * Maps skeleton on original image
	 * 
	 * @param fires
	 * @param orig
	 * @param back
	 * @return
	 */
	private FlexibleImage copyONOriginalImage(FlexibleImage fires, FlexibleImage orig, int back) {
		int[] fi = fires.getAs1A();
		int[] oi = orig.getAs1A();
		for (int index = 0; index < fi.length; index++) {
			if (fi[index] == back)
				fi[index] = oi[index];
		}
		return new FlexibleImage(fires.getWidth(), fires.getHeight(), fi);
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
		// System.out.println("ANGLES WITHIN SNAPSHOT: " + allResultsForSnapshot.size());
		for (BlockProperties rt : allResultsForSnapshot.values()) {
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
