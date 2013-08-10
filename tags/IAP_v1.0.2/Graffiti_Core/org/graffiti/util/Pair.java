// ==============================================================================
//
// Pair.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Pair.java,v 1.1 2011-01-31 09:04:59 klukas Exp $

package org.graffiti.util;

/**
 * Encapsulates two values.
 * 
 * @author Paul
 * @version $Revision: 1.1 $
 */
public class Pair {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private Object val1;
	
	/** DOCUMENT ME! */
	private Object val2;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new Pair object.
	 * 
	 * @param val1
	 *           DOCUMENT ME!
	 * @param val2
	 *           DOCUMENT ME!
	 */
	public Pair(Object val1, Object val2) {
		this.val1 = val1;
		this.val2 = val2;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object getFst() {
		return val1;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object getSnd() {
		return val2;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
