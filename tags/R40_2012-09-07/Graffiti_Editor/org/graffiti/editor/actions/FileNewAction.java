// ==============================================================================
//
// FileNewAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FileNewAction.java,v 1.1 2011-01-31 09:04:23 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;

import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.ViewManager;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.session.EditorSession;

/**
 * The action for a new graph.
 * 
 * @version $Revision: 1.1 $
 */
public class FileNewAction
					extends GraffitiAction {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** DOCUMENT ME! */
	private ViewManager viewManager;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new FileNewAction object.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 * @param viewManager
	 *           DOCUMENT ME!
	 */
	public FileNewAction(MainFrame mainFrame, ViewManager viewManager) {
		super("file.new", mainFrame, "filemenu_new");
		this.viewManager = viewManager;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see javax.swing.Action#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return viewManager.hasViews();
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
		String dv = mainFrame.getDefaultView();
		
		if (dv != null) {
			mainFrame.createInternalFrame(dv, "", false, false);
		} else {
			mainFrame.showViewChooserDialog(new EditorSession(), false, e);
		}
		
		FileHandlingManager.getInstance().throwFileNew();
		
		mainFrame.updateActions();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
