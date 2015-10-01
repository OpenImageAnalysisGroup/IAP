package iap.blocks.image_analysis_tools.cvppp_2014;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.Colors;
import org.Vector2i;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.image_analysis_tools.imageJ.externalPlugins.MaximumFinder;
import iap.blocks.image_analysis_tools.leafClustering.Feature;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class LeafCountCvppp {
	
	Image[] images;
	Image[] resultImages;
	HashMap<String, ArrayList<Feature>> centerPoints = new HashMap<String, ArrayList<Feature>>();
	boolean debug = false;
	boolean markAndSaveResultImages = true;
	
	public LeafCountCvppp(Image[] images) {
		this.images = images;
		resultImages = new Image[images.length];
	}
	
	public void detectLeaves(int maxTolerance) throws InterruptedException {
		
		// old tuning parms
		// final double maxTolerance = 10.0; // 0.1 * getTuningValue("maxTolerance", 1.0, 1.0, 30.0, tune); // A1 : 10, A2 : 11
		final double scaleErode = 0.0;// getTuningValue("scaleErode", 1.0, 1.0, 30.0, tune); // A 2 : 0
		
		for (int idx = 0; idx < images.length; idx++) {
			final int idx_fin = idx;
			final Image segmented = images[idx];
			resultImages[idx_fin] = runS(maxTolerance, segmented.copy(), scaleErode);
			resultImages[idx_fin].setFilename(segmented.getFileName());
		}
	}
	
	private Image runS(double maxTolerance, Image segmented, double scaleErode) {
		Image segmentedUnchanged = segmented.copy();
		ArrayList<Feature> leafCenterPoints;
		
		leafCenterPoints = detectCenterPoints(segmented.copy(), maxTolerance);
		
		String id = "1";
		
		ArrayList<Color> colors = Colors.get(leafCenterPoints.size(), 1);
		segmented.setFilename(segmentedUnchanged.getFileName());
		segmented = insertSplitPoints(segmented, leafCenterPoints, true);
		ImageCanvas ic = new ImageCanvas(new Image(segmented.getWidth(), segmented.getHeight(), 0));
		ImageCanvas ic2 = new ImageCanvas(segmented.copy());
		int coloridx = 0;
		for (Feature p : leafCenterPoints) {
			ic.drawRectangle((int) p.getPosition().getX() - 10, (int) p.getPosition().getY() - 10, 21, 21, colors.get(coloridx), 1);
			ic2.drawRectangle((int) p.getPosition().getX() - 10, (int) p.getPosition().getY() - 10, 21, 21, colors.get(coloridx), 1);
			coloridx++;
		}
		
		ImagePlus ip = ic2.getImage().getAsImagePlus();
		ImageConverter imgc = new ImageConverter(ip);
		imgc.convertRGBtoIndexedColor(256);
		
		centerPoints.put(id, leafCenterPoints);
		Image resultImage = insertSplitPoints(segmentedUnchanged.show("IN", false), leafCenterPoints, false);
		return resultImage;
	}
	
	private Image insertSplitPoints(Image segmented, ArrayList<Feature> leafCenterPoints, boolean debugDraw) {
		Image segmentedUnchanged = segmented.copy();
		float[] dist = (float[]) segmented.io().bm().edmFloat().getPixels();
		segmented.show("seg", false);
		Image resultImg = segmented;
		Image skel = segmented.io().skeletonize().getImage().show("skel", false);
		int[] mapped = copyDistMapOnSkeleton(dist, skel);
		new Image(skel.getWidth(), skel.getHeight(), mapped).show("mapped", false);
		
		GraphAnalysisCvppp ga = new GraphAnalysisCvppp(segmented, mapped, leafCenterPoints, segmented.getWidth(), segmented.getHeight(),
			ImageOperation.BACKGROUND_COLORint);
		ga.doTracking(null);
		// ga.doTracking(segmented.getFileName().contains("_030") ? "graph30" : null);
		ArrayList<PositionAndColor> pp = ga.getSplitPoints();
		
		if (!pp.isEmpty()) {
			ImageCanvas ic = new ImageCanvas(segmentedUnchanged.copy());
			
			int[][] seg2d = segmented.getAs2A();
			
			for (PositionAndColor p : pp) {
			
			int nearX = -1, nearY = -1, r = p.intensityInt / 1000 + 20;
			double mindist = Double.MAX_VALUE;
			double mindistToCenter = Double.MAX_VALUE;
			int w = segmented.getWidth();
			int h = segmented.getHeight();
			
			for (int x = p.x - r; x < p.x + r; x++) {
				for (int y = p.y - r; y < p.y + r; y++) {
					if (x < 0 || y < 0 || x >= seg2d.length || y >= seg2d[0].length)
						continue;
					if (seg2d[x][y] == ImageOperation.BACKGROUND_COLORint) {
						double tempdist = Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
						double tempdistToCenter = Math.sqrt((x - w / 2) * (x - w / 2) + (y - h / 2) * (y - h / 2));
						if (tempdist < mindist || (tempdist <= mindist && tempdistToCenter < mindistToCenter)) {
						mindist = tempdist;
						mindistToCenter = tempdistToCenter;
						nearX = x;
						nearY = y;
						}
					}
				}
			}
			if (mindistToCenter < Double.MAX_VALUE) {
				Vector2i dir = new Vector2i(nearX - p.x, nearY - p.y);
				dir.x = -dir.x;
				dir.y = -dir.y;
				int ox = p.x + dir.x;
				int oy = p.y + dir.y;
				int nearX2 = ox;
				int nearY2 = oy;
				int findX = ox;
				int findY = oy;
				mindist = Double.MAX_VALUE;
				mindistToCenter = Double.MAX_VALUE;
				for (int x = ox - r; x < ox + r; x++) {
					for (int y = oy - r; y < oy + r; y++) {
						if (x < 0 || y < 0 || x >= w || y >= h)
						continue;
						if (nearX == x && nearY == y)
						continue;
						if (seg2d[x][y] == ImageOperation.BACKGROUND_COLORint) {
						double tempdist = Math.sqrt((x - findX) * (x - findX) + (y - findY) * (y - findY));
						if (tempdist < mindist) {
							mindist = tempdist;
							nearX2 = x;
							nearY2 = y;
						}
						}
					}
				}
				if (debugDraw)
					ic = ic.drawCircle(p.x, p.y, 5, Color.BLUE.getRGB(), 0, 1);
				ic = ic.drawLine(nearX, nearY, nearX2, nearY2, debugDraw ? Color.PINK.getRGB() : ImageOperation.BACKGROUND_COLORint, 0, 0);
			} else {
				// System.out.println("WARNING: No path!");
			}
			}
			
			resultImg = ic.getImage();
		} else {
			// System.out.println("WARNING: " + leafCenterPoints.size() + " center points but no minima along skeleton!");
		}
		return resultImg;
	}
	
	private int[] copyDistMapOnSkeleton(float[] dist, Image skel) {
		int[] skel1d = skel.getAs1A();
		float background = ImageOperation.BACKGROUND_COLORint;
		int[] res = new int[dist.length];
		for (int i = 0; i < dist.length; i++) {
			float pix;
			if (Math.abs(skel1d[i] - background) > 0.0001)
			pix = dist[i] * 1000;
			else
			pix = background;
			res[i] = (int) pix;
		}
		return res;
	}
	
	private synchronized ArrayList<Feature> detectCenterPoints(Image img, double maxTolerance) {
		FloatProcessor edmfp = img.io().bm().edmFloat();
		
		if (debug)
			new Image(edmfp.getBufferedImage()).show("distmap");
			
		MaximumFinder mf = new MaximumFinder();
		ByteProcessor bp = mf.findMaxima(edmfp, maxTolerance, ImageProcessor.NO_THRESHOLD, mf.LIST, false, true);
		
		// if (debug)
		// new Image(bp.getBufferedImage()).show("Maximas ");
		
		ResultsTable rt = mf.getRt();
		
		if (debug)
			rt.show("results");
			
		ArrayList<Feature> centerPoints = new ArrayList<Feature>();
		
		for (int i = 0; i < rt.getCounter(); i++) {
			int x = (int) rt.getValue("X", i);
			int y = (int) rt.getValue("Y", i);
			centerPoints.add(new Feature(x, y));
		}
		
		return centerPoints;
	}
	
	public HashMap<String, ArrayList<Feature>> getLeafCenterPoints() {
		return centerPoints;
	}
	
	public Image[] getResultImages() {
		return resultImages;
	}
}
