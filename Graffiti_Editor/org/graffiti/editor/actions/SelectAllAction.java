/*
 * Copyright (c) 2003 IPK Gatersleben
 * $Id: SelectAllAction.java,v 1.1 2011-01-31 09:04:23 klukas Exp $
 */

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.SelectionAction;
import org.graffiti.selection.Selection;

/**
 * Represents a &quot;select all graph elements&quot; action.
 * 
 * @version $Revision: 1.1 $
 */
public class SelectAllAction
					extends SelectionAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new copy action.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public SelectAllAction(MainFrame mainFrame) {
		super("edit.selectAll", mainFrame);
	}
	
	/**
	 * Returns the help context for the action.
	 * 
	 * @return HelpContext, the help context for the action
	 */
	@Override
	public HelpContext getHelpContext() {
		return null; 
	}
	
	/**
	 * Executes this action.
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		// this check can be discarded when the isEnabled-function works correctly
		if (!mainFrame.isSessionActive()) {
			return;
		}
		getGraph().getListenerManager().transactionStarted(this);
		mainFrame.getActiveEditorSession().getActiveView().getViewComponent().repaint();
		Selection selection =
							mainFrame.getActiveEditorSession().getSelectionModel()
												.getActiveSelection();
		
		selection.clear();
		selection.addAll(getGraph().getGraphElements());
		
		mainFrame.getActiveEditorSession().getSelectionModel()
							.selectionChanged();
		getGraph().getListenerManager().transactionFinished(this);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isEnabled() {
		if (!mainFrame.isSessionActive()) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sets the internal <code>enable</code> flag, which depends on the given
	 * list of selected items.
	 * 
	 * @param items
	 *           the items, which determine the internal state of the <code>enable</code> flag.
	 */
	@Override
	protected void enable(List<?> items) {
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
