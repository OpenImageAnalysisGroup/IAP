/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.gui.GraffitiComponent;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

/**
 * This plugin contains the standard editing tools.
 * 
 * @version $Revision: 1.1 $
 */
public class KeggToolbarPlugin
					extends IPK_EditorPluginAdapter {
	public KeggToolbarPlugin() {
		guiComponents = new GraffitiComponent[] {
				// new KeggNavigationToolbarComponent("defaultToolbar"), // defaultToolbar // toolbarPanel
				// new ClusterHelperToolbarComponent("defaultToolbar")
				};
		
		algorithms = new Algorithm[] {
							new MergeWindowsAlgorithm(),
							new SelectWindowsAlgorithm(),
							new ColorizeSuperGraphAlgorithm(),
							new CreateOrgSpecificSuperGraphsAlgorithm()
		};
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
