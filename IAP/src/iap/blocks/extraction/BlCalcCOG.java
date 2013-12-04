package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImage;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
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
				final Vector2d cog = fi.io().stat().getCOG();
				if (cog != null) {
					String pos = options.getCameraPosition() == CameraPosition.SIDE ? "RESULT_side." : "RESULT_top.";
					getProperties().setNumericProperty(getBlockPosition(),
							pos + "vis.cog.x", cog.x, "px");
					getProperties().setNumericProperty(getBlockPosition(),
							pos + "vis.cog.y", cog.y, "px");
				}
				RunnableOnImage runnableOnMask = new RunnableOnImage() {
					@Override
					public Image postProcess(Image in) {
						return in.io().canvas().drawCircle((int) cog.x, (int) cog.y, 5, Color.BLACK.getRGB(), 0.5d, 1).getImage();
					}
				};
				getProperties().addImagePostProcessor(CameraType.VIS, null, runnableOnMask);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fi = input().masks() != null ? input().masks().fluo() : null;
		if (getBoolean("Process Fluo Instead of Vis", true)) {
			if (fi != null) {
				final Vector2d cog = fi.io().stat().getCOG();
				if (cog != null) {
					String pos = options.getCameraPosition() == CameraPosition.SIDE ? "RESULT_side." : "RESULT_top.";
					getProperties().setNumericProperty(getBlockPosition(),
							pos + "fluo.cog.x", cog.x, "px");
					getProperties().setNumericProperty(getBlockPosition(),
							pos + "fluo.cog.y", cog.y, "px");
				}
				RunnableOnImage runnableOnMask = new RunnableOnImage() {
					@Override
					public Image postProcess(Image in) {
						return in.io().canvas().drawCircle((int) cog.x, (int) cog.y, 5, Color.BLACK.getRGB(), 0.5d, 1).getImage();
					}
				};
				getProperties().addImagePostProcessor(CameraType.FLUO, null, runnableOnMask);
				
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
	protected boolean isChangingImages() {
		return false;
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
