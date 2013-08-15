package org;

public interface MeasurementFilter {
	boolean filterOut(String plantId, Integer day);
	
	boolean isGlobalOutlierOrSpecificOutlier(Object measurement);
}
