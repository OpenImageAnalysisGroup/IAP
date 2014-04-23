// ==============================================================================
//
// BoolPair.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: BoolPair.java,v 1.1 2011-01-31 09:04:58 klukas Exp $

package org.graffiti.util;

/**
 * Encapsulates two boolean values.
 * 
 * @author Paul
 * @version $Revision: 1.1 $
 */
public class BoolPair {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private boolean bool1;
	
	/** DOCUMENT ME! */
	private boolean bool2;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new BoolPair object.
	 * 
	 * @param bool1
	 *           DOCUMENT ME!
	 * @param bool2
	 *           DOCUMENT ME!
	 */
	public BoolPair(boolean bool1, boolean bool2) {
		this.bool1 = bool1;
		this.bool2 = bool2;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean getFst() {
		return bool1;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean getSnd() {
		return bool2;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
