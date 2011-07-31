/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;
import java.util.ArrayList;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
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
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		
		ArrayList<MarkerPair> numericResult = new ArrayList<MarkerPair>();
		
		FlexibleImage vis = getInput().getMasks().getVis();
		if (options.getCameraPosition() == CameraPosition.SIDE && vis != null) {
			numericResult = getMarkers(vis);
			
			int w = vis.getWidth();
			int h = vis.getHeight();
			
			int n = 0;
			int i = 1;
			for (MarkerPair mp : numericResult) {
				if (mp.getLeft() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), mp.getLeft().x / w);
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), mp.getLeft().y / h);
				}
				i += 2;
				if (mp.getRight() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i), mp.getRight().x / w);
					getProperties().setNumericProperty(0, PropertyNames.getPropertyName(i + 1), mp.getRight().y / h);
				}
				i += 2;
				n++;
				if (n >= 3)
					break;
			}
			if (numericResult != null) {
				calculateDistanceBetweenMarkers(numericResult, w);
			}
			boolean debug = false;
			if (debug)
				return new ImageOperation(vis).drawMarkers(numericResult).getImage();
		}
		return getInput().getMasks().getVis();
	}
	
	// distances vertical
	private void calculateDistanceBetweenMarkers(ArrayList<MarkerPair> numericResult, int imageWidth) {
		boolean debug = false;
		if (getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y) != null
				&& getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y) != null) {
			double markerPosOneLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y).getValue() * imageWidth;
			double markerPosTwoLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y).getValue() * imageWidth;
			
			getProperties().setNumericProperty(0, PropertyNames.MARKER_DISTANCE_BOTTOM_TOP_LEFT, Math.abs(markerPosTwoLeft - markerPosOneLeft));
			if (debug)
				System.out.println("dist_vertical: " + (markerPosTwoLeft - markerPosOneLeft));
		}
		
		if (getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y) != null
				&& getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y) != null) {
			double markerPosOneRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y).getValue() * imageWidth;
			double markerPosTwoRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y).getValue() * imageWidth;
			
			getProperties().setNumericProperty(0, PropertyNames.MARKER_DISTANCE_BOTTOM_TOP_RIGHT, Math.abs(markerPosTwoRight - markerPosOneRight));
			if (debug)
				System.out.println("dist_vertical: " + (markerPosTwoRight - markerPosOneRight));
		}
		
		// distances horizontal
		int[] distances = new int[numericResult.size()];
		int n = 0;
		for (MarkerPair mp : numericResult) {
			if (mp.getLeft() != null && mp.getRight() != null) {
				distances[n] = (int) Math.abs(mp.getLeft().x - mp.getRight().x);
			}
			n++;
		}
		
		int maxDist = max(distances);
		getProperties().setNumericProperty(0, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT, maxDist);
		
		if (debug)
			System.out.println("maxDist_horizontal: " + maxDist);
	}
	
	private int max(int[] distances) {
		int max = 0;
		for (int n : distances)
			if (n > max)
				max = n;
		return max;
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
		double s = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		return image.getIO().searchBlueMarkers(s * s / 1.2, options.getCameraPosition(), options.isMaize());
	}
}
