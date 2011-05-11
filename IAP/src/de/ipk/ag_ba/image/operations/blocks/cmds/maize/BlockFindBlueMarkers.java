/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.util.ArrayList;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.MarkerPair;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author pape, klukas
 */
public class BlockFindBlueMarkers extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		// getProperties().setNumericProperty(0, PropertyNames.RESULT_TOP_MAIN_AXIS_ROTATION, getMarkers(getInput().getMasks().getVis()));
		// getProperties().setNumericProperty(0, PropertyNames.VIS_MARKER_POS_1_X, value)
		
		return getInput().getMasks().getVis();
	}
	
	private ArrayList<MarkerPair> getMarkers(FlexibleImage image) {
		return new ImageOperation(image).searchBlueMarkers();
	}
}
