/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.radial_tree;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * Plugin for a radial tree layouter algorithm
 * 
 * @author Joerg Bartelheimer
 */
public class RadialTreeLayoutPlugin extends GenericPluginAdapter {
	
	/**
	 * Creates a new GraphTreeLayoutPlugin object.
	 */
	public RadialTreeLayoutPlugin() {
		this.algorithms = new Algorithm[1];
		this.algorithms[0] = new RadialTreeLayout();
	}
	
}
