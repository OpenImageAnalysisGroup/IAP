// ==============================================================================
//
// SelectionAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SelectionAction.java,v 1.1 2011-01-31 09:04:32 klukas Exp $

package org.graffiti.plugin.actions;

import java.util.ArrayList;
import java.util.List;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

/**
 * Represents an action, which depends on a selection.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class SelectionAction extends GraffitiAction {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new selection action with the given name.
	 * 
	 * @param name
	 *           DOCUMENT ME!
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public SelectionAction(String name, MainFrame mainFrame) {
		super(name, mainFrame, null);
	}
	
	// ~ Methods ================================================================
	
	@Override
	public abstract boolean isEnabled();
	
	/**
	 * Returns the current list of selected items of this action.
	 * 
	 * @return the current list of selected items of this action.
	 */
	public List<GraphElement> getSelectedItems() {
		ArrayList<GraphElement> result = new ArrayList<GraphElement>();
		EditorSession session = MainFrame.getInstance().getActiveEditorSession();
		if (session != null) {
			Selection selection = session.getSelectionModel().getActiveSelection();
			if (selection != null)
				result.addAll(selection.getElements());
		}
		return result;
	}
	
	public Selection getSelection() {
		EditorSession session = MainFrame.getInstance().getActiveEditorSession();
		if (session != null) {
			Selection selection = session.getSelectionModel().getActiveSelection();
			return selection;
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code>, if this action should survive a focus change.
	 * 
	 * @return <code>true</code>, if this action should survive a focus change.
	 */
	public boolean surviveFocusChange() {
		return false;
	}
	
	/**
	 * Sets the internal <code>enable</code> flag, which depends on the given
	 * list of selected items.
	 * 
	 * @param items
	 *           the items, which determine the internal state of the <code>enable</code> flag.
	 */
	protected abstract void enable(List<?> items);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
