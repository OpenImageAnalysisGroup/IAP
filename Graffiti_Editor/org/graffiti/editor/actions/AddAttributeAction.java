// ==============================================================================
//
// AddAttributeAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AddAttributeAction.java,v 1.1 2011-01-31 09:04:22 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.SelectionAction;
import org.graffiti.selection.SelectionEvent;

/**
 * Represents an action to add an attribute to the selected item.
 * 
 * @author flierl
 * @version $Revision: 1.1 $
 */
public class AddAttributeAction
					extends SelectionAction {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new add attribute action.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public AddAttributeAction(MainFrame mainFrame) {
		super("action.add.attribute", mainFrame);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the help context of this action.
	 * 
	 * @return the help context of this action.
	 */
	@Override
	public HelpContext getHelpContext() {
		return null; //
	}
	
	/**
	 * Returns the name of this action.
	 * 
	 * @return the name of this action.
	 */
	@Override
	public String getName() {
		return null;
	}
	
	public void actionPerformed(ActionEvent e) {
		// 
	}
	
	public void selectionChanged(SelectionEvent e) {
		//
	}
	
	/**
	 * Returns <code>true</code>, if this action should survive a focus change
	 * in the editor.
	 * 
	 * @return <code>true</code>, if this action should survive a focus chage
	 *         in the editor.
	 */
	@Override
	public boolean surviveFocusChange() {
		return true;
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
