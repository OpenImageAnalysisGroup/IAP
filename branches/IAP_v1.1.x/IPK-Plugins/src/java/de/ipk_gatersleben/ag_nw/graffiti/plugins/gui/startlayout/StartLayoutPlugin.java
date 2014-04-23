/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.startlayout;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class StartLayoutPlugin
					extends IPK_PluginAdapter {
	public StartLayoutPlugin() {
		this.algorithms = new Algorithm[] {
							new StartLayoutAlgorithm()
		};
	}
}
