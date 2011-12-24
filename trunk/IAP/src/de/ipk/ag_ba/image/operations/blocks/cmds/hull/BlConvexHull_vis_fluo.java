package de.ipk.ag_ba.image.operations.blocks.cmds.hull;

import ij.measure.ResultsTable;

import java.awt.Color;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
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
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, BlockResultSet>> time2allResultsForSnapshot,
			TreeMap<Long, BlockResultSet> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		Double lastVolumeIAP = null;
		Long lastTimeVolumeIAP = null;
		final long timeForOneDay = 1000 * 60 * 60 * 24;
		
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] { "RESULT_side.area", "RESULT_top.area" });
		
		for (Long time : time2inSamples.keySet()) {
			TreeMap<String, BlockResultSet> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (!time2summaryResult.containsKey(time))
				time2summaryResult.put(time, new BlockResults());
			BlockResultSet summaryResult = time2summaryResult.get(time);
			
			double areaSum = 0;
			int areaCnt = 0;
			double sideArea_for_angleNearestTo0 = Double.NaN;
			double sideArea_for_angleNearestTo90 = Double.NaN;
			double distanceTo0 = Double.MAX_VALUE;
			double distanceTo90 = Double.MAX_VALUE;
			DescriptiveStatistics areaStat = DescriptiveStatistics.newInstance();
			
			for (String key : allResultsForSnapshot.keySet()) {
				BlockResultSet rt = allResultsForSnapshot.get(key);
				for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_side.area")) {
					if (v.getValue() != null) {
						double area = v.getValue().doubleValue();
						areaStat.addValue(area);
						areaSum += area;
						areaCnt += 1;
						
						TreeMap<String, ImageData> tid = time2inImages.get(time);
						if (tid != null) {
							ImageData id = tid.get(key);
							Double pos = id.getPosition();
							if (pos == null)
								pos = 0d;
							if (Math.abs(pos - 0) < distanceTo0) {
								distanceTo0 = Math.abs(pos - 0);
								sideArea_for_angleNearestTo0 = area;
							}
							if (Math.abs(pos - 90) < distanceTo90) {
								distanceTo0 = Math.abs(pos - 90);
								sideArea_for_angleNearestTo90 = area;
							}
						}
					}
				}
			}
			
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.area.min", areaStat.getMin());
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.area.max", areaStat.getMax());
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.area.median", areaStat.getPercentile(50));
			summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.area.avg", areaStat.getMean());
			
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
				volume_iap = Math.ceil(volume_iap);
				summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.iap", volume_iap);
				
				if (lastTimeVolumeIAP != null && lastVolumeIAP > 0) {
					double ratio = volume_iap / lastVolumeIAP;
					double ratioPerDay = ratio / (time - lastTimeVolumeIAP) * timeForOneDay;
					summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.iap.relative", ratioPerDay);
				}
				
				lastVolumeIAP = volume_iap;
				lastTimeVolumeIAP = time;
				
				if (!Double.isNaN(sideArea_for_angleNearestTo0) && !Double.isNaN(sideArea_for_angleNearestTo90)) {
					double volume_lt = Math.sqrt(sideArea_for_angleNearestTo0 * sideArea_for_angleNearestTo90 * avgTopArea);
					volume_lt = Math.ceil(volume_lt);
					summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.lt", volume_lt);
				}
			}
		}
	}
}
