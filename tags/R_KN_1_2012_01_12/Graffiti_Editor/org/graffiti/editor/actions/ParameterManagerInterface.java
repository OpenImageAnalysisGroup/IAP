// ==============================================================================
//
// ParameterManagerInterface.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ParameterManagerInterface.java,v 1.1 2011-01-31 09:04:22 klukas Exp $

package org.graffiti.editor.actions;

/**
 * DOCUMENT ME!
 * 
 * @author Christian Klukas
 */
interface ParameterManagerInterface {
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param paramName
	 *           DOCUMENT ME!
	 * @param newValue
	 *           DOCUMENT ME!
	 */
	public void notifyParameterUpdate(String paramName, double newValue);
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int parameterQueueSize();
	
	/**
	 * Gets last parameter Update, removes all updates for the given parameter
	 * from the queue
	 * 
	 * @param paramName
	 * @return DOCUMENT ME!
	 */
	double getParameterChange(String paramName);
	
	/**
	 * Gets last parameter Update given parameter from the queue
	 * 
	 * @param paramName
	 * @return DOCUMENT ME!
	 */
	double peekParameterChange(String paramName);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
