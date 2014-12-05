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

import org.StringManipulationTools;
import org.Vector2i;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.ipk.ag_ba.image.operation.ImageMoments;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operation.TopBottomLeftRight;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

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
		int minimumSizeOfRegion = getInt("Minimum Region Size", 30);
		
		Image img = input().masks().vis();
		
		if (img == null)
			return null;
		
		RegionLabeling rl = new RegionLabeling(img, false, background, -1);
		rl.detectClusters();
		
		LinkedList<Feature> featureList = getFeaturesFromClusters(img, rl, minimumSizeOfRegion);
		Image marked = null;
		if (featureList != null)
			marked = saveAndMarkResults(img, featureList, markResults, saveResults, trackResults, input().images().getVisInfo());
		
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
		
		Image clusteredImage = rl.getClusterImage().show("rl image", debugValues);
		CameraType ct = img.getCameraType();
		
		// get skeleton-image
		Image skel = getResultSet().getImage("skeleton_" + ct.toString()).getImage();
		
		if (skel != null) {
			clusteredImage.copy().io().or(skel).getImage()
					.show("skel images on mask" + ct.toString(), debugValues);
			// removes flowers from skleton
			skel = skel.io().removeOutliers().removePixel(clusteredImage, background).getImage().show("removed flowers from skel + outlier removal", debugValues);
			
			int[] skelArray = skel.getAs1A();
			ImageCanvas icSkelPoints = new ImageCanvas(clusteredImage);
			
			int idx = 0;
			for (ArrayList<PositionAndColor> cluster : regions) {
				if (!deleted[idx]) {
					Vector2i dim = clusterDimensions[idx];
					TopBottomLeftRight bounds = boundingBox[idx];
					Vector2i centerPoint = centerPoints[idx];
					double minDistanceToOtherRegion = getMinDist(centerPoint, centerPoints);
					Image flowerImage = getClusterImage(cluster, dim, bounds);
					// flowerImage.show("flower");
					
					if (flowerImage.getWidth() > 1 && flowerImage.getHeight() > 1) {
						ImageMoments im = new ImageMoments(flowerImage);
						Point coG = im.getCenterOfGravity();
						Point coGWeighted = im.getCenterOfGravityWeigthed(ColorMode.BLUE);
						Point directionNextSekeltonPoint = getConnectedDirectionFromSkeleton(skelArray, cluster, img.getWidth());
						
						icSkelPoints.drawCircle(directionNextSekeltonPoint.x, directionNextSekeltonPoint.y, 3, Color.BLUE.getRGB(), 0.0, 2);
						
						if (directionNextSekeltonPoint.x == -1)
							directionNextSekeltonPoint = new Point(coGWeighted.x + bounds.getLeftX(), coGWeighted.x + bounds.getTopY());
						
						double ec = im.getEccentricity();
						
						// direction
						Vector2D direction = new Vector2D(directionNextSekeltonPoint.x, directionNextSekeltonPoint.y);
						
						int centerX = centerPoint.x - bounds.getLeftX();
						int centerY = centerPoint.y - bounds.getTopY();
						
						if (true) {
							ImageCanvas ic = new ImageCanvas(im.drawMoments().copy());
							ic.drawCircle(centerX, centerY, 5, Color.BLUE.getRGB(), 0, 1);
							ic.drawCircle(coG.x, coG.y, 5, Color.YELLOW.getRGB(), 0, 1);
							ic.drawCircle(coGWeighted.x, coGWeighted.y, 5, Color.RED.getRGB(), 0, 1);
							ic.drawCircle(directionNextSekeltonPoint.x, directionNextSekeltonPoint.y, 5, Color.BLACK.getRGB(), 0, 1);
							ic.drawLine(new Point(centerX, centerY), new Point((int) (direction.getX()),
									(int) (direction.getY())), Color.ORANGE.getRGB(), 0.0, 1);
							ic.getImage().show("marked flower", debugValues);
						}
						
						Vector2i pos = centerPoints[idx];
						Feature tempFeature = new Feature(new Integer(pos.x), new Integer(pos.y));
						tempFeature.addFeature("size", areas[idx], FeatureObjectType.NUMERIC);
						tempFeature.addFeature("eccentricity", ec, FeatureObjectType.NUMERIC);
						tempFeature.addFeature("minDistToOtherRegion", minDistanceToOtherRegion, FeatureObjectType.NUMERIC);
						tempFeature.addFeature("direction_1", direction, FeatureObjectType.VECTOR);
						// tempFeature.addFeature("angle_1",
						// angle(new Vector2D(direction.getX() - centerX, direction.getY() - centerY), new Vector2D(centerX - centerX, (centerY + 1) - centerY))
						// * 180 / Math.PI,
						// FeatureObjectType.NUMERIC);
						flist.add(tempFeature);
					}
				}
				idx++;
			}
			icSkelPoints.getImage().show("icclu", debugValues);
		} else
			if (debugValues)
				System.out.println("No skeleton available for flower detection!");
		
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
	private Point getConnectedDirectionFromSkeleton(int[] skelimg, ArrayList<PositionAndColor> cluster, int w) {
		ArrayList<PositionAndColor> connectedSkelPoints = new ArrayList<PositionAndColor>();
		
		int idx = 0;
		for (PositionAndColor pix : cluster) {
			idx = pix.x + w * pix.y;
			
			// if skeleton on Cluster
			if (skelimg[idx] != background)
				continue;
			
			// search neighbors
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
			System.out.println(this.getName() + ": No skeleton point found.");
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
	
	private Image saveAndMarkResults(Image img, LinkedList<Feature> featureList, boolean markResults, boolean saveResults, boolean trackResults,
			ImageData imageRef) {
		
		boolean saveResultObject = trackResults;
		
		if (markResults) {
			ImageCanvas ic = new ImageCanvas(img);
			for (Feature p : featureList) {
				Vector2D direction = (Vector2D) p.getFeature("direction_1");
				Vector2D direction_norm = new Vector2D(direction.getX() - p.getPosition().getX(), direction.getY() - p.getPosition().getY());
				Vector2D up = new Vector2D(0, 1);
				double angle = 180d - (angle(direction_norm, up) * 180 / Math.PI);
				if (direction != null && direction.getX() != 0 && direction.getY() != 0) {
					ic.drawRectangle((int) p.getPosition().getX() - 10, (int) p.getPosition().getY() - 10, 21, 21, Color.RED, 1);
					ic.drawLine(new Point((int) (p.getPosition().getX()), (int) (p.getPosition().getY())), new Point((int) (direction.getX()),
							(int) (direction.getY())), Color.GREEN.getRGB(), 0.0, 1);
					ic.text((int) direction.getX(), (int) direction.getY() + 30, "A: " + StringManipulationTools.formatNumber(angle, 0), Color.BLACK, 15);
				}
			}
			img = ic.getImage();
		}
		
		if (saveResults) {
			CameraPosition pos = optionsAndResults.getCameraPosition();
			// save leaf count
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower.count"), featureList.size(), "flower", this, imageRef);
			
			// save x and y position
			double avg_angle = 0d;
			double avg_y_pos = 0d;
			int num = 1;
			for (Feature p : featureList) {
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + num + ".position.x"),
						(int) p.getPosition().getX(), "flower", this, imageRef);
				
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + num + ".position.y"),
						(int) p.getPosition().getY(), "flower", this, imageRef);
				
				// save direction_1
				Vector2D direction = (Vector2D) p.getFeature("direction_1");
				double angle_norm = direction.getNorm();
				Vector2D direction_norm = new Vector2D(direction.getX() - p.getPosition().getX(), direction.getY() - p.getPosition().getY());
				Vector2D up = new Vector2D(0, 1);
				double angle = 180d - (angle(direction_norm, up) * 180 / Math.PI);
				
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + num + ".length"),
						angle_norm, "flower", this, imageRef);
				
				// calc min distance
				double min_dist = getMinDist(p.getPosition().getX(), p.getPosition().getY(), featureList);
				
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + num + ".mindist"),
						min_dist, "flower", this, imageRef);
				
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + num + ".angle"),
						angle, "flower", this, imageRef);
				
				avg_angle += angle;
				avg_y_pos += p.getPosition().getY();
				num++;
			}
			
			// save averages
			double am = (avg_angle / (num - 1));
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + "angle.mean"),
					am, "flower", this, imageRef);
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(pos, img.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "flower." + "position.y.mean"),
					avg_y_pos / (num - 1), "flower", this, imageRef);
		}
		
		if (saveResultObject) {
			String name = this.getClass().getSimpleName();
			name = name.toLowerCase();
			getResultSet().setObjectResult(getBlockPosition(), "leaftiplist" + "_" + img.getCameraType(), featureList);
		}
		
		return img;
	}
	
	private double angle(Vector2D a, Vector2D b) {
		return Math.acos((a.getX() * b.getX() + a.getY() * b.getY())
				/ (Math.sqrt((a.getX() * a.getX() + a.getY() * a.getY())) * Math.sqrt((b.getX() * b.getX() + b.getY() * b.getY()))));
	}
	
	private double getMinDist(double x, double y, LinkedList<Feature> featureList) {
		double min_dist = Double.MAX_VALUE;
		for (Feature f : featureList) {
			double tempx = f.getPosition().getX();
			double tempy = f.getPosition().getY();
			if (tempx == x && tempy == y)
				continue;
			double dist = Math.sqrt(((tempx - x) * (tempx - x)) + ((tempy - y) * (tempy - y)));
			if (dist < min_dist)
				min_dist = dist;
		}
		return min_dist;
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
		return "Detect Flowers";
	}
	
	@Override
	public String getDescription() {
		return "Detects Flowers and extract Features. Before flowers should be segmented by a segmentation block. Tested for tobacco flowers.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("flower.count", "Number of detected flowers"),
				new CalculatedProperty("flower.*.position.x", "Position (X-axis) of detected flower."),
				new CalculatedProperty("flower.*.position.y", "Position (Y-axis) of detected flower.")
		};
	}
}
