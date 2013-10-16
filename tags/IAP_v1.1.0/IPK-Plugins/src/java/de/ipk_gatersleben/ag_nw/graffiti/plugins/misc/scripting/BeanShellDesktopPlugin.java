/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.scripting;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * Plugin Adapter for the BeanShell Plugin.
 * 
 * @author Dirk Koschuetzki
 * @author Christian Klukas
 */
public class BeanShellDesktopPlugin
					extends IPK_PluginAdapter {
	
	/**
	 * Creates a new BeanShellDesktopPlugin object.
	 */
	public BeanShellDesktopPlugin() {
		this.algorithms = new Algorithm[1];
		this.algorithms[0] = new BeanShellDesktopAlgorithm();
	}
}
