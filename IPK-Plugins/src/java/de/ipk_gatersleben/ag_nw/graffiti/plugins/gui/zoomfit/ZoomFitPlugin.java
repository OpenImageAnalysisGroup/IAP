/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit;

import org.graffiti.plugin.gui.GraffitiComponent;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

/**
 * This plugin contains the standard editing tools.
 * 
 * @version $Revision: 1.1 $
 */
public class ZoomFitPlugin
		extends IPK_EditorPluginAdapter {
	public ZoomFitPlugin() {
		if (!org.SystemAnalysis.isHeadless())
			guiComponents = new GraffitiComponent[] {
					new ZoomFitChangeComponent("defaultToolbar"), // defaultToolbar // toolbarPanel
					new AlignNodesComponent("defaultToolbar")
			};
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
