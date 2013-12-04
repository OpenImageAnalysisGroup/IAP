package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operation.ImageCanvas;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author pape, klukas
 */
public class BlSkeletonize_Arabidopsis extends AbstractSnapshotAnalysisBlock {
	
	private boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected Image processFLUOmask() {
		Image vis = input().masks().vis();
		Image fluo = input().masks().fluo() != null ? input().masks().fluo().copy() : null;
		if (fluo == null)
			return fluo;
		Image res = fluo.copy();
		
		boolean analyzeSide = getBoolean("Analyze Side Images", false);
		if (analyzeSide)
			if (options.getCameraPosition() == CameraPosition.SIDE && vis != null && fluo != null && getProperties() != null) {
				Image viswork = fluo.copy().show("fluo", debug);
				
				if (viswork != null)
					if (vis != null && fluo != null) {
						Image sk = calcSkeleton(viswork, vis, fluo, fluo.copy());
						if (sk != null) {
							boolean drawSkeleton = getBoolean("Calculate Skeleton", true);
							res = res.io().drawSkeleton(sk, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
							if (res != null)
								getProperties().setImage("skeleton_fluo", sk);
						}
					}
			}
		if (options.getCameraPosition() == CameraPosition.TOP && vis != null && fluo != null && getProperties() != null) {
			ImageOperation in = fluo.copy().io();
			Image viswork = in.blur(getDouble("blur fluo", 0d)).getImage().show("blur fluo res", debug);
			
			if (viswork != null)
				if (vis != null && fluo != null) {
					Image sk = calcSkeleton(viswork, vis, fluo, fluo.copy());
					if (sk != null) {
						boolean drawSkeleton = getBoolean("Calculate Skeleton", true);
						res = res.io().drawSkeleton(sk, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
						if (res != null)
							getProperties().setImage("skeleton_fluo", sk);
					}
				}
		}
		return input().masks().fluo();
	}
	
	public Image calcSkeleton(Image inp, Image vis, Image fluo, Image inpFLUOunchanged) {
		// ***skeleton calculations***
		SkeletonProcessor2d skel2d = new SkeletonProcessor2d(getInvert(inp.io().skeletonize(false).show("input", debug).getImage()));
		skel2d.findEndpointsAndBranches2();
		
		skel2d.print("endpoints and branches", debug);
		
		double xf = fluo.getWidth() / (double) vis.getWidth();
		double yf = fluo.getHeight() / (double) vis.getHeight();
		int w = vis.getWidth();
		int h = vis.getHeight();
		
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		skel2d.deleteShortEndLimbs(getInt("minimum limbs length (absolute)", 15), false, new HashSet<Point>());
		skel2d.deleteShortEndLimbs(-getInt("relative limbs threshold (percent)", 50), false, new HashSet<Point>());
		
		boolean specialLeafWidthCalculations = true;
		Double leafWidthInPixels = null;
		if (specialLeafWidthCalculations) {
			ArrayList<Point> branchPoints = skel2d.getBranches();
			if (branchPoints.size() > 0) {
				int[][] tempImage = new int[w][h];
				int clear = ImageOperation.BACKGROUND_COLORint;
				for (int x = 0; x < w; x++)
					for (int y = 0; y < h; y++)
						tempImage[x][y] = clear;
				Image clearImage = new Image(tempImage).copy();
				int black = Color.BLACK.getRGB();
				for (Point p : branchPoints)
					tempImage[p.x][p.y] = black;
				Image temp = new Image(tempImage);
				temp = temp.io().hull().setCustomBackgroundImageForDrawing(clearImage).
						find(true, false, false, true, false, black, black, black, black, null, 0d).getImage();
				temp = temp.io().border().floodFillFromOutside(clear, black).getImage().show("INNER HULL", debug);
				tempImage = temp.getAs2A();
				int[][] ttt = inpFLUOunchanged.getAs2A();
				int wf = inpFLUOunchanged.getWidth();
				int hf = inpFLUOunchanged.getHeight();
				for (int x = 0; x < wf; x++)
					for (int y = 0; y < hf; y++) {
						if (tempImage[x][y] != black)
							ttt[x][y] = clear;
					}
				for (Point p : branchPoints)
					if (p.x < wf && p.y < hf && p.x > 0 && p.y > 0)
						ttt[p.x][p.y] = clear;
				temp = new Image(ttt).io().show("FINAL", debug).getImage();
				leafWidthInPixels = 0d;
				int skeletonLength;
				do {
					skeletonLength = temp.io().skeletonize(false).show("SKELETON", false).countFilledPixels();
					if (skeletonLength > 0)
						leafWidthInPixels++;
					temp = temp.io().erode().getImage();
				} while (skeletonLength > 0);
			}
		}
		
		int leafcount = skel2d.endlimbs.size();
		Image skelres = skel2d.getAsFlexibleImage();
		int leaflength = skelres.io().countFilledPixels(SkeletonProcessor2d.getDefaultBackground());
		
		if (debug)
			System.out.println("leafcount: " + leafcount + " leaflength: " + leaflength + " numofendpoints: " + skel2d.endpoints.size());
		// FlexibleImage result = MapOriginalOnSkelUseingMedian(skelres, vis, Color.BLACK.getRGB());
		// result.display("res", false);
		// FlexibleImage result2 = skel2d.copyONOriginalImage(vis);
		// result2.display("res2", false);
		
		// ***Saved***
		Double distHorizontal = options.getCalculatedBlueMarkerDistance();
		double normFactor = distHorizontal != null ? options.getREAL_MARKER_DISTANCE() / distHorizontal : 1;
		
		boolean specialSkeletonBasedLeafWidthCalculation = true;
		if (specialSkeletonBasedLeafWidthCalculation) {
			Image inputImage = inpFLUOunchanged.copy().show(" inp img 2", false);
			int clear = ImageOperation.BACKGROUND_COLORint;
			int[][] inp2d = inputImage.getAs2A();
			int wf = inputImage.getWidth();
			int hf = inputImage.getHeight();
			for (Point p : skel2d.branches)
				if (p.x < wf && p.y < hf && p.x > 0 && p.y > 0)
					inp2d[p.x][p.y] = clear;
			
			inputImage = new Image(inp2d);
			
			ImageCanvas canvas = inputImage.io().canvas();
			ArrayList<Point> branchPoints = skel2d.getBranches();
			int lw;
			if (leafWidthInPixels != null)
				lw = (int) Math.ceil(leafWidthInPixels) * 3;
			else
				lw = 1;
			for (Point p : branchPoints)
				canvas.fillRect(p.x - lw / 2, p.y - lw / 2, lw, lw, clear);
			inputImage = canvas.getImage().show("CLEARED (" + branchPoints.size() + ") lw=" + leafWidthInPixels, debug);
			
			// repeat erode operation until no filled pixel
			Double leafWidthInPixels2 = 0d;
			int skeletonLength;
			ImageStack fis = debug ? new ImageStack() : null;
			ImageOperation ioo = inputImage.io();
			do {
				ioo = ioo.skeletonize(false).show("SKELETON2", false);
				skeletonLength = ioo.countFilledPixels();
				if (skeletonLength > 0)
					leafWidthInPixels2++;
				if (fis != null)
					fis.addImage("Leaf width 1: " + leafWidthInPixels + ", Leaf width 2: " + leafWidthInPixels2, inputImage.copy());
				ioo = ioo.erode();
			} while (skeletonLength > 0);
			ioo = null;
			if (fis != null) {
				fis.addImage("LW=" + leafWidthInPixels, inputImage);
				fis.show("SKEL2");
			}
			// number of repeats is 1/4 of maximum leaf width, but the actual number of repeats (not 4x) is stored
			if (leafWidthInPixels2 != null && leafWidthInPixels2 > 0 && !Double.isNaN(leafWidthInPixels2) && !Double.isInfinite(leafWidthInPixels2)) {
				if (distHorizontal != null)
					rt.addValue("leaf.width.whole.max.norm", leafWidthInPixels2 * normFactor);
				rt.addValue("leaf.width.whole.max", leafWidthInPixels2);
			}
			// System.out.print("Leaf width: " + leafWidthInPixels + " // " + leafWidthInPixels2);
		}
		
		rt.addValue("leaf.count", leafcount);
		if (leafcount > 0) {
			if (distHorizontal != null)
				rt.addValue("leaf.length.sum.norm", leaflength * normFactor);
			rt.addValue("leaf.length.sum", leaflength);
		}
		
		if (leafWidthInPixels != null && leafWidthInPixels > 0 && !Double.isNaN(leafWidthInPixels) && !Double.isInfinite(leafWidthInPixels)) {
			if (distHorizontal != null)
				rt.addValue("leaf.width.outer.max.norm", leafWidthInPixels * normFactor);
			rt.addValue("leaf.width.outer.max", leafWidthInPixels);
		}
		
		if (leafcount > 0) {
			if (distHorizontal != null)
				rt.addValue("leaf.length.average.norm", leaflength * normFactor / leafcount);
			rt.addValue("leaf.length.average", leaflength / leafcount);
		}
		
		if (leafcount > 0) {
			double filled = inp.io().countFilledPixels();
			if (distHorizontal != null)
				rt.addValue("leaf.width.average.norm", (filled / leaflength) * normFactor);
			rt.addValue("leaf.width.average", filled / leaflength);
			// System.out.println(" // " + (int) (filled / leaflength));
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
	
	private Image MapOriginalOnSkelUseingMedian(Image skeleton, Image original, int back) {
		int w = skeleton.getWidth();
		int h = skeleton.getHeight();
		int[] img = skeleton.getAs1A().clone();
		int[] oi = original.getAs1A().clone();
		if (img.length != oi.length)
			throw new RuntimeException("Skeleton image has different size than the image, it should be mapped onto!");
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
		return new Image(w, h, img);
	}
	
	private int median(int center, int above, int left, int right, int below) {
		int[] temp = { center, above, left, right, below };
		java.util.Arrays.sort(temp);
		return temp[2];
	}
	
	/**
	 * Function to invert skeleton image, invert from class imageoperation does not work
	 * 
	 * @param input
	 * @return
	 */
	private Image getInvert(Image input) {
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
		return new Image(res);
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		for (Long time : time2inSamples.keySet()) {
			TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (time2summaryResult.get(time) != null)
				for (Integer tray : time2summaryResult.get(time).keySet()) {
					if (!time2summaryResult.containsKey(time))
						time2summaryResult.put(time, new HashMap<Integer, BlockResultSet>());
					BlockResultSet summaryResult = time2summaryResult.get(time).get(tray);
					Double maxLeafcount = -1d;
					Double maxLeaflength = -1d;
					Double maxLeaflengthNorm = -1d;
					ArrayList<Double> lc = new ArrayList<Double>();
					
					Integer a = null;
					searchLoop: for (String key : allResultsForSnapshot.keySet()) {
						BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
						for (BlockPropertyValue v : rt.getPropertiesSearch("RESULT_top.main.axis.rotation")) {
							if (v.getValue() != null) {
								a = v.getValue().intValue();
								// System.out.println("main.axis.rotation: " + a);
								break searchLoop;
							}
						}
					}
					
					String bestAngle = null;
					if (a != null) {
						a = a % 180;
						Double bestDiff = Double.MAX_VALUE;
						for (String dc : allResultsForSnapshot.keySet()) {
							double d = Double.parseDouble(dc.substring(dc.indexOf(";") + ";".length()));
							if (d >= 0) {
								double dist = Math.abs(a - d);
								if (dist < bestDiff) {
									bestAngle = dc;
									bestDiff = dist;
								}
							}
						}
					}
					// System.out.println("ANGLES WITHIN SNAPSHOT: " + allResultsForSnapshot.size());
					for (String keyC : allResultsForSnapshot.keySet()) {
						BlockResultSet rt = allResultsForSnapshot.get(keyC).get(tray);
						
						if (bestAngle != null && keyC.equals(bestAngle)) {
							// System.out.println("Best side angle: " + bestAngle);
							Double cnt = null;
							for (BlockPropertyValue v : rt.getPropertiesSearch("RESULT_side.leaf.count")) {
								if (v.getValue() != null)
									cnt = v.getValue();
							}
							if (cnt != null && summaryResult != null) {
								summaryResult.setNumericProperty(getBlockPosition(),
										"RESULT_side.leaf.count.best", cnt, null);
								// System.out.println("Leaf count for best side image: " + cnt);
							}
						}
						
						for (BlockPropertyValue v : rt.getPropertiesSearch("RESULT_side.leaf.count")) {
							if (v.getValue() != null) {
								if (v.getValue() > maxLeafcount)
									maxLeafcount = v.getValue();
								lc.add(v.getValue());
							}
						}
						for (BlockPropertyValue v : rt.getPropertiesSearch("RESULT_side.leaf.length.sum")) {
							if (v.getValue() != null) {
								if (v.getValue() > maxLeaflength)
									maxLeaflength = v.getValue();
							}
						}
						for (BlockPropertyValue v : rt.getPropertiesSearch("RESULT_side.leaf.length.sum.norm")) {
							if (v.getValue() != null) {
								if (v.getValue() > maxLeaflengthNorm)
									maxLeaflengthNorm = v.getValue();
							}
						}
					}
					
					if (summaryResult != null && maxLeafcount != null && maxLeafcount > 0) {
						summaryResult.setNumericProperty(getBlockPosition(),
								"RESULT_side.leaf.count.max", maxLeafcount, null);
						// System.out.println("MAX leaf count: " + maxLeafcount);
						Double[] lca = lc.toArray(new Double[] {});
						Arrays.sort(lca);
						Double median = lca[lca.length / 2];
						summaryResult.setNumericProperty(getBlockPosition(),
								"RESULT_side.leaf.count.median", median, null);
					}
					if (maxLeaflength != null && maxLeaflength > 0)
						summaryResult.setNumericProperty(getBlockPosition(),
								"RESULT_side.leaf.length.sum.max", maxLeaflength, "px");
					if (maxLeaflengthNorm != null && maxLeaflengthNorm > 0)
						summaryResult.setNumericProperty(getBlockPosition(),
								"RESULT_side.leaf.length.sum.norm.max", maxLeaflengthNorm, "mm");
				}
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Skeletonize VIS and FLUO";
	}
	
	@Override
	public String getDescription() {
		return "Skeletonize VIS and FLUO images and extract according skeleton features.";
	}
}
