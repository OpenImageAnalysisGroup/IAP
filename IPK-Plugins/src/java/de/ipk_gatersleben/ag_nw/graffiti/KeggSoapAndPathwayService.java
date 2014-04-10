/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.HelperClass;
import org.Vector2d;
import org.graffiti.editor.actions.ClipboardService;
import org.graffiti.editor.actions.CopyAction;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.DatabaseBasedLabelReplacementService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggPathwayEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;

/*
 * Created on 10.01.2005 by Christian Klukas
 */

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggSoapAndPathwayService
		implements Runnable, BackgroundTaskStatusProvider, HelperClass {
	
	private String status1;
	private String targetDirectory;
	private ArrayList<KeggPathwayEntry> keggPathwayEntries;
	private boolean pleaseStop = false;
	private String status2;
	private double statusFine = -1d;
	private Color enzymeColor;
	
	public void setOptions(String targetdirectory, ArrayList<KeggPathwayEntry> keggPathwayEntries,
			Color enzymeColor) {
		this.targetDirectory = targetdirectory;
		this.keggPathwayEntries = keggPathwayEntries;
		this.enzymeColor = enzymeColor;
	}
	
	private static Graph getKeggPathway(
			int index,
			ArrayList<KeggPathwayEntry> keggPathwayEntries,
			Color enzymeColor) {
		if (index >= keggPathwayEntries.size() || index < 0)
			return null;
		else {
			try {
				return KeggService.getKeggPathwayGravistoGraph(keggPathwayEntries
						.get(index), false, enzymeColor);
			} catch (Exception er) {
				ErrorMsg.addErrorMessage(er);
			}
			return null;
		}
	}
	
	public static void interpreteDatabaseIdentifiers(Collection<Node> nodes,
			boolean storeOldId) {
		String oldid = "oldid";
		if (!storeOldId)
			oldid = null;
		DatabaseBasedLabelReplacementService mrs = new DatabaseBasedLabelReplacementService(
				nodes,
				false, // compoundNameToID,
				true, // boolean compoundIDtoName,
				false, // boolean ecNumberToName,
				false, // boolean ecNameOrSynonymeToECnumber,
				false, // boolean reactionNumberToName,
				false, // boolean reactionNameToNo,
				false,
				false,
				true,
				false,
				true, // boolean increaseNodeSize,
				false, // boolean useShortName
				storeOldId,
				true, // use greek name
				oldid);
		mrs.run();
	}
	
	public static void interpreteDatabaseIdentifiers(Collection<Node> nodes,
			boolean storeOldId, boolean resizeNodes) {
		String oldid = "oldlabel";
		ArrayList<Node> validNodes = new ArrayList<Node>();
		for (Node n : nodes) {
			boolean workNeeded = false;
			String lbl = AttributeHelper.getLabel(n, "");
			if (lbl != null && lbl.length() > 4) {
				lbl = lbl.substring(1, 4);
				try {
					int a = Integer.parseInt(lbl); // is the label like a number?
					if (a >= 0)
						workNeeded = true; // we don't like numeric IDs, lets try to exchange with proper names
				} catch (Exception e) {
					// empty
				}
			}
			if (workNeeded)
				validNodes.add(n);
		}
		if (!storeOldId)
			oldid = null;
		DatabaseBasedLabelReplacementService mrs = new DatabaseBasedLabelReplacementService(
				validNodes,
				false, // compoundNameToID,
				true, // boolean compoundIDtoName,
				false, // boolean ecNumberToName,
				false, // boolean ecNameOrSynonymeToECnumber,
				false, // boolean reactionNumberToName,
				false, // boolean reactionNameToNo,
				true, // kegg id --> KO-EC
				true, // ko ID --> ko Name
				true, // brite based ko ID --> gene name
				false, // brite based ko ID --> EC id
				resizeNodes, // boolean increaseNodeSize,
				false, // boolean useShortName
				storeOldId,
				true, // use greek name
				oldid);
		mrs.run();
	}
	
	public static boolean writeGML(Graph g, String fileName) {
		if (g == null || g.getNodes().size() <= 0)
			return false;
		CopyAction.doCopyGraph(g, new Selection());
		String gml = ClipboardService.readFromClipboardAsText();
		if (gml == null || gml.length() <= 0)
			return false;
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(gml);
			out.close();
			return true;
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int i = 0;
		Graph graph;
		int maxI = keggPathwayEntries.size();
		int errorCount = 0;
		int success = 0;
		int currErrCnt = ErrorMsg.getErrorMsgCount();
		while ((i < maxI) && !pleaseStop) {
			graph = getKeggPathway(i++, keggPathwayEntries, enzymeColor);
			statusFine = ((double) i) / (double) maxI * 100d;
			if (graph == null) {
				errorCount++;
				System.err.println("Kegg Pathway " + keggPathwayEntries.get(i - 1) + " could not be loaded or is empty." +
						"<br>URL: " + keggPathwayEntries.get(i - 1).getPathwayURL());
				ErrorMsg.addErrorMessage("Kegg Pathway " + keggPathwayEntries.get(i - 1) + " could not be loaded or is empty." +
						"<br>URL: " + keggPathwayEntries.get(i - 1).getPathwayURL());
				continue;
			}
			String errTxt = "";
			if (ErrorMsg.getErrorMsgCount() - currErrCnt > 0) {
				errTxt = ", check error log! (" + (ErrorMsg.getErrorMsgCount() - currErrCnt) + ")";
			}
			if (errorCount > 0)
				errTxt = errTxt + ", " + errorCount + " graph(s) not loaded";
			status1 = "Process graph " + i + "/" + maxI + errTxt + ": ";
			status2 = graph.getName();
			
			String search = "rn:";
			Selection selection = new Selection();
			search = search.toUpperCase(); // case insensitive
			Stack<Node> toAdd = new Stack<Node>();
			for (Node node : graph.getNodes()) {
				String label = AttributeHelper.getLabel(node, "");
				if (label.toUpperCase().indexOf(search) >= 0)
					toAdd.add(node);
				else {
					Vector2d pos = AttributeHelper.getPositionVec2d(node);
					if (pos.x == -1 && pos.y == -1) {
						toAdd.add(node);
					}
				}
			}
			while (!toAdd.empty())
				selection.add((GraphElement) toAdd.pop());
			MyNonInteractiveSpringEmb se = new MyNonInteractiveSpringEmb(graph, selection, MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings());
			se.run();
			statusFine = (i + 0.33d) / maxI * 100d;
			interpreteDatabaseIdentifiers(graph.getNodes(), true);
			String fileName = keggPathwayEntries.get(i - 1).getMapName() + "_" + keggPathwayEntries.get(i - 1).getPathwayName().replaceAll("/", "_").trim()
					+ ".gml";
			writeGML(graph, targetDirectory + fileName);
			statusFine = (i + 0.66d) / maxI * 100d;
			success++;
		}
		statusFine = 100;
		String helpText = "";
		if (errorCount > 0) {
			helpText = ", check Help/Error Messages (" + errorCount + ")!";
		}
		if (!pleaseStop) {
			status1 = success + "/" + keggPathwayEntries.size() + " pathways saved" + helpText;
			status2 = "Target folder: " + targetDirectory;
		} else {
			status1 = "Processing user aborted" + helpText;
			status2 = success + "/" + keggPathwayEntries.size() + " pathways saved in folder " + targetDirectory;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		return (int) getCurrentStatusValueFine();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return statusFine;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	@Override
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	@Override
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseStop()
	 */
	@Override
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	@Override
	public boolean wantsToStop() {
		return pleaseStop;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	@Override
	public void pleaseContinueRun() {
		// empty
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		statusFine = value;
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
	
	// private static void initKeggList() throws JaxenException, IOException, ServiceException {
	// String url = "http://pgrc-16.ipk-gatersleben.de/~klukas/www.genome.ad.jp";
	// HTMLparser h = new HTMLparser();
	//
	// Collection<KeggPathwayEntry> pathways = h.getXMLpathways(
	// url,
	// new OrganismEntry("map", "Reference Pathways"), false);
	// KeggService.resetCache();
	// for (KeggPathwayEntry kwe : pathways) {
	// kwe.setMappingCount("");
	// keggPathwayEntries.add(kwe);
	// }
	// }
}
