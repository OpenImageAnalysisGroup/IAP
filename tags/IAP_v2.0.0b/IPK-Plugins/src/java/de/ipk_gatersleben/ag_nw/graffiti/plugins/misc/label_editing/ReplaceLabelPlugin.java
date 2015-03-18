/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class ReplaceLabelPlugin extends IPK_PluginAdapter {
	
	public ReplaceLabelPlugin() {
		this.algorithms = new Algorithm[] {
							new ChangeLabelsAlgorithm()
		};
	}
}
