/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 9, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.util;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author klukas
 */
public class DateUtils {
	public static String getDayInfo(GregorianCalendar calendar) {
		return calendar.get(GregorianCalendar.YEAR) + " " + calendar.get(GregorianCalendar.MONTH) + " " + calendar.get(GregorianCalendar.DAY_OF_MONTH);
	}
	
	public static String getMonthInfo(GregorianCalendar calendar) {
		return calendar.get(GregorianCalendar.YEAR) + " " + calendar.get(GregorianCalendar.MONTH);
	}
	
	public static String getDayInfo(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		return getDayInfo(cal);
	}
	
	public static String getMonthInfo(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		return getMonthInfo(cal);
	}
}
