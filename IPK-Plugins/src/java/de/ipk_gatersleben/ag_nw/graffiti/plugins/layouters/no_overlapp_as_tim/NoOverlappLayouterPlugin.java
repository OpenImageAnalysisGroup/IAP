/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.no_overlapp_as_tim;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * Plugin container for Tims no overlapp algorithm AS.
 * 
 * @author Christian Klukas
 */
public class NoOverlappLayouterPlugin
					extends GenericPluginAdapter {
	
	public NoOverlappLayouterPlugin() {
		this.algorithms = new Algorithm[] {
							new NoOverlappLayoutAlgorithmAS()
		};
	}
}
