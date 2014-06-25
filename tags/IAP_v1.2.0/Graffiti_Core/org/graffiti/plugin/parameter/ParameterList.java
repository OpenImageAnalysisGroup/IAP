// ==============================================================================
//
// ParameterList.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ParameterList.java,v 1.1 2011-01-31 09:05:02 klukas Exp $

package org.graffiti.plugin.parameter;

/**
 * A <code>ParameterList</code> can be used to create enumeration type
 * parameters, by grouping them into a list. The list can contain any number
 * of <code>SingleParameters</code>.
 * 
 * @version $Revision: 1.1 $
 * @see SingleParameter
 */
public interface ParameterList
					extends Parameter {
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @link aggregation
	 */
	
	/* #SingleParameter lnkSingleParameter; */

	/**
	 * Adds a <code>SingleParameter</code> to the list.
	 * 
	 * @param sp
	 *           the <code>SingleParameter</code> to add to the list.
	 */
	void addParameter(SingleParameter sp);
	
	/**
	 * Removes a <code>SingleParameter</code> from the list.
	 * 
	 * @param sp
	 *           the <code>SingleParameter</code> to remove from the list.
	 */
	void removeParameter(SingleParameter sp);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
