/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: InvertSelectionPlugin.java,v 1.3 2011-05-13 09:07:41 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.gui.GraffitiComponent;

import vanted_feature.SelectAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

public class InvertSelectionPlugin
					extends IPK_EditorPluginAdapter {
	
	public InvertSelectionPlugin() {
		algorithms = new Algorithm[] {
							new CopyDataTableAlgorithm(),
							new ChangeNodeStyle(),
							new ChangeEdgeStyle(),
							new ChangeElementStyle(),
							new SelectAlgorithm(),
							new SetToolTipAlgorithm(),
							new SearchAndSelecAlgorithm(),
							new FindReplaceDialog()
		};
		
		if (!SystemAnalysis.isHeadless())
			guiComponents = new GraffitiComponent[] {
							new SelectNodesComponent("defaultToolbar"), // defaultToolbar // toolbarPanel
							};
	}
}
