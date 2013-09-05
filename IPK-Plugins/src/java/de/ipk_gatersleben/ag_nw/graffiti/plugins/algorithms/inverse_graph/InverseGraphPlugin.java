/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.inverse_graph;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 *         21.11.2007
 */
public class InverseGraphPlugin extends IPK_PluginAdapter {
	public InverseGraphPlugin() {
		this.algorithms = new Algorithm[] {
							new InverseGraphPluginAlgorithm()
		};
	}
}
