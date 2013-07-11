// ==============================================================================
//
// RedrawViewAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: RedrawViewAction.java,v 1.1 2011-01-31 09:04:22 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

/**
 * The action for a new graph.
 * 
 * @version $Revision: 1.1 $
 */
public class RedrawViewAction
					extends GraffitiAction {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new RedrawViewAction object.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public RedrawViewAction(MainFrame mainFrame) {
		super("edit.redraw", mainFrame, "editmenu_redraw");
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see javax.swing.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		EditorSession dv = mainFrame.getActiveEditorSession();
		if (dv == null)
			return false;
		List<?> views = dv.getViews();
		return !views.isEmpty();
	}
	
	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		EditorSession dv = mainFrame.getActiveEditorSession();
		
		// hack till i find out how to do the enabling correctly
		if (dv == null)
			return;
		
		List<?> views = dv.getViews();
		
		for (Iterator<?> it = views.iterator(); it.hasNext();) {
			View view = (View) it.next();
			// view.postGraphCleared(new GraphEvent(getGraph()));
			// view.setGraph(getGraph());
			view.completeRedraw();
			mainFrame.fireSessionChanged(dv);
		}
		
		mainFrame.updateActions();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
