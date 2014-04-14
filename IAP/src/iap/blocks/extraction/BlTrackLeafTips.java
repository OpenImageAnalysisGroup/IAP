package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.imageAnalysisTools.leafClustering.BorderFeature;
import iap.blocks.imageAnalysisTools.leafClustering.Leaf;
import iap.blocks.imageAnalysisTools.leafClustering.LeafTip;
import iap.blocks.imageAnalysisTools.leafClustering.LeafTipMatcher;
import iap.blocks.imageAnalysisTools.leafClustering.Plant;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import org.Colors;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.blocks.BlockResultObject;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;

/**
 * @author pape, klukas
 */
public class BlTrackLeafTips extends AbstractSnapshotAnalysisBlock {
	
	@Override
	public void prepare() {
		CameraPosition cp = optionsAndResults.getCameraPosition();
		for (CameraType ct : CameraType.values()) {
			TreeMap<Long, ArrayList<BlockResultValue>> oldResults = optionsAndResults.searchPreviousResults("plant_" + ct, true, getWellIdx(),
					optionsAndResults.getConfigAndAngle(), true);
			BlockResultObject result1 = getResultSet().searchObjectResult(getBlockPosition(), 1, "leaftiplist_" + ct);
			ArrayList<BlockResultValue> result2 = oldResults == null || oldResults.isEmpty() ? null : oldResults.lastEntry().getValue();
			LinkedList<BorderFeature> unassignedResults = null;
			Plant previousResults = null;
			if (result1 != null) {
				unassignedResults = (LinkedList<BorderFeature>) result1.getObject();
			}
			if (result2 != null && !result2.isEmpty()) {
				previousResults = (Plant) result2.iterator().next().getObject();
			}
			
			if (result1 == null && result2 == null)
				continue;
			else {
				// get current timepoint
				SampleInterface id = input().images().getAnyInfo().getParentSample();
				Long time = id.getSampleFineTimeOrRowId();
				if (time == null)
					time = new Long(id.getTime());
				
				Normalisation n = new Normalisation(optionsAndResults.getREAL_MARKER_DISTANCE(), optionsAndResults.getCalculatedBlueMarkerDistance(),
						input().masks(), ct, optionsAndResults.getLeftShiftX(ct), optionsAndResults.getTopShiftY(ct), optionsAndResults.getCenterX(ct),
						optionsAndResults.getCenterY(ct));
				
				if (!n.isRealWorldCoordinateValid())
					continue;
				
				// check if new config equals current config
				if (previousResults != null)
					if (!optionsAndResults.getSystemOptionStorageGroup(this).equals(previousResults.getSettingFolder()))
						previousResults = null;
				
				if (previousResults != null) {
					if (unassignedResults != null) {
						// match
						matchNewResults(previousResults, unassignedResults, cp, ct, time, n);
						getResultSet().removeResultObject(result1);
					}
				} else {
					if (unassignedResults != null) {
						// first run, create new plant
						createNewPlant(unassignedResults, cp, ct, time, n);
						getResultSet().removeResultObject(result1);
					}
				}
			}
		}
	}
	
	private void createNewPlant(LinkedList<BorderFeature> unassignedResults, CameraPosition cameraPosition,
			CameraType cameraType, long timepoint, Normalisation norm) {
		LeafTipMatcher ltm = new LeafTipMatcher(unassignedResults, timepoint, norm);
		ltm.setMaxDistanceBetweenLeafTips(100.0);
		ltm.matchLeafTips();
		Plant plant = ltm.getMatchedPlant();
		plant.setSettingFolder(optionsAndResults.getSystemOptionStorageGroup(this));
		getResultSet().setObjectResult(getBlockPosition(), "plant_" + cameraType, plant);
		markAndSaveLeafFeatures(cameraPosition, cameraType, norm, plant, timepoint);
	}
	
	private void matchNewResults(Plant previousResults,
			LinkedList<BorderFeature> unassignedResults, final CameraPosition cameraPosition, final CameraType cameraType, long timepoint,
			final Normalisation norm) {
		LeafTipMatcher ltm = new LeafTipMatcher(previousResults, unassignedResults, timepoint, norm);
		ltm.setMaxDistanceBetweenLeafTips(300.0);
		ltm.matchLeafTips();
		final Plant plant = ltm.getMatchedPlant();
		// TODO calc dist between leaftips , dist / (time_n +1 - time_n) * 24*60*60*1000; Leaflength += dist;
		
		markAndSaveLeafFeatures(cameraPosition, cameraType, norm, plant, timepoint);
		
		getResultSet().setObjectResult(getBlockPosition(), "plant_" + cameraType, plant);
	}
	
	private void markAndSaveLeafFeatures(CameraPosition cameraPosition, final CameraType cameraType, final Normalisation norm, final Plant plant, long timepoint) {
		// save to resultSet
		LinkedList<Leaf> ll = plant.getLeafList();
		final ArrayList<Color> col = Colors.get(ll.size() + 1, 1);
		
		getResultSet().setNumericResult(getBlockPosition(),
				"RESULT_" + cameraPosition + "." + cameraType + ".leaf.count", ll.size(), "tracked leafs|SUSAN");
		
		if (isBestAngle())
			getResultSet().setNumericResult(getBlockPosition(),
					"RESULT_" + cameraPosition + "." + cameraType + ".leaf.count.best_angle", ll.size(), "tracked leafs|SUSAN");
		
		for (Leaf l : plant.getLeafList()) {
			LeafTip lt = l.getLast();
			LeafTip ltFirst = l.getFirst();
			
			final int num = l.leafID;
			
			if (lt == null || cameraPosition == null || cameraType == null)
				continue;
			
			if (lt != ltFirst) {
				Vector2D trans1 = new Vector2D(lt.getImageX(norm) - ltFirst.getImageX(norm), lt.getImageY(norm) - ltFirst.getImageY(norm));
				Vector2D trans2 = new Vector2D(0.0, 1.0);
				double angle = calcAngle(trans1, trans2);
				double a = (ltFirst.getRealWorldX() - lt.getRealWorldX());
				double b = (ltFirst.getRealWorldY() - lt.getRealWorldY());
				double span_norm = Math.sqrt(a * a + b * b);
				
				double c = (ltFirst.getImageX(norm) - lt.getImageX(norm));
				double d = (ltFirst.getImageY(norm) - lt.getImageY(norm));
				double span = Math.sqrt(c * c + d * d);
				
				getResultSet().setNumericResult(0,
						"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaf." + num + ".orientation",
						angle, "degree");
				
				getResultSet().setNumericResult(0,
						"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaf." + num + ".span.norm",
						span_norm, "mm");
				
				getResultSet().setNumericResult(0,
						"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaf." + num + ".span",
						span, "px");
			}
			
			final int xPos_norm = lt.getRealWorldX();
			final int yPos_norm = lt.getRealWorldY();
			final Double angle = (Double) lt.getFeature("angle");
			
			getResultSet().setNumericResult(0,
					"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaf." + num + ".x",
					xPos_norm, "px");
			getResultSet().setNumericResult(0,
					"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaf." + num + ".y",
					yPos_norm, "px");
			
			if (angle != null)
				getResultSet()
						.setNumericResult(
								0,
								"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaf." + num
										+ ".angle",
								angle, "degree");
		}
		for (Leaf l : plant.getLeafList()) {
			LeafTip last = l.getLast();
			for (LeafTip lt : l) {
				
				if (lt == null || cameraPosition == null || cameraType == null)
					continue;
				
				final boolean db = debugValues;
				final boolean isLast = last == lt && last.getTime() == timepoint;
				final int num = l.leafID;
				final int xPos = lt.getImageX(norm);
				final int yPos = lt.getImageY(norm);
				final int xPos_norm = lt.getRealWorldX();
				final int yPos_norm = lt.getRealWorldY();
				final Double angle = (Double) lt.getFeature("angle");
				final Vector2D direction = (Vector2D) lt.getFeature("direction");
				
				getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
					
					@Override
					public Image postProcessMask(Image mask) {
						ImageCanvas c = mask.io().canvas();
						if (!isLast) {
							c = c.drawRectanglePoints(xPos - 4, yPos - 4, 8, 8, col.get(num), 1);
							if (db)
								c = c.text(xPos, yPos + 10, "rx: " + xPos_norm + " ry: " + yPos_norm +
										" a: " + angle.intValue(), Color.BLACK);
						} else {
							Vector2D vv = direction.subtract(new Vector2D(xPos, yPos));
							Vector2D d = vv.getNorm() > 0.01 ?
									vv.normalize()
											.scalarMultiply((1 + (Math.sqrt(2) - 1) * (1 - Math.abs(Math.cos(2 * angle / 180. * Math.PI)))) * 16)
									: vv;
							c = c.drawRectangle(xPos - 18, yPos - 18, 36, 36, col.get(num), 2)
									.drawLine(xPos, yPos, (int) d.getX() + xPos, (int) d.getY() + yPos, col.get(num).getRGB(), 0.2, 1);
							if (db)
								c = c.text(xPos, yPos + 10, "rx: " + xPos_norm + " ry: " + yPos_norm +
										" a: " + angle.intValue(), Color.BLACK);
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
		return "Tracks Leaf Tips (prior processing of block 'Detect Leaf-Tips' is nessessary)";
	}
}
