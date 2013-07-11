/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid_placement;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class GridPlacementPlugin
					extends IPK_PluginAdapter {
	
	public GridPlacementPlugin() {
		this.algorithms = new Algorithm[] {
							new GridPlacementAlgorithm(),
							new IterateGridPlacementLayout()
		};
	}
}
