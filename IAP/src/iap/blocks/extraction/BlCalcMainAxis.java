/**
 * 
 */
package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.MainAxisCalculationResult;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Calculates the main axis rotation for visible top images. All other image
 * types and configurations are ignored.
 * Does not need any parameters.
 * 
 * @author pape, klukas
 */
public class BlCalcMainAxis extends
		AbstractSnapshotAnalysisBlock {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (options.getCameraPosition() == CameraPosition.TOP
				&& input().masks().fluo() != null) {
			MainAxisCalculationResult macr = getAngle(input().masks()
					.fluo());
			if (macr != null) {
				double angle = macr.getMinResult().getAngle();
				
				Double imageRotationAngle = input().masks().getFluoInfo().getPosition();
				
				if (imageRotationAngle == null)
					imageRotationAngle = 0d;
				
				angle = angle - imageRotationAngle;
				
				if (angle < 0)
					angle = angle + 360;
				if (angle > 180)
					angle = angle - 180;
				
				// getProperties().setNumericProperty(0,
				// PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION, angle);
				double normalizedDistanceToMainAxis = macr.getMinResult()
						.getDistanceSum()
						/ macr.getMinResult().getPixelCount()
						/ macr.getMinResult().getPixelCount();
				// getProperties().setNumericProperty(0,
				// PropertyNames.RESULT_TOP_MAIN_AXIS_NORMALIZED_DISTANCE,
				// normalizedDistanceToMainAxis);
				getProperties().setNumericProperty(0, "RESULT_top.fluo.centroid.x",
						macr.getCentroid().x, "px");
				getProperties().setNumericProperty(0, "RESULT_top.fluo.centroid.y",
						macr.getCentroid().y, "px");
				
				getProperties().setNumericProperty(getBlockPosition(), "RESULT_top.fluo.main.axis.rotation", angle, "degree");
				getProperties().setNumericProperty(getBlockPosition(), "RESULT_top.fluo.main.axis.normalized.distance.avg", normalizedDistanceToMainAxis);
			}
		}
		return input().masks().fluo();
	}
	
	private MainAxisCalculationResult getAngle(Image image) {
		return new ImageOperation(image).calculateTopMainAxis(options
				.getBackground());
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
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
		return "Calculate Main Orientation (Top)";
	}
	
	@Override
	public String getDescription() {
		return "Calculates the main axis rotation for fluo top images. All other image " +
				"types and configurations are ignored.";
	}
}
