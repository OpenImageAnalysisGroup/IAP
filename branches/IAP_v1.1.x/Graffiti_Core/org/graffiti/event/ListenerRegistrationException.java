// ==============================================================================
//
// ListenerRegistrationException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ListenerRegistrationException.java,v 1.1 2011-01-31 09:04:59 klukas Exp $

package org.graffiti.event;

/**
 * In general, the exception is used to indicate that a listener could not be
 * registered. Will be thrown, if someone tries to add a strict listener while
 * the same listener is already registered as non strict or vice versa.
 * 
 * @version $Revision: 1.1 $
 */
public class ListenerRegistrationException
					extends RuntimeException {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a <code>ListenerRegistrationException</code> with the
	 * specified detail message.
	 * 
	 * @param msg
	 *           the detail message for the exception.
	 */
	public ListenerRegistrationException(String msg) {
		super(msg);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
