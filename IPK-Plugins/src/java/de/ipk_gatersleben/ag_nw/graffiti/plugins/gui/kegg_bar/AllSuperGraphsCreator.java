/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 14.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import java.io.File;
import java.util.ArrayList;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.editor.actions.FileSaveAsAction;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.OrganismEntry;

public class AllSuperGraphsCreator implements BackgroundTaskStatusProvider,
		Runnable {
	
	private final Graph graph;
	private String message1 = "Please wait...";
	private String message2 = "";
	private boolean stop = false;
	private double progress;
	private String targetFolder;
	private final boolean convertKOsToGenes;
	private final boolean checkOrthologs;
	private final boolean checkEnzymes;
	private final boolean checkGlycans;
	private final boolean checkCompounds;
	private OrganismEntry[] orgs;
	
	public AllSuperGraphsCreator(Graph graph, String targetFolder, OrganismEntry[] orgs,
			boolean checkOrthologs, boolean checkEnzymes, boolean checkGlycans, boolean checkCompounds, boolean convertKOsToGenes) {
		this.graph = graph;
		this.targetFolder = targetFolder;
		this.orgs = orgs;
		this.convertKOsToGenes = convertKOsToGenes;
		this.checkOrthologs = checkOrthologs;
		this.checkEnzymes = checkEnzymes;
		this.checkGlycans = checkGlycans;
		this.checkCompounds = checkCompounds;
	}
	
	@Override
	public int getCurrentStatusValue() {
		return (int) getCurrentStatusValueFine();
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		// empty
	}
	
	@Override
	public double getCurrentStatusValueFine() {
		return progress;
	}
	
	@Override
	public String getCurrentStatusMessage1() {
		return message1;
	}
	
	@Override
	public String getCurrentStatusMessage2() {
		return message2;
	}
	
	@Override
	public void pleaseStop() {
		stop = true;
	}
	
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	@Override
	public void pleaseContinueRun() {
		// empty
	}
	
	@Override
	public void run() {
		KeggHelper kegg = new KeggHelper();
		try {
			message1 = "SOAP: Get KEGG Organism-List";
			if (orgs == null || orgs.length <= 0)
				orgs = (OrganismEntry[]) kegg.getOrganisms().toArray();
			double current = 0;
			int max = orgs.length;
			double step = 1d / orgs.length;
			int lastErrorCnt = ErrorMsg.getErrorMsgCount();
			
			ArrayList<String> coveredMaps = new ArrayList<String>();
			for (Node n : graph.getNodes()) {
				NodeHelper nh = new NodeHelper(n);
				String cluster = nh.getClusterID(null);
				if (cluster != null) {
					if (cluster.indexOf("map") >= 0) {
						cluster = cluster.substring(cluster.indexOf("map"));
						cluster = StringManipulationTools.stringReplace(cluster, ".xml", "");
						if (!coveredMaps.contains(cluster))
							coveredMaps.add(cluster);
					} else
						if (cluster.indexOf("ko") >= 0) {
							cluster = cluster.substring(cluster.indexOf("ko"));
							cluster = StringManipulationTools.stringReplace(cluster, ".xml", "");
							if (!coveredMaps.contains(cluster))
								coveredMaps.add(cluster);
						}
				}
			}
			for (OrganismEntry organismSelection : orgs) {
				progress = 100d * (current / max);
				message1 = "Process organism " + organismSelection.getShortName() + " (" + ((int) (current + 1)) + "/" + max + ")";
				message2 = "Copy Graph...";
				Graph graphCopy = new AdjListGraph(new ListenerManager());
				graphCopy.addGraph(graph);
				message2 = "Enumerate source maps...";
				for (Node n : graphCopy.getNodes()) {
					new NodeHelper(n);
					String keggID = (String) AttributeHelper.getAttributeValue(n,
							"kegg", "kegg_name", "", "");
					if (checkOrthologs)
						if (keggID.startsWith("ko:"))
							AttributeHelper.setAttribute(n, "kegg", "present", "not found");
					if (checkEnzymes)
						if (keggID.startsWith("ec:"))
							AttributeHelper.setAttribute(n, "kegg", "present", "not found");
					if (checkGlycans)
						if (keggID.startsWith("glycan:"))
							AttributeHelper.setAttribute(n, "kegg", "present", "not found");
					if (checkCompounds)
						if (keggID.startsWith("cpd:"))
							AttributeHelper.setAttribute(n, "kegg", "present", "not found");
				}
				double workLoad = coveredMaps.size();
				int workI = 0;
				for (String map : coveredMaps) {
					String sn = organismSelection.getShortName();
					map = StringManipulationTools.stringReplace(map, "map", sn);
					message2 = "SOAP: Check present enzymes for map " + map + "...";
					KeggService.colorizeEnzymesGlycansCompounds(graphCopy, map,
							KeggService.getDefaultEnzymeColor(), false,
							checkOrthologs, checkEnzymes, checkGlycans, checkCompounds, convertKOsToGenes);
					workI++;
					progress = 100d * (current / max + step * workI / workLoad);
					if (stop)
						break;
				}
				message2 = "Remove not present elements...";
				ArrayList<Node> toBeDeleted = new ArrayList<Node>();
				for (Node n : graphCopy.getNodes()) {
					// NodeHelper nh = new NodeHelper(n);
					String enzymePresent = (String) AttributeHelper.getAttributeValue(n, "kegg", "present", "", "", false);
					if (enzymePresent.equalsIgnoreCase("not found") /* || nh.getDegree()<=0 */) {
						toBeDeleted.add(n);
					}
				}
				// ArrayList<Edge> toBeDeletedEdges = new ArrayList<Edge>();
				// for (Edge e : graphCopy.getEdges()) {
				// if ((e.getSource()==e.getTarget()) || toBeDeleted.contains(e.getSource()) || toBeDeleted.contains(e.getTarget()))
				// toBeDeletedEdges.add(e);
				// }
				// for (Edge del : toBeDeletedEdges)
				// graphCopy.deleteEdge(del);
				for (Node del : toBeDeleted)
					graphCopy.deleteNode(del);
				// boolean degree0found = false;
				// do {
				// toBeDeleted.clear();
				// for (Node n : graphCopy.getNodes()) {
				// NodeHelper nh = new NodeHelper(n);
				// if (nh.getDegree()<=0) {
				// toBeDeleted.add(n);
				// }
				// }
				// for (Node del : toBeDeleted) {
				// graphCopy.deleteNode(del);
				// }
				// for (Node n : graphCopy.getNodes())
				// if (n.getDegree()<=0) {
				// degree0found = true;
				// break;
				// }
				// } while(degree0found);
				message2 = "Save graph...";
				String sep = File.separator;
				int currentErrorCnt = ErrorMsg.getErrorMsgCount();
				int thisErrCnt = currentErrorCnt - lastErrorCnt;
				if (!targetFolder.endsWith(sep))
					targetFolder = targetFolder + sep;
				File file = new File(targetFolder + organismSelection.getShortName() + "_err_" + (thisErrCnt) + ".gml");
				message2 = "Save graph " + file.toString() + "...";
				lastErrorCnt = ErrorMsg.getErrorMsgCount();
				FileSaveAsAction.safeFile(file, ".gml", graphCopy);
				message2 = "Graph file " + file.toString() + " created";
				current++;
				if (stop) {
					message1 = "Processing incomplete";
					message2 = "User abort";
					break;
				}
			}
			progress = 100d;
			stop = false;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
}
