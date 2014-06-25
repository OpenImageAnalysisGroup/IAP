// ==============================================================================
//
// ViewListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ViewListener.java,v 1.1 2011-01-31 09:04:24 klukas Exp $

package org.graffiti.plugin.view;

/**
 * Interface for all who want to be noticed when a different view becomes
 * active. The events that implementors get are disjoint from those that <code>SessionLister</code>s get.
 * 
 * @version $Revision: 1.1 $
 * @see org.graffiti.session.Session
 */
public interface ViewListener {
	// ~ Methods ================================================================
	
	/**
	 * This method is called when the view changes. This method is not called
	 * when another session is activated. Implement <code>SessionListener</code> if you are interested in session changed
	 * events.
	 * 
	 * @param newView
	 *           the new View.
	 */
	public void viewChanged(View newView);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
