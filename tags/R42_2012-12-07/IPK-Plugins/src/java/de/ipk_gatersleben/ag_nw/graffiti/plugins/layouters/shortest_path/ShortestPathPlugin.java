/*******************************************************************************
 * Copyright (c) 2008 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 2.3.2008
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.shortest_path;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author Christian Klukas
 */
public class ShortestPathPlugin extends GenericPluginAdapter {
	public ShortestPathPlugin() {
		this.algorithms = new Algorithm[1];
		this.algorithms[0] = new ShortestPathAlgorithm();
	}
	
}
