package de.ipk.ag_ba.image.operation;

public enum DistanceMapFloatMode {
	X, Y, DIST, ANGLE_DEGREE_m180_p180;
	
	public static DistanceMapFloatMode fromInt(int mode) {
		if (mode == 0)
			return X;
		if (mode == 1)
			return Y;
		if (mode == 2)
			return DIST;
		if (mode == 3)
			return ANGLE_DEGREE_m180_p180;
		
		throw new UnsupportedOperationException("Error");
	}
}
