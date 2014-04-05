package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import org.Colors;

import tests.JMP.leaf_clustering.BorderFeature;
import tests.JMP.leaf_clustering.Leaf;
import tests.JMP.leaf_clustering.LeafTip;
import tests.JMP.leaf_clustering.LeafTipMatcher;
import tests.JMP.leaf_clustering.Plant;
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
				
				if (previousResults != null) {
					if (unassignedResults != null) {
						// match
						matchNewResults(previousResults, unassignedResults, cp, ct, time);
						getResultSet().removeResultObject(result1);
					}
				} else {
					if (unassignedResults != null) {
						// first run, create new plant
						createNewPlant(unassignedResults, cp, ct, time);
						getResultSet().removeResultObject(result1);
					}
				}
			}
		}
	}
	
	private void createNewPlant(LinkedList<BorderFeature> unassignedResults, CameraPosition cameraPosition,
			CameraType cameraType, long timepoint) {
		LeafTipMatcher ltm = new LeafTipMatcher(unassignedResults, timepoint);
		ltm.setMinDist(100.0);
		ltm.matchLeafTips();
		getResultSet().setObjectResult(getBlockPosition(), "plant_" + cameraType, ltm.getMatchedPlant());
	}
	
	private void matchNewResults(Plant previousResults,
			LinkedList<BorderFeature> unassignedResults, CameraPosition cameraPosition, final CameraType cameraType, long timepoint) {
		LeafTipMatcher ltm = new LeafTipMatcher(previousResults, unassignedResults, timepoint);
		ltm.matchLeafTips();
		final Plant plant = ltm.getMatchedPlant();
		// TODO calc dist between leaftips , dist / (time_n +1 - time_n) * 24*60*60*1000; Leaflength += dist;
		
		getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
			
			@Override
			public Image postProcessMask(Image mask) {
				ImageCanvas c = mask.io().canvas();
				LinkedList<Leaf> ll = plant.getLeafList();
				ArrayList<Color> col = Colors.get(ll.size());
				int idx = 0;
				for (Leaf l : ll) {
					for (LeafTip lt : l) {
						c = c.fillCircle(lt.getX(), lt.getY(), 5, col.get(idx).getRGB(), 0.5)
								.drawCircle(lt.getX(), lt.getY(), 6, Color.RED.getRGB(), 0.5, 2)
						;
					}
					idx++;
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
		
		getResultSet().setObjectResult(getBlockPosition(), "plant_" + cameraType, plant);
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
