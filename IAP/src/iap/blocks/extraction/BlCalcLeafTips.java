package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import org.StringManipulationTools;
import org.Vector2d;

import de.ipk.ag_ba.image.operation.ImageMoments;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.Lab;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.properties.RunnableOnImageSet;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * This Block calculates leaf tips and some features related to the leaf tips. (It can also used for parameter tuning.)
 **/
public class BlCalcLeafTips extends AbstractSnapshotAnalysisBlock {
	boolean calcOnVis = false;
	
	@Override
	protected Image processVISmask() {
		calcOnVis = getBoolean("Calculate on Vis Image", false);
		Image img = input().masks().vis();
		if (calcOnVis)
			return doLeafTipAnalysis(img);
		else
			return img;
	}
	
	@Override
	protected Image processFLUOmask() {
		calcOnVis = getBoolean("Calculate on Vis Image", false);
		Image img = input().masks().fluo();
		if (!calcOnVis)
			return doLeafTipAnalysis(img);
		else
			return img;
	}
	
	private Image doLeafTipAnalysis(Image inputImage) {
		boolean debug = getBoolean("debug", false);
		double numofblur = getDouble("number of blur runs", 1);
		boolean optimize = getBoolean("optimize parameter", false);
		boolean useMainAxes = getBoolean("Use Main Axes From Top", true);
		
		int circlediameter = 0;
		int geometricThresh = 0;
		
		// set parameters
		if (!optimize) {
			circlediameter = getInt("search diameter", 27); // 35
			int parm = getInt("geometric threshold", 32);
			geometricThresh = (int) ((circlediameter / 2 * circlediameter / 2 * Math.PI) * ((double) parm / 100));
			// old: cd = 35, gt = k/4 + 42
		}
		
		// only for side image
		if (options.getCameraPosition() == CameraPosition.SIDE && inputImage != null) {
			
			// search for best side image
			if (useMainAxes) {
				boolean isBestAngle = isBestAngle();
				
				if (!isBestAngle)
					return inputImage;
			}
			
			Image mask = inputImage;
			Image imgorig = inputImage.copy();
			int background = ImageOperation.BACKGROUND_COLORint;
			
			mask.show("mask", debug);
			
			// blur
			mask = mask.io().blur(numofblur).getImage();
			
			double f = getDouble("object removal multiplier", 2.0);
			
			if (f > 0.0) {
				double averageLeafWidthEstimation = inputImage.io().countFilledPixels() /
						(double) inputImage.copy().io().skel().skeletonize(ImageOperation.BACKGROUND_COLORint).countFilledPixels();
				
				boolean ngUse = true;
				boolean autoTune = true;
				
				averageLeafWidthEstimation *= f;
				
				mask = mask.io().removeSmallClusters(ngUse,
						autoTune ? (int) (averageLeafWidthEstimation * averageLeafWidthEstimation) : getInt("Noise-Size-Fluo-Area", 10 * 10),
						autoTune ? (int) averageLeafWidthEstimation : getInt("Noise-Size-Fluo-Dimension-Absolute", 10),
						options.getNeighbourhood(), options.getCameraPosition(), null,
						autoTune ? true : getBoolean("Use Fluo Area Parameter", true)).show("result fluo", debugValues).getImage();
			}
			
			// dilate
			// vis_mask = vis_mask.io().mo().erode_or_dilate(10, false).getImage();
			
			mask.show("after blur, noise rm and dilate", debug);
			
			// detect borders
			ImageOperation io = new ImageOperation(mask.getAs2A()).border()
					.borderDetection(background, Color.CYAN.getRGB(),
							false);
			
			// get border length
			int borderLength = (int) io.getResultsTable().getValue("border", 0);
			Image borderimg = io.copy().getImage();
			
			borderimg.show("border_img", debug);
			
			// get border list
			int[][] borderMap = io.getImageAs2dArray();
			int[] borderList;
			
			try {
				borderList = getBorderListSorted(borderMap, borderLength);
			} catch (InterruptedException e) {
				// no border
				borderList = new int[0];
				e.printStackTrace();
			}
			
			ResultsTableWithUnits rt = new ResultsTableWithUnits();
			rt.incrementCounter();
			
			// stepsize for border scan
			int stepsize = 1;
			
			if (optimize) {
				// get average leaf width, used to estimate initial parameters
				SkeletonProcessor2d skel2d = new SkeletonProcessor2d(mask.io().skeletonize(true).getImage());
				
				Image skelimg = skel2d.getAsFlexibleImage();
				int leaflength = skelimg.io().countFilledPixels(ImageOperation.BACKGROUND_COLORint);
				int area = imgorig.io().countFilledPixels(ImageOperation.BACKGROUND_COLORint);
				
				int avg_width = area / leaflength;
				
				// optimize loop
				for (int cd = avg_width; cd <= avg_width * 2; cd++) {
					gt: for (int i = 20; i < 40; i++) {
						
						int gt = (int) ((cd / 2 * cd / 2 * Math.PI) * ((double) i / 100));
						
						ArrayList<PositionAndColor> borderlistPlusCornerestimation = CornerCandidates(mask, borderList, cd,
								gt, stepsize, false);
						ArrayList<PositionAndColor> filteredList = filterCornerCandidates(borderlistPlusCornerestimation);
						
						int num_leafs = filteredList.size();
						
						if (num_leafs > 0)
							rt.addValue("leaf." + StringManipulationTools.formatNumber(cd) + "." + StringManipulationTools.formatNumber(i), num_leafs);
						
						if (num_leafs < 5)
							i += 4;
						if (num_leafs > 15)
							break gt;
					}
				}
			} else {
				ArrayList<PositionAndColor> borderlistPlusCornerestimation = CornerCandidates(mask, borderList, circlediameter,
						geometricThresh, stepsize, false);
				ArrayList<PositionAndColor> filteredList = filterCornerCandidates(borderlistPlusCornerestimation);
				
				Object[] res = FeatureExtraction(mask, imgorig, filteredList, circlediameter, background, debug);
				
				rt = (ResultsTableWithUnits) res[1];
				
				// number of leafs
				rt.addValue("leaf.count", filteredList.size());
			}
			getProperties().storeResults("RESULT_side", "|SUSAN_corner_detection", rt, getBlockPosition());
		}
		return inputImage;
	}
	
	private boolean isBestAngle() {
		HashMap<String, HashMap<Integer, ArrayList<BlockPropertyValue>>> previousResults = options
				.getPropertiesExactMatchForPreviousResultsOfCurrentSnapshot("RESULT_top.fluo.main.axis.rotation");
		
		double sum = 0;
		int count = 0;
		
		for (HashMap<Integer, ArrayList<BlockPropertyValue>> a : previousResults.values()) {
			for (ArrayList<BlockPropertyValue> b : a.values()) {
				for (BlockPropertyValue c : b) {
					count++;
					sum += c.getValue();
				}
			}
		}
		
		if (count == 0) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Can´t calculate leaf tips, no main axis calculation available!");
			return false;
		}
		
		ImageData currentImage = input().images().getFluoInfo();
		
		double mainRotationFromTopView = sum / count;
		double mindist = Double.MAX_VALUE;
		boolean currentImageIsBest = false;
		
		for (NumericMeasurementInterface nmi : currentImage.getParentSample()) {
			if (nmi instanceof ImageData) {
				Double r = ((ImageData) nmi).getPosition();
				if (r == null)
					r = 0d;
				double dist = Math.abs(mainRotationFromTopView - r);
				if (dist < mindist) {
					mindist = dist;
					if ((((ImageData) nmi).getPosition() + "").equals((currentImage.getPosition() + "")))
						currentImageIsBest = true;
					else
						currentImageIsBest = false;
				}
			}
		}
		
		if (!currentImageIsBest)
			return false;
		
		return true;
	}
	
	/**
	 * @return Object[image gamma, results table]
	 */
	public Object[] FeatureExtraction(Image img, Image imgorig, ArrayList<PositionAndColor> borderlistPlusCornerestimation,
			final int circlediameter,
			int background, boolean debug) {
		
		Object[] res = new Object[2];
		
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		// further calculation result leaf tip regions, cog, principal axes
		int size = borderlistPlusCornerestimation.size();
		
		// sort in y direction
		Collections.sort(borderlistPlusCornerestimation, new Comparator<PositionAndColor>() {
			
			@Override
			public int compare(PositionAndColor o1, PositionAndColor o2) {
				if (o1.y == o2.y)
					return 0;
				return o1.y > o2.y ? -1 : 1;
			}
		});
		
		// gamma image
		Image imgGamma = img.copy().io().gamma(7.0).getImage();
		
		int up = 0;
		int down = 0;
		int index = 1;
		
		// getProperties().addImagePostProcessor(new RunnableOnImageSet() {
		//
		// @Override
		// public Image postProcessMask(Image imgGamma) {
		// imgGamma = imgGamma
		// .io().clearImageLeft(imgGamma.getWidth(), ImageOperation.BACKGROUND_COLORint)
		// .getImage();
		// return imgGamma;
		// }
		//
		// @Override
		// public Image postProcessImage(Image image) {
		// return image;
		// }
		//
		// @Override
		// public ImageConfiguration getConfig() {
		// if (!calcOnVis)
		// return ImageConfiguration.FluoSide;
		// else
		// return ImageConfiguration.VisSide;
		// }
		// });
		
		for (int i = 0; i < size; i++) {
			final int xtemp = borderlistPlusCornerestimation.get(i).x;
			final int ytemp = borderlistPlusCornerestimation.get(i).y;
			
			// get regionlist
			int[][] img2d = img.copy().getAs2A();
			HashSet<PositionAndColor> region = regionGrowing(xtemp, ytemp, img2d, background, circlediameter);
			// ImageCanvas can = markimg.io().canvas();
			
			final int[] dim = findDimensions(region);
			
			Image regionImage = new Image(copyRegiontoImage(dim, region));
			
			// regionImage.show("region " + i);
			
			final Point centerOfGravity = calcCenterOfGravity(regionImage, background);
			
			// orientation of major and minor axes given by
			double omega = 0.0;
			
			// use atan2 for case differentiation, see polar-coordinates
			omega = ImageMoments.calcOmega(regionImage, background);
			
			double gamma = omega + Math.PI / 2;
			if (gamma > Math.PI)
				gamma = 2 * Math.PI - gamma;
			
			// centroid
			regionImage.io().canvas()
					.drawCircle(centerOfGravity.x, centerOfGravity.y, 3, Color.RED.getRGB(), 0.5, 1);
			
			imgGamma = copyResultIntoImage(imgGamma, regionImage.copy(), dim);
			
			// calculate direction of leaftip
			int direction = 0;
			
			if (centerOfGravity.y + dim[2] > ytemp) {
				direction = -1; // up
				up++;
			} else {
				direction = 1; // down
				down++;
			}
			
			Vector2d midPoint = new Vector2d(Math.abs(regionImage.getWidth() / 2), Math.abs(regionImage.getHeight() / 2));
			
			double distCoGToMid = centerOfGravity.distance(midPoint.x, midPoint.y);
			
			double[] lambdas = ImageMoments.eigenValues(regionImage, background);
			
			// eccentricity
			double eccentricity = 0.0;
			eccentricity = Math.sqrt(1 - lambdas[1] / lambdas[0]);
			
			final Color regionColorHsvAvg = getAverageRegionColorHSV(region);
			Lab regionColorLabAvg = getAverageRegionColorLab(region);
			
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".position.x", xtemp);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".position.y", ytemp);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".omega", omega);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".gamma", gamma);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".direction", direction);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".CoG.x", centerOfGravity.x + dim[0]);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".CoG.y", centerOfGravity.y + dim[2]);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".distanceBetweenCoGandMidPoint", distCoGToMid);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".area", region.size());
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".eccentricity", eccentricity);
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".color.lab.l", regionColorLabAvg.getAverageL());
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".color.lab.a", regionColorLabAvg.getAverageA());
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".color.lab.b", regionColorLabAvg.getAverageB());
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".color.hsv.h", regionColorHsvAvg.getRed());
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".color.hsv.s", regionColorHsvAvg.getGreen());
			rt.addValue("leaf." + StringManipulationTools.formatNumber(index) + ".color.hsv.v", regionColorHsvAvg.getBlue());
			
			final int off = i == 4 ? 40 : 0;
			
			final int directionF = direction;
			final int iF = i;
			
			getProperties().addImagePostProcessor(new RunnableOnImageSet() {
				
				@Override
				public Image postProcessMask(Image imgGamma) {
					imgGamma = imgGamma
							.io()
							.canvas()
							.drawLine(xtemp, ytemp, centerOfGravity.x + dim[0], centerOfGravity.y + dim[2], Color.ORANGE.getRGB(), 0.2, 1)
							.drawCircle(xtemp, ytemp, circlediameter / 2 + 4, Color.BLUE.getRGB(), 0.5, 1)
							.text(centerOfGravity.x + xtemp + 20, centerOfGravity.y + ytemp - 20 + off, "LEAF: " + (iF + 1),
									Color.BLACK)
							.text(centerOfGravity.x + xtemp + 20, centerOfGravity.y + ytemp + 0 + off,
									"DIRECTION: " + (directionF > 0 ? "DOWN" : "UP"),
									Color.BLACK)
							.text(centerOfGravity.x + xtemp + 20, centerOfGravity.y + ytemp + 20 + off,
									"HUE: " + regionColorHsvAvg.getRed() + ", SAT: " + regionColorHsvAvg.getGreen() + ", VAL: " + regionColorHsvAvg.getBlue(),
									Color.BLACK)
							.getImage();
					return imgGamma;
				}
				
				@Override
				public Image postProcessImage(Image image) {
					return image;
				}
				
				@Override
				public ImageConfiguration getConfig() {
					if (!calcOnVis)
						return ImageConfiguration.FluoSide;
					else
						return ImageConfiguration.VisSide;
				}
			});
			
			index++;
		}
		
		rt.addValue("leaf.count.up", up);
		rt.addValue("leaf.count.down", down);
		imgGamma.show("marked", debug);
		rt.show("resultstable", debug);
		
		res[0] = imgGamma;
		res[1] = rt;
		return res;
	}
	
	private ArrayList<PositionAndColor> filterCornerCandidates(ArrayList<PositionAndColor> cornerlist) {
		int listsize = cornerlist.size();
		// get middle of peaks
		ArrayList<PositionAndColor> results = new ArrayList<PositionAndColor>();
		int start = 0;
		int temp = 0;
		
		for (int i = 0; i < listsize; i++) {
			temp = cornerlist.get(i).colorInt;
			// found peak
			if (start == 0 && temp > 0) {
				start = temp;
				int idx = 1;
				PositionAndColor tempMax = cornerlist.get(i);
				while (temp > 0) {
					if (((i + idx) % listsize) < 0)
						continue;
					temp = cornerlist.get((i + idx) % listsize).colorInt;
					if (temp > tempMax.colorInt)
						tempMax = cornerlist.get((i + idx) % listsize);
					idx++;
				}
				results.add(tempMax);
				// If over i > listsize, delete first one because it is maybe no maxima otherwise it is re-added.
				if (i + idx >= listsize)
					results.remove(0);
				start = 0;
				i = i + idx;
			}
		}
		return results;
	}
	
	private static HashSet<PositionAndColor> regionGrowing(int x, int y, int[][] img2d, int background, int diameter) {
		HashSet<PositionAndColor> region = null;
		try {
			region = regionGrowing(img2d, x, y, background, diameter / 2, false);
		} catch (InterruptedException e) {
			region = new HashSet<PositionAndColor>();
			e.printStackTrace();
		}
		return region;
	}
	
	/**
	 * Get corner-candidates for leaf tip detection.
	 * 
	 * @param img
	 * @param borderlist
	 * @param circlediameter
	 * @param geometricThresh
	 * @param stepsize
	 *           (common = 6)
	 * @return list of corner-candidates above threshold.
	 */
	static public ArrayList<PositionAndColor> CornerCandidates(Image img, int[] borderlist, int circlediameter, int geometricThresh, int stepsize,
			boolean inverse) {
		ArrayList<PositionAndColor> resultlist = new ArrayList<PositionAndColor>();
		int background = ImageOperation.BACKGROUND_COLORint;
		int xtemp;
		int ytemp;
		
		// check if stepsize is odd
		if (stepsize % 2 != 0)
			stepsize++;
		
		// double a = 100 / (double) borderlist.length;
		// iterate borderlist
		for (int idxborderlist = 0; idxborderlist < borderlist.length; idxborderlist += stepsize) {
			xtemp = borderlist[idxborderlist];
			ytemp = borderlist[idxborderlist + 1];
			// System.out.println("x: " + xtemp + " y: " + ytemp);
			
			// System.out.println(idxborderlist * a + "|" + borderlist.length * a);
			// do region-growing
			HashSet<PositionAndColor> region = regionGrowing(xtemp, ytemp, img.copy().getAs2A(), background, circlediameter);
			
			// check area
			if (!inverse)
				if (region.size() < geometricThresh)
					resultlist.add(new PositionAndColor(xtemp, ytemp, geometricThresh - region.size()));
				else
					resultlist.add(new PositionAndColor(xtemp, ytemp, 0));
			else
				if (region.size() > geometricThresh)
					resultlist.add(new PositionAndColor(xtemp, ytemp, geometricThresh - region.size()));
				else
					resultlist.add(new PositionAndColor(xtemp, ytemp, 0));
		}
		return resultlist;
	}
	
	/**
	 * Calculates sorted border list (depends on 8 neighborhood, border should be connected, given that there is one big segment), uses recursive border-search
	 * (implemented as loop).
	 * 
	 * @param borderMap
	 *           - border-image as 2d array
	 * @param borderLength
	 *           - number of border-pixels
	 * @return 1d array of sorted border-pixels
	 * @throws InterruptedException
	 */
	// TODO adaption for disconnected border has to be checked
	public static int[] getBorderListSorted(int[][] borderMap, int borderLength) throws InterruptedException {
		int w = borderMap.length;
		int h = borderMap[0].length;
		int[] borderList = new int[borderLength * 2];
		int i = 0;
		boolean find = false;
		int background = ImageOperation.BACKGROUND_COLORint;
		int rx;
		int ry;
		
		boolean debug = false;
		Image show = new Image(borderMap);
		if (debug)
			show.show("bordermap");
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (borderMap[x][y] != background) {
					find = true;
					rx = x;
					ry = y;
					boolean inside = false;
					
					if (rx - 1 >= 0 && ry - 1 >= 0 && rx + 1 < w && ry + 1 < h)
						inside = true;
					
					while (find) {
						borderList[i] = rx;
						borderList[i + 1] = ry;
						i += 2;
						borderMap[rx][ry] = background;
						find = false;
						
						if (debug) {
							show.update(new Image(borderMap));
							Thread.sleep(25);
						}
						
						inside = ry - 1 >= 0;
						if (!find && inside)
							if (borderMap[rx][ry - 1] != background) {
								find = true;
								ry = ry - 1;
							}
						
						inside = rx - 1 >= 0;
						if (!find && inside)
							if (borderMap[rx - 1][ry] != background) {
								find = true;
								rx = rx - 1;
							}
						
						inside = rx + 1 < w;
						if (!find && inside)
							if (borderMap[rx + 1][ry] != background) {
								find = true;
								rx = rx + 1;
							}
						
						inside = ry + 1 < h;
						if (!find && inside)
							if (borderMap[rx][ry + 1] != background) {
								find = true;
								ry = ry + 1;
							}
						
						inside = rx + 1 < w && ry + 1 < h;
						if (!find && inside)
							if (borderMap[rx + 1][ry + 1] != background) {
								find = true;
								rx = rx + 1;
								ry = ry + 1;
							}
						
						inside = rx - 1 >= 0 && ry - 1 >= 0;
						if (!find && inside)
							if (borderMap[rx - 1][ry - 1] != background) {
								find = true;
								rx = rx - 1;
								ry = ry - 1;
							}
						
						inside = rx - 1 >= 0 && ry + 1 < h;
						if (!find && inside)
							if (borderMap[rx - 1][ry + 1] != background) {
								find = true;
								rx = rx - 1;
								ry = ry + 1;
							}
						
						inside = rx + 1 < w && ry - 1 >= 0;
						if (!find && inside)
							if (borderMap[rx + 1][ry - 1] != background) {
								find = true;
								rx = rx + 1;
								ry = ry - 1;
							}
					}
				}
			}
		}
		return borderList;
	}
	
	/**
	 * @param img2d
	 *           - input image as 2d array
	 * @param x
	 *           - Region Growing start-point x coordinate
	 * @param y
	 *           - Region Growing start-point y coordinate
	 * @param background
	 *           - background color
	 * @param radius
	 *           - max search distance/deep (euclidian) for Region Growing from start point, for classic Region Growing, set this value to max value
	 * @return HashSet which includes Vector3d of all point coordinates plus color-values
	 * @throws InterruptedException
	 */
	static HashSet<PositionAndColor> regionGrowing(int[][] img2d, int x, int y, int background, double radius, boolean debug) throws InterruptedException {
		int[][] imgTemp = img2d.clone();
		int w = img2d.length;
		int h = img2d[1].length;
		Stack<PositionAndColor> visited = new Stack<PositionAndColor>();
		HashSet<PositionAndColor> resultRegion = new HashSet<PositionAndColor>();
		int rx = x;
		int ry = y;
		PositionAndColor start = new PositionAndColor(x, y, img2d[x][y]);
		visited.push(start);
		resultRegion.add(start);
		boolean find;
		boolean inside = false;
		double dist = 0.0;
		Image show = new Image(imgTemp);
		if (debug)
			show.show("debug");
		
		// test if new pixel is in image space
		if (rx >= 0 && ry >= 0 && rx < w && ry < h)
			inside = true;
		
		imgTemp[rx][ry] = background;
		
		while (!visited.empty()) {
			// update process window for debug
			if (imgTemp[rx][ry] != background && debug) {
				show.update(new Image(imgTemp));
				Thread.sleep(5);
			}
			imgTemp[rx][ry] = background;
			
			find = false;
			
			if (dist < radius) {
				inside = rx - 1 >= 0 && ry - 1 >= 0;
				if (inside)
					if (imgTemp[rx - 1][ry - 1] != background) {
						find = true;
						rx = rx - 1;
						ry = ry - 1;
					}
				
				inside = ry - 1 >= 0;
				if (!find && inside)
					if (imgTemp[rx][ry - 1] != background) {
						find = true;
						ry = ry - 1;
					}
				
				inside = ry - 1 >= 0 && rx + 1 < w;
				if (!find && inside)
					if (imgTemp[rx + 1][ry - 1] != background) {
						find = true;
						rx = rx + 1;
						ry = ry - 1;
					}
				
				inside = rx - 1 >= 0;
				if (!find && inside)
					if (imgTemp[rx - 1][ry] != background) {
						find = true;
						rx = rx - 1;
					}
				
				inside = rx + 1 < w;
				if (!find && inside)
					if (imgTemp[rx + 1][ry] != background) {
						find = true;
						rx = rx + 1;
					}
				
				inside = rx - 1 >= 0 && ry + 1 < h;
				if (!find && inside)
					if (imgTemp[rx - 1][ry + 1] != background) {
						find = true;
						rx = rx - 1;
						ry = ry + 1;
					}
				
				inside = ry + 1 < h;
				if (!find && inside)
					if (imgTemp[rx][ry + 1] != background) {
						find = true;
						ry = ry + 1;
					}
				
				inside = rx + 1 < w && ry + 1 < h;
				if (!find && inside)
					if (imgTemp[rx + 1][ry + 1] != background) {
						find = true;
						rx = rx + 1;
						ry = ry + 1;
					}
				
				// find new pixel?
				if (find) {
					PositionAndColor temp = new PositionAndColor(rx, ry, img2d[rx][ry]);
					// count++;
					resultRegion.add(temp);
					visited.push(temp);
					dist = Math.sqrt((x - rx) * (x - rx) + (y - ry) * (y - ry));
					// no pixel found -> go back
				} else {
					if (!visited.empty())
						visited.pop();
					if (!visited.empty()) {
						rx = visited.peek().x;
						ry = visited.peek().y;
						dist = Math.sqrt((x - rx) * (x - rx) + (y - ry) * (y - ry));
					}
				}
				// new pixel is not in radius -> go back
			} else {
				if (!visited.empty())
					visited.pop();
				if (!visited.empty()) {
					rx = visited.peek().x;
					ry = visited.peek().y;
					dist = Math.sqrt((x - rx) * (x - rx) + (y - ry) * (y - ry));
				}
				inside = true;
			}
		}
		return resultRegion;
	}
	
	/**
	 * Find maximal dimensions of a region (hashset).
	 * 
	 * @param hashSet
	 * @return int[] = {left, right, top, bottom}
	 */
	private static int[] findDimensions(HashSet<PositionAndColor> hashSet) {
		int[] dim = { Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE };
		for (Iterator<PositionAndColor> i = hashSet.iterator(); i.hasNext();) {
			PositionAndColor temp = i.next();
			if (temp.x < dim[0])
				dim[0] = temp.x;
			if (temp.x > dim[1])
				dim[1] = temp.x;
			if (temp.y < dim[2])
				dim[2] = temp.y;
			if (temp.y > dim[3])
				dim[3] = temp.y;
		}
		return dim;
	}
	
	/**
	 * Extract region (hashset) into 2d array.
	 * 
	 * @param dim
	 * @param region
	 * @return
	 */
	private static int[][] copyRegiontoImage(int[] dim, HashSet<PositionAndColor> region) {
		int[][] res = new int[(dim[1] - dim[0]) + 1][(dim[3] - dim[2]) + 1];
		ImageOperation.fillArray(res, ImageOperation.BACKGROUND_COLORint);
		for (Iterator<PositionAndColor> i = region.iterator(); i.hasNext();) {
			PositionAndColor temp = i.next();
			res[temp.x - dim[0]][temp.y - dim[2]] = temp.colorInt;
		}
		return res;
	}
	
	/**
	 * Calculation of the center of gravity (also known as centroid) of an image segment.
	 * (only supports already segmentated images, uses binary information, 1 if pixel != background color else 0)
	 * 
	 * @param img
	 *           - input image
	 * @param background
	 *           - background color
	 * @return Point
	 */
	static public Point calcCenterOfGravity(int[][] img, int background) {
		int w = img.length;
		int h = img[1].length;
		int sumX = 0;
		int sumY = 0;
		int area = 0;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (img[x][y] != background) {
					sumX += x;
					sumY += y;
					area++;
				}
			}
		}
		return new Point(sumX / area, sumY / area);
	}
	
	static public Point calcCenterOfGravity(Image image, int background) {
		int[][] img = image.getAs2A();
		return calcCenterOfGravity(img, background);
	}
	
	/**
	 * Copy result image on a other image (only if != background)
	 * 
	 * @param img
	 * @param imgMark
	 * @param dim
	 * @return
	 */
	private static Image copyResultIntoImage(Image img, Image imgMark, int[] dim) {
		int[][] img2d = img.getAs2A();
		int[][] img2dMark = imgMark.getAs2A();
		
		for (int x = 0; x < imgMark.getWidth(); x++) {
			for (int y = 0; y < imgMark.getHeight(); y++) {
				if (img2dMark[x][y] != ImageOperation.BACKGROUND_COLORint)
					img2d[dim[0] + x][dim[2] + y] = img2dMark[x][y];
			}
		}
		return new Image(img2d);
	}
	
	/**
	 * Get average region color HSV.
	 * 
	 * @param region
	 * @return
	 */
	private static Color getAverageRegionColorHSV(HashSet<PositionAndColor> region) {
		int c = 0, r = 0, g = 0, b = 0;
		float[] hsbvals = new float[3];
		float[] hsbvalssum = new float[3];
		
		for (PositionAndColor v : region) {
			c = v.colorInt;
			r += ((c & 0xff0000) >> 16);
			g += ((c & 0x00ff00) >> 8);
			b += (c & 0x0000ff);
			Color.RGBtoHSB(r, g, b, hsbvals);
			hsbvalssum[0] += hsbvals[0];
			hsbvalssum[1] += hsbvals[1];
			hsbvalssum[2] += hsbvals[2];
		}
		double size = region.size();
		return new Color((int) (r / size), (int) (g / size), (int) (b / size));
	}
	
	/**
	 * Get average Lab color of a region.
	 * 
	 * @param region
	 * @return
	 */
	private static Lab getAverageRegionColorLab(HashSet<PositionAndColor> region) {
		int c = 0;
		int r, g, b;
		int Li, ai, bi;
		
		double sumL = 0;
		double sumA = 0;
		double sumB = 0;
		
		int count = 0;
		float[][][] lab = ImageOperation.getLabCubeInstance();
		for (PositionAndColor v : region) {
			c = v.colorInt;
			
			r = ((c & 0xff0000) >> 16); // R 0..1
			g = ((c & 0x00ff00) >> 8); // G 0..1
			b = (c & 0x0000ff); // B 0..1
			
			Li = (int) lab[r][g][b];
			ai = (int) lab[r][g][b + 256];
			bi = (int) lab[r][g][b + 512];
			
			sumL += Li;
			sumA += ai;
			sumB += bi;
			
			count++;
		}
		
		return new Lab(sumL / count, sumA / count, sumB / count);
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Calculate Leaf Tips (testing)";
	}
	
	@Override
	public String getDescription() {
		return "Calculates leaf tips of a plant. (number of leafs)";
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		// TODO Auto-generated method stub
		return null;
	}
}
