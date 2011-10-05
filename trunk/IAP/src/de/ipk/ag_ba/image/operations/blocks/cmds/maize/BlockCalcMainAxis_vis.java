/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import ij.measure.ResultsTable;

import org.SystemAnalysis;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
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
public class BlockCalcMainAxis_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		
		if (options.getCameraPosition() == CameraPosition.TOP && getInput().getMasks().getVis() != null) {
			MainAxisCalculationResult macr = getAngle(getInput().getMasks().getVis());
			if (macr != null) {
				double angle = macr.getMinResult().getAngle();
				
				double imageRotationAngle = 0;
				if (options.hasDoubleSetting(Setting.INPUT_VIS_IMAGE_ROTATION_ANGLE)) {
					imageRotationAngle = options.getDoubleSetting(Setting.INPUT_VIS_IMAGE_ROTATION_ANGLE);
					System.out.println("Considering top image rotation: " + imageRotationAngle);
				}
				
				angle = angle - imageRotationAngle;
				
				// getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION, angle);
				double normalizedDistanceToMainAxis = macr.getMinResult().getDistanceSum() / macr.getMinResult().getPixelCount()
						/ macr.getMinResult().getPixelCount();
				// getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_MAIN_AXIS_NORMALIZED_DISTANCE, normalizedDistanceToMainAxis);
				getProperties().setNumericProperty(0, PropertyNames.CENTROID_X, macr.getCentroid().x);
				getProperties().setNumericProperty(0, PropertyNames.CENTROID_Y, macr.getCentroid().y);
				
				ResultsTable rt = new ResultsTable();
				rt.incrementCounter();
				rt.addValue("main.axis.rotation", angle);
				rt.addValue("main.axis.normalized.distance.avg", normalizedDistanceToMainAxis);
				
				getProperties().storeResults("RESULT_top.", rt, getBlockPosition());
			} else {
				if (!SystemAnalysis.isHeadless()) {
					// getInput().getImages().getVis().print("(Image) Could not determine main axis angle (" + SystemAnalysisExt.getCurrentTime() + ")");
					// getInput().getMasks().getVis().print("(Mask) Could not determine main axis angle (" + SystemAnalysisExt.getCurrentTime() + ")");
				}
				System.err.println("ERROR: BlockCalculateMainAxis: Could not determine main axis angle!");
				getInput().getMasks().getVis().print("main axis problem");
			}
		}
		return getInput().getMasks().getVis();
	}
	
	private MainAxisCalculationResult getAngle(FlexibleImage image) {
		return new ImageOperation(image).calculateTopMainAxis(options.getBackground());
	}
}
