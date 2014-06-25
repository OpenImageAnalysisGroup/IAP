/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.resize_and_grid_layout;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * Plugin for Gridlayouter algorithm
 * 
 * @author Christian Klukas, Grid Layout copied from Grid Layouter Plugin (Falk Schreiber)
 */
public class ResizeAndGridLayoutPlugin
					extends IPK_PluginAdapter {
	
	/**
	 * Creates a new GridLayouterPlugin object.
	 */
	public ResizeAndGridLayoutPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			this.algorithms = new Algorithm[] {
								new GridLayoutAlgorithm(),
								new ResizeFromMappingCountLayoutAlgorithm(),
								new ArrayLayout()
			};
	}
}
