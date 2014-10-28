package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

/**
 * Calculates volume estimation values from the side and top areas.
 * Processes by default vis and fluo data. Optionally also nir and ir data.
 * 
 * @author klukas
 */
public class BlCalcVolumes extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			CalculatesProperties propertyCalculator) {
		if (getBoolean("process VIS", true))
			calculatePlantVolumeMeasures(CameraType.VIS, plandID2time2waterData, time2allResultsForSnapshot, time2summaryResult,
					false);
		if (getBoolean("process VIS normalized", true))
			calculatePlantVolumeMeasures(CameraType.VIS, plandID2time2waterData, time2allResultsForSnapshot, time2summaryResult,
					true);
		
		if (getBoolean("process FLUO", false))
			calculatePlantVolumeMeasures(CameraType.FLUO, plandID2time2waterData, time2allResultsForSnapshot, time2summaryResult,
					false);
		if (getBoolean("process FLUO normalized", false))
			calculatePlantVolumeMeasures(CameraType.FLUO, plandID2time2waterData, time2allResultsForSnapshot, time2summaryResult,
					true);
		
		if (getBoolean("process NIR", false))
			calculatePlantVolumeMeasures(CameraType.NIR, plandID2time2waterData, time2allResultsForSnapshot, time2summaryResult,
					false);
		if (getBoolean("process NIR normalized", false))
			calculatePlantVolumeMeasures(CameraType.NIR, plandID2time2waterData, time2allResultsForSnapshot, time2summaryResult,
					true);
		
		if (getBoolean("process IR", false))
			calculatePlantVolumeMeasures(CameraType.IR, plandID2time2waterData, time2allResultsForSnapshot, time2summaryResult,
					false);
		if (getBoolean("process IR normalized", false))
			calculatePlantVolumeMeasures(CameraType.IR, plandID2time2waterData, time2allResultsForSnapshot, time2summaryResult,
					true);
	}
	
	private void calculatePlantVolumeMeasures(
			CameraType cameraType,
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2summaryResult, boolean normalized) {
		
		String plantID = null;
		
		Double lastVolumeIAP = null;
		Long lastTimeVolumeIAP = null;
		
		Double lastSideAreaIAP = null;
		Long lastTimeSideAreaIAP = null;
		
		final double timeForOneDayD = 1000 * 60 * 60 * 24d;
		
		for (Long time : time2allResultsForSnapshot.keySet()) {
			TreeMap<String, HashMap<String, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
			if (allResultsForSnapshot == null)
				continue;
			if (!time2summaryResult.containsKey(time)) {
				time2summaryResult.put(time, new TreeMap<String, HashMap<String, BlockResultSet>>());
			}
			TreeSet<String> ks;
			synchronized (allResultsForSnapshot) {
				ks = new TreeSet<String>(allResultsForSnapshot.keySet());
			}
			TreeSet<String> allTrays = new TreeSet<String>();
			for (String key : ks) {
				allTrays.addAll(allResultsForSnapshot.get(key).keySet());
			}
			if (!time2summaryResult.get(time).containsKey("-720"))
				time2summaryResult.get(time).put("-720", new HashMap<String, BlockResultSet>());
			if (time2summaryResult.get(time).get("-720").isEmpty())
				for (String knownTray : allTrays)
					time2summaryResult.get(time).get("-720").put(knownTray, new BlockResults(null));
			for (String tray : time2summaryResult.get(time).get("-720").keySet()) {
				double areaSum = 0;
				double areaCnt = 0;
				double sideArea_for_angleNearestTo0 = Double.NaN;
				double sideArea_for_angleNearestTo45 = Double.NaN;
				double sideArea_for_angleNearestTo90 = Double.NaN;
				double distanceTo0 = Double.MAX_VALUE;
				double distanceTo45 = Double.MAX_VALUE;
				double distanceTo90 = Double.MAX_VALUE;
				DescriptiveStatistics areaStat = new DescriptiveStatistics();
				NumericMeasurement3D imageRef = null;
				
				for (String key : ks) {
					if (allResultsForSnapshot.get(key) == null)
						continue;
					BlockResultSet rt;
					synchronized (allResultsForSnapshot) {
						rt = allResultsForSnapshot.get(key).get(tray);
					}
					if (rt == null || rt.isNumericStoreEmpty())
						continue;
					for (BlockResultValue v : rt.searchResults(true, new Trait(optionsAndResults.getCameraPosition(), cameraType, TraitCategory.GEOMETRY, "area"
							+ (normalized ? ".norm" : "")).toString(), false)) {
						if (v.getValue() != null) {
							imageRef = v.getBinary();
							double area = v.getValue().doubleValue();
							areaStat.addValue(area);
							areaSum += area;
							areaCnt += 1;
							
							plantID = v.getBinary().getReplicateID() + ";" + v.getBinary().getQualityAnnotation();
							Double pos = v.getPosition();
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
				
				BlockResultSet summaryResult = time2summaryResult.get(time).get("-720").get(tray);
				
				if (areaStat.getN() > 0) {
					if (getBoolean("Store additional area statistics values", false)) {
						summaryResult.setNumericResult(getBlockPosition(),
								new Trait(optionsAndResults.getCameraPosition(), cameraType, TraitCategory.GEOMETRY, "area.min" + (normalized ? ".norm" : "")),
								areaStat.getMin(),
								normalized ? "mm^2" : "px", this, imageRef);
						summaryResult.setNumericResult(getBlockPosition(),
								new Trait(optionsAndResults.getCameraPosition(), cameraType, TraitCategory.GEOMETRY, "area.max" + (normalized ? ".norm" : "")),
								areaStat.getMax(),
								normalized ? "mm^2" : "px", this, imageRef);
						summaryResult.setNumericResult(getBlockPosition(),
								new Trait(optionsAndResults.getCameraPosition(), cameraType, TraitCategory.GEOMETRY, "area.median" + (normalized ? ".norm" : "")),
								areaStat.getPercentile(50),
								normalized ? "mm^2" : "px", this, imageRef);
					}
				}
				
				double topAreaSum = 0;
				double topAreaCnt = 0;
				
				synchronized (allResultsForSnapshot) {
					TreeSet<String> ks2 = new TreeSet<String>(allResultsForSnapshot.keySet());
					for (String key : ks2) {
						BlockResultSet rt;
						synchronized (allResultsForSnapshot) {
							HashMap<String, BlockResultSet> kk = allResultsForSnapshot.get(key);
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
					
					if (getBoolean("Calculate WUE", false))
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
											new Trait(optionsAndResults.getCameraPosition(), cameraType, TraitCategory.DERIVED, "area.avg.wue"
													+ (normalized ? ".norm" : "")), wue, "px/ml/day",
											this, imageRef);
									
								}
							}
							if (waterUsePerDay != null && waterUsePerDay > 0 && !Double.isInfinite(waterUsePerDay)) {
								double wue = (relativeGrowthPerDay - 1) * 100 / waterUsePerDay;
								if (!Double.isNaN(wue) && !Double.isInfinite(wue)) {
									summaryResult.setNumericResult(getBlockPosition(),
											new Trait(optionsAndResults.getCameraPosition(), cameraType, TraitCategory.DERIVED, "area.avg.wue.relative"
													+ (normalized ? ".norm" : "")), wue,
											"percent growth/ml/day", this, imageRef);
									
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
							new Trait(CameraPosition.COMBINED, cameraType, TraitCategory.GEOMETRY, "volume.iap" + (normalized ? ".norm" : "")), volume_iap, "voxel",
							this, imageRef);
					summaryResult.setNumericResult(getBlockPosition(),
							new Trait(CameraPosition.COMBINED, cameraType, TraitCategory.GEOMETRY, "volume.iap_max" + (normalized ? ".norm" : "")), volume_iap_max,
							"voxel", this, imageRef);
					
					if (lastTimeVolumeIAP != null && lastVolumeIAP > 0 && plantID != null) {
						double ratio = volume_iap / lastVolumeIAP;
						double ratioPerDay = (Math.pow(ratio, timeForOneDayD / ((time - lastTimeVolumeIAP))) - 1) * 100;
						summaryResult.setNumericResult(getBlockPosition(),
								new Trait(CameraPosition.COMBINED, cameraType, TraitCategory.DERIVED, "volume.iap.relative_percent" + (normalized ? ".norm" : "")),
								ratioPerDay,
								"percent/day", this, imageRef);
						double days = (time - lastTimeVolumeIAP) / timeForOneDayD;
						double absoluteGrowthPerDay = (volume_iap - lastVolumeIAP) / days;
						
						Double waterUsePerDay = getWaterUsePerDay(
								plandID2time2waterData.get(plantID),
								time, lastTimeVolumeIAP, timeForOneDayD);
						
						if (waterUsePerDay != null && waterUsePerDay > 0 && !Double.isInfinite(waterUsePerDay) && !Double.isNaN(waterUsePerDay)) {
							double wue = absoluteGrowthPerDay / waterUsePerDay;
							summaryResult.setNumericResult(getBlockPosition(),
									new Trait(CameraPosition.COMBINED, cameraType, TraitCategory.DERIVED, "volume.iap.wue" + (normalized ? ".norm" : "")), wue,
									"px^3/ml/day", this, imageRef);
						}
					}
					
					lastVolumeIAP = volume_iap;
					lastTimeVolumeIAP = time;
					
					if (!Double.isNaN(sideArea_for_angleNearestTo0) && !Double.isNaN(sideArea_for_angleNearestTo90)) {
						double volume_lt = Math.sqrt(sideArea_for_angleNearestTo0 * sideArea_for_angleNearestTo90 * avgTopArea);
						summaryResult.setNumericResult(getBlockPosition(),
								new Trait(CameraPosition.COMBINED, cameraType, TraitCategory.GEOMETRY, "volume.lt" + (normalized ? ".norm" : "")), volume_lt,
								normalized ? "mm^3" : "px^3",
								this, imageRef);
						double area = sideArea_for_angleNearestTo0 + sideArea_for_angleNearestTo90 + avgTopArea;
						summaryResult.setNumericResult(getBlockPosition(),
								new Trait(CameraPosition.COMBINED, cameraType, TraitCategory.GEOMETRY, "volume.area090T" + (normalized ? ".norm" : "")), area,
								normalized ? "mm^2" : "px^2",
								this, imageRef);
						double areaLog = sideArea_for_angleNearestTo0 + sideArea_for_angleNearestTo90 + Math.log(avgTopArea) / 3;
						summaryResult.setNumericResult(getBlockPosition(),
								new Trait(CameraPosition.COMBINED, cameraType, TraitCategory.GEOMETRY, "volume.area090LogT" + (normalized ? ".norm" : "")), areaLog,
								normalized ? "mm^2"
										: "px^2", this, imageRef);
					}
					
					if (!Double.isNaN(sideArea_for_angleNearestTo0) && !Double.isNaN(sideArea_for_angleNearestTo45) && !Double.isNaN(sideArea_for_angleNearestTo90)) {
						double s1, s2, s3, t1; // side area 1, 2, 3 and top area
						t1 = avgTopArea;
						s1 = sideArea_for_angleNearestTo0;
						s2 = sideArea_for_angleNearestTo45;
						s3 = sideArea_for_angleNearestTo90;
						double volume_prism = Math.sqrt(t1 * s2 * s3 / 2d * Math.sqrt(1 - Math.pow((s2 * s2 + s3 * s3 - s1 * s1) / (2d * s2 * s3), 2)));
						summaryResult.setNumericResult(getBlockPosition(),
								new Trait(CameraPosition.COMBINED, cameraType, TraitCategory.GEOMETRY, "volume.prism" + (normalized ? ".norm" : "")), volume_prism,
								normalized ? "mm^3"
										: "px^3", this, imageRef);
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
	public boolean isChangingImages() {
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
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("area.min",
						"From all side views, the minimum number of foreground pixels."),
				new CalculatedProperty("area.min.norm",
						"From all side view, the minimum of the normalized area of foreground pixels according to real-world coordinates."),
				new CalculatedProperty("area.max",
						"From all side views, the maximum number of foreground pixels."),
				new CalculatedProperty("area.max.norm",
						"From all side view, the maximum of the normalized area of foreground pixels according to real-world coordinates."),
				new CalculatedProperty("area.median",
						"From all side views, the median number of foreground pixels."),
				new CalculatedProperty("area.median.norm",
						"From all side view, the median of the normalized area of foreground pixels according to real-world coordinates."),
				new CalculatedProperty("area.avg.wue",
						"The average side area is used to calculate the 'water use efficiency', by taking into account the exact sample time, "
								+ "the increase of side area from the previous sample time to the current sample time and the amount of water applied "
								+ "to the plant during this time. If the watering data does not exactly cover the sample time span, the fraction of the "
								+ "water amount from watering data covering a larger time span around the current relevant time span is calculated and considered. "
								+ "The exact calculation scheme is currently not documented. For details consult the implementation (source code)."),
				new CalculatedProperty("area.avg.wue.norm",
						"The exact calculation scheme is currently not documented. For details consult the implementation (source code)."),
				new CalculatedProperty("volume.iap",
						"The exact calculation scheme is currently not documented. For details consult the implementation (source code)."),
				new CalculatedProperty("volume.iap.norm",
						"The exact calculation scheme is currently not documented. For details consult the implementation (source code)."),
				new CalculatedProperty("volume.iap_max",
						"The exact calculation scheme is currently not documented. For details consult the implementation (source code)."),
				new CalculatedProperty("volume.iap_max.norm",
						"The exact calculation scheme is currently not documented. For details consult the implementation (source code)."),
				new CalculatedProperty("volume.iap.wue",
						"The exact calculation scheme is currently not documented. For details consult the implementation (source code)."),
				new CalculatedProperty("volume.iap.wue.norm",
						"The exact calculation scheme is currently not documented. For details consult the implementation (source code)."),
				new CalculatedProperty("volume.lt",
						"Volume estimation according to this calculation scheme: "
								+ "Math.sqrt(sideArea_for_angleNearestTo0 * sideArea_for_angleNearestTo90 * avgTopArea)"),
				new CalculatedProperty("volume.lt.norm",
						"See volume.lt. Calculation based on area values, normalized according to real-world coordinates."),
				new CalculatedProperty("volume.area090T",
						"Volume estimation according to this calculation scheme: sideArea_for_angleNearestTo0 + sideArea_for_angleNearestTo90 + avgTopArea"),
				new CalculatedProperty("volume.area090T.norm",
						"See volume.area090T. Calculation based on area values, normalized according to real-world coordinates."),
				new CalculatedProperty(
						"volume.area090LogT",
						"Volume estimation according to this calculation scheme: sideArea_for_angleNearestTo0 + sideArea_for_angleNearestTo90 + Math.log(avgTopArea) / 3"),
				new CalculatedProperty("volume.area090LogT.norm",
						"See volume.area090LogT. Calculation based on area values, normalized according to real-world coordinates."),
				new CalculatedProperty("volume.prism",
						"Volume estimation according to this calculation scheme: t1 = avgTopArea; s1 = sideArea_for_angleNearestTo0; "
								+ "s2 = sideArea_for_angleNearestTo45; s3 = sideArea_for_angleNearestTo90; "
								+ "volume_prism = Math.sqrt(t1 * s2 * s3 / 2d * Math.sqrt(1 - Math.pow((s2 * s2 + s3 * s3 - s1 * s1) / (2d * s2 * s3), 2)));"),
				new CalculatedProperty("volume.prism.norm",
						"See volume.prism. Calculation based on area values, normalized according to real-world coordinates."),
		};
	}
}
