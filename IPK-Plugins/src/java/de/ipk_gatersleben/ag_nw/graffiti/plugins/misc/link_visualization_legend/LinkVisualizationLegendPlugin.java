/*******************************************************************************
 * Copyright (c) 2003-2008 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.link_visualization_legend;

import javax.swing.JMenuItem;

import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class LinkVisualizationLegendPlugin
					extends IPK_PluginAdapter
					implements SessionListener, ProvidesGeneralContextMenu {
	
	Session activeSession = null;
	
	public JMenuItem[] getCurrentContextMenuItem() {
		if (activeSession == null)
			return null;
		else
			return new JMenuItem[] { new JMenuItem("Test 123") };
	}
	
	public void sessionChanged(Session s) {
		this.activeSession = s;
	}
	
	public void sessionDataChanged(Session s) {
		// empty
	}
	
}
