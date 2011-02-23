/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: InvertSelectionPlugin.java,v 1.2 2011-02-23 14:41:28 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.awt.GraphicsEnvironment;

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
		
		if (!GraphicsEnvironment.isHeadless())
			guiComponents = new GraffitiComponent[] {
							new SelectNodesComponent("defaultToolbar"), // defaultToolbar // toolbarPanel
							};
	}
}
