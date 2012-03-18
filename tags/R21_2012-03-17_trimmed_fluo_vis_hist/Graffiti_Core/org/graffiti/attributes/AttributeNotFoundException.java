// ==============================================================================
//
// AttributeNotFoundException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeNotFoundException.java,v 1.1 2011-01-31 09:04:43 klukas Exp $

package org.graffiti.attributes;

/**
 * The <code>AttributeNotFoundException</code> will be thrown if a method tries
 * to access a nonexistent attribute.
 * 
 * @version $Revision: 1.1 $
 */
public class AttributeNotFoundException
					extends RuntimeException {
	// ~ Constructors ===========================================================
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs an <code>AttributeNotFoundException</code> with the specified
	 * detail message.
	 * 
	 * @param msg
	 *           the detail message which is saved for later retrieval by the <code>getMessage()</code> method.
	 */
	public AttributeNotFoundException(String msg) {
		super(msg);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
