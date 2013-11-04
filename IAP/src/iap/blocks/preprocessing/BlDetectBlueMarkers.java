/**
 * 
 */
package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.MarkerPair;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author pape, klukas
 */
public class BlDetectBlueMarkers extends AbstractSnapshotAnalysisBlock {
	
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
			if (!getBoolean("Use fixed marker distance", options.getCameraPosition() == CameraPosition.TOP))
				markerMask = getMarkers(vis.copy(), numericResult);
			
			int w = vis.getWidth();
			int h = vis.getHeight();
			
			int n = 0;
			int i = 1;
			if (getProperties() == null)
				reportError((Exception) null, "getProperties returns NULL");
			if (!getBoolean("Use fixed marker distance", options.getCameraPosition() == CameraPosition.TOP))
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
			calculateDistanceBetweenMarkers(numericResult, w, input().masks().getVisInfo().getParentSample().getParentCondition().getParentSubstance().getInfo());
		}
		if (input().masks().vis() != null)
			return vis;
		else
			return null;
	}
	
	private void calculateDistanceBetweenMarkers(ArrayList<MarkerPair> numericResult, int imageWidth, String cameraConfig) {
		int maxDist = getInt("Fixed marker distance (px) (" + cameraConfig + ")", 1350);
		if (getBoolean("Use fixed marker distance", options.getCameraPosition() == CameraPosition.TOP)) {
			if (cameraConfig == null || cameraConfig.trim().isEmpty())
				cameraConfig = "unknown camera config";
			getProperties().setNumericProperty(0, options.getCameraPosition().name().toLowerCase() + ".optics.blue_marker_distance", maxDist, "px");
			getProperties().setNumericProperty(0, options.getCameraPosition().name().toLowerCase() + ".optics.blue_marker_distance.predefined", 1, "0/1");
			options.setCalculatedBlueMarkerDistance(maxDist);
		} else
			if (!numericResult.isEmpty()) {
				if (getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y.getName()) != null
						&& getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y.getName()) != null) {
					double markerPosOneLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y.getName()).getValue()
							* imageWidth;
					double markerPosTwoLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y.getName()).getValue()
							* imageWidth;
					
					getProperties().setNumericProperty(0, PropertyNames.MARKER_DISTANCE_BOTTOM_TOP_LEFT.getName(), Math.abs(markerPosTwoLeft - markerPosOneLeft));
					if (debug)
						System.out.println("dist_vertical: " + (markerPosTwoLeft - markerPosOneLeft));
				}
				
				if (getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y.getName()) != null
						&& getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y.getName()) != null) {
					double markerPosOneRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y.getName()).getValue()
							* imageWidth;
					double markerPosTwoRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y.getName()).getValue()
							* imageWidth;
					
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
				
				maxDist = max(distances);
				getProperties().setNumericProperty(0, options.getCameraPosition().name().toLowerCase() + ".optics.blue_marker_distance", maxDist, "px");
				
				if (maxDist > 0)
					options.setCalculatedBlueMarkerDistance(maxDist);
				
				if (debug)
					System.out.println("maxDist_horizontal: " + maxDist + " " + (maxDist == 0 ? "NO MARKERS FOUND" : ""));
			}
	}
	
	private int max(int[] distances) {
		int max = 0;
		for (int n : distances)
			if (n > max)
				max = n;
		return max;
	}
	
	private Image getMarkers(Image image, ArrayList<MarkerPair> result) {
		boolean clearBlueMarkers = getBoolean("Remove blue markers from image", true);
		ImageOperation io = image.io().searchBlueMarkers(result, options.getCameraPosition(), true, clearBlueMarkers, debug);
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
	
	public static java.awt.geom.Rectangle2D.Double getRelativeBlueMarkerRectangle(BlockResults res) {
		BlockProperty leftX1 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X.getName());
		BlockProperty leftX2 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_X.getName());
		BlockProperty leftX3 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_LEFT_X.getName());
		
		BlockProperty rightX1 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X.getName());
		BlockProperty rightX2 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_X.getName());
		BlockProperty rightX3 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_RIGHT_X.getName());
		
		BlockProperty leftY1 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y.getName());
		BlockProperty leftY2 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_LEFT_Y.getName());
		BlockProperty leftY3 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_LEFT_Y.getName());
		
		BlockProperty rightY1 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y.getName());
		BlockProperty rightY2 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_2_RIGHT_Y.getName());
		BlockProperty rightY3 = res.getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_RIGHT_Y.getName());
		
		Double mostLeftX = min(leftX1, leftX2, leftX3);
		Double mostRightX = max(rightX1, rightX2, rightX3);
		
		Double mostTopY = min(leftY1, leftY2, leftY3, rightY1, rightY2, rightY3);
		Double mostBottomY = max(leftY1, leftY2, leftY3, rightY1, rightY2, rightY3);
		
		if (mostLeftX != null && mostRightX != null && mostTopY != null && mostBottomY != null)
			return new Rectangle2D.Double(mostLeftX, mostTopY, mostRightX - mostLeftX, mostBottomY - mostTopY);
		else
			return null;
	}
	
	private static Double min(BlockProperty... bp) {
		double min = Double.MAX_VALUE;
		for (BlockProperty b : bp) {
			if (b != null) {
				double v = b.getValue();
				if (v < min)
					min = v;
			}
		}
		if (min < Double.MAX_VALUE)
			return min;
		else
			return null;
		
	}
	
	private static Double max(BlockProperty... bp) {
		double max = Double.MIN_VALUE;
		for (BlockProperty b : bp) {
			if (b != null) {
				double v = b.getValue();
				if (v > max)
					max = v;
			}
		}
		if (max > Double.MIN_VALUE)
			return max;
		else
			return null;
	}
	
	@Override
	public String getName() {
		return "Detect Blue Markers";
	}
	
	@Override
	public String getDescription() {
		return "Detects blue side markers and uses the provided real-world distance to calculate a conversion factor from px to mm. " +
				"Optionally a fixed objects distance can be entered and used, instead.";
	}
}
