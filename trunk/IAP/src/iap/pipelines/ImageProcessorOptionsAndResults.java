/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package iap.pipelines;

import iap.blocks.data_structures.ImageAnalysisBlock;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.SystemOptions;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.server.databases.DatabaseTarget;

/**
 * @author klukas
 */
public class ImageProcessorOptionsAndResults {
	
	public static final double DEFAULT_MARKER_DIST = 1150d;
	
	private CameraPosition cameraPosition = CameraPosition.UNKNOWN;
	private NeighbourhoodSetting neighbourhood = NeighbourhoodSetting.NB4;
	private final int nirBackground = new Color(180, 180, 180).getRGB();
	private int well_cnt;
	private int unit_test_idx;
	private int unit_test_steps;
	private SystemOptions optSystemOptionStorage;
	
	private String infoCamConfOrLateOrEearly;
	
	private String customNullBlockPrefix;
	
	private final TreeMap<String, HashMap<Integer, BlockResultSet>> previousResultsForThisTimePoint;
	
	private Double cameraAngle;
	
	private final TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> plantResults;
	
	public ImageProcessorOptionsAndResults(SystemOptions options, TreeMap<String, HashMap<Integer, BlockResultSet>> previousResultsForThisTimePoint,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> plantResults) {
		this.optSystemOptionStorage = options;
		this.previousResultsForThisTimePoint = previousResultsForThisTimePoint;
		this.plantResults = plantResults;
	}
	
	public enum CameraPosition {
		UNKNOWN, TOP, SIDE;
		
		@Override
		public String toString() {
			switch (this) {
				case TOP:
					return "top";
				case SIDE:
					return "side";
			}
			return "unknown";
		}
	}
	
	public void setCameraInfos(CameraPosition cameraTyp, String cameraConfig, String lateOrEarly, Double cameraAngle) {
		this.cameraPosition = cameraTyp;
		this.cameraAngle = cameraAngle;
		
		if (lateOrEarly == null && cameraConfig == null)
			infoCamConfOrLateOrEearly = null;
		else
			if (lateOrEarly != null && cameraConfig == null)
				infoCamConfOrLateOrEearly = " (" + lateOrEarly + ")";
			else
				if (cameraConfig != null && lateOrEarly == null)
					infoCamConfOrLateOrEearly = " for " + cameraConfig;
				else
					infoCamConfOrLateOrEearly = " for " + cameraConfig + " (" + lateOrEarly + ")";
		
	}
	
	public SystemOptions getOptSystemOptions() {
		return optSystemOptionStorage;
	}
	
	public CameraPosition getCameraPosition() {
		return cameraPosition;
	}
	
	public int getBackground() {
		return ImageOperation.BACKGROUND_COLORint;
	}
	
	public void setNeighbourhood(NeighbourhoodSetting neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
	
	public NeighbourhoodSetting getNeighbourhood() {
		return neighbourhood;
	}
	
	public int getNirBackground() {
		return nirBackground;
	}
	
	public void setWellCnt(int tray_cnt) {
		this.well_cnt = tray_cnt;
	}
	
	public int getWellCnt() {
		return well_cnt;
	}
	
	public void setUnitTestInfo(int unit_test_idx, int unit_test_steps) {
		this.unit_test_idx = unit_test_idx;
		this.unit_test_steps = unit_test_steps;
	}
	
	public double getUnitTestIdx() {
		return unit_test_idx;
	}
	
	public double getUnitTestSteps() {
		return unit_test_steps;
	}
	
	public void setSystemOptionStorage(SystemOptions systemOptionStorage) {
		this.setOptSystemOptionStorage(systemOptionStorage);
	}
	
	public void setCustomNullBlockPrefix(String customNullBlockPrefix) {
		this.customNullBlockPrefix = customNullBlockPrefix;
	}
	
	public String getSystemOptionStorageGroup() {
		if (infoCamConfOrLateOrEearly != null) {
			if (getCameraPosition() != CameraPosition.UNKNOWN)
				return getCameraPosition() + " settings" + infoCamConfOrLateOrEearly;
			else
				return (customNullBlockPrefix != null ? customNullBlockPrefix : "Postprocessing") + infoCamConfOrLateOrEearly;
		} else {
			if (getCameraPosition() != CameraPosition.UNKNOWN)
				return getCameraPosition() + " settings";
			else
				return (customNullBlockPrefix != null ? customNullBlockPrefix : "Postprocessing");
		}
	}
	
	private String getSystemOptionStorageGroupWithoutCustomInfo() {
		if (getCameraPosition() != CameraPosition.UNKNOWN)
			return getCameraPosition() + " settings";
		else
			return (customNullBlockPrefix != null ? customNullBlockPrefix : "Postprocessing");
	}
	
	private void setOptSystemOptionStorage(SystemOptions optSystemOptionStorage) {
		this.optSystemOptionStorage = optSystemOptionStorage;
	}
	
	public boolean getBooleanSetting(ImageAnalysisBlock block, String title, boolean defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getBoolean(
					getSystemOptionStorageGroup(), getSettingName(block, title), infoCamConfOrLateOrEearly == null ? defaultValue :
							optSystemOptionStorage.getBoolean(getSystemOptionStorageGroupWithoutCustomInfo(), getSettingName(block, title), defaultValue)
					);
	}
	
	public boolean setBooleanSetting(ImageAnalysisBlock block, String title, boolean defaultValue) {
		if (optSystemOptionStorage == null)
			return false;
		else {
			optSystemOptionStorage.setBoolean(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
			return true;
		}
	}
	
	private String getSettingName(ImageAnalysisBlock block, String title) {
		return block != null ?
				block.getClass().getCanonicalName() + "//" + title :
				title;
	}
	
	public double getDoubleSetting(ImageAnalysisBlock block, String title, double defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getDouble(
					getSystemOptionStorageGroup(), getSettingName(block, title),
					infoCamConfOrLateOrEearly == null ? defaultValue :
							optSystemOptionStorage.getDouble(
									getSystemOptionStorageGroupWithoutCustomInfo(), getSettingName(block, title), defaultValue));
	}
	
	public int getIntSetting(ImageAnalysisBlock block, String title, int defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getInteger(
					getSystemOptionStorageGroup(), getSettingName(block, title),
					infoCamConfOrLateOrEearly == null ? defaultValue :
							optSystemOptionStorage.getInteger(
									getSystemOptionStorageGroupWithoutCustomInfo(), getSettingName(block, title), defaultValue));
	}
	
	public void setIntSetting(ImageAnalysisBlock block, String title, int value) {
		if (optSystemOptionStorage == null)
			return;
		else
			optSystemOptionStorage.setInteger(
					getSystemOptionStorageGroup(), getSettingName(block, title), value);
	}
	
	public String getStringSetting(ImageAnalysisBlock block, String title, String defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getString(
					getSystemOptionStorageGroup(), getSettingName(block, title),
					infoCamConfOrLateOrEearly == null ? defaultValue :
							optSystemOptionStorage.getString(
									getSystemOptionStorageGroupWithoutCustomInfo(), getSettingName(block, title), defaultValue));
	}
	
	public String getStringSettingRadio(ImageAnalysisBlock block, String title, String defaultValue, ArrayList<String> possibleValues) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getStringRadioSelection(
					getSystemOptionStorageGroup(), getSettingName(block, title), possibleValues,
					infoCamConfOrLateOrEearly == null ? defaultValue :
							optSystemOptionStorage.getStringRadioSelection(
									getSystemOptionStorageGroupWithoutCustomInfo(), getSettingName(block, title), possibleValues,
									defaultValue, true),
					true);
	}
	
	public Color getColorSetting(ImageAnalysisBlock block, String title, Color defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getColor(
					getSystemOptionStorageGroup(), getSettingName(block, title),
					infoCamConfOrLateOrEearly == null ? defaultValue :
							optSystemOptionStorage.getColor(getSystemOptionStorageGroupWithoutCustomInfo(),
									getSettingName(block, title), defaultValue));
	}
	
	public void setColorSetting(ImageAnalysisBlock block, String title, Color value) {
		if (optSystemOptionStorage == null)
			;
		else
			optSystemOptionStorage.setColor(
					getSystemOptionStorageGroup(), getSettingName(block, title), value);
	}
	
	public Integer[] getIntArraySetting(ImageAnalysisBlock block, String title, Integer[] defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getIntArray(
					getSystemOptionStorageGroup(), getSettingName(block, title),
					infoCamConfOrLateOrEearly == null ? defaultValue :
							optSystemOptionStorage.getIntArray(
									getSystemOptionStorageGroupWithoutCustomInfo(), getSettingName(block, title), defaultValue));
	}
	
	Double calculatedBlueMarkerDistance = null;
	
	private String configAndAngle;
	
	public boolean forceDebugStack;
	public ArrayList<ImageStack> forcedDebugStacks;
	
	public DatabaseTarget databaseTarget;
	
	private final int[] leftShiftX = new int[CameraType.values().length];
	
	private final int[] topShiftY = new int[CameraType.values().length];
	
	private final int[] imageCenterX = new int[CameraType.values().length];
	
	private final int[] imageCenterY = new int[CameraType.values().length];
	
	public Double getCalculatedBlueMarkerDistance() {
		return calculatedBlueMarkerDistance;
	}
	
	public void setCalculatedBlueMarkerDistance(double maxDist) {
		this.calculatedBlueMarkerDistance = maxDist;
	}
	
	public Double getREAL_MARKER_DISTANCE() {
		Double realDist = getDoubleSetting(null, "Real Blue Marker Distance", 1350);
		if (realDist < 0)
			return null;
		else
			return realDist;
	}
	
	public TreeMap<Long, ArrayList<BlockResultValue>> searchPreviousResults(String string,
			boolean exact, int well, String valid_config, boolean removeReturnedValue) {
		if (valid_config == null) {
			throw new IllegalArgumentException("valid_config must not be null!");
		}
		TreeMap<Long, ArrayList<BlockResultValue>> res = new TreeMap<Long, ArrayList<BlockResultValue>>();
		synchronized (plantResults) {
			for (Long time : plantResults.keySet()) {
				HashMap<String, ArrayList<BlockResultValue>> r = searchResultsOfCurrentSnapshot(string, exact, well, valid_config, removeReturnedValue,
						plantResults.get(time));
				for (String k : r.keySet()) {
					res.put(time, r.get(k));
				}
			}
		}
		return res;
	}
	
	/**
	 * @param valid_config
	 *           - can be null to get all results (all angles).
	 * @param removeReturnedValue
	 * @param optPreviousResult
	 * @return config -> result list
	 */
	public HashMap<String, ArrayList<BlockResultValue>> searchResultsOfCurrentSnapshot(String string,
			boolean exact, int valid_well, String valid_config, boolean removeReturnedValue, TreeMap<String, HashMap<Integer, BlockResultSet>> optPreviousResult) {
		HashMap<String, ArrayList<BlockResultValue>> res = new HashMap<String, ArrayList<BlockResultValue>>();
		TreeMap<String, HashMap<Integer, BlockResultSet>> ds = optPreviousResult;
		if (ds == null)
			ds = previousResultsForThisTimePoint;
		if (ds != null) {
			synchronized (ds) {
				for (String config : ds.keySet()) {
					if (valid_config != null && !valid_config.equals(config))
						continue;
					HashMap<Integer, BlockResultSet> rs = ds.get(config);
					if (rs != null && !rs.isEmpty()) {
						for (Integer well : rs.keySet()) {
							if (well != valid_well)
								continue;
							ArrayList<BlockResultValue> v = rs.get(well).searchResults(exact, string, removeReturnedValue);
							if (v != null)
								res.put(config, v);
						}
					}
				}
			}
		}
		return res;
	}
	
	public Double getCameraAngle() {
		return cameraAngle;
	}
	
	public void setConfigAndAngle(String configAndAngle) {
		this.configAndAngle = configAndAngle;
		
	}
	
	public String getConfigAndAngle() {
		return configAndAngle;
	}
	
	public void shiftImage(int leftShiftX, int topShiftY, CameraType cameraType) {
		this.leftShiftX[cameraType.ordinal()] += leftShiftX;
		this.topShiftY[cameraType.ordinal()] += topShiftY;
	}
	
	public int getLeftShiftX(CameraType ct) {
		return leftShiftX[ct.ordinal()];
	}
	
	public int getTopShiftY(CameraType ct) {
		return topShiftY[ct.ordinal()];
	}
	
	public void setImageCenter(int width, int height, CameraType ct) {
		this.imageCenterX[ct.ordinal()] = width;
		this.imageCenterY[ct.ordinal()] = height;
	}
	
	public int getCenterX(CameraType ct) {
		return imageCenterX[ct.ordinal()];
	}
	
	public int getCenterY(CameraType ct) {
		return imageCenterY[ct.ordinal()];
	}
}
