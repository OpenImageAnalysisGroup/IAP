package de.ipk.ag_ba.image.operations.blocks.cmds.hull;

import ij.measure.ResultsTable;

import java.awt.Color;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
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
		// getInput().getMasks().getVis().copy().saveToFile(ReleaseInfo.getDesktopFolder() + File.separator + "MaizeVISMask.png");
		FlexibleImage image = input().masks().vis();
		ImageData info = input().images().getVisInfo();
		ImageOperation res = processImage(image, info);
		return res != null ? res.getImage() : null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage image = input().masks().fluo();
		ImageData info = input().images().getFluoInfo();
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
		if (true) { // distHorizontal != null || options.getCameraPosition() == CameraPosition.TOP
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
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		Double lastVolumeIAP = null;
		Long lastTimeVolumeIAP = null;
		final long timeForOneDay = 1000 * 60 * 60 * 24;
		
		String plantID = null;
		
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] { "RESULT_side.area", "RESULT_top.area" });
		
		for (Long time : time2inSamples.keySet()) {
			TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (!time2summaryResult.containsKey(time)) {
				time2summaryResult.put(time, new HashMap<Integer, BlockResultSet>());
			}
			TreeSet<Integer> allTrays = new TreeSet<Integer>();
			for (String key : allResultsForSnapshot.keySet()) {
				allTrays.addAll(allResultsForSnapshot.get(key).keySet());
			}
			if (time2summaryResult.get(time).isEmpty())
				for (Integer knownTray : allTrays)
					time2summaryResult.get(time).put(knownTray, new BlockResults());
			for (Integer tray : time2summaryResult.get(time).keySet()) {
				BlockResultSet summaryResult = time2summaryResult.get(time).get(tray);
				
				double areaSum = 0, areaSumFluo = 0, areaSumFluoWeight = 0;
				int areaCnt = 0, areaCntFluo = 0, areaCntWeight = 0;
				double topAreaSumFluo = 0, topAreaWeightSumFluo = 0;
				int topAreaCntFluo = 0, topAreaCntWeightCnt = 0;
				double sideArea_for_angleNearestTo0 = Double.NaN;
				double sideArea_for_angleNearestTo90 = Double.NaN;
				double distanceTo0 = Double.MAX_VALUE;
				double distanceTo90 = Double.MAX_VALUE;
				DescriptiveStatistics areaStat = new DescriptiveStatistics();
				
				for (String key : allResultsForSnapshot.keySet()) {
					BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
					for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_side.area")) {
						if (v.getValue() != null) {
							double area = v.getValue().doubleValue();
							areaStat.addValue(area);
							areaSum += area;
							areaCnt += 1;
							
							TreeMap<String, ImageData> tid = time2inImages.get(time);
							if (tid != null) {
								ImageData id = tid.get(key);
								if (id != null) {
									plantID = id.getReplicateID() + ";" + id.getQualityAnnotation();
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
					for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_side.fluo.filled.pixels")) {
						if (v.getValue() != null) {
							double area = v.getValue().doubleValue();
							areaSumFluo += area;
							areaCntFluo += 1;
						}
					}
					for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_side.fluo.intensity.phenol.plant_weight")) {
						if (v.getValue() != null) {
							double area = v.getValue().doubleValue();
							areaSumFluoWeight += area;
							areaCntWeight += 1;
						}
					}
					for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_top.fluo.filled.pixels")) {
						if (v.getValue() != null) {
							double area = v.getValue().doubleValue();
							topAreaSumFluo += area;
							topAreaCntFluo += 1;
						}
					}
					for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_top.fluo.intensity.phenol.plant_weight")) {
						if (v.getValue() != null) {
							double area = v.getValue().doubleValue();
							topAreaWeightSumFluo += area;
							topAreaCntWeightCnt += 1;
						}
					}
				}
				
				if (areaCntFluo > 0 && topAreaCntFluo > 0) {
					double avgTopArea = topAreaSumFluo / topAreaCntFluo;
					double avgArea = areaSumFluo / areaCntFluo;
					double volume_iap = Math.sqrt(avgArea * avgArea * avgTopArea);
					summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.fluo.iap", volume_iap);
				}
				if (areaCntWeight > 0 && topAreaCntWeightCnt > 0) {
					double avgTopArea = topAreaWeightSumFluo / topAreaCntWeightCnt;
					double avgArea = areaSumFluoWeight / areaCntWeight;
					double volume_iap = Math.sqrt(avgArea * avgArea * avgTopArea);
					summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.fluo.plant_weight.iap", volume_iap);
				}
				
				summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.area.min", areaStat.getMin());
				summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.area.max", areaStat.getMax());
				summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.area.median", areaStat.getPercentile(50));
				summaryResult.setNumericProperty(getBlockPosition(), "RESULT_side.area.avg", areaStat.getMean());
				
				double topAreaSum = 0;
				double topAreaCnt = 0;
				for (String key : allResultsForSnapshot.keySet()) {
					BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
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
					double side = areaStat.getMax();
					double volume_iap_max = Math.sqrt(side * side * avgTopArea);
					summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.iap", volume_iap);
					summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.iap_max", volume_iap_max);
					
					if (lastTimeVolumeIAP != null && lastVolumeIAP > 0 && plantID != null) {
						double ratio = volume_iap / lastVolumeIAP;
						double ratioPerDay = ratio / (time - lastTimeVolumeIAP) * timeForOneDay;
						summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.iap.relative", ratioPerDay);
						double days = 1d / (time - lastTimeVolumeIAP) * timeForOneDay;
						double growthPerDay = (volume_iap - lastVolumeIAP) * days;
						
						Double waterUsePerDay = getWaterUsePerDay(
								plandID2time2waterData.get(plantID),
								time, lastTimeVolumeIAP, timeForOneDay);
						
						if (waterUsePerDay != null && waterUsePerDay > 0 && !Double.isInfinite(waterUsePerDay)) {
							double wue = growthPerDay / waterUsePerDay;
							summaryResult.setNumericProperty(getBlockPosition(), "RESULT_volume.iap.wue", wue);
						}
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
	
	private Double getWaterUsePerDay(
			TreeMap<Long, Double> time2waterData,
			Long endTime, Long startTime, long timeForOneDay) {
		// time == startTime, OK
		// time < endTime, OK
		Double waterSum = 0d;
		
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(startTime));
		gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
		gc.set(GregorianCalendar.MINUTE, 0);
		gc.set(GregorianCalendar.SECOND, 0);
		gc.set(GregorianCalendar.MILLISECOND, 0);
		startTime = gc.getTimeInMillis();
		
		Long firstWaterTime = null;
		Long lastWaterTime = null;
		boolean prolonged = false;
		if (time2waterData != null)
			for (Long time : time2waterData.keySet()) {
				if (time == null)
					continue;
				if (!prolonged && time >= endTime) {
					prolonged = true;
					endTime = time;
				}
				if (time >= startTime && time < endTime) {
					waterSum += time2waterData.get(time);
					if (firstWaterTime == null || time < firstWaterTime)
						firstWaterTime = time;
					if (lastWaterTime == null || time > lastWaterTime)
						lastWaterTime = time;
				}
			}
		if (firstWaterTime == null || lastWaterTime == null)
			return null;
		else {
			long duration = lastWaterTime - firstWaterTime;
			return waterSum / duration * timeForOneDay;
		}
	}
}
