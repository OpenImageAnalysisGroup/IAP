package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.image_analysis_tools.leafClustering.Feature;
import iap.blocks.image_analysis_tools.leafClustering.FeatureObject.FeatureObjectType;
import iap.blocks.image_analysis_tools.methods.RegionLabeling;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import org.Vector2i;

import de.ipk.ag_ba.image.operation.ImageMoments;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape
 *         Block for object detection, includes possibility to save results for tracking.
 */
public class BlFlowerDetectionAndFeatureExtraction extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	int background = ImageOperation.BACKGROUND_COLORint;
	
	@Override
	protected Image processVISmask() {
		
		// options
		boolean markResults = getBoolean("Mark Results", true);
		boolean saveResults = getBoolean("Save Results", true);
		boolean trackResults = getBoolean("Track Results", true);
		int minimumSizeOfRegion = getInt("Minimum Region Size", 15);
		
		Image img = input().masks().vis();
		
		if (img == null)
			return null;
		
		RegionLabeling rl = new RegionLabeling(img, false, background, -1);
		rl.detectClusters();
		
		LinkedList<Feature> featureList = getFeaturesFromClusters(img, rl, minimumSizeOfRegion);
		Image marked = saveAndMarkResults(img, featureList, markResults, saveResults, trackResults);
		
		return marked;
	}
	
	private LinkedList<Feature> getFeaturesFromClusters(Image img, RegionLabeling rl, int minimumSizeOfRegion) {
		
		LinkedList<ArrayList<PositionAndColor>> regions = rl.getRegionList();
		LinkedList<Feature> flist = new LinkedList<Feature>();
		boolean[] deleted = new boolean[rl.getClusterCount()];
		int[] areas = rl.getClusterSize();
		
		// remove small flowers
		for (int i = 0; i < areas.length; i++) {
			if (areas[i] < minimumSizeOfRegion) {
				deleted[i] = true;
			}
		}
		
		Vector2i[] centerPoints = rl.getClusterCenterPoints();
		Vector2i[] clusterDimensions = rl.getClusterDimension();
		TopBottomLeftRight[] boundingBox = rl.getBoundingBox();
		
		Image clusteredImage = rl.getClusterImage().show("rl image", true);
		CameraType ct = img.getCameraType();
		
		// get skeleton-image and workimage to connect lose leaves and for optimization
		Image skel = getResultSet().getImage("skeleton_" + ct.toString());
		// Image skel_workimge = getResultSet().getImage("skeleton_workimage_" + ct.toString());
		
		if (skel != null) {
			clusteredImage.copy().io().or(skel).getImage()
					.show("skel images on mask" + ct.toString(), debugValues);
			// removes flowers from skleton
			skel = skel.io().removePixel(clusteredImage, background).getImage();
		}
		
		int[] skelArray = skel.getAs1A();
		ImageCanvas icclu = new ImageCanvas(clusteredImage);
		
		int idx = 0;
		for (ArrayList<PositionAndColor> cluster : regions) {
			if (!deleted[idx]) {
				Vector2i dim = clusterDimensions[idx];
				TopBottomLeftRight bounds = boundingBox[idx];
				Vector2i centerPoint = centerPoints[idx];
				double minDistanceToOtherRegion = getMinDist(centerPoint, centerPoints);
				Image flowerImage = getClusterImage(cluster, dim, bounds);
				// flowerImage.show("flower");
				ImageMoments im = new ImageMoments(flowerImage);
				Point coG = im.getCenterOfGravity();
				Point coGWeighted = im.getCenterOfGravityWeigthed(ColorMode.BLUE);
				Point directionNextSekeltonPoint = getConnectedDirectionFromSkeleton(skelArray, clusteredImage, cluster, img.getWidth());
				
				icclu.drawCircle(directionNextSekeltonPoint.x, directionNextSekeltonPoint.y, 10, Color.BLUE.getRGB(), 0.0, 2);
				
				if (directionNextSekeltonPoint.x == -1)
					directionNextSekeltonPoint = coGWeighted;
				double ec = im.getEccentricity();
				// direction
				Vector2i direction = new Vector2i(directionNextSekeltonPoint.x, directionNextSekeltonPoint.y);
				
				if (true) {
					ImageCanvas ic = new ImageCanvas(im.drawMoments().copy());
					int centerX = centerPoint.x - bounds.getLeftX();
					int centerY = centerPoint.y - bounds.getTopY();
					ic.drawCircle(centerX, centerY, 5, Color.BLUE.getRGB(), 0, 1);
					ic.drawCircle(coG.x, coG.y, 5, Color.YELLOW.getRGB(), 0, 1);
					ic.drawCircle(coGWeighted.x, coGWeighted.y, 5, Color.RED.getRGB(), 0, 1);
					ic.drawCircle(directionNextSekeltonPoint.x, directionNextSekeltonPoint.y, 5, Color.BLACK.getRGB(), 0, 1);
					ic.drawLine(new Point(centerX, centerY), new Point((direction.x),
							(direction.y)), Color.ORANGE.getRGB(), 0.0, 1);
					ic.getImage().show("marked flower", false);
				}
				
				direction.x = direction.x;
				direction.y = direction.y;
				
				Vector2i pos = centerPoints[idx];
				Feature tempFeature = new Feature(new Integer(pos.x), new Integer(pos.y));
				tempFeature.addFeature("size", areas[idx], FeatureObjectType.NUMERIC);
				tempFeature.addFeature("eccentricity", ec, FeatureObjectType.NUMERIC);
				tempFeature.addFeature("minDistToOtherRegion", minDistanceToOtherRegion, FeatureObjectType.NUMERIC);
				tempFeature.addFeature("direction_1", direction, FeatureObjectType.NUMERIC);
				flist.add(tempFeature);
			}
			idx++;
		}
		icclu.getImage().show("petioles");
		return flist;
	}
	
	/**
	 * Search all skeleton points connected to a flower.
	 * 
	 * @param skelimg
	 *           - skeleton image
	 * @param clusteredImage
	 * @param cluster
	 * @return
	 */
	private Point getConnectedDirectionFromSkeleton(int[] skelimg, Image clusteredImage, ArrayList<PositionAndColor> cluster, int w) {
		ArrayList<PositionAndColor> connectedSkelPoints = new ArrayList<PositionAndColor>();
		
		int idx = 0;
		for (PositionAndColor pix : cluster) {
			idx = pix.x + w * pix.y;
			
			// if skeleton on Cluster
			if (skelimg[idx] != background)
				continue;
			
			int f = idx - 1; // left
			if (idx % w > 0 && skelimg[f] != background) {
				connectedSkelPoints.add(new PositionAndColor(idx % w, idx / w, skelimg[f]));
			}
			f = idx - w; // above
			if (idx > w && skelimg[f] != background) {
				connectedSkelPoints.add(new PositionAndColor(idx % w, idx / w, skelimg[f]));
			}
			f = idx + 1; // right
			if ((idx) % w < w - 1 && skelimg[f] != background) {
				connectedSkelPoints.add(new PositionAndColor(idx % w, idx / w, skelimg[f]));
			}
			f = idx + w; // below
			if (idx < skelimg.length - w && skelimg[f] != background) {
				connectedSkelPoints.add(new PositionAndColor(idx % w, idx / w, skelimg[f]));
			}
			
			f = idx - 1 - w; // left/above
			if (idx % w > 0 && skelimg[f] != background) {
				connectedSkelPoints.add(new PositionAndColor(idx % w, idx / w, skelimg[f]));
			}
			f = idx - w + 1; // right/above
			if (idx > w && skelimg[f] != background) {
				connectedSkelPoints.add(new PositionAndColor(idx % w, idx / w, skelimg[f]));
			}
			f = idx - 1 + w; // left/below
			if ((idx) % w < w - 1 && skelimg[f] != background) {
				connectedSkelPoints.add(new PositionAndColor(idx % w, idx / w, skelimg[f]));
			}
			f = idx + w + 1; // right/below
			if (idx < skelimg.length - w && skelimg[f] != background) {
				connectedSkelPoints.add(new PositionAndColor(idx % w, idx / w, skelimg[f]));
			}
		}
		
		Point center = new Point();
		int i = 0;
		for (PositionAndColor pos : connectedSkelPoints) {
			center.x += pos.x;
			center.y += pos.y;
			i++;
		}
		if (i > 0) {
			center.x = center.x / i;
			center.y = center.y / i;
		} else {
			center.x = -1;
			center.y = -1;
			System.out.println("No skeleton point found.");
		}
		
		return center;
	}
	
	private double getMinDist(Vector2i centerPoint, Vector2i[] centerPoints) {
		double minDist = Double.MAX_VALUE;
		for (Vector2i cp : centerPoints) {
			if (centerPoint.distance(cp) < minDist)
				minDist = centerPoint.distance(cp);
		}
		return minDist;
	}
	
	private Image getClusterImage(ArrayList<PositionAndColor> cluster, Vector2i dim, TopBottomLeftRight bounds) {
		int[][] img = new int[dim.x][dim.y];
		ImageOperation.fillArray(img, ImageOperation.BACKGROUND_COLORint);
		
		for (PositionAndColor pix : cluster) {
			int posX = pix.x - bounds.getLeftX();
			int posY = pix.y - bounds.getTopY();
			if (posX < dim.x && posY < dim.y)
				img[posX][posY] = pix.intensityInt;
		}
		return new Image(img);
	}
	
	private Image saveAndMarkResults(Image img, LinkedList<Feature> featureList, boolean markResults, boolean saveResults, boolean trackResults) {
		
		boolean saveResultObject = trackResults;
		
		if (markResults) {
			ImageCanvas ic = new ImageCanvas(img);
			for (Feature p : featureList) {
				Vector2i direction = (Vector2i) p.getFeature("direction_1");
				ic.drawRectangle((int) p.getPosition().getX() - 10, (int) p.getPosition().getY() - 10, 21, 21, Color.RED, 3);
				ic.drawLine(new Point((int) (p.getPosition().getX()), (int) (p.getPosition().getY())), new Point((direction.x),
						(direction.y)),
						Color.GREEN.getRGB(), 0.0, 2);
			}
			img = ic.getImage();
		}
		
		if (saveResults) {
			String pos = optionsAndResults.getCameraPosition() == CameraPosition.SIDE ? "RESULT_side." : "RESULT_top.";
			// save leaf count
			getResultSet().setNumericResult(getBlockPosition(),
					pos + img.getCameraType() + ".flower.count", featureList.size(), "flower|CENTERPOINTS", this);
			
			// save x and y position
			int num = 1;
			for (Feature p : featureList) {
				getResultSet().setNumericResult(getBlockPosition(),
						pos + img.getCameraType() + ".flower.x" + "." + num, (int) p.getPosition().getX(), "flower|CENTERPOINTS", this);
				
				getResultSet().setNumericResult(getBlockPosition(),
						pos + img.getCameraType() + ".flower.y" + "." + num, (int) p.getPosition().getY(), "flower|CENTERPOINTS", this);
				num++;
			}
		}
		
		if (saveResultObject) {
			String name = this.getClass().getSimpleName();
			name = name.toLowerCase();
			getResultSet().setObjectResult(getBlockPosition(), "leaftiplist" + "_" + img.getCameraType(), featureList);
		}
		
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
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Detect Flowers (tested for tobacco)";
	}
	
	@Override
	public String getDescription() {
		return "Detects Flowers and extract Features. Before flowers should be segmented by a segmentation block.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("flower.count", "!todo"),
				new CalculatedProperty("flower.x", "!todo"),
				new CalculatedProperty("flower.y", "!todo")
		};
	}
}
