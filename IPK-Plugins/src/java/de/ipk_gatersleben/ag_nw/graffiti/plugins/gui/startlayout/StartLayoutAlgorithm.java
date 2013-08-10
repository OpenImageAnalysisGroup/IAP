/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.startlayout;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.services.AlgorithmServices;

/**
 * @author Christian Klukas
 */
public class StartLayoutAlgorithm extends AbstractAlgorithm {
	
	public String getName() {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_LAYOUT))
			return "Layout Network";
		else
			return null;
	}
	
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	/**
	 * Performs the layout.
	 */
	public void execute() {
		AlgorithmServices.selectAndRunLayoutAlgorithm(graph,
							selection,
							"Layout Network", true);
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
}
