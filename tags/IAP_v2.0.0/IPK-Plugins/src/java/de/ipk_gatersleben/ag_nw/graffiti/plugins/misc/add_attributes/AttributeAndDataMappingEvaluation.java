/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.add_attributes;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class AttributeAndDataMappingEvaluation extends IPK_PluginAdapter {
	public AttributeAndDataMappingEvaluation() {
		algorithms = new Algorithm[] {
							new AddInterestingAttributes(),
							new ComputeAttributesAlgorithm()
		};
	}
}
