package de.ipk.ag_ba.image.operations.blocks.cmds.hull;

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
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
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
		FlexibleImage image = input().masks().vis();
		ImageOperation res = processImage("vis.", image);
		return res != null ? res.getImage() : null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage image = input().masks().fluo();
		ImageOperation res = processImage("fluo.", image);
		return res != null ? res.getImage() : null;
	}
	
	private ImageOperation processImage(String prefix, FlexibleImage image) {
		ResultsTableWithUnits numericResults;
		ImageOperation res;
		if (image == null) {
			return null;
		}
		BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
		Integer realDist = options.getIntSetting(Setting.REAL_MARKER_DISTANCE);
		if (distHorizontal == null)
			realDist = null;
		boolean drawHull = options.getBooleanSetting(Setting.DRAW_CONVEX_HULL);
		res = new ImageOperation(image).hull().find(true, false, drawHull, drawHull, Color.RED.getRGB(),
				Color.CYAN.getRGB(),
				Color.RED.getRGB(), distHorizontal, realDist);
		
		numericResults = res.getResultsTable();
		if (options.getCameraPosition() == CameraPosition.SIDE && numericResults != null)
			getProperties().storeResults(
					"RESULT_side." + prefix, numericResults,
					getBlockPosition());
		if (options.getCameraPosition() == CameraPosition.TOP && numericResults != null)
			getProperties().storeResults(
					"RESULT_top." + prefix, numericResults, getBlockPosition());
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
		
		Double lastSideAreaIAP = null;
		Long lastTimeSideAreaIAP = null;
		
		final double timeForOneDayD = 1000 * 60 * 60 * 24d;
		
		String plantID = null;
		
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] { "RESULT_side.vis.area", "RESULT_top.vis.area" });
		
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
				double areaCnt = 0, areaCntFluo = 0, areaCntWeight = 0;
				double topAreaSumFluo = 0, topAreaWeightSumFluo = 0;
				double topAreaCntFluo = 0, topAreaCntWeightCnt = 0;
				double sideArea_for_angleNearestTo0 = Double.NaN;
				double sideArea_for_angleNearestTo45 = Double.NaN;
				double sideArea_for_angleNearestTo90 = Double.NaN;
				double distanceTo0 = Double.MAX_VALUE;
				double distanceTo45 = Double.MAX_VALUE;
				double distanceTo90 = Double.MAX_VALUE;
				DescriptiveStatistics areaStat = new DescriptiveStatistics();
				
				for (String key : allResultsForSnapshot.keySet()) {
					BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
					if (rt == null || rt.isNumericStoreEmpty())
						continue;
					for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_side.vis.area")) {
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
									pos = pos % 180;
									if (pos > 90)
										pos = 180 - pos;
									if (Math.abs(pos - 0) < distanceTo0) {
										distanceTo0 = Math.abs(pos - 0);
										sideArea_for_angleNearestTo0 = area;
									}
									if (Math.abs(pos - 45) < distanceTo45) {
										distanceTo45 = Math.abs(pos - 45);
										sideArea_for_angleNearestTo45 = area;
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
					summaryResult.setNumericProperty(getBlockPosition(),
							"RESULT_volume.fluo.iap", volume_iap, "px^3");
				}
				if (areaCntWeight > 0 && topAreaCntWeightCnt > 0) {
					double avgTopArea = topAreaWeightSumFluo / topAreaCntWeightCnt;
					double avgArea = areaSumFluoWeight / areaCntWeight;
					double volume_iap = Math.sqrt(avgArea * avgArea * avgTopArea);
					summaryResult.setNumericProperty(getBlockPosition(),
							"RESULT_volume.fluo.plant_weight.iap", volume_iap, null);
				}
				
				if (areaStat.getN() > 0) {
					summaryResult.setNumericProperty(getBlockPosition(),
							"RESULT_side.vis.area.min", areaStat.getMin(), "px^2");
					summaryResult.setNumericProperty(getBlockPosition(),
							"RESULT_side.vis.area.max", areaStat.getMax(), "px^2");
					summaryResult.setNumericProperty(getBlockPosition(),
							"RESULT_side.vis.area.median", areaStat.getPercentile(50), "px^2");
					summaryResult.setNumericProperty(getBlockPosition(),
							"RESULT_side.vis.area.avg", areaStat.getMean(), "px^2");
				}
				
				double topAreaSum = 0;
				double topAreaCnt = 0;
				for (String key : allResultsForSnapshot.keySet()) {
					BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
					for (BlockPropertyValue v : rt.getPropertiesExactMatch("RESULT_top.vis.area")) {
						if (v.getValue() != null) {
							topAreaSum += v.getValue().doubleValue();
							topAreaCnt += 1;
						}
					}
				}
				
				if (areaCnt > 0) {
					double avgArea = areaSum / areaCnt;
					
					if (lastTimeSideAreaIAP != null && lastSideAreaIAP > 0 && plantID != null) {
						double days = (time / 1000d - lastTimeSideAreaIAP / 1000d) / (timeForOneDayD / 1000d);
						
						double absoluteGrowthPerDay = (avgArea - lastSideAreaIAP) / days;
						double relativeGrowthPerDay = Math.pow(avgArea / lastSideAreaIAP, 1d / days);
						Double waterUsePerDay = getWaterUsePerDay(
								plandID2time2waterData.get(plantID),
								time, lastTimeSideAreaIAP, timeForOneDayD);
						
						if (waterUsePerDay != null && waterUsePerDay > 0 && !Double.isInfinite(waterUsePerDay)) {
							double wue = absoluteGrowthPerDay / waterUsePerDay;
							if (!Double.isNaN(wue) && !Double.isInfinite(wue)) {
								summaryResult.setNumericProperty(getBlockPosition(),
										"RESULT_side.vis.area.avg.wue", wue, "px^2/ml/day");
								System.out.println("[Absolute] Plant " + plantID + " has been watered with about " + waterUsePerDay.intValue() + " ml per day, at "
										+ new Date(time).toString() + ", used for side area growth of " + wue + " pixels per ml per day");
								
							}
						}
						if (waterUsePerDay != null && waterUsePerDay > 0 && !Double.isInfinite(waterUsePerDay)) {
							double wue = relativeGrowthPerDay / waterUsePerDay;
							if (!Double.isNaN(wue) && !Double.isInfinite(wue)) {
								summaryResult.setNumericProperty(getBlockPosition(),
										"RESULT_side.vis.area.avg.wue.relative", wue, "percent/ml/day");
								System.out.println("[Relative] Plant " + plantID + " has been watered with about " + waterUsePerDay.intValue() + " ml per day, at "
										+ new Date(time).toString() + ", used for side area growth of " + wue + " percent per ml per day");
								
							}
						}
					}
					
					lastSideAreaIAP = avgArea;
					lastTimeSideAreaIAP = time;
				}
				
				if (areaCnt > 0 && topAreaCnt > 0) {
					double avgTopArea = topAreaSum / topAreaCnt;
					double avgArea = areaSum / areaCnt;
					double volume_iap = Math.sqrt(avgArea * avgArea * avgTopArea);
					double side = areaStat.getMax();
					double volume_iap_max = Math.sqrt(side * side * avgTopArea);
					summaryResult.setNumericProperty(getBlockPosition(),
							"RESULT_volume.vis.iap", volume_iap, "px^3");
					summaryResult.setNumericProperty(getBlockPosition(),
							"RESULT_volume.vis.iap_max", volume_iap_max, "px^3");
					
					if (lastTimeVolumeIAP != null && lastVolumeIAP > 0 && plantID != null) {
						double ratio = volume_iap / lastVolumeIAP;
						double ratioPerDay = Math.pow(ratio, timeForOneDayD / ((time - lastTimeVolumeIAP)));
						summaryResult.setNumericProperty(getBlockPosition(),
								"RESULT_volume.vis.iap.relative", ratioPerDay, "percent/day");
						double days = (time - lastTimeVolumeIAP) / timeForOneDayD;
						double absoluteGrowthPerDay = (volume_iap - lastVolumeIAP) / days;
						
						Double waterUsePerDay = getWaterUsePerDay(
								plandID2time2waterData.get(plantID),
								time, lastTimeVolumeIAP, timeForOneDayD);
						
						if (waterUsePerDay != null && waterUsePerDay > 0 && !Double.isInfinite(waterUsePerDay) && !Double.isNaN(waterUsePerDay)) {
							double wue = absoluteGrowthPerDay / waterUsePerDay;
							summaryResult.setNumericProperty(getBlockPosition(),
									"RESULT_volume.vis.iap.wue", wue, "px^3/ml/day");
							
							System.out.println("Plant " + plantID + " has been watered with about "
									+ waterUsePerDay.intValue() + " ml per day, at "
									+ new Date(time).toString() + ", used for side volume growth of "
									+ wue + " pixels per ml per day, relative volume growth: "
									+ ratioPerDay + ", volume: " + volume_iap + " --> " + lastTimeVolumeIAP);
							
						}
					}
					
					lastVolumeIAP = volume_iap;
					lastTimeVolumeIAP = time;
					
					if (!Double.isNaN(sideArea_for_angleNearestTo0) && !Double.isNaN(sideArea_for_angleNearestTo90)) {
						double volume_lt = Math.sqrt(sideArea_for_angleNearestTo0 * sideArea_for_angleNearestTo90 * avgTopArea);
						summaryResult.setNumericProperty(getBlockPosition(),
								"RESULT_volume.vis.lt", volume_lt, "px^3");
						double area = sideArea_for_angleNearestTo0 + sideArea_for_angleNearestTo90 + avgTopArea;
						summaryResult.setNumericProperty(getBlockPosition(),
								"RESULT_volume.vis.area090T", area, "px^2");
					}
					
					if (!Double.isNaN(sideArea_for_angleNearestTo0) && !Double.isNaN(sideArea_for_angleNearestTo45) && !Double.isNaN(sideArea_for_angleNearestTo90)) {
						double s1, s2, s3, t1; // side area 1, 2, 3 and top area
						t1 = avgTopArea;
						s1 = sideArea_for_angleNearestTo0;
						s2 = sideArea_for_angleNearestTo45;
						s3 = sideArea_for_angleNearestTo90;
						double volume_prism = Math.sqrt(t1 * s2 * s3 / 2d * Math.sqrt(1 - Math.pow((s2 * s2 + s3 * s3 - s1 * s1) / (2d * s2 * s3), 2)));
						summaryResult.setNumericProperty(getBlockPosition(),
								"RESULT_volume.vis.prism", volume_prism, "px^3");
					}
				}
			}
		}
		
	}
	
	private Double getWaterUsePerDay(
			TreeMap<Long, Double> time2waterData,
			Long endTime, Long startTime, double timeForOneDay) {
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
				if (time >= startTime && time <= endTime) {
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
