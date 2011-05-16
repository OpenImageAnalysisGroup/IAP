/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.MainAxisCalculationResult;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Calculates the main axis rotation for visible top images. All other image types and configs are ignored.
 * 
 * @author pape, klukas
 */
public class BlockCalculateMainAxis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		if (options.getCameraTyp() == CameraTyp.TOP) {
			MainAxisCalculationResult r = getAngle(getInput().getMasks().getVis());
			
			double angle = r.getMinResult().getAngle();
			
			double imageRotationAngle = 0;
			if (options.getVis() != null) {
				imageRotationAngle = options.getVis().getPosition();
				System.out.println("Rotation: " + imageRotationAngle);
			}
			getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION, angle);
			double normalizedDistanceToMainAxis = r.getMinResult().getDistanceSum() / r.getMinResult().getPixelCount() / r.getMinResult().getPixelCount();
			getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_MAIN_AXIS_NORMALIZED_DISTANCE, normalizedDistanceToMainAxis);
		}
		
		return getInput().getMasks().getVis();
	}
	
	private MainAxisCalculationResult getAngle(FlexibleImage image) {
		return new ImageOperation(image).calculateTopMainAxis(options.getBackground());
	}
}
