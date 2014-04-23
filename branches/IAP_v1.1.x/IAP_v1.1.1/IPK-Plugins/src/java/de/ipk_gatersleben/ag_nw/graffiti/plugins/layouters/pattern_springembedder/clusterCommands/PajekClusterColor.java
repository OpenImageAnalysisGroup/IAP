/****************
 * c***************************************************************
 * Copyright ) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorParameter;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class PajekClusterColor extends AbstractAlgorithm {
	
	ClusterColorAttribute cca;
	
	private static String modeNode = "Colorize Nodes and Edges";
	private static String modeSurr = "Colorize Surrounding of Nodes";
	
	private String modeOfOperation = modeNode;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Pathway-Subgraph Coloring";
		else
			return "Color Clusters";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		Set<String> clusters = new TreeSet<String>();
		for (GraphElement n : graph.getGraphElements()) {
			String clusterId = NodeTools.getClusterID(n, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
		}
		if (clusters.size() <= 0)
			throw new PreconditionException(
								"No cluster information available for this graph!");
	}
	
	@Override
	public Parameter[] getParameters() {
		Graph g = graph;
		Set<String> clusters = new TreeSet<String>();
		for (GraphElement n : g.getGraphElements()) {
			String clusterId = NodeTools.getClusterID(n, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
		}
		
		ClusterColorAttribute cca = (ClusterColorAttribute) AttributeHelper
							.getAttributeValue(g, ClusterColorAttribute.attributeFolder,
												ClusterColorAttribute.attributeName, ClusterColorAttribute
																	.getDefaultValue(clusters.size()),
												new ClusterColorAttribute("resulttype"), false);
		
		if (cca.getClusterColors() != null && cca.getClusterColors().size() > clusters.size()) {
			cca.trimColorSelection(clusters.size());
		}
		
		cca.ensureMinimumColorSelection(clusters.size());
		ClusterColorAttribute cca_new = new ClusterColorAttribute(
							ClusterColorAttribute.attributeName, cca.getString());
		ClusterColorParameter op = new ClusterColorParameter(cca_new, "Cluster-Colors", ClusterColorAttribute.desc);
		
		ArrayList<String> modeList = new ArrayList<String>();
		modeList.add(modeNode);
		modeList.add(modeSurr);
		ObjectListParameter modeParam = new ObjectListParameter(modeOfOperation, "Visualization Mode",
							"Use either the node fill color or a coloring of the node surrounding to visualize different clusters.",
							modeList);
		
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return new Parameter[] { op };
		else
			return new Parameter[] { op, modeParam };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		ClusterColorAttribute cca = (ClusterColorAttribute) ((ClusterColorParameter) params[0]).getValue();
		cca = (ClusterColorAttribute) cca.copy();
		if (graph.getAttributes().getCollection().containsKey(cca.getPath()))
			graph.removeAttribute(cca.getPath());
		graph.addAttribute(cca, ClusterColorAttribute.attributeFolder);
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			modeOfOperation = (String) ((ObjectListParameter) params[1]).getValue();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.extension.Extension#getCategory()
	 */
	@Override
	public String getCategory() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Nodes";
		else
			return "Cluster";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Please select the desired colors for the different subsets of graph elements,<br>" +
							"depending on their cluster Id.<br>" +
							"The first button row determines the fill color for nodes of the particular subset<br>" +
							"and the line color for edges.<br>" +
							"The second row determines the border coloring for nodes.<br>" +
							"If selected, edges with assigned cluster IDs are also colored.";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		Graph g = graph;
		try {
			g.getListenerManager().transactionStarted(this);
			ClusterColorAttribute cca = (ClusterColorAttribute) AttributeHelper.getAttributeValue(g, ClusterColorAttribute.attributeFolder,
								ClusterColorAttribute.attributeName, ClusterColorAttribute.getDefaultValue(1), new ClusterColorAttribute("resulttype"));
			
			if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR || modeOfOperation.equals(modeNode))
				executeClusterColoringOnGraph(g, cca);
			if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR
								&& modeOfOperation.equals(modeSurr)) {
				AttributeHelper.setAttribute(graph, "", "background_coloring", new Boolean(true));
			}
			if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR
								&& modeOfOperation.equals(modeNode)) {
				AttributeHelper.setAttribute(graph, "", "background_coloring", new Boolean(false));
			}
			
			Graph emptyGraph = new AdjListGraph();
			Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(g, "cluster", "clustergraph", emptyGraph, new AdjListGraph(), false);
			if (clusterGraph != emptyGraph) {
				executeClusterColoringOnGraph(clusterGraph, cca);
			}
		} finally {
			g.getListenerManager().transactionFinished(this);
		}
	}
	
	public static void executeClusterColoringOnGraph(Graph g, ClusterColorAttribute cca) {
		Set<String> clusters = new TreeSet<String>();
		for (GraphElement ge : g.getGraphElements()) {
			String clusterId = NodeTools.getClusterID(ge, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
		}
		cca.ensureMinimumColorSelection(clusters.size());
		String[] clusterValues = clusters.toArray(new String[0]);
		g.getListenerManager().transactionStarted(g);
		try {
			for (GraphElement ge : g.getGraphElements()) {
				String clusterId = NodeTools.getClusterID(ge, "");
				if (!clusterId.equals("")) {
					for (int i = 0; i < clusterValues.length; i++) {
						if (clusterValues[i].equals(clusterId)) {
							AttributeHelper.setFillColor(ge, cca.getClusterColor(i));
							if (ge instanceof Edge)
								AttributeHelper.setOutlineColor(ge, cca.getClusterColor(i));
							else
								AttributeHelper.setOutlineColor(ge, cca.getClusterOutlineColor(i));
						}
					}
				}
			}
		} finally {
			g.getListenerManager().transactionFinished(g);
		}
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return false; // would only work if all open graphs contain the same set of clusters
	}
}
