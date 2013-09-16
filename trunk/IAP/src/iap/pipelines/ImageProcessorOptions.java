/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package iap.pipelines;

import iap.blocks.data_structures.ImageAnalysisBlock;

import java.awt.Color;

import org.SystemOptions;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;

/**
 * @author klukas
 */
public class ImageProcessorOptions {
	
	public static final double DEFAULT_MARKER_DIST = 1150d;
	
	private CameraPosition cameraPosition = CameraPosition.UNKNOWN;
	private NeighbourhoodSetting neighbourhood = NeighbourhoodSetting.NB4;
	private final int nirBackground = new Color(180, 180, 180).getRGB();
	private int tray_idx;
	private int tray_cnt;
	private int unit_test_idx;
	private int unit_test_steps;
	private SystemOptions optSystemOptionStorage;
	
	private String inf;
	
	private String customNullBlockPrefix;
	
	public ImageProcessorOptions(SystemOptions options) {
		this.optSystemOptionStorage = options;
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
	
	public void setCameraInfos(CameraPosition cameraTyp, String cameraConfig, String lateOrEarly) {
		this.cameraPosition = cameraTyp;
		
		if (lateOrEarly == null && cameraConfig == null)
			inf = null;
		else
			if (lateOrEarly != null && cameraConfig == null)
				inf = " (" + lateOrEarly + ")";
			else
				if (cameraConfig != null && lateOrEarly == null)
					inf = " for " + cameraConfig;
				else
					inf = " for " + cameraConfig + " (" + lateOrEarly + ")";
		
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
	
	public void setTrayCnt(int tray_idx, int tray_cnt) {
		this.tray_idx = tray_idx;
		this.tray_cnt = tray_cnt;
	}
	
	public int getTrayCnt() {
		return tray_cnt;
	}
	
	public int getTrayIdx() {
		return tray_idx;
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
		if (inf != null) {
			if (getCameraPosition() != CameraPosition.UNKNOWN)
				return getCameraPosition() + " settings" + inf;
			else
				return (customNullBlockPrefix != null ? customNullBlockPrefix : "Postprocessing") + inf;
		} else {
			if (getCameraPosition() != CameraPosition.UNKNOWN)
				return getCameraPosition() + " settings";
			else
				return (customNullBlockPrefix != null ? customNullBlockPrefix : "Postprocessing");
		}
	}
	
	private void setOptSystemOptionStorage(SystemOptions optSystemOptionStorage) {
		this.optSystemOptionStorage = optSystemOptionStorage;
	}
	
	public boolean getBooleanSetting(ImageAnalysisBlock block, String title, boolean defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getBoolean(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
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
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	public int getIntSetting(ImageAnalysisBlock block, String title, int defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getInteger(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	public String getStringSetting(ImageAnalysisBlock block, String title, String defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getString(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	public Color getColorSetting(ImageAnalysisBlock block, String title, Color defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getColor(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	public Integer[] getIntArraySetting(ImageAnalysisBlock block, String title, Integer[] defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getIntArray(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	Double calculatedBlueMarkerDistance = null;
	
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
	
}
