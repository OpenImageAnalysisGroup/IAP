/**
 * 
 */
package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import org.graffiti.plugin.parameter.Parameter;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.MainAxisCalculationResult;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Calculates the main axis rotation for visible top images. All other image
 * types and configs are ignored.
 * Does not need any parameters.
 * 
 * @author pape, klukas
 */
public class BlCalcMainAxis extends
		AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected Image processVISmask() {
		if (options.getCameraPosition() == CameraPosition.TOP
				&& input().masks().vis() != null) {
			MainAxisCalculationResult macr = getAngle(input().masks()
					.vis());
			if (macr != null) {
				double angle = macr.getMinResult().getAngle();
				
				double imageRotationAngle = getDouble("OFFSET_VIS_IMAGE_ROTATION_ANGLE", 0);
				
				angle = angle - imageRotationAngle;
				
				// getProperties().setNumericProperty(0,
				// PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION, angle);
				double normalizedDistanceToMainAxis = macr.getMinResult()
						.getDistanceSum()
						/ macr.getMinResult().getPixelCount()
						/ macr.getMinResult().getPixelCount();
				// getProperties().setNumericProperty(0,
				// PropertyNames.RESULT_TOP_MAIN_AXIS_NORMALIZED_DISTANCE,
				// normalizedDistanceToMainAxis);
				getProperties().setNumericProperty(0, PropertyNames.CENTROID_X,
						macr.getCentroid().x);
				getProperties().setNumericProperty(0, PropertyNames.CENTROID_Y,
						macr.getCentroid().y);
				
				ResultsTableWithUnits rt = new ResultsTableWithUnits();
				rt.incrementCounter();
				rt.addValue("main.axis.rotation", angle);
				rt.addValue("main.axis.normalized.distance.avg",
						normalizedDistanceToMainAxis);
				
				getProperties().storeResults("RESULT_top.", rt,
						getBlockPosition());
			}
		}
		return input().masks().vis();
	}
	
	private MainAxisCalculationResult getAngle(Image image) {
		return new ImageOperation(image).calculateTopMainAxis(options
				.getBackground());
	}
	
	@Override
	public Parameter[] getParameters() {
		// no parameters are needed
		return new Parameter[] {};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		super.setParameters(params);
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
}
