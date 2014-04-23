/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 01.07.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.HelperClass;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.LabelAttribute;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzClassEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.QuadNumber;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.ChartsColumnAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute.XMLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

/**
 * @author klukas
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class Experiment2GraphHelper implements BackgroundTaskStatusProviderSupportingExternalCall, HelperClass {
	
	public static String mapFolder = "mapping";
	
	public static String mapVarName = "measurementdata"; // data
	
	private static int dxNewNode = 150;
	
	private static int dyNewNode = 150;
	
	private String status1 = "Init Data Mapping...";
	
	private String status2;
	
	private boolean pleaseStop;
	
	private int progressValue;
	
	/**
	 * Get a list of mapped xml data that has been assigned to a node (or other
	 * GraphElement).
	 * 
	 * @return A list of mapped data or null, if no mapping data is available.
	 */
	public static ExperimentInterface getMappedDataListFromGraphElement(GraphElement graphElement) {
		return getMappedDataListFromGraphElement(graphElement, mapVarName);
	}
	
	public static ExperimentInterface getMappedDataListFromGraphElement(GraphElement graphElement, String mapVarName) {
		try {
			CollectionAttribute ca = (CollectionAttribute) graphElement.getAttribute(mapFolder);
			XMLAttribute xa = (XMLAttribute) ca.getAttribute(mapVarName);
			return xa.getMappedData();
		} catch (AttributeNotFoundException e) {
			return null;
		}
	}
	
	public static XMLAttribute getXMLdataAttribute(GraphElement graphElement) {
		try {
			CollectionAttribute ca = (CollectionAttribute) graphElement.getAttribute(mapFolder);
			XMLAttribute xa = (XMLAttribute) ca.getAttribute(mapVarName);
			return xa;
		} catch (AttributeNotFoundException e) {
			return null;
		}
	}
	
	public static Integer getMappedDataListSizeFromGraphElement(GraphElement graphElement) {
		try {
			CollectionAttribute ca = (CollectionAttribute) graphElement.getAttribute(mapFolder);
			XMLAttribute xa = (XMLAttribute) ca.getAttribute(mapVarName);
			return xa.getMappingDataListSize();
		} catch (AttributeNotFoundException e) {
			return null;
		}
	}
	
	// public MapResult mapDataToGraphElements(
	// boolean mapAlsoInCaseOfEmptyDataset, Document doc,
	// Collection<org.graffiti.graph.GraphElement> targetGraphElements,
	// Graph addNewGraphElementsToThisGraph,
	// boolean doUserMapping,
	// int diagramStyle,
	// int minimumLines,
	// boolean considerCompoundDb,
	// boolean considerEnzymeDb,
	// boolean considerKoDb,
	// boolean considerMappingToKEGGmapNodes,
	// boolean selectResult) {
	// return mapDataToGraphElements(mapAlsoInCaseOfEmptyDataset, doc,
	// targetGraphElements,
	// addNewGraphElementsToThisGraph,
	// doUserMapping,
	// diagramStyle,
	// minimumLines,
	// -1,
	// considerCompoundDb,
	// considerEnzymeDb,
	// considerKoDb,
	// considerMappingToKEGGmapNodes,
	// selectResult);
	// }
	
	public MapResult mapDataToGraphElements(boolean mapAlsoInCaseOfEmptyDataset, ExperimentInterface md,
			Collection<org.graffiti.graph.GraphElement> targetGraphElements, Graph addNewGraphElementsToThisGraph,
			boolean doUserMapping, String diagramStyle, int minimumLines, boolean considerCompoundDb,
			boolean considerEnzymeDb, boolean considerKoDb, boolean considerMappingToKEGGmapNodes, boolean selectResult) {
		int diagramsPerRow = -1;
		return mapDataToGraphElements(mapAlsoInCaseOfEmptyDataset, md, targetGraphElements,
				addNewGraphElementsToThisGraph, doUserMapping, diagramStyle, minimumLines, diagramsPerRow,
				considerCompoundDb, considerEnzymeDb, considerKoDb, considerMappingToKEGGmapNodes, selectResult);
	}
	
	/**
	 * Maps the data from the XML document to the list of nodes
	 * 
	 * @param mapAlsoInCaseOfEmptyDataset
	 *           If true, a mapping will be done although no data points are in
	 *           the data set. This creates also empty diagrams.
	 * @param doc
	 *           The XML Document with the experimental data.
	 * @param targetGraphElements
	 *           The node list where the data should be mapped.
	 * @param addNewGraphElementsToThisGraph
	 *           If not null, new nodes will be added to this graph if one or
	 *           more mappings are done for a node, if set to false, each time a
	 *           mapping is done, the count will be increased. Set this to true,
	 *           to find out, how many nodes have data mapped.
	 * @param validTimePoints
	 * @param validConditons
	 * @return MapResult
	 */
	@SuppressWarnings("unchecked")
	public MapResult mapDataToGraphElements(boolean mapAlsoInCaseOfEmptyDataset, ExperimentInterface mappingData,
			Collection<org.graffiti.graph.GraphElement> targetGraphElements, Graph addNewGraphElementsToThisGraph,
			boolean doUserMapping, String diagramStyle, int minimumLines, int diagramsPerRow, boolean considerCompoundDb,
			boolean considerEnzymeDb, boolean considerKoDb, boolean considerMappingToKEGGmapNodes, boolean selectResult) {
		if (mappingData == null)
			return null;
		MapResult mapResult = new MapResult();
		progressValue = -1;
		pleaseStop = false;
		status1 = "Prepare Data Mapping";
		
		int offY = 0;
		int offX = 100;
		int cntX = 0;
		int cntY = 0;
		if (addNewGraphElementsToThisGraph != null) {
			status2 = "Enumerate existing nodes positioning...";
			Vector2d maxxy = NodeTools.getMaximumXY(addNewGraphElementsToThisGraph.getNodes(), 1, 0, 0, true);
			offY += maxxy.y;
			offY += dyNewNode;
		}
		status2 = "Enumerate node names and alternative identifiers...";
		
		HashMap<org.graffiti.graph.GraphElement, HashSet<String>> graphElement2possibleIDs = new HashMap<org.graffiti.graph.GraphElement, HashSet<String>>();
		for (org.graffiti.graph.GraphElement n : targetGraphElements) {
			graphElement2possibleIDs.put(n, getAlternativeIdsAndLabelsOfGraphElement(n, considerCompoundDb,
					considerEnzymeDb, considerKoDb, considerMappingToKEGGmapNodes));
		}
		
		HashMap<String, QuadNumber> id2quad = new HashMap<String, QuadNumber>();
		
		status2 = "";
		status1 = "Get experiment name...";
		status1 = "Start Data Mapping...";
		Set<org.graffiti.graph.GraphElement> newNodesThatShouldNotAgainBeUsedForMapping = new HashSet<org.graffiti.graph.GraphElement>();
		int msl = mappingData.size();
		int i = 0;
		HashMap<String, EnzClassEntry> knownEnzClassEntries = new HashMap<String, EnzClassEntry>();
		ArrayList<org.graffiti.graph.GraphElement> workList = new ArrayList<org.graffiti.graph.GraphElement>(
				targetGraphElements);
		
		HashSet<org.graffiti.graph.GraphElement> resultElements = new HashSet<org.graffiti.graph.GraphElement>();
		
		int maxRow = (int) Math.sqrt(mappingData.size());
		if (maxRow < 8)
			maxRow = 8;
		
		for (int firstEdgesThenNodes = 0; firstEdgesThenNodes <= 1; firstEdgesThenNodes++)
			for (SubstanceInterface substanceData : mappingData) {
				if (pleaseStop) {
					status2 = "(execution aborted)";
					break;
				}
				progressValue = (100 * i) / msl;
				
				String substanceMainName = substanceData.getName();
				
				// connection to processing of new edges / nodes XXXX_ABC_XXXX (see
				// below)
				if (firstEdgesThenNodes == 0 && substanceMainName.indexOf("^") <= 0)
					continue;
				if (firstEdgesThenNodes == 1 && substanceMainName.indexOf("^") > 0)
					continue;
				
				if (minimumLines > 0) {
					int lines = substanceData.size();
					if (lines < minimumLines)
						continue;
				}
				status1 = "Process Data (" + mapResult.substanceCount + " substances mapped)...";
				if (substanceMainName != null && substanceMainName.endsWith(".0"))
					substanceMainName = substanceMainName.substring(0, substanceMainName.length() - ".0".length());
				status2 = "Substance: " + substanceMainName;
				
				HashSet<org.graffiti.graph.Node> edgeMappingPossibleSourceNodes = new HashSet<org.graffiti.graph.Node>();
				HashSet<org.graffiti.graph.Node> edgeMappingPossibleTargetNodes = new HashSet<org.graffiti.graph.Node>();
				
				HashSet<String> substanceNames = new HashSet<String>();
				substanceNames.add(substanceMainName);
				if (substanceData.getSynonyms() != null)
					substanceNames.addAll(substanceData.getSynonyms());
				HashSet<org.graffiti.graph.GraphElement> addMappingDataToTheseGraphElements = new HashSet<org.graffiti.graph.GraphElement>();
				for (String substanceName : substanceNames) {
					substanceName = substanceName.trim();
					if (substanceName.length() <= 0)
						continue;
					enumeratePossibleTargetGraphElementsForSubstance(considerEnzymeDb, considerKoDb,
							graphElement2possibleIDs, id2quad, newNodesThatShouldNotAgainBeUsedForMapping,
							knownEnzClassEntries, workList, addMappingDataToTheseGraphElements, substanceName, false,
							edgeMappingPossibleSourceNodes, edgeMappingPossibleTargetNodes);
				}
				
				if (addMappingDataToTheseGraphElements.size() == 0 && addNewGraphElementsToThisGraph != null) {
					if (substanceMainName.indexOf("^") > 0) {
						String substanceMainNameA = substanceMainName.substring(0, substanceMainName.indexOf("^"));
						String substanceMainNameB = substanceMainName
								.substring(substanceMainName.indexOf("^") + "^".length());
						substanceMainNameA = substanceMainNameA.trim();
						substanceMainNameB = substanceMainNameB.trim();
						if (edgeMappingPossibleSourceNodes.size() <= 0) {
							// connection to processing of new edges / nodes
							// XXXX_ABC_XXXX (see below)
							// here new nodes are created, in case a edge mapping is
							// desired, but only
							// source OR target nodes have been found
							// to enable later mapping of Node-data to these nodes, the
							// processing
							// of new edge-nodes needs to be done prior the processing
							// of new nodes
							// when no node mapping is possible, a new node is created
							// as the main substance name is unique, these nodes are
							// not considered
							// for mapping of further not mappable node data
							status2 = "Add new source node...";
							org.graffiti.graph.Node graphNode = GraphHelper.addNodeToGraph(addNewGraphElementsToThisGraph, 100
									+ dxNewNode * cntX, 100 + dyNewNode * cntY++, 1, 120, 120, new Color(0, 0, 0, 255),
									new Color(255, 255, 255, 255));
							mapResult.newNodes++;
							if (cntY >= maxRow) {
								cntY = 0;
								cntX += 1;
							}
							AttributeHelper.setLabel(graphNode, substanceMainNameA);
							graphElement2possibleIDs.put(graphNode, getAlternativeIdsAndLabelsOfGraphElement(graphNode,
									considerCompoundDb, considerEnzymeDb, considerKoDb, considerMappingToKEGGmapNodes));
							edgeMappingPossibleSourceNodes.add(graphNode);
							workList.add(graphNode);
							status2 = "Add new graph node finished";
						}
						if (edgeMappingPossibleTargetNodes.size() <= 0) {
							status2 = "Add new target node...";
							org.graffiti.graph.Node graphNode = GraphHelper.addNodeToGraph(addNewGraphElementsToThisGraph,
									offX + dxNewNode * cntX, offY + dyNewNode * cntY++, 1, 120, 120, new Color(0, 0, 0, 255),
									new Color(255, 255, 255, 255));
							mapResult.newNodes++;
							if (cntY >= maxRow) {
								cntY = 0;
								cntX += 1;
							}
							AttributeHelper.setLabel(graphNode, substanceMainNameB);
							graphElement2possibleIDs.put(graphNode, getAlternativeIdsAndLabelsOfGraphElement(graphNode,
									considerCompoundDb, considerEnzymeDb, considerKoDb, considerMappingToKEGGmapNodes));
							edgeMappingPossibleTargetNodes.add(graphNode);
							workList.add(graphNode);
							status2 = "Add new graph node finished";
						}
						// now create edges between all source and target nodes
						for (org.graffiti.graph.Node sourceNode : edgeMappingPossibleSourceNodes) {
							for (org.graffiti.graph.Node targetNode : edgeMappingPossibleTargetNodes) {
								org.graffiti.graph.Edge nEdge = addNewGraphElementsToThisGraph.addEdge(sourceNode, targetNode,
										true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.black, Color.black, true));
								mapResult.newEdges++;
								addMappingDataToTheseGraphElements.add(nEdge);
								if (sourceNode == targetNode) {
									Vector2d np = AttributeHelper.getPositionVec2d(sourceNode);
									Collection<Vector2d> bendPoints = new ArrayList<Vector2d>();
									bendPoints.add(new Vector2d(np.x + 50, np.y - 50));
									bendPoints.add(new Vector2d(np.x + 50, np.y + 50));
									GraphHelper.addBends(nEdge, bendPoints);
								}
							}
						}
					} else {
						// No name mapping possible -> create new graph node
						status2 = "Add new graph node...";
						org.graffiti.graph.Node graphNode = GraphHelper.addNodeToGraph(addNewGraphElementsToThisGraph, offX
								+ dxNewNode * cntX, offY + dyNewNode * cntY++, 1, 120, 120, new Color(0, 0, 0, 255), new Color(
								255, 255, 255, 255));
						mapResult.newNodes++;
						if (cntY >= maxRow) {
							cntY = 0;
							cntX += 1;
						}
						AttributeHelper.setLabel(graphNode, substanceMainName);
						addMappingDataToTheseGraphElements.add(graphNode);
						// newNodesThatShouldNotAgainBeUsedForMapping.add(graphNode);
						// this new node is not in the possibleIdAndGraphNode list and
						// thus needs not to be specially skipped
						status2 = "Add new graph node finished";
					}
				}
				resultElements.addAll(addMappingDataToTheseGraphElements);
				createAndAddExperimentalDataAttribute(mapResult, diagramStyle, diagramsPerRow, substanceData,
						substanceMainName, addMappingDataToTheseGraphElements);
				if (addMappingDataToTheseGraphElements.size() > 0)
					mapResult.substanceCount++;
				i++;
			}
		progressValue = 100;
		status1 = "Data Mapping finished";
		if (!pleaseStop) {
			status2 = "";
		}
		calcMinMaxMappingCountStatistics(mapResult, resultElements);
		if (selectResult)
			GraphHelper.selectElements((Collection) resultElements);
		
		return mapResult;
	}
	
	private void calcMinMaxMappingCountStatistics(MapResult mapResult,
			HashSet<org.graffiti.graph.GraphElement> resultElements) {
		int minMappCount = Integer.MAX_VALUE;
		int maxMappCount = 0;
		for (GraphElement ge : resultElements) {
			if (ge instanceof org.graffiti.graph.Node)
				mapResult.targetCountNodes++;
			if (ge instanceof org.graffiti.graph.Edge)
				mapResult.targetCountEdges++;
			List<SubstanceInterface> mappedData = NodeTools.getMappedDataListFromNode(ge);
			if (mappedData != null) {
				int mappings = mappedData.size();
				if (mappings < minMappCount)
					minMappCount = mappings;
				if (mappings > maxMappCount)
					maxMappCount = mappings;
			}
		}
		if (minMappCount > maxMappCount)
			minMappCount = maxMappCount;
		mapResult.minMappingCount = minMappCount;
		mapResult.maxMappingCount = maxMappCount;
	}
	
	private void createAndAddExperimentalDataAttribute(MapResult mapResult, String diagramStyle, int diagramsPerRow,
			SubstanceInterface xmlSubstanceNode, String substanceMainName,
			HashSet<org.graffiti.graph.GraphElement> addMappingDataToTheseGraphElements) {
		for (org.graffiti.graph.GraphElement targetGraphElement : addMappingDataToTheseGraphElements) {
			status1 = "Map XML data for substance " + substanceMainName + " to graph element "
					+ targetGraphElement.toString();
			addMappingData2Node(xmlSubstanceNode, targetGraphElement, diagramStyle);
			AttributeHelper.setAttribute(targetGraphElement, "charting", ChartsColumnAttribute.name, new ChartsColumnAttribute(diagramsPerRow));
			// if (targetGraphElement!=null && (targetGraphElement instanceof
			// org.graffiti.graph.Node) && diagramStyle>0 && diagramStyle<6
			if (targetGraphElement != null && (targetGraphElement instanceof org.graffiti.graph.Node)
					&& GraffitiCharts.isNoAutoOrHide(diagramStyle) && GraffitiCharts.isNotHeatmap(diagramStyle)
					&& AttributeHelper.getHeight((org.graffiti.graph.Node) targetGraphElement) > 30) {
				LabelAttribute la = AttributeHelper.getLabel(-1, (org.graffiti.graph.Node) targetGraphElement);
				if (la != null) {
					String alignment = la.getAlignment();
					if (alignment == null || alignment.length() <= 0 || alignment.equals("c"))
						la.setAlignment("t");
				}
			}
		}
	}
	
	private void enumeratePossibleTargetGraphElementsForSubstance(boolean considerEnzymeDb, boolean considerKoDb,
			HashMap<org.graffiti.graph.GraphElement, HashSet<String>> graphNode2possibleIDs,
			HashMap<String, QuadNumber> id2quad,
			Set<org.graffiti.graph.GraphElement> newGraphElementsThatShouldNotAgainBeUsedForMapping,
			HashMap<String, EnzClassEntry> knownEnzClassEntries, ArrayList<org.graffiti.graph.GraphElement> workList,
			HashSet<org.graffiti.graph.GraphElement> addMappingDataToTheseGraphElements, String substanceNameFromDataset,
			boolean edgeSearch, HashSet<org.graffiti.graph.Node> edgeMappingPossibleSourceNodes,
			HashSet<org.graffiti.graph.Node> edgeMappingPossibleTargetNodes) {
		
		// process graph edge mapping
		// enumerate possible target and source nodes by recursive call
		// enumerate edges, which connect the source nodes with target nodes
		// if no edges are found, fill the edgeMappingPossibleSourceNodes and
		// edgeMappingPossibleTargetNodes structures
		int specialCharPos = substanceNameFromDataset.indexOf("^");
		if (specialCharPos > 0) {
			String substanceA = substanceNameFromDataset.substring(0, specialCharPos);
			String substanceB = substanceNameFromDataset.substring(specialCharPos + "^".length());
			HashSet<org.graffiti.graph.GraphElement> possibleTargetsA = new HashSet<GraphElement>();
			HashSet<org.graffiti.graph.GraphElement> possibleTargetsB = new HashSet<GraphElement>();
			enumeratePossibleTargetGraphElementsForSubstance(considerEnzymeDb, considerKoDb, graphNode2possibleIDs,
					id2quad, newGraphElementsThatShouldNotAgainBeUsedForMapping, knownEnzClassEntries, workList,
					possibleTargetsA, substanceA, true, null, null);
			enumeratePossibleTargetGraphElementsForSubstance(considerEnzymeDb, considerKoDb, graphNode2possibleIDs,
					id2quad, newGraphElementsThatShouldNotAgainBeUsedForMapping, knownEnzClassEntries, workList,
					possibleTargetsB, substanceB, true, null, null);
			
			for (GraphElement geA : possibleTargetsA) {
				if (!(geA instanceof org.graffiti.graph.Node))
					continue;
				org.graffiti.graph.Node nA = (org.graffiti.graph.Node) geA;
				for (GraphElement geB : possibleTargetsB) {
					if (!(geB instanceof org.graffiti.graph.Node))
						continue;
					org.graffiti.graph.Node nB = (org.graffiti.graph.Node) geB;
					for (Edge ee : nA.getEdges()) {
						if ((ee.getSource() == nA && ee.getTarget() == nB) || (ee.getSource() == nB && ee.getTarget() == nA)) {
							addMappingDataToTheseGraphElements.add(ee);
						}
					}
				}
			}
			
			if (addMappingDataToTheseGraphElements.size() <= 0) {
				for (GraphElement geA : possibleTargetsA) {
					if (!(geA instanceof org.graffiti.graph.Node))
						continue;
					org.graffiti.graph.Node nA = (org.graffiti.graph.Node) geA;
					if (edgeMappingPossibleSourceNodes != null)
						edgeMappingPossibleSourceNodes.add(nA);
				}
				for (GraphElement geB : possibleTargetsB) {
					if (!(geB instanceof org.graffiti.graph.Node))
						continue;
					org.graffiti.graph.Node nB = (org.graffiti.graph.Node) geB;
					if (edgeMappingPossibleTargetNodes != null)
						edgeMappingPossibleTargetNodes.add(nB);
				}
			}
			return;
		}
		
		// process graph node mapping or direct (label based) graph edge mapping,
		// or process enumeration of possible source or target nodes
		// for mapping to graph edges (see above)
		if (substanceNameFromDataset.startsWith("EC "))
			substanceNameFromDataset = substanceNameFromDataset.substring("EC ".length());
		if (substanceNameFromDataset.startsWith("EC:"))
			substanceNameFromDataset = substanceNameFromDataset.substring("EC:".length());
		
		// String possibleOrganismId = "";
		// if (substanceNameFromDataset.indexOf(":")>0)
		// possibleOrganismId = substanceNameFromDataset.substring(0,
		// substanceNameFromDataset.indexOf(":")).toUpperCase();
		String possibleGeneIdFromDataset = substanceNameFromDataset.substring(substanceNameFromDataset.indexOf(":") + 1);
		Collection<KoEntry> matchingKoFromDataset = null;
		if (considerKoDb)
			matchingKoFromDataset = KoService.getKoFromGeneIdOrKO(possibleGeneIdFromDataset);
		// matchingKo = KoService.getKoFromGeneId(possibleOrganismId,
		// possibleGeneId);
		for (org.graffiti.graph.GraphElement graphElement : workList) {
			if (!edgeSearch && newGraphElementsThatShouldNotAgainBeUsedForMapping.contains(graphElement))
				continue;
			HashSet<String> idListFromGraph = graphNode2possibleIDs.get(graphElement);
			for (String idFromGraph : idListFromGraph) {
				if (idFromGraph.equalsIgnoreCase(substanceNameFromDataset)) {
					addMappingDataToTheseGraphElements.add(graphElement);
				} else {
					if (considerKoDb && matchingKoFromDataset.size() > 0 && idFromGraph.startsWith("ko:")) { // organism
						// code
						// not
						// needed
						// any
						// more,
						// substanceName.indexOf(":")>0
						// this substanceName might be a gene identifier, which will
						// be matched against the KO database
						// gene lists to get the corresponding KO ID, this KO ID might
						// be matching the active
						// idAndGraphNode - ID
						String koIdOfGraphNode;
						if (idFromGraph.startsWith("ko:"))
							koIdOfGraphNode = idFromGraph.substring("ko:".length());
						else
							koIdOfGraphNode = idFromGraph;
						for (KoEntry koe : matchingKoFromDataset) {
							if (koIdOfGraphNode.equals(koe.getKoID())) {
								addMappingDataToTheseGraphElements.add(graphElement);
								break;
							}
						}
					}
					if (considerEnzymeDb) {
						// make deeper check, check if these nodes represent EC
						// numbers
						// if so, 1.2.3.4 should also match 1.2.3.- !
						EnzClassEntry ece = knownEnzClassEntries.get(substanceNameFromDataset);
						if (!knownEnzClassEntries.containsKey(substanceNameFromDataset)) {
							ece = EnzClassEntry.getEnzClassEntry(substanceNameFromDataset);
							knownEnzClassEntries.put(substanceNameFromDataset, ece);
						}
						// new: nodes that are just newly created will not be used
						// again for mapping
						// for normal data this does not happen anyways, as all
						// substance data that belongs together
						// will be normally processed in one step. This additional
						// restriction is introduced, to avoid
						// the following situation:
						// gene expression data with a measured substance 1.1.1.-
						// would gather all 1.1.1.2, 1.1.1.3, ...
						// nodes that would normally be created. If one of these
						// substances (e.g. 1.1.1.1) would be
						// processed prior to 1.1.1.-, then this node would be created
						// and used
						if (ece != null) {
							QuadNumber quadNumber = id2quad.get(idFromGraph);
							if (quadNumber == null) {
								quadNumber = new QuadNumber(idFromGraph);
								id2quad.put(idFromGraph, quadNumber);
							}
							if (quadNumber.isValidQuadNumber())
								if (ece.isValidMatchFor_Inversed(quadNumber))
									addMappingDataToTheseGraphElements.add(graphElement);
						}
					}
				}
			}
		}
	}
	
	public synchronized static void addMappingData2Node(SubstanceInterface xmlSubstanceNode,
			org.graffiti.graph.GraphElement graphNode, String diagramStyle) {
		XMLAttribute xa;
		CollectionAttribute ca;
		try {
			ca = (CollectionAttribute) graphNode.getAttribute(mapFolder);
		} catch (AttributeNotFoundException e2) {
			graphNode.addAttribute(new HashMapAttribute(mapFolder), "");
			ca = (CollectionAttribute) graphNode.getAttribute(mapFolder);
		}
		NodeTools.setNodeComponentType(graphNode, diagramStyle);
		try {
			xa = (XMLAttribute) ca.getAttribute(mapVarName);
		} catch (AttributeNotFoundException e) {
			ca.add(new XMLAttribute(mapVarName), false);
			xa = (XMLAttribute) ca.getAttribute(mapVarName);
		}
		// Add XML Substance Data to Mapping List
		xa.addData(xmlSubstanceNode);
	}
	
	public static void addMappingData2Node(SubstanceInterface xmlSubstanceNode, org.graffiti.graph.Node graphNode) {
		List<MyComparableDataPoint> testList = NodeTools.getSortedAverageDataSetValues(xmlSubstanceNode, null);
		Set<String> times = new HashSet<String>();
		for (MyComparableDataPoint mcdp : testList) {
			times.add(mcdp.timeUnitAndTime);
		}
		
		Set<String> conditions = new HashSet<String>();
		for (MyComparableDataPoint mcdp : testList) {
			conditions.add(mcdp.serie);
		}
		
		if (times.size() > 1)
			addMappingData2Node(xmlSubstanceNode, graphNode, GraffitiCharts.LINE.getName());
		else {
			if (conditions.size() > 1)
				addMappingData2Node(xmlSubstanceNode, graphNode, GraffitiCharts.BAR_FLAT.getName());
			else
				addMappingData2Node(xmlSubstanceNode, graphNode, GraffitiCharts.HEATMAP.getName());
		}
		
	}
	
	public static void addExperimentNameInfoToAllLinesForThisSubstanceNode(Node xmlSubstanceNode) {
		xmlSubstanceNode.getOwnerDocument().getElementsByTagName("line");
		Node lineNode = xmlSubstanceNode.getFirstChild();
		while (lineNode != null) {
			if (lineNode.getNodeName().equalsIgnoreCase("line")
					&& lineNode.getAttributes().getNamedItem("experimentname") == null) {
				Attr attr = lineNode.getOwnerDocument().createAttribute("experimentname");
				// the following command will use the getOwnerDocument call
				// to retrieve the experimentname from the top level experimentinfo
				// structure, which is not available once the data has been saved to
				// file
				// and then read from file. In this case only the sub-tree for the
				// substance
				// is saved, not the structures above the hierachy
				attr.setNodeValue(XPathHelper.getExperimentNameFromLineNode(lineNode));
				Element e = (Element) lineNode;
				e.setAttributeNode(attr);
			}
			lineNode = lineNode.getNextSibling();
		}
	}
	
	private HashSet<String> getAlternativeIdsAndLabelsOfGraphElement(org.graffiti.graph.GraphElement graphElement,
			boolean considerCompoundDb, boolean considerEnzymeDb, boolean considerKoDb,
			boolean considerMappingToKEGGmapNodes) {
		HashSet<String> result = new HashSet<String>();
		for (String nodeName : AttributeHelper.getLabels(graphElement)) {
			if (nodeName != null) {
				result.add(nodeName);
				String strippedHtml = StringManipulationTools.removeHTMLtags(nodeName);
				if (strippedHtml != null && strippedHtml.length() > 0 && (strippedHtml.length() != nodeName.length()))
					result.add(strippedHtml);
				if (considerEnzymeDb) {
					EnzymeEntry eze = EnzymeService.getEnzymeInformation(nodeName, false);
					if (eze != null) {
						result.add(eze.getDE());
						for (String an : eze.getAN())
							result.add(an);
					}
				}
				
				if (considerCompoundDb) {
					CompoundEntry ce = CompoundService.getInformation(nodeName);
					if (ce != null) {
						result.add(ce.getID());
						for (String name : ce.getNames())
							result.add(name);
					}
				}
			}
		}
		if (graphElement instanceof org.graffiti.graph.Node) {
			org.graffiti.graph.Node n = (org.graffiti.graph.Node) graphElement;
			String keggId = KeggGmlHelper.getKeggId(graphElement);
			if (keggId != null && keggId.length() > 0) {
				keggId = keggId.trim();
				String lbl = AttributeHelper.getLabel(n, "");
				String keggType = KeggGmlHelper.getKeggType(n);
				if (keggType != null && keggType.length() > 0 && keggType.equalsIgnoreCase("map")
						&& considerMappingToKEGGmapNodes && lbl.indexOf("TITLE:") < 0) {
					String prevStatus1 = status1;
					String prevStatus2 = status2;
					status1 = "Retrieve KEGG Map Link Node Elements...";
					status2 = "Map " + keggId + ": call for Enzyme list...";
					String[] enzymes = KeggHelper.getKeggEnzymesOfMap(keggId);
					status2 = "Map " + keggId + ": call for Compound list...";
					String[] compounds = KeggHelper.getKeggCompoundsOfMap(keggId);
					status2 = "Map " + keggId + ": call for KO list...";
					String[] kos = KeggHelper.getKeggKOsOfMap(keggId);
					String se = AttributeHelper.getStringList(enzymes, ",");
					se = StringManipulationTools.stringReplace(se, "ec:", "");
					String sc = AttributeHelper.getStringList(compounds, ",");
					sc = StringManipulationTools.stringReplace(sc, "cpd:", "");
					String sk = AttributeHelper.getStringList(kos, ",");
					sk = StringManipulationTools.stringReplace(sk, "ko:", "");
					AttributeHelper.setAttribute(graphElement, "kegg_soap_result", "enzymes", se);
					AttributeHelper.setAttribute(graphElement, "kegg_soap_result", "compounds", sc);
					AttributeHelper.setAttribute(graphElement, "kegg_soap_result", "ko", sk);
					if (enzymes != null && enzymes.length > 0)
						for (String e : enzymes) {
							if (considerEnzymeDb) {
								EnzymeEntry eze = EnzymeService.getEnzymeInformation(e, false);
								if (eze != null) {
									result.add(eze.getDE());
									for (String an : eze.getAN())
										result.add(an);
								}
							}
							result.add(e.replace("ec:", ""));
						}
					if (compounds != null && compounds.length > 0)
						for (String c : compounds) {
							if (considerCompoundDb) {
								CompoundEntry ce = CompoundService.getInformation(c);
								if (ce != null) {
									result.add(ce.getID());
									for (String name : ce.getNames())
										result.add(name);
								}
							}
							result.add(c.replace("cpd:", ""));
						}
					if (kos != null && kos.length > 0)
						for (String ko : kos)
							result.add(ko.replace("ko:", ""));
					status1 = prevStatus1;
					status2 = prevStatus2;
				}
				
				// check if this a species specific pathway
				// if yes, then the organism code of the KEGG IDs is removed
				ArrayList<String> keggIds = new ArrayList<String>();
				if (keggId.indexOf(" ") > 0) {
					String[] ids = keggId.split(" ");
					for (String id : ids) {
						id = id.trim();
						if (id.length() > 0) {
							keggIds.add(id);
							if (id.contains(":"))
								keggIds.add(id.substring(id.indexOf(":") + ":".length()));
						}
					}
				} else {
					keggIds.add(keggId);
					if (keggId.contains(":"))
						keggIds.add(keggId.substring(keggId.indexOf(":") + ":".length()));
				}
				for (String id : keggIds)
					result.add(id);
			}
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		return progressValue;
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
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return getCurrentStatusValue();
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
	
	@Override
	public void setCurrentStatusValue(int value) {
		progressValue = value;
	}
	
	@Override
	public void setCurrentStatusValueFine(double value) {
		setCurrentStatusValueFine(value);
	}
	
	@Override
	public boolean wantsToStop() {
		return pleaseStop;
	}
	
	@Override
	public void setCurrentStatusText1(String status) {
		status1 = status;
	}
	
	@Override
	public void setCurrentStatusText2(String status) {
		status2 = status;
	}
	
	/*
	 * (non-Javadoc)
	 * @seeorg.BackgroundTaskStatusProviderSupportingExternalCall#
	 * setCurrentStatusValueFineAdd(double)
	 */
	@Override
	public void setCurrentStatusValueFineAdd(double smallProgressStep) {
		progressValue += smallProgressStep;
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
	
	@Override
	public void setPrefix1(String prefix1) {
		// empty
	}
}
