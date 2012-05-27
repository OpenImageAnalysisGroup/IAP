// ==============================================================================
//
// KeyData.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: KeyData.java,v 1.1 2011-01-31 09:03:25 klukas Exp $

package org.graffiti.plugins.ios.exporters.graphml;

/**
 * Class <code>KeyData</code> is a utility class for creating (int,String)
 * tuples.
 * 
 * @author $Author: klukas $
 */
class KeyData {
	// ~ Instance fields ========================================================
	
	/** The id serving as a key. */
	private Integer id;
	
	/** The type the id maps to. */
	private String type;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new KeyData object.
	 * 
	 * @param id
	 *           the id of the tuple.
	 * @param type
	 *           the type of the tuple.
	 */
	KeyData(Integer id, String type) {
		this.id = id;
		this.type = type;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the id of the tuple.
	 * 
	 * @return the id of the tuple.
	 */
	int getId() {
		return this.id.intValue();
	}
	
	/**
	 * Returns the type of the tuple.
	 * 
	 * @return the type of the tuple.
	 */
	String getType() {
		return this.type;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
