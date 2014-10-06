package de.ipk.ag_ba.image.operations.blocks.properties;

import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashMap;

public enum PropertyNames {
	RESULT_VIS_MARKER_POS_1_LEFT_X, RESULT_VIS_MARKER_POS_1_LEFT_Y, RESULT_VIS_MARKER_POS_1_RIGHT_X, RESULT_VIS_MARKER_POS_1_RIGHT_Y, RESULT_VIS_MARKER_POS_2_LEFT_X, RESULT_VIS_MARKER_POS_2_LEFT_Y,
	RESULT_VIS_MARKER_POS_3_LEFT_X, RESULT_VIS_MARKER_POS_3_LEFT_Y, RESULT_VIS_MARKER_POS_3_RIGHT_X, RESULT_VIS_MARKER_POS_3_RIGHT_Y, RESULT_VIS_MARKER_POS_2_RIGHT_X, RESULT_VIS_MARKER_POS_2_RIGHT_Y,
	
	MARKER_DISTANCE_BOTTOM_TOP_LEFT, MARKER_DISTANCE_BOTTOM_TOP_RIGHT;
	
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
	
	public String getName(CameraPosition pos) {
		switch (this) {
			case RESULT_VIS_MARKER_POS_1_LEFT_Y:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.1.left.y";
			case RESULT_VIS_MARKER_POS_1_RIGHT_Y:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.1.right.y";
				
			case RESULT_VIS_MARKER_POS_2_LEFT_Y:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.2.left.y";
			case RESULT_VIS_MARKER_POS_2_RIGHT_Y:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.2.right.y";
				
			case RESULT_VIS_MARKER_POS_3_LEFT_Y:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.3.left.y";
			case RESULT_VIS_MARKER_POS_3_RIGHT_Y:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.3.right.y";
				
			case RESULT_VIS_MARKER_POS_1_LEFT_X:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.1.left.x";
			case RESULT_VIS_MARKER_POS_1_RIGHT_X:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.1.right.x";
				
			case RESULT_VIS_MARKER_POS_2_LEFT_X:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.2.left.x";
			case RESULT_VIS_MARKER_POS_2_RIGHT_X:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.2.right.x";
				
			case RESULT_VIS_MARKER_POS_3_LEFT_X:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.3.left.x";
			case RESULT_VIS_MARKER_POS_3_RIGHT_X:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.3.right.x";
				
			case MARKER_DISTANCE_BOTTOM_TOP_LEFT:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.vertical_distance.left_1_2";
			case MARKER_DISTANCE_BOTTOM_TOP_RIGHT:
				return (pos != null ? "RESULT_" + pos + "." : "") + "vis.marker.vertical_distance.right_1_2";
		}
		return toString();
	}
	
	public String getUnit() {
		switch (this) {
			case RESULT_VIS_MARKER_POS_1_LEFT_Y:
			case RESULT_VIS_MARKER_POS_1_RIGHT_Y:
			case RESULT_VIS_MARKER_POS_2_LEFT_Y:
			case RESULT_VIS_MARKER_POS_2_RIGHT_Y:
			case RESULT_VIS_MARKER_POS_3_LEFT_Y:
			case RESULT_VIS_MARKER_POS_3_RIGHT_Y:
			case RESULT_VIS_MARKER_POS_1_LEFT_X:
			case RESULT_VIS_MARKER_POS_1_RIGHT_X:
			case RESULT_VIS_MARKER_POS_2_LEFT_X:
			case RESULT_VIS_MARKER_POS_2_RIGHT_X:
			case RESULT_VIS_MARKER_POS_3_LEFT_X:
			case RESULT_VIS_MARKER_POS_3_RIGHT_X:
				return "percent";
			case MARKER_DISTANCE_BOTTOM_TOP_LEFT:
			case MARKER_DISTANCE_BOTTOM_TOP_RIGHT:
				return "px";
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
