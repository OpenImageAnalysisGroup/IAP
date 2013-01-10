package de.ipk.ag_ba.image.operations.blocks.properties;

import java.util.HashMap;

public enum PropertyNames {
	TRANSLATION_FLUO_X, TRANSLATION_FLUO_Y, TRANSLATION_NIR_X, TRANSLATION_NIR_Y, ROTATION_FLUO,
	ROTATION_NIR, SCALING_FLUO_X, SCALING_FLUO_Y, SCALING_NIR_X, SCALING_NIR_Y, HEIGHT_FLUO_IMAGE,
	HEIGHT_FLUO_MASK, HEIGHT_NIR_IMAGE, HEIGHT_NIR_MASK, HEIGHT_VIS_IMAGE, HEIGHT_VIS_MASK, WIDTH_FLUO_IMAGE,
	WIDTH_FLUO_MASK, WIDTH_NIR_IMAGE, WIDTH_NIR_MASK, WIDTH_VIS_IMAGE, WIDTH_VIS_MASK, END_HEIGHT_FLUO_IMAGE,
	END_WIDTH_FLUO_IMAGE, END_HEIGHT_FLUO_MASK, END_WIDTH_FLUO_MASK, END_HEIGHT_NIR_IMAGE, END_WIDTH_NIR_IMAGE,
	END_HEIGHT_NIR_MASK, END_WIDTH_NIR_MASK, END_HEIGHT_VIS_IMAGE, END_WIDTH_VIS_IMAGE, END_HEIGHT_VIS_MASK, END_WIDTH_VIS_MASK,
	
	// VIS_MARKER_POS_LEFT_1, VIS_MARKER_POS_LEFT_2, VIS_MARKER_POS_LEFT_3,
	// VIS_MARKER_POS_RIGHT_1, VIS_MARKER_POS_RIGHT_2, VIS_MARKER_POS_RIGHT_3,
	
	RESULT_MAXIMUM_SEARCH_COUNT,
	RESULT_VIS_MARKER_POS_1_LEFT_X, RESULT_VIS_MARKER_POS_1_LEFT_Y, RESULT_VIS_MARKER_POS_1_RIGHT_X, RESULT_VIS_MARKER_POS_1_RIGHT_Y, RESULT_VIS_MARKER_POS_2_LEFT_X, RESULT_VIS_MARKER_POS_2_LEFT_Y,
	RESULT_VIS_MARKER_POS_3_LEFT_X, RESULT_VIS_MARKER_POS_3_LEFT_Y, RESULT_VIS_MARKER_POS_3_RIGHT_X, RESULT_VIS_MARKER_POS_3_RIGHT_Y, RESULT_VIS_MARKER_POS_2_RIGHT_X, RESULT_VIS_MARKER_POS_2_RIGHT_Y,
	
	COUNT_PIXEL_VIS, COUNT_PIXEL_FLUO, COUNT_PIXEL_NIR, RESULT_TOP_MAIN_AXIS_ROTATION, RESULT_TOP_MAIN_AXIS_NORMALIZED_DISTANCE,
	
	CENTROID_X, CENTROID_Y,
	
	RESULT_TOP_WIDTH, RESULT_TOP_HEIGHT, RESULT_SIDE_WIDTH, RESULT_SIDE_HEIGHT,
	
	MARKER_DISTANCE_BOTTOM_TOP_LEFT, MARKER_DISTANCE_BOTTOM_TOP_RIGHT,
	
	INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, INTERNAL_CROP_BOTTOM_POT_POSITION_NIR;
	
	// public boolean storeAsResult() {
	// switch (this) {
	// case VIS_MARKER_POS_LEFT_1:
	// case VIS_MARKER_POS_LEFT_2:
	// case VIS_MARKER_POS_LEFT_3:
	// case VIS_MARKER_POS_RIGHT_1:
	// case VIS_MARKER_POS_RIGHT_2:
	// case VIS_MARKER_POS_RIGHT_3:
	// return true;
	//
	// default:
	// return false;
	// }
	// }
	//
	
	public static PropertyNames getMarkerPropertyNameFromIndex(int i) {
		
		switch (i) {
			case 1:
				return RESULT_VIS_MARKER_POS_1_LEFT_X;
			case 2:
				return RESULT_VIS_MARKER_POS_1_LEFT_Y;
			case 3:
				return RESULT_VIS_MARKER_POS_1_RIGHT_X;
			case 4:
				return RESULT_VIS_MARKER_POS_1_RIGHT_Y;
			case 5:
				return RESULT_VIS_MARKER_POS_2_LEFT_X;
			case 6:
				return RESULT_VIS_MARKER_POS_2_LEFT_Y;
			case 7:
				return RESULT_VIS_MARKER_POS_2_RIGHT_X;
			case 8:
				return RESULT_VIS_MARKER_POS_2_RIGHT_Y;
			case 9:
				return RESULT_VIS_MARKER_POS_3_LEFT_X;
			case 10:
				return RESULT_VIS_MARKER_POS_3_LEFT_Y;
			case 11:
				return RESULT_VIS_MARKER_POS_3_RIGHT_X;
			case 12:
				return RESULT_VIS_MARKER_POS_3_RIGHT_Y;
				
		}
		
		return null;
		
	}
	
	public String getName() {
		switch (this) {
			case RESULT_MAXIMUM_SEARCH_COUNT:
				return "pollen count";
			case RESULT_TOP_MAIN_AXIS_ROTATION:
				return "orientation";
				
			case RESULT_TOP_MAIN_AXIS_NORMALIZED_DISTANCE:
				return "orientation_fit";
				
			case RESULT_VIS_MARKER_POS_1_LEFT_Y:
			case RESULT_VIS_MARKER_POS_1_RIGHT_Y:
				return "mark1.y";
				
			case RESULT_VIS_MARKER_POS_2_LEFT_Y:
			case RESULT_VIS_MARKER_POS_2_RIGHT_Y:
				return "mark2.y";
				
			case RESULT_VIS_MARKER_POS_3_LEFT_Y:
			case RESULT_VIS_MARKER_POS_3_RIGHT_Y:
				return "mark3.y";
		}
		return toString();
	}
	
	public String getUnit() {
		switch (this) {
			case RESULT_MAXIMUM_SEARCH_COUNT:
				return "maxima";
			case RESULT_TOP_MAIN_AXIS_ROTATION:
				return "degree";
				
			case RESULT_TOP_MAIN_AXIS_NORMALIZED_DISTANCE:
				return "";
				
			case RESULT_VIS_MARKER_POS_1_LEFT_Y:
			case RESULT_VIS_MARKER_POS_1_RIGHT_Y:
				return "percent";
				
			case RESULT_VIS_MARKER_POS_2_LEFT_Y:
			case RESULT_VIS_MARKER_POS_2_RIGHT_Y:
				return "percent";
				
			case RESULT_VIS_MARKER_POS_3_LEFT_Y:
			case RESULT_VIS_MARKER_POS_3_RIGHT_Y:
				return "percent";
				
			case RESULT_SIDE_WIDTH:
			case RESULT_SIDE_HEIGHT:
				return "percent";
		}
		return "";
	}
	
	private static HashMap<String, PropertyNames> cache = new HashMap<String, PropertyNames>();
	
	public static PropertyNames valueOfCached(String key) {
		if (!cache.containsKey(key))
			cache.put(key, valueOf(key));
		return cache.get(key);
	}
}
