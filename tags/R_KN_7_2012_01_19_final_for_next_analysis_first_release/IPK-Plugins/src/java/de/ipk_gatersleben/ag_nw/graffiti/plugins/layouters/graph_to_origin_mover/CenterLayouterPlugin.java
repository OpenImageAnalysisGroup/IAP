/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class CenterLayouterPlugin extends IPK_PluginAdapter {
	
	public CenterLayouterPlugin() {
		this.algorithms = new Algorithm[] {
							new CenterLayouterAlgorithm()
		};
	}
}
