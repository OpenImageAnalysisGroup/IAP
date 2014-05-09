// ==============================================================================
//
// SessionListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SessionListener.java,v 1.1 2011-01-31 09:04:31 klukas Exp $

package org.graffiti.session;

/**
 * Interface for all who want to be noticed when the session changes.
 * 
 * @version $Revision: 1.1 $
 * @see org.graffiti.session.Session
 */
public interface SessionListener {
	// ~ Methods ================================================================
	
	/**
	 * This method is called when the session changes.
	 * 
	 * @param s
	 *           the new Session.
	 */
	public void sessionChanged(Session s);
	
	/**
	 * This method is called when the data (except the graph data) are changed.
	 */
	public void sessionDataChanged(Session s);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
