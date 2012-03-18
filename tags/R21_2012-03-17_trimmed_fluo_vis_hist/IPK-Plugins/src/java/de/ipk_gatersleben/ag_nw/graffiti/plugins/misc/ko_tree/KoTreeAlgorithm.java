/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: KoTreeAlgorithm.java,v 1.1 2011-01-31 09:00:45 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.ko_tree;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.RTTreeLayout;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * Create the full KO tree
 * 
 * @author Christian Klukas
 *         12. Oct. 2007
 */
public class KoTreeAlgorithm
					extends AbstractAlgorithm {
	
	private boolean includePathway = true;
	private boolean includeBrite = true;
	private boolean includeOther = true;
	private boolean oneRoot = true;
	
	public void execute() {
		
		final BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
							"Analyze KO class information", "Please wait");
		
		final HashSet<Node> newNodes = new HashSet<Node>();
		final HashSet<Edge> newEdges = new HashSet<Edge>();
		
		BackgroundTaskHelper.issueSimpleTask(
							getName(), status.getCurrentStatusMessage1(),
							getWorkProcess(graph, status, newNodes, newEdges),
							new Runnable() {
								public void run() {
									status.setCurrentStatusText1("Update view");
									status.setCurrentStatusText2("Please wait");
									GraphHelper.postUndoableNodeAndEdgeAdditions(graph, newNodes, newEdges, getName());
									MainFrame.showMessage("Added " + newNodes.size() + " nodes and " + newEdges.size() + " edges to the graph", MessageType.INFO);
								}
								
							}, status);
		
	}
	
	private Runnable getWorkProcess(
						final Graph graph,
						final BackgroundTaskStatusProviderSupportingExternalCall status,
						final HashSet<Node> newNodes,
						final HashSet<Edge> newEdges) {
		return new Runnable() {
			public void run() {
				status.setCurrentStatusText1("Analyze KO database file...");
				status.setCurrentStatusText2("Enumerate Class Information");
				
				Collection<String> classes = KoService.getCompleteClassInformation();
				
				int startX = 100;
				int stepX = 200;
				int startY = 40;
				int stepY = 0;
				
				status.setCurrentStatusText1("Create KO Class Tree");
				status.setCurrentStatusText2("");
				
				HashMap<String, Node> hierarchy_tree_itemName2node = new HashMap<String, Node>();
				
				for (String hierarchyInformation : classes) {
					if (oneRoot)
						hierarchyInformation = "KO Classes;" + hierarchyInformation;
					String[] graphH = hierarchyInformation.split(";");
					if (graphH != null && graphH.length > 0) {
						String lastelement = graphH[graphH.length - 1];
						if (!includePathway && lastelement.indexOf("[PATH:") >= 0) {
							continue;
						}
						if (!includeBrite && lastelement.indexOf("[BR:") >= 0) {
							continue;
						}
						if (!includeOther && (lastelement.indexOf("[BR:") < 0 && lastelement.indexOf("[PATH:") < 0)) {
							continue;
						}
					}
					Node lastNode = null;
					String hierarchyEntityName = "";
					for (String hierarchyEntityNameLE : graphH) {
						hierarchyEntityNameLE = hierarchyEntityNameLE.trim();
						hierarchyEntityName += ";" + hierarchyEntityNameLE;
						if (!hierarchy_tree_itemName2node.containsKey(hierarchyEntityName)) {
							Node n = GraphHelper.addNodeToGraph(
												graph,
												startX + hierarchy_tree_itemName2node.size() * stepX,
												startY + hierarchy_tree_itemName2node.size() * stepY,
												1, 150, 30, Color.BLACK, Color.WHITE);
							AttributeHelper.setLabel(n, hierarchyEntityNameLE);
							if (hierarchyEntityNameLE.toLowerCase().indexOf("path:") >= 0) {
								// this should be a map-node
								String id = hierarchyEntityNameLE.substring(
													hierarchyEntityNameLE.toLowerCase().indexOf("path:"),
													hierarchyEntityNameLE.indexOf("]", hierarchyEntityNameLE.toLowerCase().indexOf("path:")));
								KeggGmlHelper.setKeggId(n, id.toLowerCase());
								KeggGmlHelper.setKeggType(n, "map");
								AttributeHelper.setRoundedEdges(n, 15);
							}
							hierarchy_tree_itemName2node.put(hierarchyEntityName, n);
							newNodes.add(n);
						}
						Node hierarchy_tree_node = hierarchy_tree_itemName2node.get(hierarchyEntityName);
						if (lastNode != null) {
							// connect nodes
							if (!lastNode.getNeighbors().contains(hierarchy_tree_node)) {
								Edge edge = graph.addEdge(lastNode, hierarchy_tree_node, true,
													AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
								newEdges.add(edge);
							}
						}
						lastNode = hierarchy_tree_node;
					}
				}
				// layout new nodes using tree layout
				RTTreeLayout tree = new RTTreeLayout();
				Selection sel = new Selection();
				sel.addAll(newNodes);
				tree.attach(graph, sel);
				tree.execute();
			}
			
		};
	}
	
	public String getName() {
		return "Create Complete KO Tree";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active graph window available!");
		if (!KoService.isExternalKoFileAvailable()) {
			throw new PreconditionException("KO Database file not available. Use 'Help/Database Status' for further instructions.");
		}
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"With this command, the KO Database (file) is analyzed as<br>" +
							"follows: The class information provided in the KO database<br>" +
							"file is processed to create a KO hierachy tree.";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new BooleanParameter(oneRoot, "Add Root Node", "If selected, a single artificial root node is created and used."),
							new BooleanParameter(includePathway, "Include Pathway Leaf Nodes", ""),
							new BooleanParameter(includeBrite, "Include BRITE Leaf Nodes", ""),
							new BooleanParameter(includeOther, "Include Other Leaf Nodes", "") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		oneRoot = ((BooleanParameter) params[i++]).getBoolean();
		includePathway = ((BooleanParameter) params[i++]).getBoolean();
		includeBrite = ((BooleanParameter) params[i++]).getBoolean();
		includeOther = ((BooleanParameter) params[i++]).getBoolean();
	}
	
}
