/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import org.HelperClass;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
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
		if (currentStatusValueFine < lastProgress) {
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
		averageSpeed = averageSpeed * 1000; // percent per second
		
		if (true)
			averageSpeed = currentSpeedUntilNow;
		
		String result;
		
		double remainPercent = 100 - currentStatusValueFine;
		double remainTimeSeconds = remainPercent / averageSpeed;
		
		if (remainTimeSeconds < 0)
			return "";
		
		double remainTimeMinutes = remainTimeSeconds / 60 % 60;
		double remainTimeHours = remainTimeSeconds / 60 / 60 % 24;
		double remainTimeDays = remainTimeSeconds / 24 / 60 / 60 % 7;
		double remainTimeWeeks = remainTimeSeconds / 7 / 24 / 60 / 60 % 56;
		// double remainTimeMonths=remainTimeSeconds/30.5/24/60/60 % 12;
		double remainTimeYears = remainTimeSeconds / 365 / 24 / 60 / 60;
		int numberResults = 0;
		/**
		 * nn=1 --> 2h, nn=2 --> 2h 30m
		 */
		int nn = 1;
		String res[] = new String[2];
		if (remainTimeYears > 1 && numberResults < nn)
			if (remainTimeYears == 1)
				res[numberResults++] = (int) remainTimeYears + "&nbsp;year";
			else
				res[numberResults++] = (int) remainTimeYears + "&nbsp;years";
		// if (remainTimeMonths>1 && numberResults<2)
		// res[numberResults++]=(int)remainTimeMonths+" months";
		if (remainTimeWeeks > 1 && numberResults < nn)
			if (remainTimeWeeks == 1)
				res[numberResults++] = (int) remainTimeWeeks + "&nbsp;week";
			else
				res[numberResults++] = (int) remainTimeWeeks + "&nbsp;weeks";
		if (remainTimeDays > 1 && numberResults < nn)
			if (remainTimeDays == 1)
				res[numberResults++] = (int) remainTimeDays + "&nbsp;day";
			else
				res[numberResults++] = (int) remainTimeDays + "&nbsp;days";
		if (remainTimeHours > 1 && numberResults < nn)
			if (remainTimeHours == 1)
				res[numberResults++] = (int) remainTimeHours + "&nbsp;hour";
			else
				res[numberResults++] = (int) remainTimeHours + "&nbsp;hours";
		if (remainTimeMinutes > 1 && numberResults < nn)
			res[numberResults++] = (int) remainTimeMinutes + "&nbsp;min";
		if (numberResults < nn)
			res[numberResults++] = (int) remainTimeSeconds % 60 + "&nbsp;sec";
		
		if (numberResults == 2)
			result = res[0] + " " + res[1];
		else
			result = "~&nbsp;" + res[0];
		
		// increase expectation only if the value increased 10 times without
		// lowering again
		if (averageSpeed > lastSpeed) {
			biggerThanBeforeCount++;
			if (biggerThanBeforeCount <= 10)
				result = lastRes;
		} else
			biggerThanBeforeCount = 0;
		lastSpeed = averageSpeed;
		lastRes = result;
		if (remainTimeSeconds < 1)
			result = "";
		if (result.contains("year")) {
			result = "";
		}
		return result;
	}
	
}
