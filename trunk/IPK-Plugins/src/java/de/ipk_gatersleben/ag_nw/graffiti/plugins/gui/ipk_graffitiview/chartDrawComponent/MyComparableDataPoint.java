/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 11.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;

/**
 * @author Christian Klukas
 *         (c) 2004-2008 IPK-Gatersleben
 */
public class MyComparableDataPoint implements Comparable<Object> {
	
	public double mean;
	private double stddev;
	public String serie;
	public String timeUnitAndTime;
	public String measurementUnit;
	public int replicate;
	public int timeValueForComparision;
	public boolean ttestIsReference;
	public boolean ttestIsSignificantDifferent;
	public Integer timeValue;
	public String timeUnit;
	private final Integer seriesID;
	/**
	 * In case this Object is represents a Mean value, this number specifies the
	 * number of replicates, which where the basis for this mean value. If this
	 * no Mean value, it specifies the replicate id of the sample.
	 */
	private int replicateCount;
	public boolean isMeanValue;
	public Measurement xmlReference;
	
	private boolean outlier;
	
	@Override
	public String toString() {
		return serie + ": avg=" + mean + " " + measurementUnit + ", stddev=" + getStddev() + " " + measurementUnit
							+ ", time=" + timeUnitAndTime + ", replicate=" + replicate;
	}
	
	/**
	 * @param d
	 * @param serie
	 * @param timeUnitAndTime
	 * @param timeValueForComparision
	 * @param reference
	 */
	public MyComparableDataPoint(boolean isMeanValue, double mean, double stddev, String serie, String timeUnitAndTime,
						String measurementUnit, int timeValueForComparision, boolean ttestIsReference,
						boolean ttestIsSignificantDifferent, String timeUnit, int seriesID, int replicate, Measurement reference) {
		this.mean = mean;
		this.isMeanValue = isMeanValue;
		this.setStddev(stddev);
		this.serie = serie;
		this.seriesID = seriesID;
		this.timeUnitAndTime = timeUnitAndTime;
		this.timeUnit = timeUnit;
		this.timeValueForComparision = timeValueForComparision;
		this.timeValue = new Integer(timeValueForComparision);
		this.measurementUnit = measurementUnit;
		this.ttestIsReference = ttestIsReference;
		this.ttestIsSignificantDifferent = ttestIsSignificantDifferent;
		this.replicate = replicate;
		try {
			this.replicateCount = reference.getParentSample().size();
		} catch (NumberFormatException nfe) {
			this.replicateCount = -1;
		}
		this.outlier = false;
		this.xmlReference = reference;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object obj) {
		MyComparableDataPoint ct = (MyComparableDataPoint) obj;
		
		if (ct.timeValueForComparision < timeValueForComparision)
			return 1;
		if (ct.timeValueForComparision > timeValueForComparision)
			return -1;
		if (!isMeanValue)
			if (ct.replicateCount < replicateCount)
				return 1;
		if (!isMeanValue)
			if (ct.replicateCount > replicateCount)
				return -1;
		return seriesID.compareTo(ct.seriesID);
		// return serie.compareTo(ct.serie);
		// return 0;
	}
	
	public String getSOMcolumnDesc(boolean useSampleAverageValues) {
		if (useSampleAverageValues)
			return new String(serie + "§" + timeUnit + "§" + getZeros(timeValueForComparision, 9) + "§0");
		else
			return new String(serie + "§" + timeUnit + "§" + getZeros(timeValueForComparision, 9) + "§" + replicate);
	}
	
	private String getZeros(int val, int len) {
		boolean negative = val < 0;
		val = Math.abs(val);
		String result = "" + val;
		while (result.length() < len)
			result = "0" + result;
		if (negative)
			return "-" + result;
		else
			return result;
	}
	
	public void setStddev(double stddev) {
		this.stddev = stddev;
	}
	
	public double getStddev() {
		return stddev;
	}
	
	public double getStddev(boolean useStdErrInsteadOfStdDev) {
		if (!useStdErrInsteadOfStdDev)
			return stddev;
		else {
			if (replicateCount > 0)
				return stddev / Math.sqrt(replicateCount);
			else
				return Double.NaN;
		}
	}
	
	/**
	 * Returns the number of replicates, if this datapoint represents a
	 * mean-value datapoint. Otherwise it returns the replicateID.
	 * 
	 * @return Number of replicates, if this object represents a sample-mean
	 *         value, otherwise it returns the replicateId of the replicate
	 *         value.
	 */
	public int getReplicateCount() {
		return replicateCount;
	}
	
	public Integer getSeriesID() {
		return seriesID;
	}
	
	public boolean isOutlier() {
		return outlier;
	}
	
	public void setIsOutlier(boolean outlier, boolean removeOutlierFromXMLreference) {
		this.outlier = outlier;
		if (removeOutlierFromXMLreference) {
			SampleInterface sampleNode = xmlReference.getParentSample();
			if (sampleNode.remove(xmlReference))
				sampleNode.recalculateSampleAverage();
			else
				ErrorMsg.addErrorMessage("Internal Error: Data point (outlier could not be removed from the dataset!");
		}
	}
	
	public static int getTimePointFromTimeAndUnit(String timeAndUnit) {
		if (timeAndUnit == null || timeAndUnit.length() <= 0 || timeAndUnit.indexOf(" ") <= 0)
			return -1;
		String timeValue = timeAndUnit.substring(timeAndUnit.indexOf(" "));
		int res = Integer.parseInt(timeValue.trim());
		return res;
	}
	
	public static String getTimeUnitFromTimeAndUnit(String timeAndUnit) {
		if (timeAndUnit == null || timeAndUnit.length() <= 0 || timeAndUnit.indexOf(" ") <= 0)
			return "-1";
		String timeUnit = timeAndUnit.substring(0, timeAndUnit.indexOf(" "));
		return timeUnit.trim();
	}
	
	public Measurement getMeasurement() {
		return xmlReference;
	}
}
