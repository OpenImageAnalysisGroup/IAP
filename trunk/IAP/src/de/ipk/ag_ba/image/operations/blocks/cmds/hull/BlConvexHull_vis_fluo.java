package de.ipk.ag_ba.image.operations.blocks.cmds.hull;

import ij.measure.ResultsTable;

import java.awt.Color;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Calculates the convex hull for the fluorescence image and stores according data results as numeric
 * values (size of hull, centroid). The complex hull, the image borders and the centroid are drawn
 * on the result (input and result is the fluo mask).
 * 
 * @author klukas
 */
public class BlConvexHull_vis_fluo extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage image = getInput().getMasks().getVis();
		ImageData info = getInput().getImages().getVisInfo();
		ImageOperation res = processImage(image, info);
		return res != null ? res.getImage() : null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage image = getInput().getMasks().getFluo();
		ImageData info = getInput().getImages().getFluoInfo();
		ImageOperation res = processImage(image, info);
		return res != null ? res.getImage() : null;
	}
	
	private ImageOperation processImage(FlexibleImage image, ImageData info) {
		ResultsTable numericResults;
		ImageOperation res;
		if (image == null) {
			return null;
		}
		BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
		if (distHorizontal != null || options.getCameraPosition() == CameraPosition.TOP) {
			int realDist = options.getIntSetting(Setting.REAL_MARKER_DISTANCE);
			boolean drawHull = options.getBooleanSetting(Setting.DRAW_CONVEX_HULL);
			res = new ImageOperation(image).hull().find(true, false, drawHull, drawHull, Color.RED.getRGB(),
					Color.CYAN.getRGB(),
					Color.RED.getRGB(), distHorizontal, realDist);
			
			numericResults = res.getResultsTable();
		} else {
			numericResults = null;
			res = new ImageOperation(image);
		}
		if (options.getCameraPosition() == CameraPosition.SIDE && numericResults != null)
			getProperties().storeResults(
					"RESULT_side.", numericResults,
					getBlockPosition());
		if (options.getCameraPosition() == CameraPosition.TOP && numericResults != null)
			getProperties().storeResults(
					"RESULT_top.", numericResults, getBlockPosition());
		return res;
	}
	
	@Override
	public void postProcessResultsForAllAngles(
			Sample3D inSample,
			TreeMap<String, ImageData> inImages,
			TreeMap<String, BlockResultSet> allResultsForSnapshot, BlockResultSet summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		double areaSum = 0;
		int areaCnt = 0;
		for (String key : allResultsForSnapshot.keySet()) {
			BlockResultSet rt = allResultsForSnapshot.get(key);
			for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_side.area")) {
				if (v.getValue() != null) {
					areaSum += v.getValue().doubleValue();
					areaCnt += 1;
				}
			}
		}
		
		double topAreaSum = 0;
		double topAreaCnt = 0;
		for (String key : allResultsForSnapshot.keySet()) {
			BlockResultSet rt = allResultsForSnapshot.get(key);
			for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_top.area")) {
				if (v.getValue() != null) {
					topAreaSum += v.getValue().doubleValue();
					topAreaCnt += 1;
				}
			}
		}
		
		if (areaCnt > 0 && topAreaCnt > 0) {
			double avgTopArea = topAreaSum / topAreaCnt;
			double avgArea = areaSum / areaCnt;
			double volume_iap = Math.sqrt(avgArea * avgArea * avgTopArea);
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.iap", volume_iap);
		}
	}
	
}
