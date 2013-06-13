/**
 * 
 */
package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.blocks.data_structures.BlockType;

import java.util.ArrayList;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.MarkerPair;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author pape, klukas
 */
public class BlDetectBlueMarkers extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug;
	ArrayList<MarkerPair> numericResult;
	Image markerMask;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		numericResult = new ArrayList<MarkerPair>();
		markerMask = null;
	}
	
	@Override
	protected boolean isChangingImages() {
		return true;
	}
	
	@Override
	protected Image processVISmask() {
		numericResult.clear();
		Image vis = input().masks().vis();
		if (vis == null)
			vis = input().images().vis();
		
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
					getProperties().setNumericProperty(0, PropertyNames.getMarkerPropertyNameFromIndex(i).getName(), mp.getLeft().x / w);
					getProperties().setNumericProperty(0, PropertyNames.getMarkerPropertyNameFromIndex(i + 1).getName(), mp.getLeft().y / h);
				}
				i += 2;
				if (mp.getRight() != null) {
					getProperties().setNumericProperty(0, PropertyNames.getMarkerPropertyNameFromIndex(i).getName(), mp.getRight().x / w);
					getProperties().setNumericProperty(0, PropertyNames.getMarkerPropertyNameFromIndex(i + 1).getName(), mp.getRight().y / h);
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
		if (input().masks().vis() != null)
			return vis;
		else
			return null;
	}
	
	// distances vertical
	private void calculateDistanceBetweenMarkers(ArrayList<MarkerPair> numericResult, int imageWidth) {
		if (getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y.getName()) != null
				&& getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y.getName()) != null) {
			double markerPosOneLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y.getName()).getValue() * imageWidth;
			double markerPosTwoLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y.getName()).getValue() * imageWidth;
			
			getProperties().setNumericProperty(0, PropertyNames.MARKER_DISTANCE_BOTTOM_TOP_LEFT.getName(), Math.abs(markerPosTwoLeft - markerPosOneLeft));
			if (debug)
				System.out.println("dist_vertical: " + (markerPosTwoLeft - markerPosOneLeft));
		}
		
		if (getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y.getName()) != null
				&& getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y.getName()) != null) {
			double markerPosOneRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y.getName()).getValue() * imageWidth;
			double markerPosTwoRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y.getName()).getValue() * imageWidth;
			
			getProperties().setNumericProperty(0, PropertyNames.MARKER_DISTANCE_BOTTOM_TOP_RIGHT.getName(), Math.abs(markerPosTwoRight - markerPosOneRight));
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
		getProperties().setNumericProperty(0, options.getCameraPosition().name().toLowerCase() + ".optics.blue_marker_distance", maxDist, "px");
		
		if (maxDist > 0)
			options.setCalculatedBlueMarkerDistance(maxDist);
		
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
	
	private Image getMarkers(Image image, ArrayList<MarkerPair> result) {
		double s = getDouble("Scale-factor-decrease-img-and-mask", 1); // options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK);
		ImageOperation io = image.io().searchBlueMarkers(result, s * s / 1.2, options.getCameraPosition(), true,
				getBoolean("delted located blue markers", true), debug);
		return io != null ? io.getImage() : null;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		if (debug)
			new ImageOperation(processedImages.vis()).drawMarkers(numericResult).show("Marker Positions", debug);
		if (markerMask != null && processedImages.vis() != null)
			processedImages.setVis(processedImages.vis().io().and(markerMask).getImage());
		if (markerMask != null && processedMasks.vis() != null)
			processedMasks.setVis(processedMasks.vis().io().and(markerMask).getImage());
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
}
