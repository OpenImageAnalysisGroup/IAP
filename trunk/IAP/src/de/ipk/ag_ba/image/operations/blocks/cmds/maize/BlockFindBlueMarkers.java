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
			
			int w = getInput().getMasks().getVis().getWidth();
			int h = getInput().getMasks().getVis().getHeight();
			
			int n = 0;
			int i = 1;
			for (MarkerPair mp : numericResult) {
				if (mp.getLeft() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), mp.getLeft().x / w);
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), mp.getLeft().y / h);
				} else {
					// System.out.println("n=" + n + ", i=" + i + ", lx: " + mp.getLeft().x + " ly: " + mp.getLeft().y);
				}
				i += 2;
				if (mp.getRight() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), mp.getRight().x / w);
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), mp.getRight().y / h);
				} else {
					// System.out.println("n=" + n + ", i=" + i + ", rx: " + mp.getRight().x + " ry: " + mp.getRight().y);
				}
				i += 2;
				n++;
				if (n >= 3)
					break;
			}
			if (numericResult != null) {
				calculateDistanceBetweenMarkers(numericResult);
			}
		}
		return getInput().getMasks().getVis();
	}
	
	private void calculateDistanceBetweenMarkers(ArrayList<MarkerPair> numericResult) {
		if (getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y) != null
				&& getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_LEFT_Y) != null) {
			double markerPosOneLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y).getValue();
			double markerPosTwoLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_LEFT_Y).getValue();
			
			getProperties().setNumericProperty(0, PropertyNames.RESULT_DISTANCE_MARKER_LEFT, Math.abs(markerPosTwoLeft - markerPosOneLeft));
		}
		
		if (getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y) != null
				&& getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_RIGHT_Y) != null) {
			double markerPosOneRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y).getValue();
			double markerPosTwoRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_RIGHT_Y).getValue();
			
			getProperties().setNumericProperty(0, PropertyNames.RESULT_DISTANCE_MARKER_RIGHT, Math.abs(markerPosTwoRight - markerPosOneRight));
		}
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
