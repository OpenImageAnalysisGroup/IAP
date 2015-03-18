/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.expand_no_overlapp;

import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * @author Christian Klukas
 */
public class NoOverlappLayouterPlugin
					extends GenericPluginAdapter {
	public NoOverlappLayouterPlugin() {
		this.algorithms = new Algorithm[] {
							new NoOverlappLayoutAlgorithm()
		};
	}
}
