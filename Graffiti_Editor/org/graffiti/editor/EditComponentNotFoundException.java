// ==============================================================================
//
// EditComponentNotFoundException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EditComponentNotFoundException.java,v 1.1 2011-01-31 09:04:27 klukas Exp $

package org.graffiti.editor;

/**
 * Thrown if no EditComponent could be found.
 */
public class EditComponentNotFoundException
					extends Exception {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for AttributeComponentNotFoundException.
	 * 
	 * @param message
	 */
	public EditComponentNotFoundException(String message) {
		super(message);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
