/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import org.graffiti.plugin.parameter.Parameter;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.MainAxisCalculationResult;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Calculates the main axis rotation for visible top images. All other image
 * types and configs are ignored.
 * Does not need any parameters.
 * 
 * @author pape, klukas
 */
public class BlCalcMainAxis_vis extends
		AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
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
	
	private MainAxisCalculationResult getAngle(FlexibleImage image) {
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
}
