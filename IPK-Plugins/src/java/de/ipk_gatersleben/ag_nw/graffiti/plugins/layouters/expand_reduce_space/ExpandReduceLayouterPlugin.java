/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.expand_reduce_space;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class ExpandReduceLayouterPlugin
					extends IPK_PluginAdapter {
	
	/**
	 * Creates a new CircleLayouterPlugin object.
	 */
	public ExpandReduceLayouterPlugin() {
		this.algorithms = new Algorithm[] {
							new ExpandReduceLayouterAlgorithm()
		};
	}
}
