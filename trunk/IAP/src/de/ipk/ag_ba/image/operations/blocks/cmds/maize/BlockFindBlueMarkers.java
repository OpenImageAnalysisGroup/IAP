/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.util.ArrayList;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.MarkerPair;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author pape, klukas
 */
public class BlockFindBlueMarkers extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			ArrayList<MarkerPair> numericResult = getMarkers(getInput().getMasks().getVis());
			
			int n = 0;
			int i = 1;
			for (MarkerPair mp : numericResult) {
				if (mp.getLeft() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), mp.getLeft().x);
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), mp.getLeft().y);
				}
				i += 2;
				if (mp.getRight() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), mp.getRight().x);
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), mp.getRight().y);
				}
				n++;
				if (n >= 3)
					break;
			}
		}
		return getInput().getMasks().getVis();
	}
	
	private ArrayList<MarkerPair> getMarkers(FlexibleImage image) {
		return new ImageOperation(image).searchBlueMarkers();
	}
}
