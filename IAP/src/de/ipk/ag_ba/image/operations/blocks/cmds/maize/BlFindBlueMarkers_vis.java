/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.util.ArrayList;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.MarkerPair;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * @author pape, klukas
 */
public class BlFindBlueMarkers_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	ArrayList<MarkerPair> numericResult = new ArrayList<MarkerPair>();
	
	FlexibleImage markerMask = null;
	
	@Override
	protected boolean isChangingImages() {
		return true;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		numericResult.clear();
		FlexibleImage vis = getInput().getMasks().getVis();
		if (vis == null)
			vis = getInput().getImages().getVis();
		
		if (vis != null) {
			markerMask = getMarkers(vis.copy(), numericResult);
			
			int w = vis.getWidth();
			int h = vis.getHeight();
			
			int n = 0;
			int i = 1;
			if (getProperties() == null)
				reportError((Exception) null, "getProperties returns NULL");
			for (MarkerPair mp : numericResult) {
				if (mp.getLeft() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getMarkerPropertyNameFromIndex(i), mp.getLeft().x / w);
					getProperties().setNumericProperty(0, PropertyNames.getMarkerPropertyNameFromIndex(i + 1), mp.getLeft().y / h);
				}
				i += 2;
				if (mp.getRight() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getMarkerPropertyNameFromIndex(i), mp.getRight().x / w);
					getProperties().setNumericProperty(0, PropertyNames.getMarkerPropertyNameFromIndex(i + 1), mp.getRight().y / h);
				}
				i += 2;
				n++;
				if (n >= 3)
					break;
			}
			if (!numericResult.isEmpty()) {
				calculateDistanceBetweenMarkers(numericResult, w);
			}
		}
		if (getInput().getMasks().getVis() != null)
			return vis;
		else
			return null;
	}
	
	// distances vertical
	private void calculateDistanceBetweenMarkers(ArrayList<MarkerPair> numericResult, int imageWidth) {
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
		
		// distances horizontal, max dist
		int[] distances = new int[numericResult.size()];
		int n = 0;
		for (MarkerPair mp : numericResult) {
			if (mp.getLeft() != null && mp.getRight() != null) {
				distances[n] = (int) Math.abs(mp.getLeft().x - mp.getRight().x);
			}
			n++;
		}
		
		int maxDist = max(distances);
		// int maxDist = (int) Math.abs(getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X).getValue() - getProperties()
		// .getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X).getValue() * imageWidth);
		
		if (maxDist > 0)
			getProperties().setNumericProperty(0, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT, maxDist);
		
		if (debug)
			System.out.println("maxDist_horizontal: " + maxDist + " " + (maxDist == 0 ? "NO MARKERS FOUND" : ""));
	}
	
	private int max(int[] distances) {
		int max = 0;
		for (int n : distances)
			if (n > max)
				max = n;
		return max;
	}
	
	private FlexibleImage getMarkers(FlexibleImage image, ArrayList<MarkerPair> result) {
		double s = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		ImageOperation io = image.getIO().searchBlueMarkers(result, s * s / 1.2, options.getCameraPosition(), options.isMaize(), true);
		return io != null ? io.getImage() : null;
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		if (debug)
			new ImageOperation(processedImages.getVis()).drawMarkers(numericResult).print("Marker Positions", debug);
		if (markerMask != null && processedImages.getVis() != null)
			processedImages.setVis(processedImages.getVis().getIO().and(markerMask).getImage());
		if (markerMask != null && processedMasks.getVis() != null)
			processedMasks.setVis(processedMasks.getVis().getIO().and(markerMask).getImage());
	}
}
