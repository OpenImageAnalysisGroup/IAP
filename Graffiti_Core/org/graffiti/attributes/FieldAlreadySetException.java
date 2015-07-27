// ==============================================================================
//
// FieldAlreadySetException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FieldAlreadySetException.java,v 1.1 2011-01-31 09:04:41 klukas Exp $

package org.graffiti.attributes;

/**
 * The <code>FieldAlreadySetException</code> will be thrown if <code>setAttributable()</code> of <code>setParent()</code> is invoked on an
 * attribute where theses fields are not <tt>null</tt> anymore.
 * 
 * @version $Revision: 1.1 $
 */
public class FieldAlreadySetException
					extends RuntimeException {
	// ~ Constructors ===========================================================
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs an <code>FieldAlreadySetException</code> with the specified
	 * detail message.
	 * 
	 * @param msg
	 *           The detail message which is saved for later retrieval by the <code>getMessage()</code> method.
	 */
	public FieldAlreadySetException(String msg) {
		super(msg);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
