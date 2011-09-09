/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image.analysis.gernally;

import ij.plugin.filter.MaximumFinder;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;

/**
 * First: add a enumeration which describe the descriptor
 * Second: add the new Value to initStandardValues() -> Attention: choose the right method (add...Setting())
 * 
 * @author klukas, entzian
 */
public class ImageProcessorOptions {
	
	private final HashMap<Setting, LinkedList<Double>> numericSettings = new HashMap<ImageProcessorOptions.Setting, LinkedList<Double>>();
	private CameraPosition cameraTyp;
	private NeighbourhoodSetting neighbourhood;
	
	public enum Setting {
		TRANSLATE_X, TRANSLATE_Y,
		RGB_SIDE_NUMBER_OF_ERODE_LOOPS, RGB_SIDE_NUMBER_OF_DILATE_LOOPS,
		FLUO_SIDE_NUMBER_OF_ERODE_LOOPS, FLUO_SIDE_NUMBER_OF_DILATE_LOOPS,
		NIR_SIDE_NUMBER_OF_ERODE_LOOPS, NIR_SIDE_NUMBER_OF_DILATE_LOOPS,
		RGB_TOP_NUMBER_OF_ERODE_LOOPS, RGB_TOP_NUMBER_OF_DILATE_LOOPS,
		FLUO_TOP_NUMBER_OF_ERODE_LOOPS, FLUO_TOP_NUMBER_OF_DILATE_LOOPS,
		NIR_TOP_NUMBER_OF_ERODE_LOOPS, NIR_TOP_NUMBER_OF_DILATE_LOOPS,
		DILATE_RGB_TOP, ERODE_RGB_TOP,
		DILATE_FLUO_TOP, ERODE_FLUO_TOP,
		DILATE_NIR_TOP, ERODE_NIR_TOP,
		DILATE_RGB_SIDE, ERODE_RGB_SIDE,
		DILATE_FLUO_SIDE, ERODE_FLUO_SIDE,
		DILATE_NIR_SIDE, ERODE_NIR_SIDE,
		POST_PROCESS_DILATE_RGB_TOP, POST_PROCESS_ERODE_RGB_TOP,
		POST_PROCESS_DILATE_FLUO_TOP, POST_PROCESS_ERODE_FLUO_TOP,
		POST_PROCESS_DILATE_NIR_TOP, POST_PROCESS_ERODE_NIR_TOP,
		POST_PROCESS_DILATE_RGB_SIDE, POST_PROCESS_ERODE_RGB_SIDE,
		POST_PROCESS_DILATE_FLUO_SIDE, POST_PROCESS_ERODE_FLUO_SIDE,
		POST_PROCESS_DILATE_NIR_SIDE, POST_PROCESS_ERODE_NIR_SIDE,
		CLOSING_REPEAT,
		CLOSING_NIR_TOP, CLOSING_NIR_SIDE,
		DEBUG_STACK_WIDTH, MAX_THREADS_PER_IMAGE,
		FIND_MAXIMUM_TYP,

		// double
		SCALE, ROTATION_ANGLE, SCALE_X, SCALE_Y, FLUO_EPSILON_A, FLUO_EPSILON_B, RGB_EPSILON_A, RGB_EPSILON_B, NIR_EPSILON_A, NIR_EPSILON_B, FIND_MAXIMUM_TOLERANCE,

		// boolean
		PROCESS_NIR, DEBUG_TAKE_TIMES, DEBUG_OVERLAY_RESULT_IMAGE, IS_DEBUG_PRINT_EACH_STEP, IS_DEBUG_VIS, IS_DEBUG_FLUO, IS_DEBUG_NIR, REMOVE_SMALL_CLUSTER_SIZE_FLUO,
		REMOVE_SMALL_CLUSTER_SIZE_VIS,

		LAB_MIN_L_VALUE_VIS, LAB_MAX_L_VALUE_VIS, LAB_MIN_A_VALUE_VIS, LAB_MAX_A_VALUE_VIS, LAB_MIN_B_VALUE_VIS, LAB_MAX_B_VALUE_VIS, LAB_MIN_L_VALUE_FLUO,
		LAB_MAX_L_VALUE_FLUO, LAB_MIN_A_VALUE_FLUO, LAB_MAX_A_VALUE_FLUO, LAB_MIN_B_VALUE_FLUO, LAB_MAX_B_VALUE_FLUO, LAB_MIN_L_VALUE_NIR, LAB_MAX_L_VALUE_NIR,
		LAB_MIN_A_VALUE_NIR, LAB_MAX_A_VALUE_NIR, LAB_MIN_B_VALUE_NIR, LAB_MAX_B_VALUE_NIR,

		L_Diff_VIS_TOP, abDiff_VIS_TOP, L_Diff_FLUO, abDiff_FLUO, B_Diff_NIR, W_Diff_NIR, B_Diff_NIR_TOP, W_Diff_NIR_TOP,

		IS_PARAMETER_SEARCH, IS_CROP_RESULT,

		SCALE_FACTOR_DECREASE_MASK, SCALE_FACTOR_DECREASE_IMG_AND_MASK,

		INPUT_VIS_IMAGE_ROTATION_ANGLE, L_Diff_VIS_SIDE, abDiff_VIS_SIDE,

		BOTTOM_CUT_OFFSET_VIS, REAL_MARKER_DISTANCE, DRAW_CONVEX_HULL;
	}
	
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
		addIntSetting(s, value);
	}
	
	public void clearAndAddDoubleSetting(Setting s, double value) {
		clearSetting(s);
		addDoubleSetting(s, value);
	}
	
	public void clearAndAddBooleanSetting(Setting s, boolean value) {
		clearSetting(s);
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
	
	public enum ImageTyp {
		RGB, FLUO, NIR
	}
	
	public void initStandardValues(double scale) {
		
		setCameraPosition(CameraPosition.UNKNOWN);
		setNeighbourhood(NeighbourhoodSetting.NB4);
		
		addDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS, (0.001d) / 4);
		addDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO, (0.001d) / 5);
		addDoubleSetting(Setting.SCALE, scale);
		addIntSetting(Setting.ROTATION_ANGLE, -3);
		addDoubleSetting(Setting.SCALE_X, 0.95);
		addDoubleSetting(Setting.SCALE_Y, 0.87);
		
		addIntSetting(Setting.TRANSLATE_X, 0);
		addIntSetting(Setting.TRANSLATE_Y, -15);
		
		addDoubleSetting(Setting.RGB_EPSILON_A, 2.5);
		addDoubleSetting(Setting.RGB_EPSILON_B, 2.5);
		addDoubleSetting(Setting.FLUO_EPSILON_A, 0.5);
		addDoubleSetting(Setting.FLUO_EPSILON_B, 0.5);
		addDoubleSetting(Setting.NIR_EPSILON_A, 0.5);
		addDoubleSetting(Setting.NIR_EPSILON_B, 1.0);
		
		addIntSetting(Setting.RGB_SIDE_NUMBER_OF_ERODE_LOOPS, (int) Math.ceil(2 * scale));
		addIntSetting(Setting.RGB_SIDE_NUMBER_OF_DILATE_LOOPS, (int) Math.ceil(5 * scale));
		addIntSetting(Setting.FLUO_SIDE_NUMBER_OF_ERODE_LOOPS, (int) Math.ceil(2 * scale));
		addIntSetting(Setting.FLUO_SIDE_NUMBER_OF_DILATE_LOOPS, (int) Math.ceil(5 * scale));
		addIntSetting(Setting.NIR_SIDE_NUMBER_OF_ERODE_LOOPS, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.NIR_SIDE_NUMBER_OF_DILATE_LOOPS, (int) Math.ceil(2 * scale));
		
		addIntSetting(Setting.RGB_TOP_NUMBER_OF_ERODE_LOOPS, (int) Math.ceil(2 * scale));
		addIntSetting(Setting.RGB_TOP_NUMBER_OF_DILATE_LOOPS, (int) Math.ceil(5 * scale));
		addIntSetting(Setting.FLUO_TOP_NUMBER_OF_ERODE_LOOPS, (int) Math.ceil(2 * scale));
		addIntSetting(Setting.FLUO_TOP_NUMBER_OF_DILATE_LOOPS, (int) Math.ceil(5 * scale));
		addIntSetting(Setting.NIR_TOP_NUMBER_OF_ERODE_LOOPS, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.NIR_TOP_NUMBER_OF_DILATE_LOOPS, (int) Math.ceil(2 * scale));
		
		addIntSetting(Setting.DILATE_RGB_SIDE, (int) Math.ceil(3 * scale));
		addIntSetting(Setting.ERODE_RGB_SIDE, (int) Math.ceil(2 * scale));
		addIntSetting(Setting.DILATE_FLUO_SIDE, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.ERODE_FLUO_SIDE, (int) Math.ceil(1 * scale));
		
		addIntSetting(Setting.DILATE_RGB_TOP, (int) Math.ceil(10 * scale));
		addIntSetting(Setting.ERODE_RGB_TOP, (int) Math.ceil(7 * scale));
		addIntSetting(Setting.DILATE_FLUO_TOP, (int) Math.ceil(2 * scale));
		addIntSetting(Setting.ERODE_FLUO_TOP, (int) Math.ceil(1 * scale));
		
		addIntSetting(Setting.CLOSING_NIR_TOP, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.CLOSING_NIR_SIDE, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.CLOSING_REPEAT, 1);
		
		addIntSetting(Setting.POST_PROCESS_DILATE_RGB_TOP, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_DILATE_FLUO_TOP, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_DILATE_NIR_TOP, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_ERODE_RGB_TOP, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_ERODE_FLUO_TOP, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_ERODE_NIR_TOP, (int) Math.ceil(1 * scale));
		
		addIntSetting(Setting.POST_PROCESS_DILATE_RGB_SIDE, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_DILATE_FLUO_SIDE, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_DILATE_NIR_SIDE, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_ERODE_RGB_SIDE, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_ERODE_FLUO_SIDE, (int) Math.ceil(1 * scale));
		addIntSetting(Setting.POST_PROCESS_ERODE_NIR_SIDE, (int) Math.ceil(1 * scale));
		
		addBooleanSetting(Setting.PROCESS_NIR, false);
		addBooleanSetting(Setting.DEBUG_TAKE_TIMES, false);
		addBooleanSetting(Setting.DEBUG_OVERLAY_RESULT_IMAGE, false);
		addBooleanSetting(Setting.IS_DEBUG_PRINT_EACH_STEP, false);
		addBooleanSetting(Setting.IS_DEBUG_VIS, false);
		addBooleanSetting(Setting.IS_DEBUG_FLUO, false);
		addBooleanSetting(Setting.IS_DEBUG_NIR, false);
		
		addIntSetting(Setting.DEBUG_STACK_WIDTH, 1680);
		addIntSetting(Setting.MAX_THREADS_PER_IMAGE, 2);
		
		addDoubleSetting(Setting.FIND_MAXIMUM_TOLERANCE, 50.0);
		addIntSetting(Setting.FIND_MAXIMUM_TYP, MaximumFinder.COUNT);
		
		addIntSetting(Setting.LAB_MIN_L_VALUE_VIS, 0);
		addIntSetting(Setting.LAB_MAX_L_VALUE_VIS, 255);
		addIntSetting(Setting.LAB_MIN_A_VALUE_VIS, 0);
		addIntSetting(Setting.LAB_MAX_A_VALUE_VIS, 255);
		addIntSetting(Setting.LAB_MIN_B_VALUE_VIS, 0);
		addIntSetting(Setting.LAB_MAX_B_VALUE_VIS, 255);
		
		addIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 0);
		addIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
		addIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 0);
		addIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);
		addIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 0);
		addIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		
		addIntSetting(Setting.LAB_MIN_L_VALUE_NIR, 0);
		addIntSetting(Setting.LAB_MAX_L_VALUE_NIR, 255);
		addIntSetting(Setting.LAB_MIN_A_VALUE_NIR, 0);
		addIntSetting(Setting.LAB_MAX_A_VALUE_NIR, 255);
		addIntSetting(Setting.LAB_MIN_B_VALUE_NIR, 0);
		addIntSetting(Setting.LAB_MAX_B_VALUE_NIR, 255);
		
		addIntSetting(Setting.L_Diff_VIS_SIDE, 20); // 40
		addIntSetting(Setting.abDiff_VIS_SIDE, 20); // 40
		
		addIntSetting(Setting.L_Diff_VIS_TOP, 40); // 40
		addIntSetting(Setting.abDiff_VIS_TOP, 40); // 40
		addIntSetting(Setting.L_Diff_FLUO, 75);// 20
		addIntSetting(Setting.abDiff_FLUO, 40);// 30
		addIntSetting(Setting.B_Diff_NIR, 14); // 14
		addIntSetting(Setting.W_Diff_NIR, 20);
		addIntSetting(Setting.B_Diff_NIR_TOP, 14);
		addIntSetting(Setting.W_Diff_NIR_TOP, 20);
		
		addBooleanSetting(Setting.IS_PARAMETER_SEARCH, false);
		addBooleanSetting(Setting.IS_CROP_RESULT, false);
		
		addDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK, 1);
		addDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK, 1);
		
		addIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS, 80);
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
	
	private Boolean isMaize = null;
	private int nirBackground = new Color(180, 180, 180).getRGB();
	
	public void setIsMaize(boolean isMaize) {
		this.isMaize = isMaize;
	}
	
	public boolean isMaize() {
		if (isMaize == null)
			throw new UnsupportedOperationException("The isMaize Setting needs to be parameterized for each pipeline.");
		return isMaize;
	}
	
	public int getNirBackground() {
		return nirBackground;
	}
}
