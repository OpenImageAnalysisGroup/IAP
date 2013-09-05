/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.FeatureSet;
import org.HelperClass;
import org.ObjectRef;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelperBio;
import de.ipk_gatersleben.ag_nw.graffiti.KeggSoapAndPathwayService;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ProjectEntity;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithXMLexperimentData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TabDBE;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.MapResult;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.NoOverlappOfClustersAlgorithm;

@SuppressWarnings("unchecked")
public class KeggService implements BackgroundTaskStatusProvider, HelperClass {
	
	private int progressVal = -1;
	
	private String status1 = "";
	
	private String status2 = "";
	
	private boolean pleaseStop = false;
	
	public static void writeCompoundList(Collection<Node> nodeList) {
		TextFile tf = new TextFile();
		TreeMap<String, NodeHelper> id2nh = new TreeMap<String, NodeHelper>();
		for (Node n : nodeList) {
			NodeHelper nh = new NodeHelper(n);
			String id = (String) nh.getAttributeValue("kegg", "kegg_name", "", "");
			if (!id2nh.containsKey(id))
				id2nh.put(id, nh);
		}
		tf.add("ROW" + "\t" + "ID" + "\t" + "MASS" + "\t" + "FORMULA" + "\t"
				+ "NAME");
		int row = 1;
		for (String id : id2nh.keySet()) {
			NodeHelper nh = id2nh.get(id);
			String mass = (String) nh.getAttributeValue("kegg", "mass", "NA", "");
			String formula = (String) nh.getAttributeValue("kegg", "formula",
					"NA", "");
			String name = nh.getLabel();
			tf.add(getNumString("" + row++, 3) + "\t" + id + "\t" + mass + "\t"
					+ formula + "\t" + name);
		}
		try {
			tf.write("/home/klukas/steffen.txt");
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private static String getNumString(String s, int len) {
		while (s.length() < len)
			s = "0" + s;
		return s;
	}
	
	/**
	 * If loadBlockingIntoThisGraph is != null, this method adds the loaded graph
	 * to the given graph instance. If this parameter is NULL (default), then
	 * this method returns immediately and opens a new graph window editor.
	 * 
	 * @param myEntry
	 * @param loadBlockingIntoThisGraph
	 */
	public static boolean loadKeggPathwayIntoEditor(
			final KeggPathwayEntry myEntry, final Graph loadBlockingIntoThisGraph,
			final Color enzymeColor, final boolean separateClusters,
			final boolean processLabels) {
		final ObjectRef objref = new ObjectRef();
		objref.setObject(new Boolean(true));
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Graph myGraph = null;
				try {
					myGraph = KeggService.getKeggPathwayGravistoGraph(myEntry,
							true, enzymeColor);
					
					if (processLabels) {
						MainFrame.showMessage("Interpret database identifiers",
								MessageType.PERMANENT_INFO);
						KeggSoapAndPathwayService.interpreteDatabaseIdentifiers(
								myGraph.getNodes(), true, false);
					}
					
					MainFrame.showMessage(
							"Pathway loaded and processed, creating view...",
							MessageType.INFO);
					if (loadBlockingIntoThisGraph == null) {
						final Graph myGraphF = myGraph;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								MainFrame.getInstance().showGraph(myGraphF, null);
							}
						});
					} else {
						loadBlockingIntoThisGraph.setName(myGraph.getName(true));
						ArrayList<GraphElement> knownElements = new ArrayList<GraphElement>();
						knownElements.addAll(loadBlockingIntoThisGraph
								.getGraphElements());
						mergeKeggGraphs(loadBlockingIntoThisGraph, myGraph, myEntry
								.getTargetPosition(), true, separateClusters);
						Selection sel = new Selection("");
						for (GraphElement ge : loadBlockingIntoThisGraph
								.getGraphElements()) {
							if (!knownElements.contains(ge))
								sel.add(ge);
						}
						try {
							if (MainFrame.getInstance().getActiveEditorSession() != null) {
								if (MainFrame.getInstance().getActiveEditorSession()
										.getGraph() == loadBlockingIntoThisGraph) {
									MainFrame.getInstance().getActiveEditorSession()
											.getSelectionModel().setActiveSelection(sel);
									MainFrame.getInstance().getActiveEditorSession()
											.getSelectionModel().selectionChanged();
								}
							}
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				String detailInfo;
				if (myGraph == null) {
					detailInfo = "Graph instance not created";
					objref.setObject(new Boolean(false));
				} else {
					detailInfo = "graph with " + myGraph.getNumberOfNodes()
							+ " nodes and " + myGraph.getNumberOfEdges()
							+ " edges created";
					objref.setObject(new Boolean(true));
				}
				MainFrame.showMessage("Processing complete (" + detailInfo + ")",
						MessageType.INFO);
			}
		});
		if (loadBlockingIntoThisGraph == null) {
			// if (true)
			t.run();
			// else
			// BackgroundTaskHelper.issueSimpleTask(
			// "Load KEGG Pathway",
			// "Pathway: "+myEntry.getPathwayName(),
			// "Please wait until the processing is finished.",
			// new Runnable() {
			// public void run() {
			// t.run();
			// }},
			// null,
			// true);
		} else {
			t.setName("Pathway Loading");
			t.run(); // non interactive! the thread is not really used or created
		}
		return (Boolean) objref.getObject();
	}
	
	public static void mergeKeggGraphs(Graph mergeIntoThisGraph, Graph newGraph,
			Vector2d centerOfNewGraph, boolean linkToExistingMaps,
			boolean clusterSeparation) {
		Collection<GraphElement> addedNodesAndEdges = mergeIntoThisGraph
				.addGraph(newGraph);
		if (centerOfNewGraph != null) {
			Vector2d currentCenter = NodeTools.getCenter(addedNodesAndEdges);
			for (GraphElement ge : addedNodesAndEdges) {
				if (ge instanceof Node) {
					NodeHelper nh = new NodeHelper((Node) ge);
					nh.setPosition(nh.getX() - currentCenter.x + centerOfNewGraph.x,
							nh.getY() - currentCenter.y + centerOfNewGraph.y);
				}
			}
		}
		List<Node> toBeDeleted = new ArrayList<Node>();
		for (Node n : mergeIntoThisGraph.getNodes()) {
			NodeHelper nh = new NodeHelper(n);
			String kegg_map_link = null;
			String kid = KeggGmlHelper.getKeggId(n);
			if (kid != null && kid.startsWith("path:"))
				kegg_map_link = kid;
			if (kegg_map_link != null) {
				kegg_map_link = StringManipulationTools.stringReplace(kegg_map_link, "path:", "");
				for (Edge e : nh.getEdges()) {
					Node a = e.getSource();
					if (a == n)
						a = e.getTarget();
					String kegg_name_source = kid;
					for (Node searchNode : mergeIntoThisGraph.getNodes()) {
						NodeHelper snh = new NodeHelper(searchNode);
						String sourcePathwayName = snh.getClusterID(null);
						if (sourcePathwayName != null
								&& sourcePathwayName.equalsIgnoreCase(kegg_map_link)) {
							String kegg_name_target = null;
							String skid = KeggGmlHelper.getKeggId(searchNode);
							if (skid != null && skid.startsWith("path:"))
								kegg_name_target = skid;
							if (kegg_name_source != null
									&& kegg_name_target != null
									&& kegg_name_source
											.equalsIgnoreCase(kegg_name_target)) {
								Edge ne = GraphHelperBio.addEdgeCopyIfNotExistant(e, a,
										searchNode);
								AttributeHelper.setArrowtail(ne, false);
								AttributeHelper.setArrowhead(ne, false);
								if (!toBeDeleted.contains(n))
									toBeDeleted.add(n);
							}
						}
					}
				}
			}
		}
		
		if (linkToExistingMaps) {
			HashMap<String, Node> mapName2node = new HashMap<String, Node>();
			for (NodeHelper nh : GraphHelper.getHelperNodes(mergeIntoThisGraph)) {
				String keggType = (String) nh.getAttributeValue("kegg",
						"kegg_type", null, "");
				if (keggType != null && keggType.equalsIgnoreCase("map")) {
					String kegg_map_link = (String) nh.getAttributeValue("kegg",
							"kegg_map_link", null, "");
					if (kegg_map_link != null && kegg_map_link.length() > 0) {
						kegg_map_link = StringManipulationTools.stringReplace(kegg_map_link,
								"path:", "");
						if (mapName2node.containsKey(kegg_map_link)) {
							Node existingMapNode = mapName2node.get(kegg_map_link);
							for (Edge e : nh.getEdges()) {
								mergeIntoThisGraph.addEdgeCopy(e, existingMapNode, (e
										.getSource() == nh.getGraphNode() ? e.getTarget()
										: e.getSource()));
							}
							if (!toBeDeleted.contains(nh.getGraphNode()))
								toBeDeleted.add(nh.getGraphNode());
						} else {
							mapName2node.put(kegg_map_link, nh.getGraphNode());
						}
					}
				}
			}
		}
		
		for (Node del : toBeDeleted)
			mergeIntoThisGraph.deleteNode(del);
		
		if (clusterSeparation) {
			Algorithm layout = new NoOverlappOfClustersAlgorithm();
			layout.attach(mergeIntoThisGraph, new Selection("empty"));
			try {
				layout.check();
				layout.execute();
			} catch (PreconditionException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	public static void loadPathway(KeggPathwayEntry myEntry,
			boolean processLabels) {
		loadPathway(myEntry, null, processLabels);
	}
	
	public static void loadPathway(KeggPathwayEntry myEntry, Graph targetGraph,
			boolean processLabels) {
		loadPathway(myEntry, targetGraph, null, processLabels);
	}
	
	public static void loadPathway(KeggPathwayEntry myEntry, Graph targetGraph,
			Node initialMapNode, boolean processLabels) {
		loadPathway(myEntry, targetGraph, initialMapNode, true, false,
				processLabels);
	}
	
	public static void loadPathway(KeggPathwayEntry myEntry, Graph targetGraph,
			Node initialMapNode, boolean askForNewWindow,
			boolean separateClusters, boolean processLabels) {
		Object[] input;
		if (!myEntry.isReferencePathway()) {
			if (!askForNewWindow)
				input = new Object[] { new Boolean(false), new Boolean(false) };
			else {
				if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS_ENH)) {
					if (targetGraph == null)
						input = new Object[] { new Boolean(false), new Boolean(true) };
					else
						input = MyInputHelper.getInput(
								"Please specify if you would like to load the pathway in a new window<br>"
										+ "or if you would like to add it to the current graph.",
								"Load graph in new window?", new Object[] {
										null,
										null,
										(targetGraph == null ? null
												: "Load Graph in new Window?"),
										(targetGraph == null ? null : new Boolean(
												true)) });
				} else
					input = MyInputHelper.getInput(
							"Please specify if you would like to load the reference pathway<br>"
									+ "and use a color-code to mark organism-specific enzymes or if you<br>"
									+ "would like to load a organism-specific pathway.<br><br>"
									+ "The pathway with colored organism-specific enzymes looks closer<br>"
									+ "to the corresponding KEGG Pathway image. This is therefore the<br>"
									+ "recommended setting.",
							"Colorize organism-specific Enzymes, load in new Window?",
							new Object[] {
									"Colorize organism-specific Enzymes?",
									new Boolean(true),
									(targetGraph == null ? null
											: "Load Graph in new Window?"),
									(targetGraph == null ? null
											: new Boolean(true)) // ,
							// "Enzyme-Color",
							// KeggService.getDefaultEnzymeColor()
							});
			}
		} else {
			if (!askForNewWindow)
				input = new Object[] { new Boolean(false), new Boolean(false) };
			else
				if (targetGraph == null)
					input = new Object[] { new Boolean(false), new Boolean(true) };
				else
					input = MyInputHelper.getInput(
							"Please specify if you would like to load the pathway in a new window<br>"
									+ "or if you would like to add it to the current graph.",
							"Load graph in new window?",
							new Object[] {
									null,
									null,
									(targetGraph == null ? null
											: "Load Graph in new Window?"),
									(targetGraph == null ? null : new Boolean(true)) });
		}
		boolean deleteInitialMapNode = false;
		if (input != null) {
			boolean loadingOK;
			Boolean useColor = (Boolean) input[0];
			Boolean loadIntoWindow = (Boolean) input[1];
			if (loadIntoWindow == null || loadIntoWindow)
				targetGraph = null;
			else {
				deleteInitialMapNode = true;
			}
			// String s = (String) input[1];
			Color enzymeColor = KeggService.getDefaultEnzymeColor();
			if (useColor != null && useColor.booleanValue()) {
				loadingOK = KeggService.loadKeggPathwayIntoEditor(
						new KeggPathwayEntry(myEntry, true), targetGraph,
						enzymeColor, separateClusters, processLabels);
			} else {
				loadingOK = KeggService.loadKeggPathwayIntoEditor(myEntry,
						targetGraph, enzymeColor, separateClusters, processLabels);
			}
			if (deleteInitialMapNode && loadingOK && initialMapNode != null
					&& initialMapNode.getGraph() != null)
				initialMapNode.getGraph().deleteNode(initialMapNode);
		}
	}
	
	public static Graph getKeggPathwayGravistoGraph(KeggPathwayEntry myEntry,
			boolean showErrorMessages, Color enzymeColors) throws Exception {
		return getKeggPathwayGravistoGraph(myEntry, showErrorMessages,
				enzymeColors, true);
	}
	
	public static Graph getKeggPathwayGravistoGraph(KeggPathwayEntry myEntry,
			boolean showErrorMessages, Color enzymeColors, boolean includeMapNodes)
			throws Exception {
		
		InputStream inpStream = null;
		Graph myGraph = new AdjListGraph();
		int startErrorMsgCnt = ErrorMsg.getErrorMsgCount();
		try {
			if (myEntry.getPathwayURLstring() != null)
				AttributeHelper.setAttribute(myGraph, "kegg", "xml_url", myEntry
						.getPathwayURLstring());
			SAXBuilder builder = new SAXBuilder();
			Document doc;
			
			inpStream = myEntry.getOpenInputStream();
			if (inpStream != null) {
				GravistoService.setProxy();
				doc = builder.build(inpStream);
				// Lesen des Wurzelelements des JDOM-Dokuments doc
				Element kegg = doc.getRootElement();
				
				Pathway p = Pathway.getPathwayFromKGML(kegg);
				p.getGraph(myGraph);
				
				if (myEntry.isColorEnzymesAndUseReferencePathway()) {
					colorizeEnzymesGlycansCompounds(myGraph, myEntry.getMapName(),
							enzymeColors, true, true, false, false, false, true);
					AttributeHelper.copyReplaceStringAttribute(myGraph, "kegg",
							"xml_url", "xml_url_os", "map", myEntry
									.getOrganismLetters());
					AttributeHelper.copyReplaceStringAttribute(myGraph, "kegg",
							"xml_url", "xml_url_os", "ko", myEntry
									.getOrganismLetters());
				}
				
				MainFrame.showMessage("Pathway " + myEntry.getPathwayName()
						+ " loaded", MessageType.INFO);
				if ((ErrorMsg.getErrorMsgCount() - startErrorMsgCnt > 0)
						&& showErrorMessages) {
					MainFrame.showMessageDialog(
							"<html>The pathway might not be loaded completely.<br>Additional "
									+ (ErrorMsg.getErrorMsgCount() - startErrorMsgCnt)
									+ " detailed error messages should be checked with the command <i>Help/Error Messages</i>.",
							"Data processing not error free");
				}
			}
		} finally {
			if (inpStream != null)
				inpStream.close();
		}
		
		return myGraph;
	}
	
	public static void colorizeEnzymesGlycansCompounds(Graph graph,
			String mapName, Color enzymeColor, boolean markNotPresent,
			boolean orthologs, boolean enzymes, boolean glycans,
			boolean compounds, boolean convertTypeToGeneWhenProcessingOrthologs) {
		if (graph == null)
			return;
		if (orthologs) {
			try {
				String org = StringManipulationTools.removeNumbersFromString(mapName);
				String[] def = KeggHelper.get_kos_by_pathway(mapName);
				for (Node n : graph.getNodes()) {
					boolean matched = false;
					String kID = (String) AttributeHelper.getAttributeValue(n,
							"kegg", "kegg_name", "", "");
					TreeSet<String> genelist = new TreeSet<String>();
					for (String keggID : kID.split(" ")) {
						keggID = keggID.trim();
						if (keggID.startsWith("ko:")) {
							for (String ko : def) {
								if (keggID.equals(ko)) {
									AttributeHelper.setFillColor(n, enzymeColor);
									AttributeHelper.setAttribute(n, "kegg", "present",
											"putative");
									if (convertTypeToGeneWhenProcessingOrthologs) {
										String[] genes = KeggHelper.get_genes_by_ko(keggID,
												org);
										for (String d : genes) {
											genelist.add(d);
										}
									}
									matched = true;
								}
							}
							if (!matched && markNotPresent)
								AttributeHelper.setAttribute(n, "kegg", "present",
										"not found");
						}
					}
					if (genelist.size() > 0) {
						StringBuilder sb = new StringBuilder();
						for (String s : genelist) {
							if (sb.length() > 0)
								sb.append(" ");
							sb.append(s);
						}
						AttributeHelper.setAttribute(n, "kegg", "kegg_name", sb
								.toString());
						AttributeHelper.setAttribute(n, "kegg", "kegg_name_old", kID);
						KeggGmlHelper.setKeggType(n, "gene");
					}
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		if (enzymes) {
			try {
				String[] def = KeggHelper.get_enzymes_by_pathway("path:" + mapName);
				for (Node n : graph.getNodes()) {
					boolean matched = false;
					String kID = (String) AttributeHelper.getAttributeValue(n,
							"kegg", "kegg_name", "", "");
					for (String keggID : kID.split(" ")) {
						keggID = keggID.trim();
						if (keggID.startsWith("ec:")) {
							for (String enzyme : def) {
								if (keggID.equals(enzyme)) {
									AttributeHelper.setFillColor(n, enzymeColor);
									AttributeHelper.setAttribute(n, "kegg", "present",
											"putative");
									matched = true;
								}
							}
							if (!matched && markNotPresent)
								AttributeHelper.setAttribute(n, "kegg", "present",
										"not found");
						}
					}
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		if (glycans) {
			try {
				String[] def = KeggHelper.get_glycans_by_pathway("path:" + mapName);
				for (Node n : graph.getNodes()) {
					boolean matched = false;
					String kID = (String) AttributeHelper.getAttributeValue(n,
							"kegg", "kegg_name", "", "");
					for (String keggID : kID.split(" ")) {
						keggID = keggID.trim();
						if (keggID.startsWith("glycan:")) {
							for (String enzyme : def) {
								if (keggID.equals(enzyme)) {
									AttributeHelper.setFillColor(n, enzymeColor);
									AttributeHelper.setAttribute(n, "kegg", "present",
											"putative");
									matched = true;
								}
							}
							if (!matched && markNotPresent)
								AttributeHelper.setAttribute(n, "kegg", "present",
										"not found");
						}
					}
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		if (compounds) {
			try {
				String[] def = KeggHelper.get_compounds_by_pathway("path:" + mapName);
				for (Node n : graph.getNodes()) {
					boolean matched = false;
					String kID = (String) AttributeHelper.getAttributeValue(n,
							"kegg", "kegg_name", "", "");
					for (String keggID : kID.split(" ")) {
						keggID = keggID.trim();
						if (keggID.startsWith("cpd:")) {
							for (String enzyme : def) {
								if (keggID.equals(enzyme)) {
									AttributeHelper.setFillColor(n, enzymeColor);
									AttributeHelper.setAttribute(n, "kegg", "present",
											"putative");
									matched = true;
								}
							}
							if (!matched && markNotPresent)
								AttributeHelper.setAttribute(n, "kegg", "present",
										"not found");
						}
					}
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	/**
	 * @param keggTree
	 * @param keggTree
	 */
	public void processKeggTree(
			final JTree keggTree,
			final HashMap<KeggPathwayEntry, MyDefaultMutableTreeNode> pathwayToTreeNodeMap) {
		final List<KeggPathwayEntry> keggPathways = new ArrayList<KeggPathwayEntry>();
		for (KeggPathwayEntry kpe : pathwayToTreeNodeMap.keySet())
			keggPathways.add(kpe);
		int pathwayCnt = keggPathways.size();
		status1 = "Initialize";
		if (pathwayCnt <= 0) {
			status1 = "";
			MainFrame.showMessageDialog("<html>" + "Pathway list is empty.<br>"
					+ "Can't proceed with analysis!", "Error");
			return;
		}
		
		List<Object> projects = new ArrayList<Object>(); // contains ProjectEntity or a String
		projects.addAll(TabDBE.getLoadedProjectEntities());
		projects.addAll(TabDBE.getProjectList());
		if (projects.size() <= 0) {
			MainFrame.showMessageDialog("<html>"
					+ "No experimental data loaded.<br>"
					+ "Can't proceed with analysis!", "Error");
			status1 = "";
			return;
		}
		ArrayList<String> prettyFiedProjects = new ArrayList<String>();
		for (Object o : projects) {
			prettyFiedProjects.add("<html>" + o.toString());
		}
		Object sel = JOptionPane
				.showInputDialog(
						MainFrame.getInstance(),
						"Select the Experiment for which the number of possible node mappings will be analysed.",
						"Select Experiment", JOptionPane.QUESTION_MESSAGE, null,
						prettyFiedProjects.toArray(), projects.iterator().next());
		if (sel != null)
			sel = projects.get(prettyFiedProjects.indexOf(sel));
		
		if (sel instanceof ProjectEntity) {
			ProjectEntity pe = (ProjectEntity) sel;
			RunnableWithXMLexperimentData r = getPathwayMappingRunnable(keggTree,
					keggPathways, pathwayToTreeNodeMap, getDefaultEnzymeColor());
			r.setExperimenData(pe.getDocumentData());
			r.run();
		}
	}
	
	private RunnableWithXMLexperimentData getPathwayMappingRunnable(
			final JTree keggTree, final List<KeggPathwayEntry> keggPathways,
			final HashMap pathwayToTreeNodeMap, final Color enzymeColor) {
		return new RunnableWithXMLexperimentData() {
			private ExperimentInterface md = null;
			
			@Override
			public void setExperimenData(ExperimentInterface md) {
				this.md = md;
			}
			
			@Override
			public void run() {
				progressVal = 0;
				int cnt = 0;
				status1 = "Analyze possible Data Mapping...";
				for (KeggPathwayEntry myEntry : keggPathways) {
					if (pleaseStop)
						break;
					try {
						status2 = "Analyze Pathway " + myEntry.getPathwayName();
						Graph myGraph = KeggService.getKeggPathwayGravistoGraph(
								myEntry, false, enzymeColor);
						MapResult mapResult = new Experiment2GraphHelper()
								.mapDataToGraphElements(true, md, myGraph
										.getGraphElements(), null, false, GraffitiCharts.HIDDEN.getName(), 0, -1, true,
										true, false, false, false);
						DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) pathwayToTreeNodeMap
								.get(myEntry);
						if (dmt != null) {
							KeggPathwayEntry kpe = (KeggPathwayEntry) dmt
									.getUserObject();
							
							int enzymeCount = EnzymeService
									.getNumberOfEnzymeNodes(myGraph);
							int compoundCount = CompoundService
									.getNumberOfCompoundNodes(myGraph);
							
							kpe.setMappingCount(mapResult.substanceCount + "/"
									+ enzymeCount + "/" + compoundCount + "/"
									+ myGraph.getNodes().size());
							keggTree.repaint();
						}
						progressVal = ++cnt * 100 / keggPathways.size();
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e.getLocalizedMessage());
					}
				}
				if (!pleaseStop) {
					status1 = "Pathway data analyzed";
					status2 = "Ready";
				} else {
					status1 = "Pathway not completely analyzed.";
					status2 = "Processing aborted";
				}
				progressVal = 100;
			}
		};
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		return progressVal;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return getCurrentStatusValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusMessage1()
	 */
	@Override
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusMessage2()
	 */
	@Override
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#pleaseStop
	 * ()
	 */
	@Override
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #pluginWaitsForUser()
	 */
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #pleaseContinueRun()
	 */
	@Override
	public void pleaseContinueRun() {
		// empty
	}
	
	public static void loadKeggPathwayIntoGraph(InputStream in, Graph g,
			Color enzymeColor) {
		KeggPathwayEntry ke = new KeggPathwayEntry(in);
		ke.setPathwayName("[Loaded from file]");
		ke.setMapName("[Loaded from file]");
		loadKeggPathwayIntoEditor(ke, g, enzymeColor, false, false);
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		progressVal = value;
	}
	
	public static Color getDefaultEnzymeColor() {
		return new Color(200, 255, 200);
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
	
}