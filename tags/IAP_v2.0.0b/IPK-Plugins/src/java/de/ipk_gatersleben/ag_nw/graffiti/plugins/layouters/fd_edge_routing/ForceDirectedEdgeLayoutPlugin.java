/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.fd_edge_routing;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author Christian Klukas
 */
public class ForceDirectedEdgeLayoutPlugin
					extends GenericPluginAdapter {
	public ForceDirectedEdgeLayoutPlugin() {
		this.algorithms = new Algorithm[] {
							new ForceDirectedEdgeLayout()
		};
	}
}
