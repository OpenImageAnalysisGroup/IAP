/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import javax.swing.KeyStroke;

import org.SystemInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class RecreateView extends AbstractEditorAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Recreate View";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke('R', SystemInfo.getAccelModifier());
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
		if (MainFrame.getInstance().isSessionActive()) {
			GraphHelper.issueCompleteRedrawForActiveView();
		} else {
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
	}
	
	public boolean activeForView(View v) {
		return v != null;
	}
	
}