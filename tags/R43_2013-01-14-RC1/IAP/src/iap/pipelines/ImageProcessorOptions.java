/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package iap.pipelines;

import iap.blocks.data_structures.ImageAnalysisBlockFIS;

import java.awt.Color;

import org.SystemOptions;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;

/**
 * @author klukas
 */
public class ImageProcessorOptions {
	
	public static final double DEFAULT_MARKER_DIST = 1150d;
	
	private CameraPosition cameraTyp = CameraPosition.UNKNOWN;
	private NeighbourhoodSetting neighbourhood = NeighbourhoodSetting.NB4;
	private final int nirBackground = new Color(180, 180, 180).getRGB();
	private int tray_idx;
	private int tray_cnt;
	private int unit_test_idx;
	private int unit_test_steps;
	private SystemOptions optSystemOptionStorage;
	private String optSystemOptionStorageGroup;
	
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
	
	public void setCameraPosition(CameraPosition cameraTyp) {
		this.cameraTyp = cameraTyp;
		
	}
	
	public CameraPosition getCameraPosition() {
		return cameraTyp;
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
	
	public void setIsMaize(boolean isMaize) {
		optSystemOptionStorage.setBoolean("Pipeline", "is Maize", isMaize);
	}
	
	public boolean isMaize() {
		return optSystemOptionStorage.getBoolean("Pipeline", "is Maize", false);
	}
	
	public int getNirBackground() {
		return nirBackground;
	}
	
	public boolean isBarleyInBarleySystem() {
		return !isMaize() && !isHigherResVisCamera();
	}
	
	public void setHigherResVisCamera(boolean highResMaize) {
		optSystemOptionStorage.setBoolean("Pipeline", "is Vis Cam Higher Res Than Fluo", highResMaize);
	}
	
	public boolean isHigherResVisCamera() {
		return optSystemOptionStorage.getBoolean("Pipeline", "is Vis Cam Higher Res Than Fluo", false);
	}
	
	public void setIsBarley(boolean isBarley) {
		optSystemOptionStorage.setBoolean("Pipeline", "is Barley", isBarley);
	}
	
	public boolean isBarley() {
		return optSystemOptionStorage.getBoolean("Pipeline", "is Barley", false);
	}
	
	public void setIsArabidopsis(boolean isArabidopsis) {
		optSystemOptionStorage.setBoolean("Pipeline", "is Arabidopsis", isArabidopsis);
	}
	
	public boolean isArabidopsis() {
		return optSystemOptionStorage.getBoolean("Pipeline", "is Arabidopsis", false);
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
	
	public void setSystemOptionStorage(SystemOptions systemOptionStorage, String systemOptionStorageGroup) {
		this.setOptSystemOptionStorage(systemOptionStorage);
		this.setOptSystemOptionStorageGroup(systemOptionStorageGroup);
	}
	
	private String getOptSystemOptionStorageGroup() {
		return "Block Properties - " + getCameraPosition();
	}
	
	private void setOptSystemOptionStorageGroup(String optSystemOptionStorageGroup) {
		this.optSystemOptionStorageGroup = optSystemOptionStorageGroup;
	}
	
	private void setOptSystemOptionStorage(SystemOptions optSystemOptionStorage) {
		this.optSystemOptionStorage = optSystemOptionStorage;
	}
	
	public boolean getBooleanSetting(ImageAnalysisBlockFIS block, String title, boolean defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getBoolean(
					getOptSystemOptionStorageGroup(),
					block != null ?
							block.getClass().getCanonicalName() + "//" + title :
							title,
					defaultValue);
	}
	
	public double getDoubleSetting(ImageAnalysisBlockFIS block, String title, double defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getDouble(
					getOptSystemOptionStorageGroup(),
					block != null ?
							block.getClass().getCanonicalName() + "//" + title :
							title,
					defaultValue);
	}
	
	public int getIntSetting(ImageAnalysisBlockFIS block, String title, int defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getInteger(
					getOptSystemOptionStorageGroup(),
					block != null ?
							block.getClass().getCanonicalName() + "//" + title :
							title,
					defaultValue);
	}
	
	public String getStringSetting(ImageAnalysisBlockFIS block, String title, String defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getString(
					getOptSystemOptionStorageGroup(),
					block != null ?
							block.getClass().getCanonicalName() + "//" + title :
							title,
					defaultValue);
	}
	
	public Integer[] getIntArraySetting(ImageAnalysisBlockFIS block, String title, Integer[] defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getIntArray(
					getOptSystemOptionStorageGroup(),
					block != null ?
							block.getClass().getCanonicalName() + "//" + title :
							title,
					defaultValue);
	}
	
	Double calculatedBlueMarkerDistance = null;
	
	public Double getCalculatedBlueMarkerDistance() {
		return calculatedBlueMarkerDistance;
	}
	
	public void setCalculatedBlueMarkerDistance(double maxDist) {
		this.calculatedBlueMarkerDistance = maxDist;
	}
	
	public Double getREAL_MARKER_DISTANCE() {
		Double realDist = getDoubleSetting(null, "Real Blue Marker Distance", -1);
		if (realDist < 0)
			return null;
		else
			return realDist;
	}
	
}
