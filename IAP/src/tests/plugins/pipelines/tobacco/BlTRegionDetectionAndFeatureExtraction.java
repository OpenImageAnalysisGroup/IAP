package tests.plugins.pipelines.tobacco;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import org.StringManipulationTools;
import org.Vector2d;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.Lab;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Block for tobacco flower feature extraction. Also images can be marked with the results.
 * 
 * @author pape
 */
public class BlTRegionDetectionAndFeatureExtraction extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		
		boolean mark = getBoolean("mark flower results", true);
		
		Image img = input().masks().vis();
		
		if (img == null)
			return null;
		// add border
		img = img.io().addBorder(1, 1, 1, ImageOperation.BACKGROUND_COLORint).getImage();
		// get rotation of plant
		Double rotation = input().images().getVisInfo().getPosition();
		if (rotation == null)
			rotation = 0d;
		
		int background = ImageOperation.BACKGROUND_COLORint;
		int[][] img2d = img.getAs2A();
		int w = img.getWidth();
		int h = img.getHeight();
		ArrayList<HashSet<PositionAndColor>> regionlist = new ArrayList<HashSet<PositionAndColor>>();
		int minSize = getInt("minimum flower size", 20);
		
		// find flower regions
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (img2d[x][y] != background) {
					HashSet<PositionAndColor> region = regionGrowing(img2d, x, y, background, Integer.MAX_VALUE);
					if (region.size() > minSize)
						regionlist.add(region);
				}
			}
		}
		
		// sort regions (area) and delete small ones || y-position
		Collections.sort(regionlist, new Comparator<HashSet<PositionAndColor>>() {
			
			@Override
			public int compare(HashSet<PositionAndColor> a, HashSet<PositionAndColor> b) {
				if (a.size() == b.size())
					return 0;
				return a.size() > b.size() ? -1 : 1;
			}
		});
		
		// filter regions (delete small ones, maybe it is enough to use remove small clusters)
		
		ArrayList<int[][]> regionImages = new ArrayList<int[][]>();
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		int index = 1;
		// calculate features and mark result
		for (int i = 0; i < regionlist.size(); i++) {
			
			HashSet<PositionAndColor> region = regionlist.get(i);
			int[] dim = findDimensions(region);
			
			regionImages.add(copyRegiontoImage(dim, region));
			
			Image regionimg = new Image(regionImages.get(i));
			
			regionimg.show("regionimg" + index, !preventDebugValues && getBoolean("debug", false));
			
			double my20 = calcNormalizedCentralMoment(2, 0, regionimg, background);
			double my11 = calcNormalizedCentralMoment(1, 1, regionimg, background);
			double my02 = calcNormalizedCentralMoment(0, 2, regionimg, background);
			
			double omega = 0.0;
			
			if (my11 != 0)
				// use atan2 for case differentiation, see polar-coordinates
				omega = Math.atan2((2.0) * my11, my20 - my02) / 2;
			
			double gamma = omega + Math.PI / 2;
			if (gamma > Math.PI)
				gamma = 2 * Math.PI - gamma;
			
			Vector2d centerOfGravity = calcCenterOfGravity(regionimg, background);
			
			// TopBottomLeftRight ext = img.io().getExtremePoints(background);
			Vector2d midPoint = new Vector2d(Math.abs(regionimg.getWidth() / 2), Math.abs(regionimg.getHeight() / 2));
			// midPoint.translate(-dim[0], -dim[2]);
			
			double distCoGToMid = centerOfGravity.distance(midPoint.x, midPoint.y);
			
			double lambda1 = 0.0;
			double lambda2 = 0.0;
			
			double f1 = (my20 + my02) / 2;
			double f2 = Math.sqrt(4 * (my11 * my11) + ((my20 - my02) * (my20 - my02))) / 2;
			
			lambda1 = f1 + f2;
			lambda2 = f1 - f2;
			
			// eccentricity
			double eccentricity = 0.0;
			eccentricity = Math.sqrt(1 - lambda2 / lambda1);
			
			Color regionColorRgbAvg = getAverageRegionColorRGB(region);
			Lab regionColorLabAvg = getAverageRegionColorLab(region);
			Color regionColorHsvAvg = getAverageRegionColorHSV(region);
			
			// ratio y between center of gravity and geometric center
			// > 1 => pointing downwards, < 1 up
			double direction = centerOfGravity.y / midPoint.y;
			
			if (direction > 1) {
				// down
				if (gamma < Math.PI / 2) {
					// correct
				} else {
					gamma = Math.PI - gamma;
				}
			} else {
				// up
				if (gamma > Math.PI / 2) {
					// correct
				} else {
					gamma = Math.PI - gamma;
				}
			}
			// gamma = Math.abs(gamma);
			
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".omega", omega);
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".gamma", gamma);
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".direction", direction);
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".CoG.x", centerOfGravity.x + dim[0]);
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".CoG.y", centerOfGravity.y + dim[2]);
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".distanceBetweenCoGandMidPoint", distCoGToMid);
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".area", region.size());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".eccentricity", eccentricity);
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.rgb.r", regionColorRgbAvg.getRed());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.rgb.g", regionColorRgbAvg.getGreen());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.rgb.b", regionColorRgbAvg.getBlue());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.lab.l", regionColorLabAvg.getAverageL());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.lab.a", regionColorLabAvg.getAverageA());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.lab.b", regionColorLabAvg.getAverageB());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.hsv.h", regionColorHsvAvg.getRed());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.hsv.s", regionColorHsvAvg.getGreen());
			rt.addValue("flower." + StringManipulationTools.formatNumber(index) + ".color.hsv.v", regionColorHsvAvg.getBlue());
			// TODO : add border features e.g. number of border pixels
			
			// mark in image
			if (mark) {
				ImageCanvas imgMark = regionimg.copy().io().canvas();
				// final int xp = 20;
				// midpoint
				imgMark = imgMark
						.drawCircle((int) midPoint.x, (int) midPoint.y, 10, Color.YELLOW.getRGB(), 0.5, 2);
				
				// getProperties().addImagePostProcessor(new RunnableOnImageSet() {
				//
				// @Override
				// public Image postProcessImage(Image vis) {
				// // TODO Auto-generated method stub
				// return vis.io().canvas().fillCircle(xp, my, radius, color, alpha);
				// }
				//
				// @Override
				// public ImageConfiguration getConfig() {
				// // TODO Auto-generated method stub
				// return ImageConfiguration.RgbSide;
				// }
				// });
				
				// centroid
				imgMark = imgMark
						.drawCircle((int) centerOfGravity.x, (int) centerOfGravity.y, 10, Color.RED.getRGB(), 0.5, 5);
				
				de.ipk.ag_ba.image.operations.complex_hull.Point p1 = null;
				
				double length1 = 50 * lambda1;
				double length2 = 100 * lambda2;
				
				p1 = new de.ipk.ag_ba.image.operations.complex_hull.Point(
						(centerOfGravity.x + length1 * Math.cos(omega)),
						(centerOfGravity.y + length1 * Math.sin(omega)));
				
				imgMark = imgMark
						.drawLine(p1, new de.ipk.ag_ba.image.operations.complex_hull.Point(centerOfGravity.x, centerOfGravity.y), Color.BLUE.getRGB(), 0.3, 4);
				
				// 2nd
				de.ipk.ag_ba.image.operations.complex_hull.Point p2 = null;
				
				p2 = new de.ipk.ag_ba.image.operations.complex_hull.Point(
						(centerOfGravity.x + length2 * Math.sin(omega)),
						(centerOfGravity.y + length2 * -Math.cos(omega)));
				
				imgMark = imgMark
						.drawLine(p2, new de.ipk.ag_ba.image.operations.complex_hull.Point(centerOfGravity.x, centerOfGravity.y), Color.PINK.getRGB(), 0.3, 4);
				
				img = copyResultIntoImage(img, imgMark.getImage(), dim);
				
				img = img
						.io()
						.canvas()
						.text((int) centerOfGravity.x + dim[0] + 20, (int) centerOfGravity.y + dim[2], (direction > 1 ? "down" : "up"),
								Color.BLACK)
						.text((int) centerOfGravity.x + dim[0] + 20, (int) centerOfGravity.y + dim[2] + 20, (int) (gamma * 180d / Math.PI) + "°",
								Color.BLACK)
						.text((int) centerOfGravity.x + dim[0] + 20, (int) centerOfGravity.y + dim[2] + 40, region.size() + "px",
								Color.BLACK)
						.text((int) centerOfGravity.x + dim[0] + 20, (int) centerOfGravity.y + dim[2] + 60, "no. " + index,
								Color.BLACK).getImage();
			}
			index++;
		}
		
		getResultSet().storeResults("RESULT_", rt, getBlockPosition());
		
		// remove border
		img = deleteBorder(img, 1);
		
		return img;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	/**
	 * Copy the image into a new image, which size is decreased according to the specified bordersize.
	 * 
	 * @param input
	 * @param bordersize
	 * @param translatex
	 * @param translatey
	 * @return Image
	 */
	public Image deleteBorder(Image image, int bordersize) {
		if (bordersize == 0)
			return image;
		int width = image.getWidth();
		int height = image.getHeight();
		
		int[][] img2d = image.getAs2A();
		int nw = width - (2 * bordersize);
		int nh = height - (2 * bordersize);
		int[][] result = new int[nw][nh];
		
		for (int x = 0; x < nw; x++) {
			for (int y = 0; y < nh; y++) {
				result[x][y] = img2d[x + bordersize][y + bordersize];
			}
		}
		return new Image(result);
	}
	
	/**
	 * Get average region color RGB.
	 * 
	 * @param region
	 * @return
	 */
	private Color getAverageRegionColorRGB(HashSet<PositionAndColor> region) {
		int c = 0, r = 0, g = 0, b = 0;
		
		for (PositionAndColor v : region) {
			c = v.colorInt;
			r += ((c & 0xff0000) >> 16);
			g += ((c & 0x00ff00) >> 8);
			b += (c & 0x0000ff);
		}
		double size = region.size();
		return new Color((int) (r / size), (int) (g / size), (int) (b / size));
	}
	
	/**
	 * Get average HSV color of region.
	 * 
	 * @param region
	 * @return
	 */
	private Color getAverageRegionColorHSV(HashSet<PositionAndColor> region) {
		int c = 0, r = 0, g = 0, b = 0;
		float[] hsb = new float[3];
		float[] sum = new float[3];
		
		for (PositionAndColor v : region) {
			c = v.colorInt;
			r = ((c & 0xff0000) >> 16);
			g = ((c & 0x00ff00) >> 8);
			b = (c & 0x0000ff);
			Color.RGBtoHSB(r, g, b, hsb);
		}
		
		sum[0] += hsb[0];
		sum[1] += hsb[1];
		sum[2] += hsb[2];
		
		float size = region.size();
		return new Color((sum[0] / size), (sum[1] / size), (sum[2] / size));
	}
	
	/**
	 * Get average Lab color of a region.
	 * 
	 * @param region
	 * @return
	 */
	private Lab getAverageRegionColorLab(HashSet<PositionAndColor> region) {
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
	
	/**
	 * Copy result image on a other image (only if != background)
	 * 
	 * @param img
	 * @param imgMark
	 * @param dim
	 * @return
	 */
	private Image copyResultIntoImage(Image img, Image imgMark, int[] dim) {
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
	 * Extract region (hashset) into 2d array.
	 * 
	 * @param dim
	 * @param region
	 * @return
	 */
	private int[][] copyRegiontoImage(int[] dim, HashSet<PositionAndColor> region) {
		int[][] res = new int[(dim[1] - dim[0]) + 1][(dim[3] - dim[2]) + 1];
		ImageOperation.fillArray(res, ImageOperation.BACKGROUND_COLORint);
		for (Iterator<PositionAndColor> i = region.iterator(); i.hasNext();) {
			PositionAndColor temp = i.next();
			res[temp.x - dim[0]][temp.y - dim[2]] = temp.colorInt;
		}
		return res;
	}
	
	/**
	 * Find maximal dimensions of a region (hashset).
	 * 
	 * @param hashSet
	 * @return int[] = {left, right, top, bottom}
	 */
	private int[] findDimensions(HashSet<PositionAndColor> hashSet) {
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
	 * Calculation of the center of gravity (also known as centroid) of an image segment.
	 * (only supports already segmentated images, uses binary information, 1 if pixel != background color else 0)
	 * 
	 * @param img
	 *           - input image
	 * @param background
	 *           - background color
	 * @return Point
	 */
	static Vector2d calcCenterOfGravity(Image img, int background) {
		int[][] img2d = img.getAs2A();
		int w = img.getWidth();
		int h = img.getHeight();
		int sumX = 0;
		int sumY = 0;
		double area = 0;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (img2d[x][y] != background) {
					sumX += x;
					sumY += y;
					area++;
				}
			}
		}
		return new Vector2d(sumX / area, sumY / area);
	}
	
	/**
	 * Calculation of the normalized n-nd order central image moments mu_ij (greek).
	 * This moments are invariant between translation and scaling.
	 * Formula from wikipedia http://en.wikipedia.org/wiki/Image_moment (normalized by area).
	 * 
	 * @param i
	 *           - exponent i
	 * @param j
	 *           - exponent j
	 * @param img
	 *           - input image
	 * @param background
	 *           - background color
	 * @return 2nd order moment weighted by the area (first order moment)
	 */
	static double calcNormalizedCentralMoment(double i, double j, Image img, int background) {
		Vector2d centerOfGravity = calcCenterOfGravity(img, background);
		double cogX = centerOfGravity.x;
		double cogY = centerOfGravity.y;
		
		double tempSum = 0;
		double area = 0;
		
		int[][] img2d = img.getAs2A();
		int w = img.getWidth();
		int h = img.getHeight();
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (img2d[x][y] != background) {
					tempSum += Math.pow(x - cogX, i) * Math.pow(y - cogY, j);
					area++;
				}
			}
		}
		// normalization
		return tempSum / Math.pow(area, (i + j + 2) / 2);
	}
	
	/**
	 * @param x
	 *           - Region Growing start-point x coordinate
	 * @param y
	 *           - Region Growing start-point y coordinate
	 * @param background
	 *           - background color
	 * @param rad
	 *           - max search distance (euclidian) for Region Growing from start point, for classic Region Growing, set this value to max
	 * @return HashSet which includes all PositionAndColor values
	 */
	public HashSet<PositionAndColor> regionGrowing(int[][] img2d, int x, int y, int background, double rad) {
		// System.out.println("x: " + x + " y: " + y);
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
		
		// test if new pixel is in image space
		if (rx > 1 && ry > 1 && rx < w - 1 && ry < h - 1)
			inside = true;
		
		while (!visited.empty()) {
			imgTemp[rx][ry] = background;
			find = false;
			
			if (inside && dist < rad) {
				if (imgTemp[rx - 1][ry - 1] != background) {
					find = true;
					rx = rx - 1;
					ry = ry - 1;
				}
				if (imgTemp[rx][ry - 1] != background && !find) {
					find = true;
					ry = ry - 1;
				}
				
				if (imgTemp[rx + 1][ry - 1] != background && !find) {
					find = true;
					rx = rx + 1;
					ry = ry - 1;
				}
				
				if (imgTemp[rx - 1][ry] != background && !find) {
					find = true;
					rx = rx - 1;
				}
				
				if (imgTemp[rx + 1][ry] != background && !find) {
					find = true;
					rx = rx + 1;
				}
				
				if (imgTemp[rx - 1][ry + 1] != background && !find) {
					find = true;
					rx = rx - 1;
					ry = ry + 1;
				}
				
				if (imgTemp[rx][ry + 1] != background && !find) {
					find = true;
					ry = ry + 1;
				}
				
				if (imgTemp[rx + 1][ry + 1] != background && !find) {
					find = true;
					rx = rx + 1;
					ry = ry + 1;
				}
				
				// find new pixel?
				if (find) {
					PositionAndColor temp = new PositionAndColor(rx, ry, img2d[rx][ry]);
					visited.push(temp);
					resultRegion.add(temp);
					dist = Math.sqrt((x - rx) * (x - rx) + (y - ry) * (y - ry));
					// new pixel in image space and not at the image border?
					if (!(rx > 0 && ry > 0 && rx < w && ry < h))
						inside = false;
					// no pixel found -> go back
				} else {
					visited.pop();
					if (!visited.empty()) {
						rx = visited.peek().x;
						ry = visited.peek().y;
						dist = Math.sqrt((x - rx) * (x - rx) + (y - ry) * (y - ry));
					}
				}
				// new pixel is not in image space -> go back
			} else {
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
	
	@Override
	public String getName() {
		return "Tobacco Region Detection and Feature Extraction";
	}
	
	@Override
	public String getDescription() {
		return "ToDo Add Description!";
	}
}
