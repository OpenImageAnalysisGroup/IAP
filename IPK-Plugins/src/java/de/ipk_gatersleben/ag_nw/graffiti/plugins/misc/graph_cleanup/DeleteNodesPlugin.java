/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: DeleteNodesPlugin.java,v 1.1 2011-01-31 09:00:59 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.graph_cleanup;

import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * Provides a spring embedder algorithm a la KK.
 * 
 * @version $Revision: 1.1 $
 */
public class DeleteNodesPlugin extends IPK_PluginAdapter {
	
	public DeleteNodesPlugin() {
		if (!SystemAnalysis.isHeadless()) {
			this.algorithms = new Algorithm[2];
			this.algorithms[0] = new DeleteNodesAlgorithm();
			this.algorithms[1] = new NumberNodesAndEdgesAlgorithm();
		}
	}
	
}
