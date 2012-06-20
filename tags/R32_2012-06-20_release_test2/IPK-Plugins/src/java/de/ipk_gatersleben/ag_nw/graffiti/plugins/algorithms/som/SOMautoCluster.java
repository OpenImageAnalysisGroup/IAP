/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 05.07.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import org.graffiti.plugin.algorithm.AbstractAlgorithm;

public class SOMautoCluster extends AbstractAlgorithm {
	
	public String getName() {
		return null; // "SOM-Auto-Cluster Data";
		// TODO
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.extension.Extension#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	public void execute() {
		// ToDo
		// distance between nodes within clusters
		// distance between clusters
	}
}
