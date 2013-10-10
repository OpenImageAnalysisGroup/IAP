// ==============================================================================
//
// ChangeAttributeValueAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ChangeAttributeValueAction.java,v 1.1 2011-01-31 09:04:23 klukas Exp $

/* Generated by Together */
package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.SelectionAction;
import org.graffiti.selection.SelectionEvent;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:04:23 $
 */
public class ChangeAttributeValueAction
					extends SelectionAction {
	// ~ Constructors ===========================================================
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new ChangeAttributeValueAction object.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public ChangeAttributeValueAction(MainFrame mainFrame) {
		super("action.change.attribute.value", mainFrame);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the help context for the action.
	 * 
	 * @return HelpContext, the help context for the action
	 */
	@Override
	public HelpContext getHelpContext() {
		return null; //
	}
	
	/**
	 * Returns the name represented by a String.
	 * 
	 * @return String, the name
	 */
	@Override
	public String getName() {
		return null;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		//
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void selectionChanged(SelectionEvent e) {
		//
	}
	
	/**
	 * Returns <code>true</code>, if this action should survive a focus change.
	 * 
	 * @return <code>true</code>, if this action should survive a focus change.
	 */
	@Override
	public boolean surviveFocusChange() {
		return false;
	}
	
	/**
	 * Sets the internal <code>enable</code> flag, which depends on the given
	 * list of selected items.
	 * 
	 * @param selectedItems
	 *           the items, which determine the internal state of
	 *           the<code>enable</code> flag.
	 */
	@Override
	protected void enable(List<?> selectedItems) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.actions.SelectionAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		//
		return false;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------