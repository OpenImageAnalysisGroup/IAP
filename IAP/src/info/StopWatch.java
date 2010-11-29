/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package info;

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
	private final String desc;

	/**
	 * @param desc
	 *           This optional description is used by the {@link #printTime()} method to identify this stop watch.
	 */
	public StopWatch(String desc) {
		this.desc = desc;
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
		System.out.println(desc + " took " + getTime() + " ms");
	}

}
