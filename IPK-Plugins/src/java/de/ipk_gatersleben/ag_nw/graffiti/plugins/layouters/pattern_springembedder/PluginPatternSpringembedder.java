/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder;

import org.graffiti.attributes.AttributeDescription;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.BendsLaunchGUI;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.BringToFrontOrBackAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.BundleParallelEdges;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.DuplicateEdge;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.IntroduceParallelEdgeBends;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.ResizeNodesDepDegreeAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random.RandomLayouterAlgorithm;

/**
 * @author Christian Klukas
 */
public class PluginPatternSpringembedder
					extends IPK_PluginAdapter {
	
	public PluginPatternSpringembedder() {
		this.attributeDescriptions = new AttributeDescription[] {
							new AttributeDescription("background_coloring",
												BooleanAttribute.class, "Cluster-Coloring: Background-Coloring", true, true, null)
		};
		
		this.algorithms = new Algorithm[] {
							new PatternSpringembedder(),
							// new CreateClusterGraphAlgorithm(),
				// new ShowClusterGraphAlgorithm(),
				// new AddRandomClusterInformationAlgorithm(),
				// new PajekClusterColor(),
				// new ClusterIndividualLayout(),
				// new ClusterGraphLayout(),
				// new SelectClusterAlgorithm(),
				//
				// new SetCluster(),
				
				new ResizeNodesDepDegreeAlgorithm(),
							new BendsLaunchGUI(),
							new BundleParallelEdges(),
							new IntroduceParallelEdgeBends(),
							// new ResetEdgeSourceOrTarget(),
				new DuplicateEdge(),
							new BringToFrontOrBackAlgorithm(false),
							new BringToFrontOrBackAlgorithm(true),
							new RandomLayouterAlgorithm(),
							new PatternSpringembedder3d()
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.GenericPlugin#configure(java.util.prefs.Preferences)
	 */
	@Override
	public void configure(GravistoPreferences p) {
		super.configure(p);
	}
}
