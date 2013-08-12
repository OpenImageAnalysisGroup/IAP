/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.OpenFileDialogService;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid.GridLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.RTTreeLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.process_alternative_ids.ReplaceLabelFromAlternativeSubstanceNames;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class CreateFuncatGraphAlgorithm extends AbstractAlgorithm {
	
	private final String settingLabel = "Interpret Node Label";
	private final String settingKEGGid = "Interpret KEGG ID";
	private final String settingAlternativeId = "Interpret Alternative Substance IDs (select the index, below)";
	private final String settingDataAnnotation = "Interpret Hierarchy Data Annotation";
	private final String settingSpecialR = "R-calculated cluster-hierarchy (special command, load file)";
	
	private String currentInformationProvider = settingAlternativeId;
	
	private final String settingPointDivided = "Direct Interpretation (. or ; divided hierarchy information)";
	private final String settingKOenzymeLookup = "Lookup Enzyme Name/ID in Enyzme and KO databases (process KO Class Information)";
	private final String settingKOgeneLookup = "Lookup Gene ID or KO ID in KO database (process KO Class Information)";
	private final String settingKOdbLinkLookup = "Lookup KO ID in KO database (process KO DB Link Information)";
	
	private String currentInformationProcessingSetting = settingKOgeneLookup;
	private final String settingUseAllIdx = "No alternative identifiers are available";
	private String currentAlternativeSubstanceIDindex = settingUseAllIdx;
	private final String settingUseAll = "Evaluate all alternative identifiers";
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.FUNCAT_ACCESS))
			return "Create Data-Specific Hierarchy Tree";
		else
			return null;
	}
	
	@Override
	public String getDescription() {
		return "<html>"
							+ "This command creates a hierarchy-tree. The hierarchy information may be given by the node labels,<br>"
							+ "by alternative substance identifiers (for nodes with mapped data) or by a data annotation, provided<br>"
							+ "directly in the input template (type 2)."
							+ "<br>"
							+ "The hierarchy information may be evaluated directly, in case it is given by a '.' or ';' divided text<br>"
							+ "annotation. It is also possible to look-up a KEGG Pathway hierarchy, in case the selected identifier<br>"
							+ "is recognized as a enzyme name or ID and it is found in the KO database. Another possibility is to<br>"
							+ "lookup and interpret the given identifers as gene names, listed in the KO database. In this case the<br>"
							+ "gene data is also put in context to the KEGG Pathway hierarchy.<br>"
							+ "<br><small>Hint: The hierarchy menu provides a additional command for the interpretation of gene IDs in order to create a Gene Ontogy<br>"
							+ "network from the data.<br><br>";
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Parameter[] getParameters() {
		int maxID = 0;
		HashMap<Integer, String> exampleValues = new HashMap<Integer, String>();
		ArrayList<String> selvals = new ArrayList<String>();
		maxID = ReplaceLabelFromAlternativeSubstanceNames.enumerateExistingAlternativeSubstanceIDsAndTheirExamples(
							(Collection) getSelectedOrAllNodes(), maxID, exampleValues);
		if (maxID == 0) {
			selvals.add(settingUseAllIdx);
		} else {
			selvals.add("Evaluate all alternative identifiers");
			for (int i = 0; i <= maxID; i++) {
				String s = "" + i;
				String example = exampleValues.get(new Integer(i));
				if (example != null)
					s += " (e.g. " + example + ")";
				selvals.add(s);
			}
		}
		
		Collection<String> possibleValues = new ArrayList<String>();
		possibleValues.add(settingLabel);
		possibleValues.add(settingKEGGid);
		possibleValues.add(settingAlternativeId);
		possibleValues.add(settingDataAnnotation);
		possibleValues.add(settingSpecialR);
		
		if (maxID == 0 && currentInformationProvider.equals(settingAlternativeId))
			currentInformationProvider = settingLabel;
		
		Collection<String> possibleEvaluations = new ArrayList<String>();
		possibleEvaluations.add(settingPointDivided);
		possibleEvaluations.add(settingKOenzymeLookup);
		possibleEvaluations.add(settingKOgeneLookup);
		possibleEvaluations.add(settingKOdbLinkLookup);
		
		return new Parameter[] {
							new ObjectListParameter(currentInformationProvider, "Hierarchy Information Provider",
												"Select the property which provides the hierarchy information", possibleValues),
							new ObjectListParameter(currentAlternativeSubstanceIDindex, "<html>" + "Index of Alternative ID<br><small>"
												+ "(used only for provider setting 2)", "<html>"
												+ "In case the alternative substance IDs should be processed, "
												+ "this setting specifies, which identifer should be used.", selvals),
							new ObjectListParameter(currentInformationProcessingSetting, "Hierarchy Information Processing",
												"Select the way the hierarchy data should be interpreted", possibleEvaluations) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		currentInformationProvider = (String) ((ObjectListParameter) params[i++]).getValue();
		currentAlternativeSubstanceIDindex = (String) ((ObjectListParameter) params[i++]).getValue();
		currentInformationProcessingSetting = (String) ((ObjectListParameter) params[i++]).getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		final Collection<Node> workNodes = new ArrayList<Node>(getSelectedOrAllNodes());
		final HashSet<Node> newNodes = new HashSet<Node>();
		final HashSet<Edge> newEdges = new HashSet<Edge>();
		final HashMap<String, ArrayList<String>> specialRmap = new HashMap<String, ArrayList<String>>();
		if (currentInformationProvider.equals(settingSpecialR)) {
			try {
				String fileName = OpenFileDialogService.getFile(new String[] { ".txt" }, "Special R output file",
									"Process File").getAbsolutePath();
				TextFile tf = new TextFile(fileName);
				for (String s : tf) {
					String[] l = s.split("\\.");
					String key = l[0];
					if (!specialRmap.containsKey(key))
						specialRmap.put(key, new ArrayList<String>());
					StringBuilder value = new StringBuilder();
					for (int i = 1; i < l.length; i++) {
						if (value.length() > 0)
							value.append(".");
						value.append(l[i]);
					}
					
					// for (int i=l.length-1; i>1; i--) {
					// if (value.length()>0)
					// value.append(".");
					// value.append(l[i]);
					// }
					// System.out.println(key+" --> "+value.toString());
					specialRmap.get(key).add(value.toString());
				}
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		final Graph fgraph = graph;
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
							"Create Hierarchy", "");
		Runnable task = new Runnable() {
			public void run() {
				int startX = 100;
				int stepX = 200;
				int startY = 40;
				int stepY = 0;
				fgraph.getListenerManager().transactionStarted(this);
				try {
					HashMap<String, Node> hierarchy_tree_itemName2node = new HashMap<String, Node>();
					int nn = 0;
					int nn_max = workNodes.size();
					for (Node graphNode : workNodes) {
						nn++;
						status.setCurrentStatusText1("Created " + newNodes.size() + " nodes and " + newEdges.size()
											+ " edges ");
						status.setCurrentStatusText2("Process node " + nn + " / " + nn_max);
						if (status.wantsToStop())
							break;
						status.setCurrentStatusValueFine((double) nn / (double) nn_max * 100d);
						NodeHelper nh = new NodeHelper(graphNode);
						HashSet<String> checkTheseIds = new HashSet<String>();
						
						if (currentInformationProvider.equals(settingAlternativeId)) {
							if (currentAlternativeSubstanceIDindex.equals(settingUseAll))
								checkTheseIds.addAll(nh.getAlternativeIDs());
							else {
								String number = currentAlternativeSubstanceIDindex;
								if (number.contains(" ")) {
									number = number.substring(0, number.indexOf(" "));
									number = number.trim();
								}
								int idx = Integer.parseInt(number);
								Collection<String> altIdsWithThisIndex = nh.getAlternativeIDsWithIdx(idx);
								for (String altId : altIdsWithThisIndex)
									if (altId != null && altId.length() > 0)
										checkTheseIds.add(altId);
							}
							
						}
						if (currentInformationProvider.equals(settingSpecialR)) {
							String lbl = nh.getLabel();
							if (lbl != null && lbl.length() > 0) {
								ArrayList<String> res = specialRmap.get(lbl);
								if (res != null)
									for (String s : res) {
										checkTheseIds.add(s);
									}
							}
						}
						if (currentInformationProvider.equals(settingLabel)) {
							String lbl = nh.getLabel();
							if (lbl != null && lbl.length() > 0)
								checkTheseIds.add(lbl);
						}
						if (currentInformationProvider.equals(settingKEGGid)) {
							String keggID = KeggGmlHelper.getKeggId(nh);
							if (keggID != null && keggID.length() > 0) {
								if (keggID.indexOf(" ") > 0) {
									for (String s : keggID.split(" ")) {
										checkTheseIds.add(s);
									}
								} else
									checkTheseIds.add(keggID);
							}
						}
						if (currentInformationProvider.equals(settingDataAnnotation)) {
							if (Experiment2GraphHelper.getMappedDataListFromGraphElement(graphNode) != null) {
								for (SubstanceInterface mappingData : Experiment2GraphHelper
													.getMappedDataListFromGraphElement(graphNode)) {
									String funcat_desc = mappingData.getFuncat();
									checkTheseIds.add(funcat_desc);
								}
							}
						}
						String organismInfo = null;
						ArrayList<String> hierarchyInformationsForCurrentNode = new ArrayList<String>();
						for (String altId : checkTheseIds) {
							if (altId == null || altId.length() <= 0)
								continue;
							if (currentInformationProcessingSetting.equals(settingPointDivided)) {
								hierarchyInformationsForCurrentNode.add(altId);
							}
							if (currentInformationProcessingSetting.equals(settingKOenzymeLookup)) {
								EnzymeEntry enzEnt = EnzymeService.getEnzymeInformation(altId, false);
								if (enzEnt != null) {
									for (KoEntry ko : KoService.getKoFromEnzyme(enzEnt.getID())) {
										for (String cat : ko.getKoClasses()) {
											cat = "KO Classes;" + cat;
											if (!hierarchyInformationsForCurrentNode.contains(cat))
												hierarchyInformationsForCurrentNode.add(cat);
										}
									}
								}
							}
							if (currentInformationProcessingSetting.equals(settingKOgeneLookup)) {
								// produced sometimes errors:
								// if (altId.indexOf(":")>0)
								// organismInfo = altId.substring(0,
								// altId.indexOf(":")).trim();
								for (KoEntry ko : KoService.getKoFromGeneIdOrKO(altId)) {
									for (String cat : ko.getKoClasses()) {
										cat = "KO Classes;" + cat;
										if (!hierarchyInformationsForCurrentNode.contains(cat))
											hierarchyInformationsForCurrentNode.add(cat);
									}
									Set<String> orgs = ko.getOrganismCodesForGeneID(altId);
									if (orgs != null && orgs.size() == 1)
										organismInfo = orgs.iterator().next();
								}
							}
							if (currentInformationProcessingSetting.equals(settingKOdbLinkLookup)) {
								for (KoEntry ko : KoService.getKoFromDBlink(altId)) {
									for (String cat : ko.getKoClasses()) {
										cat = "KO Classes;" + cat;
										if (!hierarchyInformationsForCurrentNode.contains(cat))
											hierarchyInformationsForCurrentNode.add(cat);
									}
								}
							}
						}
						for (String hierarchyInformation : hierarchyInformationsForCurrentNode) {
							if (hierarchyInformation != null && hierarchyInformation.length() > 0) {
								hierarchyInformation = StringManipulationTools.htmlToUnicode(hierarchyInformation);
								if (hierarchyInformation.indexOf("§") >= 0) {
									ErrorMsg
														.addErrorMessage("Internal error: funcat/ko definition should not contain character § (problematic: '"
																			+ hierarchyInformation + "')");
								} else {
									if (hierarchyInformation.startsWith("KO Classes;")) {
										hierarchyInformation = StringManipulationTools.stringReplace(hierarchyInformation, ";",
															"§");
									} else {
										hierarchyInformation = StringManipulationTools.stringReplace(hierarchyInformation, ".",
															"§");
										if (hierarchyInformation.indexOf(";") >= 0)
											hierarchyInformation = StringManipulationTools.stringReplace(hierarchyInformation,
																";", "§");
									}
									String[] graphH = hierarchyInformation.split("§");
									Node lastNode = null;
									String hierarchyEntityName = "";
									for (String hierarchyEntityNameLE : graphH) {
										hierarchyEntityNameLE = hierarchyEntityNameLE.trim();
										hierarchyEntityName += "§" + hierarchyEntityNameLE;
										if (!hierarchy_tree_itemName2node.containsKey(hierarchyEntityName)) {
											// System.out.println("not found: "+hierarchyEntityName);
											Node n = GraphHelper.addNodeToGraph(fgraph, startX
																+ hierarchy_tree_itemName2node.size() * stepX, startY
																+ hierarchy_tree_itemName2node.size() * stepY, 1, 150, 30, Color.BLACK,
																Color.WHITE);
											AttributeHelper.setLabel(n, hierarchyEntityNameLE);
											if (hierarchyEntityNameLE.toLowerCase().indexOf("path:") >= 0) {
												// this should be a map-node
												String id = hierarchyEntityNameLE.substring(hierarchyEntityNameLE.toLowerCase()
																	.indexOf("path:"), hierarchyEntityNameLE.indexOf("]", hierarchyEntityNameLE
																	.toLowerCase().indexOf("path:")));
												if (organismInfo == null) {
													KeggGmlHelper.setKeggId(n, id.toLowerCase());
												} else {
													KeggGmlHelper.setKeggId(n, id.toLowerCase());
													id = StringManipulationTools.stringReplace(id, "ko", organismInfo);
													KeggGmlHelper.setKeggId(n, id.toLowerCase(), 1);
												}
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
												Edge edge = fgraph.addEdge(lastNode, hierarchy_tree_node, true, AttributeHelper
																	.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
												newEdges.add(edge);
											}
										}
										lastNode = hierarchy_tree_node;
									}
									if (lastNode != null) {
										// connect last funcat desc with node with exp.
										// data
										if (!lastNode.getNeighbors().contains(graphNode)) {
											Edge edge = fgraph.addEdge(lastNode, graphNode, true, AttributeHelper
																.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
											newEdges.add(edge);
										}
									}
								}
							}
							
						}
					}
				} finally {
					if (!status.wantsToStop()) {
						fgraph.getListenerManager().transactionFinished(this, false);
						status.setCurrentStatusText1("Created " + newNodes.size() + " nodes and " + newEdges.size()
											+ " edges ");
						status.setCurrentStatusText2("Update view. Please wait.");
						MainFrame.showMessage("Added " + newNodes.size() + " nodes and " + newEdges.size()
											+ " edges to the graph", MessageType.INFO);
						GraphHelper.postUndoableNodeAndEdgeAdditions(fgraph, newNodes, newEdges, getName());
						if (newNodes.size() > 0) {
							// layout new nodes using tree layout
							RTTreeLayout tree = new RTTreeLayout();
							tree.attach(fgraph, new Selection(newNodes));
							tree.execute();
							
							// layout gene nodes using grid layout (no resize)
							Collection<Node> geneNodes = new ArrayList<Node>(workNodes);
							geneNodes.removeAll(newNodes);
							GridLayouterAlgorithm.layoutOnGrid(geneNodes, 1, 20, 20);
							
							CenterLayouterAlgorithm.moveGraph(fgraph, getName(), true, 50, 50);
						}
					} else {
						for (Edge e : newEdges)
							fgraph.deleteEdge(e);
						for (Node n : newNodes)
							fgraph.deleteNode(n);
						status.setCurrentStatusText1("No elements will be added.");
						status.setCurrentStatusText2("Update view. Please wait.");
						fgraph.getListenerManager().transactionFinished(this);
					}
				}
			}
		};
		BackgroundTaskHelper.issueSimpleTask("Create Hierarchy", "Initialize...", task, null, status);
	}
}
