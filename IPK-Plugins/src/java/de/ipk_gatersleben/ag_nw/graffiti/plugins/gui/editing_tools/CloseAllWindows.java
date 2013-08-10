/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.util.HashSet;
import java.util.Set;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.view.View;
import org.graffiti.session.Session;

public class CloseAllWindows extends AbstractEditorAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Close All Windows";
	}
	
	@Override
	public String getCategory() {
		return "menu.window";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		Set<Session> session = new HashSet<Session>(MainFrame.getSessions());
		for (Session s : session) {
			MainFrame.getInstance().getSessionManager().closeSession(s);
		}
	}
	
	public boolean activeForView(View v) {
		return v != null;
	}
	
}