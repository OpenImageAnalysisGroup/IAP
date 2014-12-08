package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.image_analysis_tools.leafClustering.Feature;
import iap.blocks.image_analysis_tools.leafClustering.Leaf;
import iap.blocks.image_analysis_tools.leafClustering.LeafMatcher;
import iap.blocks.image_analysis_tools.leafClustering.LeafTip;
import iap.blocks.image_analysis_tools.leafClustering.Plant;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import org.Colors;
import org.StringManipulationTools;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.blocks.BlockResultObject;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResult;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author pape, klukas
 */
public class BlTrackLeafTips extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	double maxDistBetweenLeafTips;
	boolean debug = false;
	
	@Override
	public void prepare() {
		if (input().images().getAnyInfo() == null)
			return;
		
		// get parms
		maxDistBetweenLeafTips = getDouble("Maximal distance between leaf-tips", 100.0);
		boolean useNormalization = getBoolean("Do Normalization using BM", true);
		debug = getBoolean("Debug", false);
		
		CameraPosition cp = optionsAndResults.getCameraPosition();
		
		for (CameraType ct : CameraType.values()) {
			TreeMap<Long, ArrayList<BlockResultValue>> oldResults = optionsAndResults.searchPreviousResults("plant_" + ct, true, getWellIdx(),
					optionsAndResults.getConfigAndAngle(), true);
			BlockResultObject result1 = getResultSet().searchObjectResult(getBlockPosition(), 1, "leaftiplist_" + ct);
			ArrayList<BlockResultValue> result2 = oldResults == null || oldResults.isEmpty() ? null : oldResults.lastEntry().getValue();
			LinkedList<Feature> unassignedResults = null;
			Plant previousResults = null;
			
			if (result1 != null) {
				unassignedResults = (LinkedList<Feature>) result1.getObject();
			}
			
			if (result2 != null && !result2.isEmpty()) {
				previousResults = (Plant) result2.iterator().next().getObject();
			}
			
			// if no results break up
			if (result1 == null && result2 == null)
				continue;
			
			// get current timepoint
			SampleInterface id = input().images().getAnyInfo().getParentSample();
			Long time = id.getSampleFineTimeOrRowId();
			if (time == null)
				time = new Long(id.getTime());
			
			Normalisation n = new Normalisation(optionsAndResults.getREAL_MARKER_DISTANCE(), optionsAndResults.getCalculatedBlueMarkerDistance(),
					input().masks(), ct, optionsAndResults.getLeftShiftX(ct), optionsAndResults.getTopShiftY(ct), optionsAndResults.getCenterX(ct),
					optionsAndResults.getCenterY(ct));
			
			if (!n.isRealWorldCoordinateValid() && useNormalization)
				continue;
			
			if (!n.isRealWorldCoordinateValid())
				n = null;
			
			// check if new config equals current config, if not reset plant object
			if (previousResults != null)
				if (!optionsAndResults.getSystemOptionStorageGroup(this).equals(previousResults.getSettingFolder()))
					previousResults = null;
			
			if (previousResults != null) {
				if (unassignedResults != null) {
					// match new leaf tips
					matchNewResults(previousResults, unassignedResults, cp, ct, time, n, input().images().getImageInfo(ct));
					getResultSet().removeResultObject(result1);
				}
			} else {
				if (unassignedResults != null) {
					// first run, create new plant
					createNewPlant(unassignedResults, cp, ct, time, n, input().images().getImageInfo(ct));
					getResultSet().removeResultObject(result1);
				}
			}
			
		}
	}
	
	private void createNewPlant(LinkedList<Feature> unassignedResults, CameraPosition cameraPosition,
			CameraType cameraType, long timepoint, Normalisation norm, ImageData imageRef) {
		LeafMatcher ltm = new LeafMatcher(unassignedResults, timepoint, norm);
		ltm.setMaxDistanceBetweenLeafTips(maxDistBetweenLeafTips);
		ltm.matchLeafTips();
		Plant plant = ltm.getMatchedPlant();
		plant.setSettingFolder(optionsAndResults.getSystemOptionStorageGroup(this));
		getResultSet().setObjectResult(getBlockPosition(), "plant_" + cameraType, plant);
		markAndSaveLeafFeatures(cameraPosition, cameraType, norm, plant, timepoint, imageRef);
	}
	
	private void matchNewResults(Plant previousResults,
			LinkedList<Feature> unassignedResults, final CameraPosition cameraPosition, final CameraType cameraType, long timepoint,
			final Normalisation norm, ImageData imageRef) {
		LeafMatcher ltm = new LeafMatcher(previousResults, unassignedResults, timepoint, norm);
		ltm.setMaxDistanceBetweenLeafTips(maxDistBetweenLeafTips);
		ltm.matchLeafTips();
		final Plant plant = ltm.getMatchedPlant();
		// TODO calc dist between leaftips , dist / (time_n +1 - time_n) * 24*60*60*1000; Leaflength += dist;
		
		markAndSaveLeafFeatures(cameraPosition, cameraType, norm, plant, timepoint, imageRef);
		
		getResultSet().setObjectResult(getBlockPosition(), "plant_" + cameraType, plant);
	}
	
	private void markAndSaveLeafFeatures(CameraPosition cameraPosition, final CameraType cameraType, final Normalisation norm,
			final Plant plant, long timepoint, ImageData imageRef) {
		// save to resultSet
		LinkedList<Leaf> leafList = plant.getLeafList();
		final ArrayList<Color> colors = Colors.get(leafList.size() + 1, 1);
		
		getResultSet().setNumericResult(getBlockPosition(),
				new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip.tracked.count"), leafList.size(), "tracked leaves", this, imageRef);
		
		if (isBestAngle(cameraType))
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip.tracked.count.best_angle"), leafList.size(), "tracked leaves",
					this,
					imageRef);
		
		// calculate leaf parameter
		for (Leaf l : plant.getLeafList()) {
			LeafTip ltLast = l.getLast();
			LeafTip ltFirst = l.getFirst();
			
			final int num = l.leafID;
			
			if (ltLast == null || cameraPosition == null || cameraType == null)
				continue;
			
			if (ltLast != ltFirst) {
				Vector2D trans1 = new Vector2D(ltLast.getImageX() - ltFirst.getImageX(), ltLast.getImageY() - ltFirst.getImageY());
				Vector2D trans2 = new Vector2D(0.0, 1.0);
				double angle = calcAngle(trans1, trans2);
				double a = (ltFirst.getRealWorldX() - ltLast.getRealWorldX());
				double b = (ltFirst.getRealWorldY() - ltLast.getRealWorldY());
				double span_norm = Math.sqrt(a * a + b * b);
				
				double c = (ltFirst.getImageX() - ltLast.getImageX());
				double d = (ltFirst.getImageY() - ltLast.getImageY());
				double span = Math.sqrt(c * c + d * d);
				
				getResultSet().setNumericResult(
						0,
						new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip." + StringManipulationTools.formatNumberAddZeroInFront(num, 2)
								+ ".span.orientation"),
						angle, "degree", this, imageRef);
				
				getResultSet().setNumericResult(
						0,
						new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip." + StringManipulationTools.formatNumberAddZeroInFront(num, 2)
								+ ".span.norm"),
						span_norm, "mm", this, imageRef);
				
				getResultSet().setNumericResult(
						0,
						new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip." + StringManipulationTools.formatNumberAddZeroInFront(num, 2)
								+ ".span"),
						span, "px", this, imageRef);
			}
			
			final int xPos_norm = ltLast.getRealWorldX();
			final int yPos_norm = ltLast.getRealWorldY();
			Double angle = null;
			if (ltLast.getFeature("angle") != null)
				angle = (Double) ltLast.getFeature("angle");
			
			getResultSet().setNumericResult(
					0,
					new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip." + StringManipulationTools.formatNumberAddZeroInFront(num, 2)
							+ ".position.x"),
					xPos_norm, "px", this, imageRef);
			getResultSet().setNumericResult(
					0,
					new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip." + StringManipulationTools.formatNumberAddZeroInFront(num, 2)
							+ ".position.y"),
					yPos_norm, "px", this, imageRef);
			
			if (angle != null) {
				getResultSet()
						.setNumericResult(
								0,
								new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip."
										+ StringManipulationTools.formatNumberAddZeroInFront(num, 2) + ".angle"),
								angle, "degree", this, imageRef);
			}
			
			boolean saveDistToCenter = true;
			
			if (saveDistToCenter) {
				BlockResult cogXBR = getResultSet().searchNumericResult(getBlockPosition(), 1,
						new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "cog.x"));
				BlockResult cogYBR = getResultSet().searchNumericResult(getBlockPosition(), 1,
						new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "cog.y"));
				
				if (cogXBR != null && cogYBR != null) {
					int cogX = (int) cogXBR.getValue();
					int cogY = (int) cogYBR.getValue();
					int lx = ltLast.getImageX();
					int ly = ltLast.getImageY();
					
					double distToCenter = Math.sqrt((cogX - lx) * (cogX - lx) + (cogY - ly) * (cogY - ly));
					
					getResultSet().setNumericResult(
							0,
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip."
									+ StringManipulationTools.formatNumberAddZeroInFront(num, 2) + ".dist_to_cog"),
							distToCenter, "px", this, imageRef);
				}
			}
		}
		
		// calculate leaf tip parameter
		for (Leaf l : plant.getLeafList()) {
			LeafTip last = l.getLast();
			for (LeafTip lt : l) {
				
				if (lt == null || cameraPosition == null || cameraType == null)
					continue;
				
				final boolean db = debug;
				final boolean isLast = last == lt && last.getTime() == timepoint;
				final int num = l.leafID;
				final int xPos;
				final int yPos;
				final int xPos_norm = lt.getRealWorldX();
				final int yPos_norm = lt.getRealWorldY();
				final Double angle = (Double) lt.getFeature("angle");
				final Vector2D direction = (Vector2D) lt.getFeature("direction");
				
				if (norm == null) {
					xPos = xPos_norm;
					yPos = yPos_norm;
				} else {
					xPos = lt.getImageX();
					yPos = lt.getImageY();
				}
				
				getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
					
					@Override
					public Image postProcessMask(Image mask) {
						ImageCanvas c = mask.io().canvas();
						if (!isLast) {
							c = c.drawRectanglePoints(xPos - 4, yPos - 4, 1, 1, colors.get(num), 1);
							if (db)
								if (angle != null && false)
									c = c.text(xPos, yPos + 10, "rx: " + xPos_norm + " ry: " + yPos_norm +
											" a: " + angle.intValue(), Color.BLACK);
								else
									if (false)
										c = c.text(xPos, yPos + 10, "rx: " + xPos_norm + " ry: " + yPos_norm +
												" ", Color.BLACK);
						} else {
							if (direction != null) {
								Vector2D vv = direction.subtract(new Vector2D(xPos, yPos));
								Vector2D d = vv.getNorm() > 0.01 ?
										vv.normalize()
												.scalarMultiply((1 + (Math.sqrt(2) - 1) * (1 - Math.abs(Math.cos(2 * angle / 180. * Math.PI)))) * 16)
										: vv;
								c.drawLine(xPos, yPos, (int) d.getX() + xPos, (int) d.getY() + yPos, colors.get(num).getRGB(), 0.2, 1);
							}
							c = c.drawRectangle(xPos - 18, yPos - 18, 36, 36, colors.get(num), 2);
							if (db)
								if (angle != null)
									c = c.text(xPos, yPos + 10, "rx: " + xPos_norm + " ry: " + yPos_norm +
											" a: " + angle.intValue(), Color.BLACK);
								else
									c = c.text(xPos, yPos + 10, "rx: " + xPos_norm + " ry: " + yPos_norm +
											" ", Color.BLACK);
						}
						return c.getImage();
					}
					
					@Override
					public Image postProcessImage(Image image) {
						return image;
					}
					
					@Override
					public CameraType getConfig() {
						return cameraType;
					}
				});
			}
		}
	}
	
	private double calcAngle(Vector2D v1, Vector2D v2) {
		double val = (v1.getX() * v2.getX() + v1.getY() * v2.getY())
				/ ((Math.sqrt(v1.getX() * v1.getX() + v1.getY() * v1.getY())) * (Math.sqrt(v2.getX() * v2.getX() + v2.getY() * v2.getY())));
		return Math.acos(val) * 180d / Math.PI;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
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
		return "Track Leaf-Tips";
	}
	
	@Override
	public String getDescription() {
		return "Tracks Leaf Tips (prior processing of block 'Detect Leaf-Tips' is necessary)";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("leaftip.tracked.count", "Number of detected leafs, considered during leaf tracking."),
				new CalculatedProperty("leaftip.tracked.count.best_angle", "Number of detected leafs for best side view, considered during leaf tracking."),
				new CalculatedProperty("leaftip.*.span.orientation",
						"Orientation of the movement vector from first appearance of a leaf to current leaf tip position."),
				new CalculatedProperty("leaftip.*.span.norm",
						"Normalised distance from from first appearance of a leaf to the current detected leaf tip position."),
				new CalculatedProperty("leaftip.*.span", "Distance from from first appearance of a leaf to the current detected leaf tip position."),
				new CalculatedProperty("leaftip.*.x", "X position of the leaf tip center point."),
				new CalculatedProperty("leaftip.*.y", "Y position of the leaf tip center point."),
				new CalculatedProperty("leaftip.*.angle", "Leaf tip orientation."),
				new CalculatedProperty("leaftip.*.dist_to_cog", "Distance from the leaf tip position to the center of gravity of the whole plant."),
		};
	}
}
