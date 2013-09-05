/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JLabel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.OpenFileDialogService;
import org.PositionGridGenerator;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.RemoveSelectedNodesPreserveEdgesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class InterpreteGOtermsAlgorithm extends AbstractAlgorithm {
	
	private GoProcessing gp = null;
	
	private int maxDepth = 0;
	private boolean useMinDistance = true;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Create Data-Specific Gene Ontology Network";
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"This command creates a node- and data-specific GO hierarchy network.<br>" +
							"The node labels and the alternative data identifiers of mapped data<br>" +
							"(if available) are processed. In case a GO term is identified, it is<br>" +
							"directly processed. In case KO term (also EC and Gene Identifiers, related<br>" +
							"to a specific KO entry are recognized) is identified, the corresponding GO<br>" +
							"annotation (if available) is processed.<br>" +
							"<br>" +
							"You may limit the maximum distance from the (virtual) GO root node to the<br>" +
							"working set of nodes. In this case, the nodes will be connected to more general<br>" +
							"GO terms, instead of a particular defined GO term.<br>" +
							"<br>" +
							"For layouting the resulting network, the DOT layout most times gives good results.";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new IntegerParameter(maxDepth,
												"Max. distance from root-node (0=unlimited)",
												"<html>" +
																	"If set to a value larger than 0, the maximum distance from the virtual root node may <br>" +
																	"be specified with this parameter"),
							new BooleanParameter(useMinDistance,
												"Consider Minimum Distance",
												"<html>" +
																	"If selected, the minimum distance will be considered for maximum depth of hierarchy,<br>" +
																	"otherwise, the maximum distance.") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		IntegerParameter ip = (IntegerParameter) params[i++];
		maxDepth = ip.getInteger();
		if (maxDepth < 0)
			maxDepth = 0;
		BooleanParameter bp = (BooleanParameter) params[i++];
		useMinDistance = bp.getBoolean().booleanValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		if (gp == null) {
			File obofile = OpenFileDialogService.getFile(new String[] { ".obo-xml" }, "Gene Ontology File (*.obo-xml)");
			if (obofile == null)
				return;
			gp = new GoProcessing(obofile);
			if (!gp.isValid()) {
				gp = null;
				MainFrame.showMessageDialog("The input file could not be loaded. It may not be a valid gene-ontology obo-xml file!", "Error");
				return;
			}
		}
		final List<NodeHelper> workNodes = NodeHelper.getNodeHelperList(getSelectedOrAllNodes());
		final BackgroundTaskStatusProviderSupportingExternalCallImpl sp = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Initialize...", "");
		final Graph fgraph = graph;
		BackgroundTaskHelper.issueSimpleTask("Interpret GO - IDs", "Initialize...",
							new Runnable() {
								public void run() {
									fgraph.getListenerManager().transactionStarted(this);
									interpreteGO(fgraph, workNodes, sp);
									fgraph.getListenerManager().transactionFinished(this);
								}
							},
							null, sp);
	}
	
	private void interpreteGO(Graph graph, List<NodeHelper> workNodes, BackgroundTaskStatusProviderSupportingExternalCall sp) {
		HashMap<String, Node> goTerm2goNode = new HashMap<String, Node>();
		HashSet<Node> knownNodes = new HashSet<Node>();
		sp.setCurrentStatusText1("Process graph nodes...");
		sp.setCurrentStatusText1("Enumerate existing GO-Term nodes...");
		for (Node n : graph.getNodes()) {
			knownNodes.add(n);
			NodeHelper nh = new NodeHelper(n);
			String goLabel = (String) nh.getAttributeValue("go", "term", null, "");
			if (goLabel != null) {
				goTerm2goNode.put(goLabel, n);
			}
		}
		sp.setCurrentStatusText1("Enumerate Data Mapping and Node IDs...");
		PositionGridGenerator pgg = new PositionGridGenerator(250, 30, 250);
		double current = 0;
		double workload = workNodes.size();
		for (NodeHelper nh : workNodes) {
			Collection<String> ids = nh.getAlternativeIDs();
			ids.add(nh.getLabel());
			HashSet<String> goTerms = new HashSet<String>();
			String lbl = nh.getLabel();
			if (lbl != null && lbl.toUpperCase().startsWith("GO:"))
				goTerms.add(lbl);
			for (String test : ids) {
				if (test.toUpperCase().startsWith("GO:"))
					goTerms.add(test.toUpperCase());
				else {
					boolean valid = false;
					try {
						int n = Integer.parseInt(test);
						String go = GoProcessing.getCorrectGoTermFormat("GO:" + n);
						goTerms.add(go);
						valid = true;
					} catch (Exception e) {
						// empty
					}
					if (!valid) {
						EnzymeEntry eze = EnzymeService.getEnzymeInformation(test, false);
						if (eze != null) {
							String id = eze.getID();
							for (KoEntry koe : KoService.getKoFromEnzyme(id)) {
								for (String goID : koe.getKoDbLinks("GO")) {
									goTerms.add("GO:" + goID);
								}
							}
						} else {
							for (KoEntry koe : KoService.getKoFromGeneIdOrKO(test)) {
								for (String goID : koe.getKoDbLinks("GO")) {
									goTerms.add("GO:" + goID);
								}
							}
						}
					}
				}
			}
			HashSet<String> correctedGoTerms = new HashSet<String>();
			for (String g : goTerms)
				correctedGoTerms.add(GoProcessing.getCorrectGoTermFormat(g));
			if (goTerms.size() > 0) {
				for (String goTerm : correctedGoTerms) {
					connectNodeWithNodes(
										nh.getGraphNode(),
										processGoHierarchy(
															pgg, goTerm2goNode, gp, goTerm, nh.getGraphNode().getGraph()),
										"annotation");
				}
			}
			current += 1;
			sp.setCurrentStatusValueFine(current / workload * 100d);
			sp.setCurrentStatusText2("Check node " + (int) current + "/" + (int) workload + "...");
		}
		sp.setCurrentStatusText1("GO Hierarchy Created");
		sp.setCurrentStatusText2("");
		Node rootNode = goTerm2goNode.get("GO:0000000");
		if (maxDepth > 0) {
			sp.setCurrentStatusText2("Enumerate GO - Levels");
			// limit depth of go hierarchy
			ArrayList<Node> toBeDeleted = new ArrayList<Node>();
			for (Node n : graph.getNodes()) {
				if (!knownNodes.contains(n)) {
					// new go node
					if (getDepth(n, rootNode, useMinDistance) > maxDepth)
						toBeDeleted.add(n);
				}
			}
			sp.setCurrentStatusText2("Remove deeper GO - Levels (" + toBeDeleted + " nodes)");
			int cnt = RemoveSelectedNodesPreserveEdgesAlgorithm.removeNodesPreserveEdges(
								toBeDeleted, graph, false, sp);
			sp.setCurrentStatusText2("Created GO hiearchy with a maximum depth of " + maxDepth + " (removed " + cnt + " GO term nodes)");
		}
		sp.setCurrentStatusText1("Processing completed");
		if (maxDepth <= 0)
			sp.setCurrentStatusText2("Created (unlimited) GO hiearchy");
		try {
			Selection sel = new Selection("root node selection");
			if (MainFrame.getInstance().getActiveEditorSession().getGraph() == graph) {
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private int getDepth(Node n, Node rootNode, boolean minDist) {
		if (n == rootNode)
			return 0;
		if (minDist) {
			int minDepth = Integer.MAX_VALUE;
			for (Node upNode : n.getAllInNeighbors()) {
				int depth = getDepth(upNode, rootNode, minDist);
				if (depth < minDepth)
					minDepth = depth;
			}
			return minDepth + 1;
		} else {
			int maxDepth = Integer.MIN_VALUE;
			for (Node upNode : n.getAllInNeighbors()) {
				int depth = getDepth(upNode, rootNode, minDist);
				if (depth > maxDepth)
					maxDepth = depth;
			}
			return maxDepth + 1;
		}
	}
	
	public static Node processGoHierarchy(PositionGridGenerator pgg, HashMap<String, Node> goTerm2goNode, GoProcessing gp, String goTerm, Graph g) {
		Node gn;
		if (!goTerm2goNode.containsKey(goTerm)) {
			GOinformation gi = gp.getGOinformation(goTerm);
			if (gi == null)
				return null;
			gn = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(100d + Math.random() * 100d, 100d + Math.random() * 100d));
			NodeHelper gnh = new NodeHelper(gn);
			String name = gi.getName();
			if (name == null)
				name = goTerm;
			gnh.setLabel(name);
			gnh.setPosition(pgg.getNextPosition());
			gnh.setClusterID(gi.getNamespace());
			gnh.setTooltip(gi.getDefStr());
			gnh.setAttributeValue("go", "term", goTerm);
			gnh.setAttributeValue("go", "obsolete", gi.isObsolete() ? 1 : 0);
			if (gi.isObsolete())
				AttributeHelper.setDashInfo(gnh, 10, 10);
			Dimension d = new JLabel(name).getPreferredSize();
			if (d.getHeight() < 50 && d.getWidth() < 2000)
				gnh.setSize(d.getWidth() + 15, d.getHeight() + 15);
			goTerm2goNode.put(goTerm, gn);
			Collection<String> parents = gi != null ? gi.getDirectParents() : new ArrayList<String>();
			for (String gt : parents)
				connectNodeWithNodes(gn, processGoHierarchy(pgg, goTerm2goNode, gp, gt, g), "is_a");
			Collection<String> part_of = gi != null ? gi.getPartOf() : new ArrayList<String>();
			for (String gt : part_of)
				connectNodeWithNodes(gn, processGoHierarchy(pgg, goTerm2goNode, gp, gt, g), "part_of");
		}
		gn = goTerm2goNode.get(goTerm);
		return gn;
	}
	
	private static void connectNodeWithNodes(Node goNode, Node newGoNode, String type) {
		if (goNode == null || newGoNode == null)
			return;
		if (!goNode.getNeighbors().contains(newGoNode)) {
			Edge e = goNode.getGraph().addEdge(newGoNode, goNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
			AttributeHelper.setBorderWidth(e, 2d);
			AttributeHelper.setAttribute(e, "go", "relationtype", type);
			if (type.equals("part_of"))
				AttributeHelper.setOutlineColor(e, Color.RED);
			if (type.equals("annotation"))
				AttributeHelper.setOutlineColor(e, Color.MAGENTA);
			else
				AttributeHelper.setOutlineColor(e, Color.BLUE);
		}
	}
}
