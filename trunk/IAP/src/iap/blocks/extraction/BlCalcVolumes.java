package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.BlockResults;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Calculates volume estimation values from the side and top areas.
 * Processes by default vis and fluo data. Optionally also nir and ir data.
 * 
 * @author klukas
 */
public class BlCalcVolumes extends AbstractSnapshotAnalysisBlock {
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		if (getBoolean("process VIS", true))
			calculatePlantVolumeMeasures("vis", plandID2time2waterData, time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, false);
		if (getBoolean("process VIS normalized", true))
			calculatePlantVolumeMeasures("vis", plandID2time2waterData, time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, true);
		
		if (getBoolean("process FLUO", true))
			calculatePlantVolumeMeasures("fluo", plandID2time2waterData, time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, false);
		if (getBoolean("process FLUO normalized", true))
			calculatePlantVolumeMeasures("fluo", plandID2time2waterData, time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, true);
		
		if (getBoolean("process NIR", false))
			calculatePlantVolumeMeasures("nir", plandID2time2waterData, time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, false);
		if (getBoolean("process NIR normalized", false))
			calculatePlantVolumeMeasures("nir", plandID2time2waterData, time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, true);
		
		if (getBoolean("process IR", false))
			calculatePlantVolumeMeasures("ir", plandID2time2waterData, time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, false);
		if (getBoolean("process IR normalized", false))
			calculatePlantVolumeMeasures("ir", plandID2time2waterData, time2inSamples, time2inImages, time2allResultsForSnapshot, time2summaryResult, true);
	}
	
	private void calculatePlantVolumeMeasures(
			String cameraType,
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData, TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages, TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2summaryResult, boolean normalized) {
		
		String sideVisAreaTraitName = "RESULT_side." + cameraType + ".area" + (normalized ? ".norm" : "");
		
		String plantID = null;
		
		Double lastVolumeIAP = null;
		Long lastTimeVolumeIAP = null;
		
		Double lastSideAreaIAP = null;
		Long lastTimeSideAreaIAP = null;
		
		final double timeForOneDayD = 1000 * 60 * 60 * 24d;
		
		for (Long time : time2inSamples.keySet()) {
			TreeMap<String, HashMap<Integer, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (allResultsForSnapshot == null)
				continue;
			if (!time2summaryResult.containsKey(time)) {
				time2summaryResult.put(time, new TreeMap<String, HashMap<Integer, BlockResultSet>>());
			}
			TreeSet<String> ks;
			synchronized (allResultsForSnapshot) {
				ks = new TreeSet<String>(allResultsForSnapshot.keySet());
			}
			TreeSet<Integer> allTrays = new TreeSet<Integer>();
			for (String key : ks) {
				allTrays.addAll(allResultsForSnapshot.get(key).keySet());
			}
			if (!time2summaryResult.get(time).containsKey("-720"))
				time2summaryResult.get(time).put("-720", new HashMap<Integer, BlockResultSet>());
			if (time2summaryResult.get(time).get("-720").isEmpty())
				for (Integer knownTray : allTrays)
					time2summaryResult.get(time).get("-720").put(knownTray, new BlockResults(null));
			for (Integer tray : time2summaryResult.get(time).get("-720").keySet()) {
				double areaSum = 0;
				double areaCnt = 0;
				double sideArea_for_angleNearestTo0 = Double.NaN;
				double sideArea_for_angleNearestTo45 = Double.NaN;
				double sideArea_for_angleNearestTo90 = Double.NaN;
				double distanceTo0 = Double.MAX_VALUE;
				double distanceTo45 = Double.MAX_VALUE;
				double distanceTo90 = Double.MAX_VALUE;
				DescriptiveStatistics areaStat = new DescriptiveStatistics();
				
				for (String key : ks) {
					if (allResultsForSnapshot.get(key) == null)
						continue;
					BlockResultSet rt;
					synchronized (allResultsForSnapshot) {
						rt = allResultsForSnapshot.get(key).get(tray);
					}
					if (rt == null || rt.isNumericStoreEmpty())
						continue;
					for (BlockResultValue v : rt.searchResults(true, sideVisAreaTraitName, false)) {
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
				}
				
				BlockResultSet summaryResult = time2summaryResult.get(time).get("-720").get(tray);
				
				if (areaStat.getN() > 0) {
					summaryResult.setNumericResult(getBlockPosition(),
							sideVisAreaTraitName + ".min", areaStat.getMin(), normalized ? "mm^2" : "px^2");
					summaryResult.setNumericResult(getBlockPosition(),
							sideVisAreaTraitName + ".max", areaStat.getMax(), normalized ? "mm^2" : "px^2");
					summaryResult.setNumericResult(getBlockPosition(),
							sideVisAreaTraitName + ".median", areaStat.getPercentile(50), normalized ? "mm^2" : "px^2");
					summaryResult.setNumericResult(getBlockPosition(),
							sideVisAreaTraitName + ".avg", areaStat.getMean(), normalized ? "mm^2" : "px^2");
				}
				
				double topAreaSum = 0;
				double topAreaCnt = 0;
				
				synchronized (allResultsForSnapshot) {
					TreeSet<String> ks2 = new TreeSet<String>(allResultsForSnapshot.keySet());
					for (String key : ks2) {
						BlockResultSet rt;
						synchronized (allResultsForSnapshot) {
							HashMap<Integer, BlockResultSet> kk = allResultsForSnapshot.get(key);
							if (kk == null)
								continue;
							rt = allResultsForSnapshot.get(key).get(tray);
						}
						if (rt != null)
							for (BlockResultValue v : rt.searchResults(true, "RESULT_top." + cameraType + ".area" + (normalized ? ".norm" : ""), false)) {
								if (v.getValue() != null) {
									topAreaSum += v.getValue().doubleValue();
									topAreaCnt += 1;
								}
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
								summaryResult.setNumericResult(getBlockPosition(),
										"RESULT_side." + cameraType + ".area.avg.wue" + (normalized ? ".norm" : ""), wue, "px^2/ml/day");
								
							}
						}
						if (waterUsePerDay != null && waterUsePerDay > 0 && !Double.isInfinite(waterUsePerDay)) {
							double wue = relativeGrowthPerDay / waterUsePerDay;
							if (!Double.isNaN(wue) && !Double.isInfinite(wue)) {
								summaryResult.setNumericResult(getBlockPosition(),
										"RESULT_side." + cameraType + ".area.avg.wue.relative" + (normalized ? ".norm" : ""), wue, "percent/ml/day");
								
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
					summaryResult.setNumericResult(getBlockPosition(),
							"RESULT_volume." + cameraType + ".iap" + (normalized ? ".norm" : ""), volume_iap, "px^3");
					summaryResult.setNumericResult(getBlockPosition(),
							"RESULT_volume." + cameraType + ".iap_max" + (normalized ? ".norm" : ""), volume_iap_max, "px^3");
					
					if (lastTimeVolumeIAP != null && lastVolumeIAP > 0 && plantID != null) {
						double ratio = volume_iap / lastVolumeIAP;
						double ratioPerDay = Math.pow(ratio, timeForOneDayD / ((time - lastTimeVolumeIAP)));
						summaryResult.setNumericResult(getBlockPosition(),
								"RESULT_volume." + cameraType + ".iap.relative" + (normalized ? ".norm" : ""), ratioPerDay, "percent/day");
						double days = (time - lastTimeVolumeIAP) / timeForOneDayD;
						double absoluteGrowthPerDay = (volume_iap - lastVolumeIAP) / days;
						
						Double waterUsePerDay = getWaterUsePerDay(
								plandID2time2waterData.get(plantID),
								time, lastTimeVolumeIAP, timeForOneDayD);
						
						if (waterUsePerDay != null && waterUsePerDay > 0 && !Double.isInfinite(waterUsePerDay) && !Double.isNaN(waterUsePerDay)) {
							double wue = absoluteGrowthPerDay / waterUsePerDay;
							summaryResult.setNumericResult(getBlockPosition(),
									"RESULT_volume." + cameraType + ".iap.wue" + (normalized ? ".norm" : ""), wue, "px^3/ml/day");
						}
					}
					
					lastVolumeIAP = volume_iap;
					lastTimeVolumeIAP = time;
					
					if (!Double.isNaN(sideArea_for_angleNearestTo0) && !Double.isNaN(sideArea_for_angleNearestTo90)) {
						double volume_lt = Math.sqrt(sideArea_for_angleNearestTo0 * sideArea_for_angleNearestTo90 * avgTopArea);
						summaryResult.setNumericResult(getBlockPosition(),
								"RESULT_volume." + cameraType + ".lt" + (normalized ? ".norm" : ""), volume_lt, normalized ? "mm^3" : "px^3");
						double area = sideArea_for_angleNearestTo0 + sideArea_for_angleNearestTo90 + avgTopArea;
						summaryResult.setNumericResult(getBlockPosition(),
								"RESULT_volume." + cameraType + ".area090T" + (normalized ? ".norm" : ""), area, normalized ? "mm^2" : "px^2");
						double areaLog = sideArea_for_angleNearestTo0 + sideArea_for_angleNearestTo90 + Math.log(avgTopArea) / 3;
						summaryResult.setNumericResult(getBlockPosition(),
								"RESULT_volume." + cameraType + ".area090LogT" + (normalized ? ".norm" : ""), areaLog, normalized ? "mm^2" : "px^2");
					}
					
					if (!Double.isNaN(sideArea_for_angleNearestTo0) && !Double.isNaN(sideArea_for_angleNearestTo45) && !Double.isNaN(sideArea_for_angleNearestTo90)) {
						double s1, s2, s3, t1; // side area 1, 2, 3 and top area
						t1 = avgTopArea;
						s1 = sideArea_for_angleNearestTo0;
						s2 = sideArea_for_angleNearestTo45;
						s3 = sideArea_for_angleNearestTo90;
						double volume_prism = Math.sqrt(t1 * s2 * s3 / 2d * Math.sqrt(1 - Math.pow((s2 * s2 + s3 * s3 - s1 * s1) / (2d * s2 * s3), 2)));
						summaryResult.setNumericResult(getBlockPosition(),
								"RESULT_volume." + cameraType + ".prism" + (normalized ? ".norm" : ""), volume_prism, normalized ? "mm^3" : "px^3");
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
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Calculate Volume Estimations";
	}
	
	@Override
	public String getDescription() {
		return "Calculates volume estimation values from the side and top areas. " +
				"Processes by default vis and fluo data. Optionally also nir and ir data.";
	}
}
