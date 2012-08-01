/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * Plugin container for the CircleLayoutAlgorithm.
 * 
 * @author Dirk Koschützki, Christian Klukas
 */
public class CircleLayouterPlugin
					extends IPK_PluginAdapter {
	
	/**
	 * Creates a new CircleLayouterPlugin object.
	 */
	public CircleLayouterPlugin() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.RELEASE_PUBLIC) {
			this.algorithms = new Algorithm[] {
								new CircleLayouterAlgorithm(),
								new NullLayoutAlgorithm(),
								new DotLayoutAlgorithm(),
					// new CircleLayouterWithMinimumCrossingsAlgorithm()
					};
		} else {
			this.algorithms = new Algorithm[] {
								new CircleLayouterAlgorithm(),
								new NullLayoutAlgorithm(),
								new DotLayoutAlgorithm(),
					// new CircleLayouterWithMinimumCrossingsAlgorithm()
					};
		}
		// this.algorithms[2] = new CountCircularCrossingsAlgorithm();
	}
}
