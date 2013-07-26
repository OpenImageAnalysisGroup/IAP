/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.connected_components;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

public class ConnectedComponentPlugin extends GenericPluginAdapter {
	
	public ConnectedComponentPlugin() {
		this.algorithms = new Algorithm[1];
		this.algorithms[0] = new ConnectedComponentLayout();
	}
	
}
