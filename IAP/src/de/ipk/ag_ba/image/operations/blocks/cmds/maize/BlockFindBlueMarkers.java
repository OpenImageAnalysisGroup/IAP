/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;
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
		
		ArrayList<MarkerPair> numericResult = new ArrayList<MarkerPair>();
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			numericResult = getMarkers(getInput().getMasks().getVis());
			
			int n = 0;
			int i = 1;
			for (MarkerPair mp : numericResult) {
				if (mp.getLeft() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), mp.getLeft().x);
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), mp.getLeft().y);
				} else {
					System.out.println("n=" + n + ", i=" + i + ", lx: " + mp.getLeft().x + " ly: " + mp.getLeft().y);
				}
				i += 2;
				if (mp.getRight() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), mp.getRight().x);
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), mp.getRight().y);
				} else {
					System.out.println("n=" + n + ", i=" + i + ", rx: " + mp.getRight().x + " ry: " + mp.getRight().y);
				}
				i += 2;
				n++;
				if (n >= 3)
					break;
			}
		}
		return getInput().getMasks().getVis();
	}
	
	private FlexibleImage drawMarkers(FlexibleImage vis, ArrayList<MarkerPair> numericResult) {
		ImageOperation io = new ImageOperation(vis);
		
		for (int index = 0; index < numericResult.size(); index++) {
			int leftX = (int) numericResult.get(index).left.x;
			int leftY = (int) numericResult.get(index).left.y;
			int rightX = (int) numericResult.get(index).right.x;
			int rightY = (int) numericResult.get(index).right.y;
			io.drawLine(leftX, leftY, rightX, rightY, Color.CYAN, 20);
		}
		return io.getImage();
	}
	
	private ArrayList<MarkerPair> getMarkers(FlexibleImage image) {
		return new ImageOperation(image).searchBlueMarkers();
	}
}
