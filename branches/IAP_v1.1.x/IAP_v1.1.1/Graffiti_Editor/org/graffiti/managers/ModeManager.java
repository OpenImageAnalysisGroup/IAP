// ==============================================================================
//
// ModeManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ModeManager.java,v 1.1 2011-01-31 09:04:29 klukas Exp $

package org.graffiti.managers;

import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.plugin.mode.Mode;

/**
 * Provides an interface for a modes manager.
 * 
 * @version $Revision: 1.1 $
 * @see org.graffiti.managers.pluginmgr.PluginManagerListener
 */
public interface ModeManager
					extends PluginManagerListener {
	// ~ Methods ================================================================
	
	/**
	 * Returns the specified mode from the list of modes.
	 * 
	 * @return the specified mode from the list of modes.
	 */
	public Mode getMode(String mode);
	
	/**
	 * Adds the specified mode to the list of modes this manager contains.
	 * 
	 * @param mode
	 *           the mode to be added to the list.
	 */
	public void addMode(Mode mode);
	
	/**
	 * Removes the specified mode from the list of modes the manager contains.
	 * 
	 * @param mode
	 *           the mode to be removed.
	 */
	public void removeMode(Mode mode);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
