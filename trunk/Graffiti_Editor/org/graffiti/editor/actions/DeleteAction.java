// ==============================================================================
//
// DeleteAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DeleteAction.java,v 1.1 2011-01-31 09:04:21 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.SelectionAction;
import org.graffiti.selection.Selection;
import org.graffiti.undo.GraphElementsDeletionEdit;

/**
 * Represents a graph element delete action.
 * 
 * @author klukas
 * @version $Revision: 1.1 $
 */
public class DeleteAction extends SelectionAction {
	// ~ Constructors ===========================================================
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new copy action.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public DeleteAction(MainFrame mainFrame) {
		super("edit.delete", mainFrame);
	}
	
	// ~ Methods ================================================================
	
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
		Selection selection = getSelection();
		// useful to check if the selection isn't empty
		// before an edit is build.
		if (!selection.isEmpty()) {
			GraphElementsDeletionEdit edit = new GraphElementsDeletionEdit(selection.getElements(),
								getGraph(), MainFrame.getInstance().getActiveEditorSession().getGraphElementsMap());
			edit.execute();
			MainFrame.getInstance().getUndoSupport().postEdit(edit);
		}
	}
	
	@Override
	public boolean isEnabled() {
		boolean result = getSelectedItems().size() > 0;
		return result;
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
