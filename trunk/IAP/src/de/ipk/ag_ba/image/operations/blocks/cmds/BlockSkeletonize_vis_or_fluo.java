package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.analysis.maize.BlockDrawSkeleton_vis_fluo;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operation.ImageCanvas;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * calculate the skeleton to detect the leafs and the tassel.
 * REQUIRES FLUO image for bloom detection.
 * 
 * @author pape, klukas
 */
public class BlockSkeletonize_vis_or_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
	private boolean debug = false;
	private boolean debug2 = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		debug2 = getBoolean("debug2", false);
	}
	
	@Override
	protected synchronized FlexibleImage processVISmask() {
		FlexibleImage vis = input().masks().vis();
		if (!getBoolean("skeletonize", true))
			return vis;
		// getInput().getMasks().getVis().copy().saveToFile(ReleaseInfo.getDesktopFolder() + File.separator + "MaizeVISMaskBeforSkeleton.png");
		FlexibleImage fluo = input().masks().fluo() != null ? input().masks().fluo().copy() : null;
		FlexibleImage res = vis;
		if (options.isMaize()) {
			if (options.getCameraPosition() == CameraPosition.SIDE && vis != null && fluo != null && getProperties() != null) {
				FlexibleImage viswork = vis.copy().io().print("orig", debug)// .medianFilter32Bit()
						// .closing(3, 3)
						// .erode()
						.border(5)
						.dilateHorizontal(getInt("Dilate-Cnt-Vis-Hor", 20)) // 10
						.blur(getDouble("Blur-Vis", 1))
						.getImage().print("vis", debug);
				
				if (viswork != null)
					if (vis != null && fluo != null) {
						FlexibleImage sk = calcSkeleton(viswork, vis, fluo, fluo.copy());
						if (sk != null)
							getProperties().setImage("skeleton", sk);
						FlexibleImage rrr = getProperties().getImage("beforeBloomEnhancement");
						if (rrr != null)
							res = rrr;
					}
			}
		} else {
			if (options.getCameraPosition() == CameraPosition.SIDE && vis != null && fluo != null && getProperties() != null) {
				if (false) {
					FlexibleImage viswork = vis.copy().io()// .medianFilter32Bit()
							.dilate(3)
							.blur(1)
							.getImage().print("vis", debug);
					
					if (viswork != null)
						if (vis != null && fluo != null) {
							FlexibleImage sk = calcSkeleton(viswork, vis, fluo, fluo.copy());
							if (sk != null) {
								boolean drawSkeleton = getBoolean("draw_skeleton", true);
								res = res.io().drawSkeleton(sk, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
							}
						}
				}
			}
			if (options.getCameraPosition() == CameraPosition.TOP && vis != null && fluo != null && getProperties() != null) {
				if (false) {
					FlexibleImage viswork = vis.copy().io()// .medianFilter32Bit()
							.dilate(2)
							.blur(1)
							.getImage().print("vis", debug);
					
					if (viswork != null)
						if (vis != null && fluo != null) {
							FlexibleImage sk = calcSkeleton(viswork, vis, fluo, fluo.copy());
							if (sk != null) {
								boolean drawSkeleton = getBoolean("draw_skeleton", true);
								res = res.io().drawSkeleton(sk, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
							}
						}
				}
			}
			
		}
		return res;
	}
	
	@Override
	protected synchronized FlexibleImage processFLUOmask() {
		FlexibleImage vis = input().masks().vis();
		if (!getBoolean("skeletonize", true))
			return input().masks().fluo();
		FlexibleImage fluo = input().masks().fluo() != null ? input().masks().fluo().copy() : null;
		if (fluo == null)
			return fluo;
		FlexibleImage res = fluo.copy();
		if (options.isMaize()) {
			// empty
		} else {
			if (options.getCameraPosition() == CameraPosition.SIDE && vis != null && fluo != null && getProperties() != null) {
				FlexibleImage viswork = fluo.copy().io()// .medianFilter32Bit()
						.erode(getInt("Erode-Cnt-Fluo", 1))
						.dilate(getInt("Dilate-Cnt-Fluo", 1))
						.blur(getDouble("Blur-Fluo", 4))
						.getImage().print("fluo", debug);
				
				if (viswork != null)
					if (vis != null && fluo != null) {
						FlexibleImage sk = calcSkeleton(viswork, vis, fluo, fluo.copy());
						if (sk != null) {
							boolean drawSkeleton = getBoolean(new BlockDrawSkeleton_vis_fluo(), "draw_skeleton", true);
							res = res.io().drawSkeleton(sk, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
							if (res != null)
								getProperties().setImage("skeleton_fluo", sk);
						}
					}
			}
			if (options.getCameraPosition() == CameraPosition.TOP && vis != null && fluo != null && getProperties() != null) {
				FlexibleImage viswork = fluo.copy().io()// .filterRGB(150, 255, 255)
						// .erode(1)
						.dilate(getInt("Dilate-Cnt-Fluo", 4))
						// .blur(1)
						.getImage().print("fluo", debug);
				
				if (viswork != null)
					if (vis != null && fluo != null) {
						FlexibleImage sk = calcSkeleton(viswork, vis, fluo, fluo.copy());
						if (sk != null) {
							boolean drawSkeleton = getBoolean("draw_skeleton", true);
							res = res.io().drawSkeleton(sk, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
							if (res != null)
								getProperties().setImage("skeleton_fluo", sk);
						}
					}
			}
			
		}
		return input().masks().fluo();
	}
	
	public synchronized FlexibleImage calcSkeleton(FlexibleImage inp, FlexibleImage vis, FlexibleImage fluo, FlexibleImage inpFLUOunchanged) {
		// ***skeleton calculations***
		SkeletonProcessor2d skel2d = new SkeletonProcessor2d(getInvert(inp.io().skeletonize(false).getImage()));
		skel2d.findEndpointsAndBranches2();
		skel2d.print("endpoints and branches", debug);
		
		double xf = fluo.getWidth() / (double) vis.getWidth();
		double yf = fluo.getHeight() / (double) vis.getHeight();
		int w = vis.getWidth();
		int h = vis.getHeight();
		
		int leafcount = skel2d.endlimbs.size();
		// System.out.println("A Leaf count: " + leafcount);
		
		skel2d.deleteShortEndLimbs(10, false, new HashSet<Point>());
		
		leafcount = skel2d.endlimbs.size();
		// System.out.println("B Leaf count: " + leafcount);
		
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		int bloomLimbCount = 0;
		if (options.isMaize()) {
			FlexibleImage probablyBloomFluo = skel2d.calcProbablyBloomImage(fluo.io().blur(10).getImage().print("blurf", false), 0.075f, h, 20).io().// blur(3).
					thresholdGrayClearLowerThan(10, Color.BLACK.getRGB()).getImage();
			
			probablyBloomFluo = probablyBloomFluo.io().print("BEFORE", false).medianFilter32Bit().invert().removeSmallClusters(true, null).
					erode().erode().erode().erode().invert().
					getImage();
			
			if (debug2) {
				FlexibleImageStack fis = new FlexibleImageStack();
				fis.addImage("PROB", probablyBloomFluo);
				fis.addImage("FLUO", fluo);
				fis.print("CHECK THIS");
			}
			
			HashSet<Point> knownBloompoints = skel2d.detectBloom(vis, probablyBloomFluo, xf, yf);
			bloomLimbCount = knownBloompoints.size();
			skel2d.deleteShortEndLimbs(10, false, knownBloompoints);
			skel2d.detectBloom(vis, probablyBloomFluo, xf, yf);
			
			rt.addValue("fluo.bloom.area.size", probablyBloomFluo.io().print("BLOOM AREA", debug2).countFilledPixels());
		}
		if (options.isBarley())
			skel2d.deleteShortEndLimbs(20, true, new HashSet<Point>());
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
				FlexibleImage clearImage = new FlexibleImage(tempImage).copy();
				int black = Color.BLACK.getRGB();
				for (Point p : branchPoints)
					tempImage[p.x][p.y] = black;
				FlexibleImage temp = new FlexibleImage(tempImage);
				temp = temp.io().hull().setCustomBackgroundImageForDrawing(clearImage).
						find(true, false, true, false, black, black, black, null, 0).getImage();
				temp = temp.io().border().floodFillFromOutside(clear, black).getImage().print("INNER HULL", debug);
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
				temp = new FlexibleImage(ttt).io().print("FINAL", debug).getImage();
				leafWidthInPixels = 0d;
				int filled;
				ImageOperation tio = temp.io();
				temp = null;
				do {
					filled = tio.countFilledPixels();
					if (filled > 0)
						leafWidthInPixels++;
					tio.erode();
				} while (filled > 0);
			}
		}
		
		leafcount = skel2d.endlimbs.size();
		// System.out.println("C Leaf count: " + leafcount);
		FlexibleImage skelres = skel2d.getAsFlexibleImage();
		int leaflength = skelres.io().countFilledPixels(SkeletonProcessor2d.getDefaultBackground());
		leafcount -= bloomLimbCount;
		
		// ***Out***
		// System.out.println("leafcount: " + leafcount + " leaflength: " + leaflength + " numofendpoints: " + skel2d.endpoints.size());
		FlexibleImage result = MapOriginalOnSkelUseingMedian(skelres, vis, Color.BLACK.getRGB());
		result.print("res", false);
		FlexibleImage result2 = skel2d.copyONOriginalImage(vis);
		result2.print("res2", false);
		
		// ***Saved***
		BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
		double normFactor = distHorizontal != null ? options.getIntSetting(Setting.REAL_MARKER_DISTANCE) / distHorizontal.getValue() : 1;
		
		boolean specialSkeletonBasedLeafWidthCalculation = true;
		if (specialSkeletonBasedLeafWidthCalculation) {
			FlexibleImage inputImage = inpFLUOunchanged.copy().print(" inp img 2", false);
			int clear = ImageOperation.BACKGROUND_COLORint;
			int[][] inp2d = inputImage.getAs2A();
			int wf = inputImage.getWidth();
			int hf = inputImage.getHeight();
			for (Point p : skel2d.branches)
				if (p.x < wf && p.y < hf && p.x > 0 && p.y > 0)
					inp2d[p.x][p.y] = clear;
			
			inputImage = new FlexibleImage(inp2d);
			
			ImageCanvas canvas = inputImage.io().canvas();
			ArrayList<Point> branchPoints = skel2d.getBranches();
			int lw;
			if (leafWidthInPixels != null)
				lw = (int) Math.ceil(leafWidthInPixels) * 3;
			else
				lw = 1;
			for (Point p : branchPoints)
				canvas.fillRect(p.x - lw / 2, p.y - lw / 2, lw, lw, clear);
			inputImage = canvas.getImage().print("CLEARED (" + branchPoints.size() + ") lw=" + leafWidthInPixels, debug);
			
			// repeat erode operation until no filled pixel
			Double leafWidthInPixels2 = 0d;
			int filled;
			FlexibleImageStack fis = debug ? new FlexibleImageStack() : null;
			ImageOperation ioo = inputImage.io();
			do {
				filled = ioo.countFilledPixels();
				if (filled > 0)
					leafWidthInPixels2++;
				if (fis != null)
					fis.addImage("Leaf width 1: " + leafWidthInPixels + ", Leaf width 2: " + leafWidthInPixels2, inputImage.copy());
				ioo.erode();
			} while (filled > 0);
			if (fis != null) {
				fis.addImage("LW=" + leafWidthInPixels, inputImage);
				fis.print("SKEL2");
			}
			// number of repeats is 1/4 of maximum leaf width, but the actual number of repeats (not 4x) is stored
			if (leafWidthInPixels2 != null && leafWidthInPixels2 > 0 && !Double.isNaN(leafWidthInPixels2) && !Double.isInfinite(leafWidthInPixels2)) {
				if (distHorizontal != null)
					rt.addValue("leaf.width.whole.max.norm", leafWidthInPixels2 * normFactor);
				rt.addValue("leaf.width.whole.max", leafWidthInPixels2);
			}
			// System.out.print("Leaf width: " + leafWidthInPixels + " // " + leafWidthInPixels2);
		}
		
		rt.addValue("bloom.count", bloomLimbCount);
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
		
		if (bloomLimbCount > 0)
			rt.addValue("bloom", 1);
		else
			rt.addValue("bloom", 0);
		
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
	
	private synchronized FlexibleImage MapOriginalOnSkelUseingMedian(FlexibleImage skeleton, FlexibleImage original, int back) {
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
	private synchronized FlexibleImage getInvert(FlexibleImage input) {
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
	public synchronized void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		for (Long time : time2inSamples.keySet()) {
			TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (!time2summaryResult.containsKey(time))
				time2summaryResult.put(time, new HashMap<Integer, BlockResultSet>());
			for (Integer tray : time2summaryResult.get(time).keySet()) {
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
					HashMap<Integer, BlockResultSet> rt = allResultsForSnapshot.get(keyC);
					
					if (bestAngle != null && keyC.equals(bestAngle)) {
						// System.out.println("Best side angle: " + bestAngle);
						Double cnt = null;
						for (BlockPropertyValue v : rt.get(tray).getPropertiesSearch("RESULT_side.leaf.count")) {
							if (v.getValue() != null)
								cnt = v.getValue();
						}
						if (cnt != null && summaryResult != null) {
							summaryResult.setNumericProperty(getBlockPosition(),
									"RESULT_side.leaf.count.best", cnt, null);
							// System.out.println("Leaf count for best side image: " + cnt);
						}
					}
					
					for (BlockPropertyValue v : rt.get(tray).getPropertiesSearch("RESULT_side.leaf.count")) {
						if (v.getValue() != null) {
							if (v.getValue() > maxLeafcount)
								maxLeafcount = v.getValue();
							lc.add(v.getValue());
						}
					}
					for (BlockPropertyValue v : rt.get(tray).getPropertiesSearch("RESULT_side.leaf.length.sum")) {
						if (v.getValue() != null) {
							if (v.getValue() > maxLeaflength)
								maxLeaflength = v.getValue();
						}
					}
					for (BlockPropertyValue v : rt.get(tray).getPropertiesSearch("RESULT_side.leaf.length.sum.norm")) {
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
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] { "RESULT_side.leaf.length.sum.max", "RESULT_leaf.length.sum.norm.max", "RESULT_side.leaf.length.sum",
						"RESULT_leaf.length.sum.norm" });
	}
}
