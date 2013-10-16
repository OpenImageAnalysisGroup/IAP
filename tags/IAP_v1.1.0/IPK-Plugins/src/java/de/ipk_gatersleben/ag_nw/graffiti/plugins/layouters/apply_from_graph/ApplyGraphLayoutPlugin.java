/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.apply_from_graph;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author Christian Klukas
 */
public class ApplyGraphLayoutPlugin
					extends GenericPluginAdapter {
	public ApplyGraphLayoutPlugin() {
		this.algorithms = new Algorithm[] {
							new ApplyGraphLayout(),
							new ShowLayoutTab()
		};
	}
}
