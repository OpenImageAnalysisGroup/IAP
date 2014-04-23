// ==============================================================================
//
// SessionManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SessionManager.java,v 1.1 2011-01-31 09:04:31 klukas Exp $

package org.graffiti.session;

import java.util.Iterator;

/**
 * Manages the session objects.
 * 
 * @see org.graffiti.session.Session
 */
public interface SessionManager {
	// ~ Methods ================================================================
	
	/**
	 * Returns the current active session.
	 * 
	 * @return the current active session.
	 */
	public Session getActiveSession();
	
	/**
	 * Returns <code>true</code>, if a session is active.
	 * 
	 * @return <code>true</code>, if a session is active.
	 */
	public boolean isSessionActive();
	
	/**
	 * Returns an iterator over all sessions.
	 * 
	 * @return an iterator over all sessions.
	 */
	public Iterator<Session> getSessionsIterator();
	
	/**
	 * Adds the given session to the list of sessions.
	 * 
	 * @param es
	 *           the new session to add.
	 */
	public void addSession(Session es);
	
	/**
	 * Adds a <code>SelectionListener</code>.
	 */
	public void addSessionListener(SessionListener sl);
	
	/**
	 * Called, if the session or data (except graph data) in the session have
	 * been changed.
	 */
	public void fireSessionDataChanged(Session session);
	
	/**
	 * Removes the given session from the list of sessions.
	 * 
	 * @param es
	 *           the session to remove from the list.
	 */
	public boolean closeSession(Session es);
	
	/**
	 * Removes a <code>SelectionListener</code>.
	 */
	public void removeSessionListener(SessionListener sl);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
