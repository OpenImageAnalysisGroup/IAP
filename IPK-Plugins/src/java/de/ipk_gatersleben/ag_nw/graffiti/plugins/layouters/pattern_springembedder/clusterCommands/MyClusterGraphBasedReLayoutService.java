/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.HelperClass;
import org.Release;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.EdgeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.RelationType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class MyClusterGraphBasedReLayoutService
		implements BackgroundTaskStatusProviderSupportingExternalCall, Runnable, HelperClass {
	boolean userBreak = false;
	double statusValue = -1;
	boolean pleaseStop = false;
	
	String status1, status2;
	
	private Algorithm layoutAlgorithm;
	private final boolean currentOptionWaitForLayout;
	private final Graph graph;
	private Algorithm optLayoutAlgorithm2;
	
	public MyClusterGraphBasedReLayoutService(boolean waitForLayout, Graph g) {
		this.currentOptionWaitForLayout = waitForLayout;
		this.graph = g;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		return (int) statusValue;
	}
	
	public void setAlgorithm(Algorithm algorithm, Algorithm optAlgorithm2) {
		this.layoutAlgorithm = algorithm;
		this.optLayoutAlgorithm2 = optAlgorithm2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return statusValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	@Override
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	@Override
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#pleaseStop()
	 */
	@Override
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (checkStop())
			return;
		Graph mainGraph = graph;
		if (mainGraph == null) {
			ErrorMsg.addErrorMessage("Graph may not be null!");
			pleaseStop();
		}
		if (checkStop())
			return;
		HashSet<String> sourceClusterNames = new HashSet<String>();
		status1 = "Process cluster IDs...";
		for (Node n : mainGraph.getNodes()) {
			String clusterId = NodeTools.getClusterID(n, null);
			if (clusterId == null) {
				clusterId = "no cluster";
				NodeTools.setClusterID(n, "no cluster");
			}
			sourceClusterNames.add(clusterId);
		}
		if (checkStop())
			return;
		
		boolean creationNeeded = true;
		Graph testGraph = (Graph) AttributeHelper.getAttributeValue(mainGraph, "cluster", "clustergraph", null, new AdjListGraph(), false);
		if (testGraph != null) {
			creationNeeded = false;
			Collection<String> clusterNames = GraphHelper.getClusters(testGraph.getNodes());
			if (sourceClusterNames.size() == clusterNames.size()) {
				boolean allfound = true;
				for (String cn : clusterNames) {
					if (!sourceClusterNames.contains(cn)) {
						allfound = false;
						System.out.println("CLUSTER NOT FOUND (RE-CREATION NEEDED): " + cn);
						break;
					}
				}
				if (!allfound)
					creationNeeded = true;
			} else
				creationNeeded = true;
		}
		final Graph clusterGraph;
		if (!creationNeeded) {
			status1 = "Using existing overview-graph...";
			clusterGraph = testGraph;
		} else {
			status1 = "Create overview-graph...";
			clusterGraph = GraphHelper.createClusterReferenceGraph(mainGraph, null);
			AttributeHelper.setAttribute(mainGraph, "cluster", "clustergraph", clusterGraph);
			if (clusterGraph == null) {
				pleaseStop();
				MainFrame.showMessageDialog("Error: Could not create overview-graph", "Error");
			}
		}
		if (clusterGraph == null) {
			pleaseStop();
			MainFrame.showMessageDialog("Error: Could not create overview-graph", "Error");
		}
		
		if (checkStop())
			return;
		
		status1 = "Process cluster node size and position...";
		HashMap<String, Vector2d> clusterLocationsBeforeLayout = new HashMap<String, Vector2d>();
		if (clusterGraph != null) {
			GraphHelper.setClusterGraphNodeSizeAndPositionFromReferenceGraph(mainGraph, clusterGraph, this);
			// GraphHelper.printNodeLayout(mainGraph);
			// GraphHelper.printNodeLayout(clusterGraph);
			for (Node clusterNode : clusterGraph.getNodes()) {
				String clusterId = NodeTools.getClusterID(clusterNode, "");
				Point2D position = AttributeHelper.getPosition(clusterNode);
				clusterLocationsBeforeLayout.put(clusterId, new Vector2d(position));
			}
		}
		
		if (checkStop())
			return;
		
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			// add kegg-ed relevant information to edges
			for (Edge e : clusterGraph.getEdges()) {
				String srcKeggId = KeggGmlHelper.getKeggId(e.getSource());
				String tgtKeggId = KeggGmlHelper.getKeggId(e.getTarget());
				KeggGmlHelper.setRelationSourceTargetInformation(e, 0, srcKeggId, tgtKeggId);
				KeggGmlHelper.setRelationSubtypeName(e, 0, "");
				KeggGmlHelper.setRelationTypeInformation(e, 0, RelationType.maplink);
			}
		}
		
		status2 = "Layout overview-graph...";
		layoutAlgorithm.attach(clusterGraph, new Selection());
		layoutAlgorithm.execute();
		while (BackgroundTaskHelper.isTaskWithGivenReferenceRunning(layoutAlgorithm)) {
			status2 = "Wait for layout to finish...";
			try {
				Thread.sleep(50);
			} catch (InterruptedException ie) {
				ErrorMsg.addErrorMessage(ie.getLocalizedMessage());
			}
		}
		layoutAlgorithm.reset();
		
		if (optLayoutAlgorithm2 != null) {
			optLayoutAlgorithm2.attach(clusterGraph, new Selection());
			optLayoutAlgorithm2.execute();
			while (BackgroundTaskHelper.isTaskWithGivenReferenceRunning(optLayoutAlgorithm2)) {
				status2 = "Wait for layout 2 to finish...";
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
					ErrorMsg.addErrorMessage(ie.getLocalizedMessage());
				}
			}
			optLayoutAlgorithm2.reset();
		}
		
		status2 = "Layout finished";
		
		if (!checkStop()) {
			if (currentOptionWaitForLayout) {
				clusterGraph.setName("overview-graph (apply layout then click OK, you may then close this window)");
				clusterGraph.setModified(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						MainFrame.getInstance().showGraph(clusterGraph, null);
					}
				});
				status1 = "You may now manually modify the overview-graph layout!";
				status2 = "Click OK, to continue re-layout of source graph.";
				userBreak = true;
				double oldStatus = statusValue;
				statusValue = 0;
				while (userBreak) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException ie) {
						ErrorMsg.addErrorMessage(ie.getLocalizedMessage());
						userBreak = false;
					}
				}
				clusterGraph.setModified(false);
				statusValue = oldStatus;
				if (!checkStop()) {
					status1 = "Continue processing...";
					status2 = "Apply layout to source graph...";
				}
			}
		}
		if (checkStop())
			return;
		HashMap<String, Vector2d> clusterLocationsAfterLayout = new HashMap<String, Vector2d>();
		for (Node clusterNode : clusterGraph.getNodes()) {
			String clusterId = NodeTools.getClusterID(clusterNode, "");
			Point2D position = AttributeHelper.getPosition(clusterNode);
			clusterLocationsAfterLayout.put(clusterId, new Vector2d(position));
		}
		
		// GraphHelper.printNodeLayout(mainGraph);
		// GraphHelper.printNodeLayout(clusterGraph);
		
		if (checkStop())
			return;
		
		// ArrayList<Graph> clusterGraphsToBeAnalyzed = new ArrayList<Graph>();
		// ArrayList<String> clusterGraphsToBeAnalyzedID = new ArrayList<String>();
		
		status1 = "Apply layout...";
		status2 = "";
		
		boolean error = false;
		
		final HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		for (Node n : mainGraph.getNodes()) {
			String clusterId = NodeTools.getClusterID(n, null);
			if (clusterId != null && clusterId.length() > 0) {
				Vector2d currentPosition = AttributeHelper.getPositionVec2d(n);
				Vector2d clusterBefore = clusterLocationsBeforeLayout.get(clusterId);
				Vector2d clusterAfter = clusterLocationsAfterLayout.get(clusterId);
				if (clusterAfter == null) {
					error = true;
				} else {
					Vector2d newPosition = new Vector2d(
							currentPosition.x + (clusterAfter.x - clusterBefore.x),
							currentPosition.y + (clusterAfter.y - clusterBefore.y)
							);
					nodes2newPositions.put(n, newPosition);
				}
			}
		}
		// Move also edge bends
		final HashMap<CoordinateAttribute, Vector2d> bends2newPositions = new HashMap<CoordinateAttribute, Vector2d>();
		for (Edge e : mainGraph.getEdges()) {
			String clusterId = NodeTools.getClusterID(e, null);
			if (clusterId != null && clusterId.length() > 0) {
				Vector2d clusterBefore = clusterLocationsBeforeLayout.get(clusterId);
				Vector2d clusterAfter = clusterLocationsAfterLayout.get(clusterId);
				if (clusterAfter == null)
					error = true;
				else
					EdgeHelper.moveBends(e, (clusterAfter.x - clusterBefore.x), (clusterAfter.y - clusterBefore.y), bends2newPositions);
			}
		}
		final boolean errorF = error;
		final Graph mainGraphF = mainGraph;
		BackgroundTaskHelper.executeLaterOnSwingTask(50, new Runnable() {
			@Override
			public void run() {
				if (errorF)
					MainFrame.showMessageDialog(
							"<html>" +
									"Some sub-graph positions could not be updated." +
									"Eventually you closed the overview-graph window, before applying the layout to the<br>" +
									"source graph. It is recommended, to first modify the overview-graph layout. Then<br>" +
									"continue the re-layout operation by clicking onto the OK button in the progress panel.<br>" +
									"Then you may close the overview-graph window.", "Error");
				// GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, "Overview-Graph based layout");
				GraphHelper.applyUndoableNodeAndBendPositionUpdate(nodes2newPositions, bends2newPositions, "Overview-Graph based layout");
				
				for (Node n : mainGraphF.getNodes()) {
					String clusterId = NodeTools.getClusterID(n, "no cluster");
					if (clusterId.equals("no cluster"))
						NodeTools.setClusterID(n, "");
				}
				GravistoService.getInstance().runAlgorithm(
						new CenterLayouterAlgorithm(), mainGraphF, new Selection(""),
						null);
			}
		});
		statusValue = 100;
		
	}
	
	/**
	 * @return
	 */
	private boolean checkStop() {
		if (pleaseStop) {
			status1 = "Layout not complete: aborted";
			status2 = "";
			statusValue = 100d;
		}
		return pleaseStop;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	@Override
	public boolean pluginWaitsForUser() {
		return userBreak;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	@Override
	public void pleaseContinueRun() {
		userBreak = false;
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		statusValue = value;
	}
	
	@Override
	public void setCurrentStatusValueFine(double value) {
		statusValue = value;;
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
	 * @see org.BackgroundTaskStatusProviderSupportingExternalCall#setCurrentStatusValueFineAdd(double)
	 */
	@Override
	public void setCurrentStatusValueFineAdd(double smallProgressStep) {
		statusValue += smallProgressStep;
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
