/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.swig_cola;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class ColaLayouterPlugin
					extends IPK_PluginAdapter {
	
	public ColaLayouterPlugin() {
		// if (ReleaseInfo.getRunningReleaseStatus()==Release.RELEASE_IPK)
		this.algorithms = new Algorithm[] {
							new ColaLayouterAlgorithm(),
							new SBGNcolaLayouterAlgorithm()
		};
	}
}
