// ==============================================================================
//
// CloseViewAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: CloseViewAction.java,v 1.1 2011-01-31 09:04:23 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;

import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.GraffitiAction;

/**
 * Represents a clone view action.
 */
public class CloseViewAction
					extends GraffitiAction {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new close view action.
	 */
	public CloseViewAction() {
		super("action.view.close", null, "filemenu_close");
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see javax.swing.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return false;
	}
	
	/**
	 * Returns the help context for this action.
	 * 
	 * @return the help context for this action.
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	/**
	 * Returns the name of this action.
	 * 
	 * @return String, the name of this action.
	 */
	@Override
	public String getName() {
		return null;
	}
	
	/**
	 * Executes this action.
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
