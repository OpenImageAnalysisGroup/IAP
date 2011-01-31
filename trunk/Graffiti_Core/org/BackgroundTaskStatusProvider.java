/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package org;

/**
 * @author klukas
 */
public interface BackgroundTaskStatusProvider {
	/**
	 * Returns the completion status. WARNING: This method must be Thread-Safe!
	 * 
	 * @return A number from 0..100 which represents the completion status. If -1
	 *         is returned, the progress bar is set to "indeterminate", which
	 *         means, that the progress bar will float from left to right and
	 *         reverse. (Useful if status can not be determined) Other values let
	 *         the progressbar disappear.
	 */
	public abstract int getCurrentStatusValue();
	
	/**
	 * Override this method and pass a implementor of this interface to any other
	 * service method. This method can use this call to update the status value.
	 * 
	 * @param value
	 */
	public abstract void setCurrentStatusValue(int value);
	
	/**
	 * Same as <code>getCurrentStatusValue()</code>, but this method should
	 * return a finer granted progress value. If this is not needed, the code for <code>getCurrentStatusValue()</code> could be reused by the client.
	 * 
	 * @return The current progress value (fine).
	 */
	public abstract double getCurrentStatusValueFine();
	
	/**
	 * Returns a status message on what is going on. WARNING: This method must be
	 * Thread-Safe!
	 * 
	 * @return A status message, or null if not needed.
	 */
	public abstract String getCurrentStatusMessage1();
	
	/**
	 * Returns a status message on what is going on. Is used the same like <code>getCurrentStatusMessage1</code>. This second message adds
	 * flexibility. If not needed, the first message should be used and this
	 * should return null if not needed. WARNING: This method must be
	 * Thread-Safe!
	 * 
	 * @return A status message, or null if not needed.
	 */
	public abstract String getCurrentStatusMessage2();
	
	/**
	 * If this method is called on the status provider, the linked work task
	 * should stop its execution as soon as possible.
	 */
	public void pleaseStop();
	
	/**
	 * @return Let this method return true in order to show a "Continue" button
	 *         in the GUI. When this buttons is clicked, the method <code>pleaseContinueRun</code> is called. Use these two methods to
	 *         let the user interact with the GUI while the algorithm is waiting
	 *         for the user to be ready for the continued work of the algorithm.
	 */
	public boolean pluginWaitsForUser();
	
	/**
	 * This method is called as soon as the user indicates that he is comfortable
	 * to let the algorithm continue its work.
	 */
	public void pleaseContinueRun();
}