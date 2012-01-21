/*******************************************************************************
 * Copyright (c) 2003-2008 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: ReferencesPlugin.java,v 1.1 2011-01-31 09:00:33 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references;

import java.util.HashMap;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

public class ReferencesPlugin
					extends IPK_EditorPluginAdapter {
	
	@SuppressWarnings("unchecked")
	public ReferencesPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.PATHWAY_FILE_REFERENCE)) {
			algorithms = new Algorithm[] {
								new LinkSelection()
			};
			attributes = new Class[] {
								NodePathwayLinkVisualizationAttribute.class
			};
			StringAttribute.putAttributeType("pathway_link_visualization", NodePathwayLinkVisualizationAttribute.class);
			attributeComponents = new HashMap();
			attributeComponents.put(NodePathwayLinkVisualizationAttribute.class, PathwayLinkVisualizationComponent.class);
			valueEditComponents = new HashMap();
			valueEditComponents.put(NodePathwayLinkVisualizationAttribute.class, NodePathwayLinkVisualizationAttributeEditor.class);
		}
	};
}
