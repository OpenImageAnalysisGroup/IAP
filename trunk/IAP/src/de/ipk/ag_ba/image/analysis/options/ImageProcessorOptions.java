/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image.analysis.options;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;

import org.SystemOptions;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.ImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;

/**
 * @author klukas, entzian
 */
public class ImageProcessorOptions {
	
	private final HashMap<Setting, LinkedList<Double>> numericSettings = new HashMap<ImageProcessorOptions.Setting, LinkedList<Double>>();
	
	public enum Setting {
		SCALE_FACTOR_DECREASE_MASK, SCALE_FACTOR_DECREASE_IMG_AND_MASK,

		L_Diff_VIS_TOP, abDiff_VIS_TOP, L_Diff_FLUO, abDiff_FLUO, B_Diff_NIR, W_Diff_NIR, B_Diff_NIR_TOP, W_Diff_NIR_TOP,

		L_Diff_VIS_SIDE, abDiff_VIS_SIDE,

		BOTTOM_CUT_OFFSET_VIS, REAL_MARKER_DISTANCE;
	}
	
	private CameraPosition cameraTyp = CameraPosition.UNKNOWN;
	private NeighbourhoodSetting neighbourhood = NeighbourhoodSetting.NB4;
	private final int nirBackground = new Color(180, 180, 180).getRGB();
	private boolean higherResVisCamera;
	private boolean isMaize;
	private boolean isBarley;
	private boolean isArabidopsis;
	private int tray_idx;
	private int tray_cnt;
	private int unit_test_idx;
	private int unit_test_steps;
	private SystemOptions optSystemOptionStorage;
	private String optSystemOptionStorageGroup;
	
	public ImageProcessorOptions() {
		this(1.0);
	}
	
	public ImageProcessorOptions(double scale) {
		initStandardValues(scale);
	}
	
	public void addIntSetting(Setting s, int value) {
		addDoubleSetting(s, value);
	}
	
	public void addBooleanSetting(Setting s, boolean value) {
		if (value)
			addDoubleSetting(s, 1.0);
		else
			addDoubleSetting(s, -1.0);
	}
	
	public double showNextValue(Setting s) {
		return numericSettings.get(s).peek();
	}
	
	public void addDoubleSetting(Setting s, double value) {
		if (!numericSettings.containsKey(s))
			numericSettings.put(s, new LinkedList<Double>());
		// doubleOptions.put(s, new Stack<Double>());
		numericSettings.get(s).addLast(value);
	}
	
	public void showLinkedList(Setting s) {
		System.out.println("LinkedList: ");
		int i = 1;
		while (numericSettings.get(s).isEmpty()) {
			System.out.println(i + ". TopElement: " + numericSettings.get(s).pop());
			i++;
		}
	}
	
	public boolean getBooleanSetting(Setting s) {
		return (getDoubleSetting(s) > 0) ? true : false;
	}
	
	public Integer getIntSetting(Setting s) {
		return getDoubleSetting(s) != null ? getDoubleSetting(s).intValue() : null;
	}
	
	public boolean hasDoubleSetting(Setting s) {
		return numericSettings.containsKey(s);
	}
	
	public Double getDoubleSetting(Setting s) {
		if (numericSettings.get(s).size() > 1)
			return numericSettings.get(s).pollFirst();
		// return doubleOptions.get(s).pop();
		
		else
			return numericSettings.get(s).peek();
	}
	
	public void clearSetting(Setting s) {
		if (numericSettings.containsKey(s)) {
			numericSettings.remove(s);
		}
	}
	
	public void clearAndAddIntSetting(Setting s, int value) {
		clearSetting(s);
		
		if (getOptSystemOptionStorage() != null && getOptSystemOptionStorageGroup() != null) {
			value = getOptSystemOptionStorage().getInteger(getOptSystemOptionStorageGroup(), s.name(), value);
		}
		
		addIntSetting(s, value);
	}
	
	public void clearAndAddDoubleSetting(Setting s, double value) {
		clearSetting(s);
		
		if (getOptSystemOptionStorage() != null && getOptSystemOptionStorageGroup() != null) {
			value = getOptSystemOptionStorage().getDouble(getOptSystemOptionStorageGroup(), s.name(), value);
		}
		
		addDoubleSetting(s, value);
	}
	
	public void clearAndAddBooleanSetting(Setting s, boolean value) {
		clearSetting(s);
		
		if (getOptSystemOptionStorage() != null && getOptSystemOptionStorageGroup() != null) {
			value = getOptSystemOptionStorage().getBoolean(getOptSystemOptionStorageGroup(), s.name(), value);
		}
		
		addBooleanSetting(s, value);
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
			return "unknown_camera_pos";
		}
	}
	
	public void initStandardValues(double scale) {
		
		// addIntSetting(Setting.L_Diff_VIS_SIDE, 20); // 40
		// addIntSetting(Setting.abDiff_VIS_SIDE, 20); // 40
		//
		// addIntSetting(Setting.L_Diff_VIS_TOP, 40); // 40
		// addIntSetting(Setting.abDiff_VIS_TOP, 40); // 40
		// addIntSetting(Setting.L_Diff_FLUO, 75);// 20
		// addIntSetting(Setting.abDiff_FLUO, 40);// 30
		// addIntSetting(Setting.B_Diff_NIR, 14); // 14
		// addIntSetting(Setting.W_Diff_NIR, 20);
		// addIntSetting(Setting.B_Diff_NIR_TOP, 14);
		// addIntSetting(Setting.W_Diff_NIR_TOP, 20);
		
		// addDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK, 1);
		// addDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK, 1);
		
		addIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS, 30);
		addIntSetting(Setting.REAL_MARKER_DISTANCE, 1128);
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
		this.isMaize = isMaize;
	}
	
	public boolean isMaize() {
		return isMaize;
	}
	
	public int getNirBackground() {
		return nirBackground;
	}
	
	public boolean isBarleyInBarleySystem() {
		return !isMaize() && !isHigherResVisCamera();
	}
	
	public void setHigherResVisCamera(boolean highResMaize) {
		this.higherResVisCamera = highResMaize;
	}
	
	public boolean isHigherResVisCamera() {
		return higherResVisCamera;
	}
	
	public void setIsBarley(boolean isBarley) {
		this.isBarley = isBarley;
	}
	
	public void setIsArabidopsis(boolean isArabidopsis) {
		this.isArabidopsis = isArabidopsis;
	}
	
	public boolean isArabidopsis() {
		return isArabidopsis;
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
	
	public boolean isBarley() {
		return isBarley;
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
		if (getCameraPosition() == CameraPosition.UNKNOWN)
			return null;
		return optSystemOptionStorageGroup + "-" + getCameraPosition();
	}
	
	private void setOptSystemOptionStorageGroup(String optSystemOptionStorageGroup) {
		this.optSystemOptionStorageGroup = optSystemOptionStorageGroup;
	}
	
	private SystemOptions getOptSystemOptionStorage() {
		if (getCameraPosition() == CameraPosition.UNKNOWN)
			return null;
		return optSystemOptionStorage;
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
					block.getClass().getCanonicalName() + "//" + title,
					defaultValue);
	}
	
	public double getDoubleSetting(ImageAnalysisBlockFIS block, String title, double defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getDouble(
					getOptSystemOptionStorageGroup(),
					block.getClass().getCanonicalName() + "//" + title,
					defaultValue);
	}
	
	public int getIntSetting(ImageAnalysisBlockFIS block, String title, int defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getInteger(
					getOptSystemOptionStorageGroup(),
					block.getClass().getCanonicalName() + "//" + title,
					defaultValue);
	}
	
	public String getStringSetting(ImageAnalysisBlockFIS block, String title, String defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getString(
					getOptSystemOptionStorageGroup(),
					block.getClass().getCanonicalName() + "//" + title,
					defaultValue);
	}
	
	public Integer[] getIntArraySetting(ImageAnalysisBlockFIS block, String title, Integer[] defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getIntArray(
					getOptSystemOptionStorageGroup(),
					block.getClass().getCanonicalName() + "//" + title,
					defaultValue);
	}
}
