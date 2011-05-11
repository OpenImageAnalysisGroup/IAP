/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author pape, klukas
 */
public class BlockCalculateMainAxis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION, getAngle(getInput().getMasks().getVis()));
		
		return getInput().getMasks().getVis();
	}
	
	private int getAngle(FlexibleImage image) {
		return new ImageOperation(image).calculateTopMainAxis(options.getBackground());
	}
}
