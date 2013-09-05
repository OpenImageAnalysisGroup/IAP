/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.08.2004 by Christian Klukas
 */
package org;

/**
 * @author Christian Klukas
 *         (c) 2011 IPK-Gatersleben
 */
public class ProgressStatusService implements HelperClass {
	
	private long lastUpdate = System.currentTimeMillis();
	private long firstUpdate = System.currentTimeMillis();
	private double firstStatusValueFine = 0;
	
	private double lastProgress = 0;
	
	private final double[] lastSpeeds = new double[150];
	
	int fillStatus = 0;
	
	int initialFillStatus = 0;
	
	int biggerThanBeforeCount = 0;
	
	double lastSpeed = 0;
	
	String lastRes = "";
	
	/**
	 * @param intermediate
	 * @param currentStatusValueFine
	 * @return
	 */
	public String getRemainTime(boolean intermediate, double currentStatusValueFine) {
		long thisUpdate = System.currentTimeMillis();
		if (intermediate) {
			firstUpdate = thisUpdate;
			firstStatusValueFine = 0;
			return "";
		}
		double currentSpeedUntilNow = 0;
		if (currentStatusValueFine < lastProgress || System.currentTimeMillis() - firstUpdate > 30000) {
			firstUpdate = thisUpdate;
			firstStatusValueFine = currentStatusValueFine;
		}
		if (firstStatusValueFine < 0)
			firstStatusValueFine = 0;
		if (thisUpdate - firstUpdate != 0)
			currentSpeedUntilNow = (currentStatusValueFine - firstStatusValueFine) / (thisUpdate - firstUpdate) * 1000;
		lastUpdate = thisUpdate;
		lastSpeeds[fillStatus++ % lastSpeeds.length] = currentSpeedUntilNow;
		if (initialFillStatus < lastSpeeds.length)
			initialFillStatus++;
		lastProgress = currentStatusValueFine;
		double averageSpeed = 0;
		for (int i = 0; i < initialFillStatus; i++)
			averageSpeed += lastSpeeds[i] / initialFillStatus;
		// averageSpeed = averageSpeed * 1000; // percent per second
		
		if (true)
			averageSpeed = currentSpeedUntilNow;
		
		double remainPercent = 100 - currentStatusValueFine;
		double remainTimeSeconds = remainPercent / averageSpeed;
		
		return getRemainTimeString(averageSpeed, remainTimeSeconds);
	}
	
	public String getRemainTimeString(double averageSpeed, double remainTimeSeconds) {
		return getRemainTimeString(averageSpeed, remainTimeSeconds, 1);
	}
	
	public String getRemainTimeString(double averageSpeed, double remainTimeSeconds, int nn) {
		String result = "";
		if (remainTimeSeconds < 1)
			return "less than 1 second";
		
		double remainTimeYears = remainTimeSeconds / 365 / 24 / 60 / 60;
		int numberResults = 0;
		/**
		 * nn=1 --> 2h, nn=2 --> 2h 30m
		 */
		String res[] = new String[nn];
		if (remainTimeYears >= 1 && numberResults < nn) {
			if (((int) remainTimeYears) == 1)
				res[numberResults++] = (int) remainTimeYears + "&nbsp;year";
			else
				res[numberResults++] = (int) remainTimeYears + "&nbsp;years";
			remainTimeSeconds -= 365 * 24 * 60 * 60 * (int) remainTimeYears;
		}
		
		double remainTimeMonths = remainTimeSeconds / 30.5 / 24 / 60 / 60 % 12;
		
		if (remainTimeMonths >= 1 && numberResults < nn) {
			remainTimeSeconds -= 30.5 * 24 * 60 * 60 * (int) remainTimeMonths;
			double remainTimeWeeks = remainTimeSeconds / 7 / 24 / 60 / 60 % 56;
			if ((int) remainTimeWeeks == 4) {
				remainTimeMonths += 1;
				remainTimeSeconds -= 30.5 * 24 * 60 * 60 * 1;
			}
			if (((int) remainTimeMonths) == 1)
				res[numberResults++] = (int) remainTimeMonths + "&nbsp;month";
			else
				res[numberResults++] = (int) remainTimeMonths + "&nbsp;months";
			
		}
		
		double remainTimeWeeks = remainTimeSeconds / 7 / 24 / 60 / 60 % 56;
		
		if (remainTimeWeeks >= 1 && numberResults < nn) {
			if (((int) remainTimeWeeks) == 1)
				res[numberResults++] = (int) remainTimeWeeks + "&nbsp;week";
			else
				res[numberResults++] = (int) remainTimeWeeks + "&nbsp;weeks";
			remainTimeSeconds -= 7 * 24 * 60 * 60 * (int) remainTimeWeeks;
		}
		
		double remainTimeDays = remainTimeSeconds / 24 / 60 / 60 % 7;
		
		if (((int) remainTimeDays) >= 1 && numberResults < nn) {
			if ((int) remainTimeDays == 1)
				res[numberResults++] = (int) remainTimeDays + "&nbsp;day";
			else
				res[numberResults++] = (int) remainTimeDays + "&nbsp;days";
			remainTimeSeconds -= 24 * 60 * 60 * (int) remainTimeDays;
		}
		
		double remainTimeHours = remainTimeSeconds / 60 / 60 % 24;
		
		if (((int) remainTimeHours) >= 1 && numberResults < nn) {
			if ((int) remainTimeHours == 1)
				res[numberResults++] = (int) remainTimeHours + "&nbsp;hour";
			else
				res[numberResults++] = (int) remainTimeHours + "&nbsp;hours";
			remainTimeSeconds -= 60 * 60 * (int) remainTimeHours;
		}
		
		double remainTimeMinutes = remainTimeSeconds / 60 % 60;
		
		if (((int) remainTimeMinutes) >= 1 && numberResults < nn) {
			res[numberResults++] = (int) remainTimeMinutes + "&nbsp;min";
			remainTimeSeconds -= 60 * (int) remainTimeMinutes;
		}
		if (numberResults < nn && remainTimeSeconds > 0)
			res[numberResults++] = (int) remainTimeSeconds % 60 + "&nbsp;sec";
		
		if (numberResults == 3)
			result = res[0] + " " + res[1] + " " + res[2];
		else
			if (numberResults == 2)
				result = res[0] + " " + res[1];
			else
				result = /* "~&nbsp;" + */res[0];
		
		if (averageSpeed > 0) {
			// increase expectation only if the value increased 10 times without
			// lowering again
			if (averageSpeed > lastSpeed) {
				biggerThanBeforeCount++;
				if (biggerThanBeforeCount <= 10)
					result = lastRes;
			} else
				biggerThanBeforeCount = 0;
			lastSpeed = averageSpeed;
		}
		lastRes = result;
		if (result != null) {
			if (result.contains("years") && remainTimeYears > 1000)
				return "";
			else
				return result;
		} else
			return "";
	}
	
}
