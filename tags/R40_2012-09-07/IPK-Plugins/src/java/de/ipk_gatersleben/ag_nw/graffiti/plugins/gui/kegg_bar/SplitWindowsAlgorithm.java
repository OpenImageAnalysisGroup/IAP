/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 13.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import org.graffiti.plugin.algorithm.AbstractAlgorithm;

public class SplitWindowsAlgorithm extends AbstractAlgorithm {
	
	public String getName() {
		return "Split current file";
	}
	
	@Override
	public String getCategory() {
		return "menu.window";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Create a new graphs which contains individual subgraphs,<br>" +
							"defined by their cluster ID.";
	}
	
	public void execute() {
		// for (GraffitiInternalFrame s : del) {
		// s.doDefaultCloseAction();
		// }
		// MainFrame.getInstance().showGraph(newGraph, getActionEvent());
		
	}
}
