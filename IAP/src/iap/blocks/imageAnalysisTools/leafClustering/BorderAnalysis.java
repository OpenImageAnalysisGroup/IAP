package iap.blocks.imageAnalysisTools.leafClustering;

import iap.blocks.imageAnalysisTools.leafClustering.FeatureObject.FeatureObjectType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeSet;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape
 */
public class BorderAnalysis {
	
	Image image;
	int[][] borderImage;
	int borderLength;
	private ArrayList<ArrayList<Integer>> borderLists;
	private final BorderFeatureList borderFeatureList;
	private LinkedList<BorderFeature> peakList;
	boolean debug = false;
	boolean onlyBiggest = true;
	boolean checkSplit = true;
	
	public BorderAnalysis(Image img) {
		ImageOperation borderIO = img.io().border().borderDetection(ImageOperation.BACKGROUND_COLORint, Color.BLUE.getRGB(), false);
		borderIO = borderIO.skeletonize().replaceColor(Color.BLACK.getRGB(), Color.BLUE.getRGB());
		Image boImg = borderIO.getImage();
		borderLength = boImg.io().countFilledPixels();
		borderImage = borderIO.getAs2D();
		borderLists = getBorderLists(borderImage, borderLength, debug);
		borderLists = sort();
		borderFeatureList = new BorderFeatureList(borderLists, onlyBiggest);
		image = img;
		this.setDebug(false);
	}
	
	/**
	 * Extract Peaks of feature list. (get middle of peaks)
	 **/
	public void getPeaksFromBorder(double minSizeOfPeak, int distBetweenPeaks, String filterKey) {
		peakList = new LinkedList<BorderFeature>();
		int listsize = borderFeatureList.size();
		int[] peaks = null;
		
		// convert data
		double[] data = new double[borderFeatureList.size()];
		for (int idxFeature = 0; idxFeature < listsize; idxFeature++) {
			if ((borderFeatureList.getFeature(idxFeature, filterKey)) != null) {
				data[idxFeature] = (Double) borderFeatureList.getFeature(idxFeature, filterKey);
			}
		}
		
		// for (double d : data) {
		// System.out.println(d);
		// }
		
		boolean duplicateArrayForEdgePeakDetection = true;
		if (duplicateArrayForEdgePeakDetection) {
			double[] data2 = new double[data.length * 2];
			for (int i = 0; i < data2.length; i++)
				data2[i] = data[i % data.length];
			peaks = CurveAnalysis.findMaximaIJ(data2, 1, true);
			TreeSet<Integer> val = new TreeSet<Integer>();
			for (int p : peaks) {
				val.add(p % data.length);
			}
			peaks = new int[val.size()];
			int i = 0;
			for (int p : val)
				peaks[i++] = p;
		} else
			peaks = CurveAnalysis.findMaximaIJ(data, 1, false);
		
		peaks = CurveAnalysis.summarizeMaxima(peaks, listsize, distBetweenPeaks);
		
		// save results
		for (int idx = 0; idx < peaks.length; idx++) {
			peakList.add(borderFeatureList.getFeatureMap(peaks[idx]));
			peakList.get(idx).addFeature("borderposition", peaks[idx], FeatureObjectType.NUMERIC);
		}
	}
	
	public void plot(int waitTime, int radius) {
		Image img = image.copy();
		
		ImageCanvas ic = img.copy().io().canvas();
		LinkedList<Point3d> normSUSANList = borderFeatureList.normalizeBorderFeatureList("susan");
		
		// plot SUSAN feature
		for (Point3d p : normSUSANList) {
			ic.drawCircle((int) p.x, (int) p.y, 3, Color.HSBtoRGB((float) (p.z / 255.0) * 0.3f, (float) 1.0, (float) 1.0), 0.5, 1);
		}
		
		// plot peak list and peak features
		for (BorderFeature p : peakList) {
			Vector2D pos = p.getPosition();
			Vector2D direction = (Vector2D) p.getFeature("direction");
			Double angle = (Double) p.getFeature("angle");
			Integer borderpos = (Integer) p.getFeature("borderposition");
			ic.drawCircle((int) pos.getX(), (int) pos.getY(), radius, Color.GREEN.getRGB(), 0.5, 3);
			ic.text((int) pos.getX() + 10, (int) pos.getY() + 10, "x: " + (int) pos.getX() + " y: " + (int) pos.getY(), Color.DARK_GRAY);
			if (direction != null)
				ic.drawLine((int) pos.getX(), (int) pos.getY(), (int) direction.getX(), (int) direction.getY(), Color.BLUE.getRGB(), 0.5, 2)
						.text((int) direction.getX() + 10, (int) direction.getY() + 10, "dir: " + angle.intValue(), Color.DARK_GRAY)
						.text((int) pos.getX(), (int) pos.getY() - 10, "bp: " + borderpos, Color.DARK_GRAY);
		}
		
		ic.getImage().show("susanImage");
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Approximation of leaf tip direction.
	 */
	public void approxDirection(int triangleSize) {
		// LinkedList<Point3d> directionList = new LinkedList<Point3d>();
		int listSize = borderFeatureList.size();
		for (int idx = 0; idx < listSize; idx++) {
			int back = (((idx - triangleSize) % listSize) + listSize) % listSize;
			int ahead = (idx + triangleSize) % listSize;
			
			// is peak
			for (BorderFeature p : peakList) {
				Vector2D pos = p.getPosition();
				Vector2D p2 = borderFeatureList.get(idx).getPosition();
				if (pos.getX() == p2.getX() && pos.getY() == p2.getY()) {
					Vector2D p1 = borderFeatureList.get(back).getPosition();
					Vector2D p3 = borderFeatureList.get(ahead).getPosition();
					Point2d p4 = new Point2d((p1.getX() + p3.getX()) / 2, (p1.getY() + p3.getY()) / 2);
					Vector2D v1 = new Vector2D((p2.getX() - p4.x) * 1.5 + p4.x, (p2.getY() - p4.y) * 1.5 + p4.y);
					p.addFeature("direction", v1, FeatureObjectType.VECTOR);
					// p.addFeature("angle", v1.angle(new Vector2d(1.0, 0.0)));
					// calculate angle
					Vector2D trans1 = v1.subtract(p2);
					Vector2D trans2 = new Vector2D(0.0, 1.0);
					double angle = calcAngle(trans1, trans2);
					p.addFeature("angle", angle, FeatureObjectType.NUMERIC);
				}
			}
		}
	}
	
	private double calcAngle(Vector2D v1, Vector2D v2) {
		double val = (v1.getX() * v2.getX() + v1.getY() * v2.getY())
				/ ((Math.sqrt(v1.getX() * v1.getX() + v1.getY() * v1.getY())) * (Math.sqrt(v2.getX() * v2.getX() + v2.getY() * v2.getY())));
		return Math.acos(val) * 180d / Math.PI;
	}
	
	// TODO adapt for multiple borders if needed.
	public void calcSUSAN(int radius, int geometricThresh) {
		int background = ImageOperation.BACKGROUND_COLORint;
		int xtemp;
		int ytemp;
		int[][] img2d = image.getAs2A();
		int w = image.getWidth();
		int h = image.getHeight();
		int stepsize = 1;
		boolean debug = false;
		String key = "susan";
		// check if stepsize is odd
		if (stepsize % 2 != 0)
			stepsize++;
		
		// iterate list of border-lists
		for (ArrayList<Integer> tempArray : borderLists) {
			int listSize = tempArray.size();
			for (int idx = 0; idx < listSize; idx += 2) {
				xtemp = tempArray.get(idx);
				ytemp = tempArray.get(idx + 1);
				
				// get region (boundingbox) in size of circle-diameter
				int[][] predefinedRegion = ImageOperation.crop(img2d, w, h, xtemp - radius, xtemp + radius, ytemp - radius, ytemp + radius);
				
				// do region-growing
				ArrayList<PositionAndColor> region = regionGrowing(radius, radius, predefinedRegion, background, radius, geometricThresh, debug);
				
				// check area (condition: (region.size() < geometricThresh) => get only positive results)
				if (region != null) {
					if (region.size() < geometricThresh) {
						// test for split region
						boolean split = false;
						if (checkSplit)
							split = isSplit(predefinedRegion, region, radius, debug);
						if (!split || !checkSplit)
							borderFeatureList.addFeature(idx / 2, (double) (geometricThresh - region.size()), key, FeatureObjectType.NUMERIC);
						else
							borderFeatureList.addFeature(idx / 2, 0.0, key, FeatureObjectType.NUMERIC);
					} else
						borderFeatureList.addFeature(idx / 2, 0.0, key, FeatureObjectType.NUMERIC);
				} else
					borderFeatureList.addFeature(idx / 2, Double.NaN, key, FeatureObjectType.NUMERIC);
			}
			if (onlyBiggest)
				break;
		}
	}
	
	public static boolean isSplit(int[][] region, ArrayList<PositionAndColor> regionColors, int radius, boolean debug) {
		debug = false;
		int[][] region2d = new int[region.length][region[0].length];
		region2d = copyRegiontoArray(region, regionColors);
		int w = region2d.length;
		int h = region2d[0].length;
		LinkedList<int[]> circleCoordinates = getCircleCoordinatesSorted(radius - (int) (radius * 0.05));
		int splitCount = 0, count = 0;
		boolean pfound = false, found = false, firstfound = false;
		int background = ImageOperation.BACKGROUND_COLORint;
		for (int[] p : circleCoordinates) {
			int px = (p[0] + radius); // for test + radius
			int py = (p[1] + radius);
			// TODO background
			if (px < w && py < h && px >= 0 && py >= 0) {
				if (region2d[px][py] != background && region2d[px][py] != -1) {
					pfound = true;
					if (count == 0)
						firstfound = true;
					if (count == circleCoordinates.size() - 1 && firstfound)
						splitCount--;
					if (debug)
						region2d[px][py] = Color.BLUE.getRGB();
				}
				if (pfound) {
					if (!found) {
						found = true;
						splitCount++;
					}
				} else {
					found = false;
				}
			}
			pfound = false;
			count++;
		}
		if (debug && splitCount > 1) {
			new Image(region2d).show("detect");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return splitCount > 1 ? true : false;
	}
	
	/**
	 * Find maximal dimensions of a region.
	 * 
	 * @param list
	 * @return int[] = {left, right, top, bottom}
	 */
	public static int[] findDimensions(ArrayList<PositionAndColor> list) {
		int[] dim = { Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE };
		if (list == null)
			return null;
		for (Iterator<PositionAndColor> i = list.iterator(); i.hasNext();) {
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
	 * Extract region into 2d array.
	 * 
	 * @param dim
	 * @param region
	 * @return
	 */
	public static int[][] copyRegiontoArray(int[] dim, ArrayList<PositionAndColor> region) {
		int[][] res = new int[(dim[1] - dim[0]) + 1][(dim[3] - dim[2]) + 1];
		ImageOperation.fillArray(res, ImageOperation.BACKGROUND_COLORint);
		for (Iterator<PositionAndColor> i = region.iterator(); i.hasNext();) {
			PositionAndColor temp = i.next();
			res[temp.x - dim[0]][temp.y - dim[2]] = temp.colorInt;
		}
		return res;
	}
	
	private static int[][] copyRegiontoArray(int[][] res, ArrayList<PositionAndColor> regiona) {
		ImageOperation.fillArray(res, ImageOperation.BACKGROUND_COLORint);
		for (Iterator<PositionAndColor> i = regiona.iterator(); i.hasNext();) {
			PositionAndColor temp = i.next();
			res[temp.x][temp.y] = temp.colorInt;
		}
		return res;
	}
	
	public static boolean isSplit(ArrayList<PositionAndColor> region,
			int radius, boolean debug) throws InterruptedException {
		final int[] dim = findDimensions(region);
		int[][] region2d = copyRegiontoArray(dim, region);
		int w = region2d.length;
		int h = region2d[0].length;
		LinkedList<int[]> circleCoordinates = getCircleCoordinatesSorted(radius);
		
		int splitCount = 0, count = 0;
		boolean pfound = false, found = false, firstfound = false;
		int background = ImageOperation.BACKGROUND_COLORint;
		for (int[] p : circleCoordinates) {
			int px = (p[0] + radius); // for test + radius
			int py = (p[1] + radius);
			// TODO background
			if (px < w && py < h && px >= 0 && py >= 0) {
				if (region2d[px][py] != background && region2d[px][py] != -1) {
					pfound = true;
					if (count == 0)
						firstfound = true;
					if (count == circleCoordinates.size() - 1 && firstfound)
						splitCount--;
					if (debug)
						region2d[px][py] = Color.BLUE.getRGB();
				}
				if (pfound) {
					if (!found) {
						found = true;
						splitCount++;
					}
				} else {
					found = false;
				}
			}
			pfound = false;
			count++;
		}
		if (debug) {
			new Image(region2d).show("detect");
			Thread.sleep(10000);
		}
		return splitCount > 1 ? true : false;
	}
	
	private static LinkedList<int[]> sortCircleCoordinates(LinkedList<int[]> circleCoordinates) {
		LinkedList<int[]> sorted = new LinkedList<int[]>();
		
		int[] a = circleCoordinates.pollFirst();
		
		while (!circleCoordinates.isEmpty()) {
			int mindist = Integer.MAX_VALUE;
			int[] pmindist = null;
			for (int[] j : circleCoordinates) {
				int dist = distQ(a, j);
				if (dist < mindist) {
					mindist = dist;
					pmindist = j;
				}
			}
			sorted.add(a.clone());
			a = pmindist.clone();
			circleCoordinates.remove(pmindist);
		}
		return sorted;
	}
	
	private static int distQ(int[] a, int[] b) {
		return (a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]);
	}
	
	private static LinkedList<int[]> getCircleCoordinatesSorted(int radius) {
		LinkedList<int[]> res = new LinkedList<int[]>();
		int x = 0;
		int y = radius;
		int f = 1 - radius;
		res.add(new int[] { 0, radius });
		res.add(new int[] { radius, 0 });
		res.add(new int[] { 0, -radius });
		res.add(new int[] { -radius, 0 });
		while (x < y) {
			x = x + 1;
			if (f < 0) {
				f = f + 2 * x - 1;
			} else {
				f = f + 2 * (x - y);
				y = y - 1;
			}
			res.add(new int[] { x, y });
			res.add(new int[] { y, x });
			res.add(new int[] { -x, y });
			res.add(new int[] { y, -x });
			res.add(new int[] { x, -y });
			res.add(new int[] { -y, x });
			res.add(new int[] { -x, -y });
			res.add(new int[] { -y, -x });
		}
		return res = sortCircleCoordinates(res);
	}
	
	public static ArrayList<PositionAndColor> regionGrowing(int x, int y, int[][] img2d, int background, int radius, int geometricThresh, boolean debug) {
		ArrayList<PositionAndColor> region = null;
		try {
			region = regionGrowing(img2d, x, y, background, radius, geometricThresh, debug);
		} catch (InterruptedException e) {
			region = new ArrayList<PositionAndColor>();
			e.printStackTrace();
		}
		return region;
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
	 *           - max search distance/deep (euclidean) for Region Growing from start point, for classic Region Growing, set this value to max value
	 * @return HashSet which includes Vector3d of all point coordinates plus color-values
	 * @throws InterruptedException
	 */
	static ArrayList<PositionAndColor> regionGrowing(int[][] img2d, int x, int y, int background, double radius, int geometricThresh, boolean debug)
			throws InterruptedException {
		radius = radius * radius;
		int[][] imgTemp = img2d.clone();
		int w = img2d.length;
		int h = img2d[1].length;
		Stack<PositionAndColor> visited = new Stack<PositionAndColor>();
		ArrayList<PositionAndColor> resultRegion = new ArrayList<PositionAndColor>();
		int rx = x;
		int ry = y;
		PositionAndColor start = new PositionAndColor(x, y, img2d[x][y]);
		visited.push(start);
		resultRegion.add(start);
		boolean find;
		boolean inside = false;
		double dist = 0.0;
		Image show = null;
		boolean speedUpButLossResults = false;
		if (debug) {
			show = new Image(imgTemp);
			show.show("debug");
		}
		
		// test if new pixel is in image space
		if (rx >= 0 && ry >= 0 && rx < w && ry < h)
			inside = true;
		
		imgTemp[rx][ry] = background;
		
		while (!visited.empty()) {
			// update process window for debug
			if (imgTemp[rx][ry] != background && debug) {
				show.update(new Image(imgTemp));
				Thread.sleep(1);
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
				
				// Found new pixel?
				if (find) {
					PositionAndColor temp = new PositionAndColor(rx, ry, img2d[rx][ry]);
					// count++;
					resultRegion.add(temp);
					// current region bigger than geometricThresh
					if (resultRegion.size() > geometricThresh - 1 && speedUpButLossResults)
						return resultRegion;
					visited.push(temp);
					dist = (x - rx) * (x - rx) + (y - ry) * (y - ry);
					// no pixel found -> go back
				} else {
					if (!visited.empty())
						visited.pop();
					if (!visited.empty()) {
						rx = visited.peek().x;
						ry = visited.peek().y;
						dist = (x - rx) * (x - rx) + (y - ry) * (y - ry);
					}
				}
				// new pixel is not in radius -> go back
			} else {
				if (!visited.empty())
					visited.pop();
				if (!visited.empty()) {
					rx = visited.peek().x;
					ry = visited.peek().y;
					dist = (x - rx) * (x - rx) + (y - ry) * (y - ry);
				}
				inside = true;
			}
		}
		return resultRegion;
	}
	
	/**
	 * Calculates sorted border list (depends on 8 neighborhood, border should be connected, assumed there is one big segment), uses recursive border-search
	 * (implemented as loop).
	 * 
	 * @param borderMap
	 *           - border-image as 2d array
	 * @param borderLength
	 *           - number of border-pixels
	 * @return 1d array of sorted border-pixels ([obj_1[x_1,y_1,x_2,y_2, ... , x_n, y_n], obj_2[x_1,y_1,x_2,y_2, ... , x_n, y_n], ... , obj_n ])
	 * @throws InterruptedException
	 */
	// TODO adaption for disconnected border has to be checked
	private static ArrayList<ArrayList<Integer>> getBorderLists(int[][] borderMap, int borderLength, boolean debug) {
		int w = borderMap.length;
		int h = borderMap[0].length;
		ArrayList<Integer> borderList = new ArrayList<Integer>();
		
		boolean find = false;
		int background = ImageOperation.BACKGROUND_COLORint;
		int rx;
		int ry;
		ArrayList<ArrayList<Integer>> borderListList = new ArrayList<ArrayList<Integer>>();
		
		int debugSpeed = 2;
		Image show = new Image(borderMap);
		if (debug)
			show.show("getBorder debug");
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (borderMap[x][y] != background) {
					int i = 0;
					find = true;
					rx = x;
					ry = y;
					boolean inside = false;
					
					if (rx - 1 >= 0 && ry - 1 >= 0 && rx + 1 < w && ry + 1 < h)
						inside = true;
					
					while (find) {
						borderList.add(rx);
						borderList.add(ry);
						i += 2;
						borderMap[rx][ry] = background;
						find = false;
						
						if (debug) {
							show.update(new Image(borderMap));
							try {
								Thread.sleep(debugSpeed);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
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
					
					// check if at least 1 element in list
					if (borderList.size() > 0 && borderList != null) {
						borderListList.add((ArrayList<Integer>) borderList.clone());
						borderList.clear();
					}
				}
			}
		}
		return borderListList;
	}
	
	private ArrayList<ArrayList<Integer>> sort() {
		Collections.sort(borderLists, new Comparator<ArrayList<Integer>>() {
			
			@Override
			public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
				if (o1.size() == o2.size())
					return 0;
				return o1.size() < o2.size() ? 1 : -1;
			}
		});
		return borderLists;
	}
	
	public ArrayList<ArrayList<Integer>> getBorderLists() {
		return borderLists;
	}
	
	public LinkedList<BorderFeature> getPeakList() {
		return peakList;
	}
	
	public void setOnlyBiggest(boolean val) {
		onlyBiggest = val;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setCheckSplit(boolean checkSplit) {
		this.checkSplit = checkSplit;
	}
}
