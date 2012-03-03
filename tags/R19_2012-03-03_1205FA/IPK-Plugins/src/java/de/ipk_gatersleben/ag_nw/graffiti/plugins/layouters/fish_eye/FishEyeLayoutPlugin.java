/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.11.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.fish_eye;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * Plugin for a fish eye layouter algorithm
 * 
 * @author Joerg Bartelheimer
 */
public class FishEyeLayoutPlugin extends GenericPluginAdapter {
	
	/**
	 * Creates a new FishEyeLayoutPlugin object.
	 */
	public FishEyeLayoutPlugin() {
		this.algorithms = new Algorithm[1];
		this.algorithms[0] = new FishEyeLayout();
	}
}
