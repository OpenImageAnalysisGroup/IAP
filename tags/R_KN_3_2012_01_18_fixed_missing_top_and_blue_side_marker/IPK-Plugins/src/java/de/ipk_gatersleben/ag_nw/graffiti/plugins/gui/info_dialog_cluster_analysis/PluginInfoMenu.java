/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
// ==============================================================================
//
// GuiComponentsTestPlugin.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PluginInfoMenu.java,v 1.2 2011-02-23 14:41:28 klukas Exp $

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_cluster_analysis;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.gui.GraffitiComponent;

import de.ipk_gatersleben.ag_nw.graffiti.DBE_EditorPluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * This is a simple example for a gui component plugin for Graffiti.
 * 
 * @author chris
 */
public class PluginInfoMenu
					extends DBE_EditorPluginAdapter {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new GuiComponentsPlugin object.
	 */
	public PluginInfoMenu() {
		
		if (GravistoService.getInstance()
				.getMainFrame() != null)
			GravistoService.getInstance()
							.getMainFrame()
							.setTitle(DBEgravistoHelper.CLUSTER_ANALYSIS_VERSION);
		
		this.guiComponents = new GraffitiComponent[1];
		
		// menu example
		this.guiComponents[0] = new MenuItemInfoDialog();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
