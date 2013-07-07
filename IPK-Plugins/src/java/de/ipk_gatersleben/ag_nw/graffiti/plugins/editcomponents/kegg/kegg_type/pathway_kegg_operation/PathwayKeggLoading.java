/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 09.06.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.pathway_kegg_operation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggPathwayEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.RelationType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.NoOverlappOfClustersAlgorithm;

public class PathwayKeggLoading {
	
	public static void collapsePathway(Graph graph, String curVal, Node sourceNode) {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS_ENH))
			return;
		String pathwayId = KeggGmlHelper.getKeggId(sourceNode);
		
		if (pathwayId == null || !pathwayId.startsWith("path:"))
			return;
		
		if (sourceNode != null) {
			ArrayList<Node> nnll = new ArrayList<Node>();
			for (Node n : graph.getNodes()) {
				NodeHelper nh = new NodeHelper(n);
				String tId = nh.getClusterID("");
				if (tId != null && tId.equals(pathwayId))
					nnll.add(n);
			}
			Vector2d ctr = NodeTools.getCenter(nnll);
			AttributeHelper.setPosition(sourceNode, ctr);
		}
		String currentLabel = KeggGmlHelper.getKeggGraphicsTitle(sourceNode);
		if (currentLabel.startsWith("TITLE:") || currentLabel.startsWith("<html>TITLE:")) {
			currentLabel = currentLabel.replaceAll("TITLE:", "");
			KeggGmlHelper.setKeggGraphicsTitle(sourceNode, currentLabel);
		}
		
		Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
		Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
		HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
		Pathway p = Pathway.getPathwayFromGraph(graph, warnings, errors, entry2graphNode);
		
		Entry mapEntry = null;
		for (Entry e : p.getEntries())
			if (e.getSourceGraphNode() == sourceNode && e.getType() == EntryType.map) {
				mapEntry = e;
				break;
			}
		
		if (mapEntry == null) {
			MainFrame.showMessageDialog(
								"<html>" +
													"Map Entry could not be found in generated KGML Pathway.<br>" +
													"Can't proceed.", "Error");
			return;
		}
		
		HashSet<Entry> entriesOfMap = new HashSet<Entry>();
		for (Entry e : p.getEntries())
			if (e.getSourcePathwayKeggId() != null && e.getSourcePathwayKeggId().equalsIgnoreCase(pathwayId))
				entriesOfMap.add(e);
		
		HashSet<Relation> relDel = new HashSet<Relation>();
		for (Relation r : p.getRelations()) {
			Entry se = r.getSourceEntry();
			Entry te = r.getTargetEntry();
			if (entriesOfMap.contains(se) && !entriesOfMap.contains(te)) {
				r.setSourceEntry(mapEntry);
				r.setType(RelationType.maplink);
			}
			if (entriesOfMap.contains(te) && !entriesOfMap.contains(se)) {
				r.setTargetEntry(mapEntry);
				r.setType(RelationType.maplink);
			}
			if (entriesOfMap.contains(se) && entriesOfMap.contains(te)) {
				relDel.add(r);
			}
		}
		for (Relation rd : relDel) {
			p.getRelations().remove(rd);
		}
		
		for (Entry eom : entriesOfMap) {
			if (eom != mapEntry) {
				if (eom.getType() == EntryType.enzyme) {
					IdRef mr = new IdRef(mapEntry, mapEntry.getId().getValue());
					eom.setMapRef(mr);
					eom.setGraphics(null);
				} else
					p.getEntries().remove(eom);
			}
		}
		graph.deleteAll(graph.getGraphElements());
		p.getGraph(graph);
	}
	
	public static void loadAndMergePathway(Graph baseGraph, String curVal, Node sourceNode, boolean separateClusters) {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS_ENH))
			return;
		
		InputStream inpStream = null;
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		KeggPathwayEntry kpe = KeggPathwayEntry.getKeggPathwayEntryFromMap(curVal);
		try {
			Vector2d targetCenterOfNewPathwayGraph = null;
			if (sourceNode != null) {
				targetCenterOfNewPathwayGraph = AttributeHelper.getPositionVec2d(sourceNode);
			}
			inpStream = kpe.getOpenInputStream();
			doc = builder.build(inpStream);
			Element kegg = doc.getRootElement();
			Pathway addThisPathway = Pathway.getPathwayFromKGML(kegg);
			Pathway combinedPathwayInformation =
								processInternalMapLinks(baseGraph, addThisPathway, targetCenterOfNewPathwayGraph);
			GraphHelper.clearSelection();
			baseGraph.clear();
			Graph gn = new AdjListGraph(new ListenerManager());
			combinedPathwayInformation.getGraph(gn);
			baseGraph.addGraph(gn);
			if (separateClusters) {
				GravistoService.getInstance().runAlgorithm(
									new NoOverlappOfClustersAlgorithm(), baseGraph, new Selection(""), null);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static Pathway processInternalMapLinks(Graph baseGraph, Pathway optAddPathway, Vector2d optCenterOfAddGraph) {
		if (optAddPathway != null) {
			Graph addGraph = optAddPathway.getGraph();
			// move graph elements to target-center position (source node position)
			if (optCenterOfAddGraph != null)
				repositionGraphNodes(optCenterOfAddGraph, addGraph);
			
			baseGraph.addGraph(addGraph);
		}
		Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
		Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
		HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
		
		// at this stage the pathway contains the source pathway and the new pathway
		// later, the source pathway map-link needs to be removed
		// also all new map-links need to be "re-routed" to the prior existing map links
		Pathway combinedPathway = Pathway.getPathwayFromGraph(baseGraph, warnings, errors, entry2graphNode);
		
		// * identify source pathway ids in combined pathway
		// * identify "hidden" eentries inside map link nodes, representing the source pathway ids
		// * remove map link entries (not map link title entries) which represent the loaded
		// pathways
		// * create new connections, which connect the subtype entry-nodes
		// * some information about specific connectivity of the pathways is lost during this process
		
		HashSet<String> loadedMaps = new HashSet<String>();
		for (Entry e : combinedPathway.getEntries()) {
			if (e.getType() != EntryType.map && e.getSourcePathwayKeggId() != null && e.getGraphics() != null) {
				// System.out.println("Loaded map: "+e.getSourcePathwayKeggId()+" says: "+e.getVisibleName());
				loadedMaps.add(e.getSourcePathwayKeggId());
			}
		}
		
		HashSet<Entry> mapEntriesToBeDeleted = new HashSet<Entry>();
		for (String processLinksToThisPathway : loadedMaps) {
			for (Entry mapEntry : combinedPathway.getEntries()) {
				if (mapEntry.getType() != EntryType.map || mapEntry.getVisibleName().contains("TITLE:"))
					continue;
				if (mapEntry.getName().getId().equals(processLinksToThisPathway)) {
					for (Relation r : combinedPathway.getRelations()) {
						if (r.getSourceEntry() == mapEntry) {
							// set source to sub-component of relation in other pathway
							Entry e = findNearestTargetEntry(combinedPathway, r.getSubtypeRefs(), r.getTargetEntry(), processLinksToThisPathway, entry2graphNode);
							if (e != null) {
								r.setSourceEntry(e);
								mapEntriesToBeDeleted.add(mapEntry);
							}
						}
						if (r.getTargetEntry() == mapEntry) {
							// set target to sub-component of relation in other pathway
							Entry e = findNearestTargetEntry(combinedPathway, r.getSubtypeRefs(), r.getSourceEntry(), processLinksToThisPathway, entry2graphNode);
							if (e != null) {
								r.setTargetEntry(e);
								mapEntriesToBeDeleted.add(mapEntry);
							}
						}
					}
				}
			}
		}
		// delete "hidden" elements, assigned to a map-entry
		HashSet<Entry> hiddenEntriesToBeDeleted = new HashSet<Entry>();
		for (Entry e : combinedPathway.getEntries())
			if (e.getMapRef() != null && mapEntriesToBeDeleted.contains(e.getMapRef().getRef()))
				hiddenEntriesToBeDeleted.add(e);
		for (Entry del : hiddenEntriesToBeDeleted)
			combinedPathway.getEntries().remove(del);
		
		// remove not needed map entries
		for (Entry del : mapEntriesToBeDeleted) {
			combinedPathway.getEntries().remove(del);
			System.out.println("(1) Delete map entry: " + del.getVisibleName());
		}
		
		// merge entries of new pathway with existing map references in remaining network
		HashSet<Entry> mapEntriesOfNewPathway = new HashSet<Entry>();
		for (Entry e : combinedPathway.getEntries()) {
			if (e.getType() == EntryType.map) {
				if (optAddPathway != null) {
					if (e.getSourcePathwayKeggId() != null && e.getSourcePathwayKeggId().equals(optAddPathway.getName().getId())) {
						mapEntriesOfNewPathway.add(e);
						System.out.println("(2a) Proposal of deletion of map entry: " + e.getVisibleName());
					}
				} else {
					mapEntriesOfNewPathway.add(e);
					System.out.println("(2b) Proposal of deletion of map entry: " + e.getVisibleName());
				}
			}
		}
		combinedPathway.removeMergeTheseEntriesIfPossible(mapEntriesOfNewPathway);
		
		return combinedPathway;
	}
	
	private static void repositionGraphNodes(Vector2d centerOfAddGraph, Graph addGraph) {
		Vector2d currCenter = NodeTools.getCenter(addGraph.getNodes());
		double diffX = centerOfAddGraph.x - currCenter.x;
		double diffY = centerOfAddGraph.y - currCenter.y;
		for (Node n2 : addGraph.getNodes()) {
			NodeHelper nh = new NodeHelper(n2);
			nh.setPosition(nh.getX() + diffX, nh.getY() + diffY);
		}
	}
	
	private static Entry findNearestTargetEntry(
						Pathway pathway,
						ArrayList<IdRef> subtypeRefs,
						Entry replacementEntry,
						String processLinksToThisPathway,
						HashMap<Entry, Node> entry2graphNode) {
		ArrayList<String> searchIds = new ArrayList<String>();
		if (subtypeRefs != null) {
			for (IdRef sr : subtypeRefs) {
				String id = sr.getRef().getName().getId();
				searchIds.add(id);
			}
		}
		searchIds.add(replacementEntry.getName().getId());
		for (String searchThisId : searchIds) {
			ArrayList<Entry> possibleTargetsOrSources = new ArrayList<Entry>();
			for (Entry e : pathway.getEntries())
				if (e.getName().getId().equals(searchThisId) &&
									e.getSourcePathwayKeggId().equals(processLinksToThisPathway)) {
					possibleTargetsOrSources.add(e);
				}
			
			// if no target has been found, return title map node of target pathway
			if (possibleTargetsOrSources.size() <= 0)
				for (Entry e : pathway.getEntries())
					if (e.getType() == EntryType.map)
						if (e.getName().getId().equals(processLinksToThisPathway))
							if (e.getSourcePathwayKeggId().equals(processLinksToThisPathway)) {
								possibleTargetsOrSources.add(e);
							}
			
			Entry eWithMinDist = null;
			double minDist = Double.MAX_VALUE;
			
			for (Entry possibleTargetOrSource : possibleTargetsOrSources) {
				double dist = getDistance(possibleTargetOrSource, replacementEntry, entry2graphNode);
				if (dist < minDist || eWithMinDist == null) {
					minDist = dist;
					eWithMinDist = possibleTargetOrSource;
				}
			}
			if (eWithMinDist != null)
				return eWithMinDist;
		}
		return null;
	}
	
	private static double getDistance(Entry ea, Entry eb, HashMap<Entry, Node> entry2graphNode) {
		Node a = entry2graphNode.get(ea);
		Node b = entry2graphNode.get(eb);
		if (a == null || b == null)
			return Double.NaN;
		Vector2d p1 = AttributeHelper.getPositionVec2d(a);
		Vector2d p2 = AttributeHelper.getPositionVec2d(b);
		return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
	}
}
