// ==============================================================================
//
// FileCloseAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FileCloseAction.java,v 1.1 2011-01-31 09:04:22 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

/**
 * The action for closing a graph.
 * 
 * @version $Revision: 1.1 $
 */
public class FileCloseAction extends GraffitiAction {
	// ~ Constructors
	// ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new FileCloseAction object.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public FileCloseAction(MainFrame mainFrame) {
		super("file.close", mainFrame, "filemenu_close");
	}
	
	// ~ Methods
	// ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isEnabled() {
		return mainFrame.isSessionActive();
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
		EditorSession es = mainFrame.getActiveEditorSession();
		Collection<View> views = new ArrayList<View>(es.getViews());
		for (View v : views) {
			closeInternalFrame(v.getViewComponent());
		}
	}
	
	private void closeInternalFrame(JComponent viewComponent) {
		if (viewComponent instanceof JInternalFrame) {
			JInternalFrame jif = (JInternalFrame) viewComponent;
			jif.doDefaultCloseAction();
		} else {
			if (viewComponent.getParent() != null
								&& viewComponent.getParent() instanceof JComponent)
				closeInternalFrame((JComponent) viewComponent.getParent());
			else {
				if (viewComponent.getParent() != null)
					ErrorMsg.addErrorMessage("View component parent of type: "
										+ viewComponent.getParent().getClass()
															.getCanonicalName());
				else
					ErrorMsg
										.addErrorMessage("View component parent of type: [null]");
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
