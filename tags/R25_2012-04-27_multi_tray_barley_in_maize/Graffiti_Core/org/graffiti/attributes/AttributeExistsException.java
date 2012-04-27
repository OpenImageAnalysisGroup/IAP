// ==============================================================================
//
// AttributeExistsException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeExistsException.java,v 1.1 2011-01-31 09:04:41 klukas Exp $

package org.graffiti.attributes;

/**
 * The <code>AttributeExistsException</code> will be thrown if a method tries
 * to add an attribute at a location where another attribute already exists.
 * 
 * @version $Revision: 1.1 $
 */
public class AttributeExistsException
					extends RuntimeException {
	// ~ Constructors ===========================================================
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs an <code>AttributeExistsException</code> with the specified
	 * detail message.
	 * 
	 * @param msg
	 *           the detail message which is saved for later retrieval by the <code>getMessage()</code> method.
	 */
	public AttributeExistsException(String msg) {
		super(msg);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
