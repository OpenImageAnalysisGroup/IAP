/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package info;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * This convenience class makes it easy to stop times.
 * Just construct it with a description and later call {@link #printTime()} to
 * print a message with the
 * notation of the elapsed time.
 * 
 * @author klukas
 */
public class StopWatch {
	
	private long start = System.currentTimeMillis();
	private String desc;
	
	/**
	 * @param desc
	 *           This optional description is used by the {@link #printTime()} method to identify this stop watch.
	 */
	public StopWatch(String desc) {
		this.desc = desc;
	}
	
	public StopWatch(String desc, boolean printStart) {
		this.desc = desc;
		if (printStart)
			System.out.println(desc + "...");
	}
	
	public void reset() {
		start = System.currentTimeMillis();
	}
	
	/**
	 * @return The time, elapsed since the construction of the object or since
	 *         the
	 *         last {@link #reset()} operation.
	 */
	public long getTime() {
		return System.currentTimeMillis() - start;
	}
	
	/**
	 * Prints the time, elapsed since the construction of the object or since the
	 * last {@link #reset()} operation.
	 */
	public void printTime() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " + desc + " took " + getTime() + " ms");
	}
	
	public String getDescription() {
		return desc;
	}
	
	public void printTime(int minTime) {
		if (minTime == 0 || getTime() - start > minTime)
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " + desc + " took " + getTime() + " ms");
	}
	
	public String getTimeString() {
		return getTime() / 1000 + " s";
	}
	
	public static String getNiceTime() {
		return IAPservice.getCurrentTimeAsNiceString();
	}
	
	public void setDescription(String desc) {
		this.desc = desc;
	}
	
}
