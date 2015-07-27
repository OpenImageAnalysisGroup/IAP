// ==============================================================================
//
// LimitableParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LimitableParameter.java,v 1.1 2011-01-31 09:05:03 klukas Exp $

package org.graffiti.plugin.parameter;

/**
 * The value of a <code>LimitableParameter</code> can be limited by giving
 * maximum and minimum values.
 * 
 * @version $Revision: 1.1 $
 */
public interface LimitableParameter
					extends SingleParameter {
	// ~ Methods ================================================================
	
	/**
	 * Returns the maximum value for this <code>LimitableParameter</code>.
	 * 
	 * @return the maximum value for this <code>LimitableParameter</code>.
	 */
	public Comparable<?> getMax();
	
	/**
	 * Returns the minimum value for this <code>LimitableParameter</code>.
	 * 
	 * @return the minimum value for this <code>LimitableParameter</code>.
	 */
	public Comparable<?> getMin();
	
	/**
	 * Returns <code>true</code> if the value is between the minimum and the
	 * maximum, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the value is between the minimum and the
	 *         maximum, <code>false</code> otherwise.
	 */
	public boolean isValid();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
