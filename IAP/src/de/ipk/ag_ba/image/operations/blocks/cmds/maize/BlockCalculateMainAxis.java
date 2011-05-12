/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
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
		
		getInput().getMasks().getVis().print("Debug");
		
		if (options.getCameraTyp() == CameraTyp.TOP)
			getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION, getAngle(getInput().getMasks().getVis()));
		
		return getInput().getMasks().getVis();
	}
	
	private int getAngle(FlexibleImage image) {
		return new ImageOperation(image).calculateTopMainAxis(options.getBackground());
	}
}
