/**
 * 
 */
package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

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
public class BlCalcMainAxis
		extends AbstractSnapshotAnalysisBlock
		implements CalculatesProperties {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (optionsAndResults.getCameraPosition() == CameraPosition.TOP
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
				
				double normalizedDistanceToMainAxis = macr.getMinResult()
						.getDistanceSum()
						/ macr.getMinResult().getPixelCount()
						/ macr.getMinResult().getPixelCount();
				
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(optionsAndResults.getCameraPosition(), CameraType.FLUO, TraitCategory.GEOMETRY, "main_axis.rotation"), angle,
						"degree", this);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(optionsAndResults.getCameraPosition(), CameraType.FLUO, TraitCategory.GEOMETRY, "main_axis.normalized.distance.avg"),
						normalizedDistanceToMainAxis, this);
			}
		}
		return input().masks().fluo();
	}
	
	private MainAxisCalculationResult getAngle(Image image) {
		return new ImageOperation(image).calculateTopMainAxis(optionsAndResults
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
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("main_axis.rotation",
						"The orientation of the line (in degree), 0 indicates horizontal orientation "
								+ "(when looking at the top-image), 90 means orientation from top to bottom "
								+ "(when looking at the image)."),
				new CalculatedProperty("main_axis.normalized.distance.avg",
						"A centre line is calculated by detecting a line crossing the "
								+ "centre of the image. This line is oriented so that the sum of "
								+ "the distances of the plant pixels to this line is minimal. For "
								+ "maize plants this line orientation corresponds to the main "
								+ "leaf orientation. This value indicates the average distance "
								+ "of the plant pixels to this line. The higher this value, the "
								+ "less oriented are the plant leaves relative to the centre"
								+ "line.")
		};
	}
}
