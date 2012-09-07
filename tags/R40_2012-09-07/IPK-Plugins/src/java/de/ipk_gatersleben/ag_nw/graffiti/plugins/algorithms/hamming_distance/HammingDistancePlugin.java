/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.hamming_distance;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * Plugin for Hamming distance algorithm
 * 
 * @author Falk Schreiber
 */
public class HammingDistancePlugin
					extends GenericPluginAdapter {
	
	/**
	 * Creates a new HammingDistancePlugin object.
	 */
	public HammingDistancePlugin() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG)
			this.algorithms = new Algorithm[] {
								new HammingDistanceAlgorithm()
			};
	}
}
