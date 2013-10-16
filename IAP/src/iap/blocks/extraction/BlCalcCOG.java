package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import org.Vector2d;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Christian Klukas
 */
public class BlCalcCOG extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
	}
	
	@Override
	protected Image processVISmask() {
		Image fi = input().masks() != null ? input().masks().vis() : null;
		if (!getBoolean("Process Fluo Instead of Vis", true)) {
			if (fi != null) {
				Vector2d cog = fi.io().stat().getCOG();
				if (cog != null) {
					getProperties().setNumericProperty(getBlockPosition(),
							"RESULT_side.vis.cog.x", cog.x, "px");
					getProperties().setNumericProperty(getBlockPosition(),
							"RESULT_side.vis.cog.y", cog.y, "px");
				}
			}
		}
		return fi;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fi = input().masks() != null ? input().masks().fluo() : null;
		if (getBoolean("Process Fluo Instead of Vis", true)) {
			if (fi != null) {
				Vector2d cog = fi.io().stat().getCOG();
				if (cog != null) {
					getProperties().setNumericProperty(getBlockPosition(),
							"RESULT_side.fluo.cog.x", cog.x, "px");
					getProperties().setNumericProperty(getBlockPosition(),
							"RESULT_side.fluo.cog.y", cog.y, "px");
				}
			}
		}
		return fi;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Calculate Center of Gravity";
	}
	
	@Override
	public String getDescription() {
		return "Calculates the center of gravity.";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "Process Fluo images (default) or Vis images (if instead selected).";
	}
}
