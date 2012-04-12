package org.graffiti.session;

/**
 * @author hendrik, klukas
 */
public interface SessionListenerExt extends SessionListener {
	
	/**
	 * Is called once the session has been removed from the system.
	 * 
	 * @param session
	 */
	void sessionClosed(Session session);
}
