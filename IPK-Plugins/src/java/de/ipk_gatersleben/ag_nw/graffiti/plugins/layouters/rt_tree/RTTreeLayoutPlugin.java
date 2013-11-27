/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * Plugin for a tree layouter algorithm
 * 
 * @author Joerg Bartelheimer
 */
public class RTTreeLayoutPlugin extends GenericPluginAdapter {
	
	/**
	 * Creates a new GraphTreeLayoutPlugin object.
	 */
	public RTTreeLayoutPlugin() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG)
			this.algorithms = new Algorithm[] {
								new RTTreeLayout(),
								new MultiTreeLayout()
			};
		else
			this.algorithms = new Algorithm[] {
								new RTTreeLayout()
			};
	}
	
}
