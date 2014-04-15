package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImage;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

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
	protected Image processMask(Image fi) {
		if (fi == null)
			return null;
		if (!getBoolean("Process " + fi.getCameraType(), true)) {
			if (fi != null) {
				final Vector2d cog = fi.io().stat().getCOG();
				if (cog != null) {
					String pos = optionsAndResults.getCameraPosition() == CameraPosition.SIDE ? "RESULT_side." : "RESULT_top.";
					getResultSet().setNumericResult(getBlockPosition(),
							pos + fi.getCameraType() + ".cog.x", cog.x, "px");
					getResultSet().setNumericResult(getBlockPosition(),
							pos + fi.getCameraType() + ".cog.y", cog.y, "px");
					
					Double averageDistance = fi.io().stat().calculateAverageDistanceTo(cog);
					if (averageDistance != null)
						getResultSet().setNumericResult(0, pos + fi.getCameraType() + ".cog.avg_distance_to_center", averageDistance, "px");
					
				}
				RunnableOnImage runnableOnMask = new RunnableOnImage() {
					@Override
					public Image postProcess(Image in) {
						return in.io().canvas().drawCircle((int) cog.x, (int) cog.y, 5, Color.BLACK.getRGB(), 0.5d, 1).getImage();
					}
				};
				getResultSet().addImagePostProcessor(fi.getCameraType(), null, runnableOnMask);
			}
		}
		return fi;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
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
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	public String getName() {
		return "Calculate Center of Gravity";
	}
	
	@Override
	public String getDescription() {
		return "Calculates the center of gravity and the average distance of the plant pixels to the center.";
	}
}
