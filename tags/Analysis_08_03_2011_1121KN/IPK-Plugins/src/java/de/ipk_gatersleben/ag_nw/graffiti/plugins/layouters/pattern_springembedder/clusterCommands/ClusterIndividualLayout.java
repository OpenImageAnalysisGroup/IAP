/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.services.RunAlgorithmDialog;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ClusterIndividualLayout extends AbstractAlgorithm {
	boolean currentOptionShowGraphs = false;
	boolean currentOptionWaitForLayout = false;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Layout Pathway-Subgraphs";
		else
			return "Layout Subgraphs";
	}
	
	@Override
	public String getDescription() {
		return "Cluster Layout Parameters";
	}
	
	@Override
	public Parameter[] getParameters() {
		BooleanParameter showClusterGraphs =
							new BooleanParameter(currentOptionShowGraphs,
												"Show Subgraphs", "If set to true, the extracted cluster graphs are shown in the editor");
		BooleanParameter waitForClusterLayout =
							new BooleanParameter(currentOptionWaitForLayout,
												"Wait for Layout", "If set to true, the layout of the cluster graphs is applied after the user confirms the action");
		
		return new Parameter[] { showClusterGraphs, waitForClusterLayout };
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false; // recursive run should be avoided, thus it is not labeled as a layout algorithm
	}
	
	@Override
	public void reset() {
		currentOptionShowGraphs = false;
		currentOptionWaitForLayout = false;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		currentOptionShowGraphs = ((BooleanParameter) params[0]).getBoolean().booleanValue();
		currentOptionWaitForLayout = ((BooleanParameter) params[1]).getBoolean().booleanValue();
		if (currentOptionWaitForLayout)
			currentOptionShowGraphs = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.extension.Extension#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Cluster";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null)
			throw new PreconditionException("No graph available!");
		Set<String> clusters = new TreeSet<String>();
		for (Node n : graph.getNodes()) {
			String clusterId = NodeTools.getClusterID(n, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
		}
		if (clusters.size() <= 0)
			throw new PreconditionException(
								"No cluster information available for this graph!");
		
		// Graph clusterBackgroundGraph = (Graph) AttributeHelper.getAttributeValue(
		// graph, "cluster", "clustergraph", null, new AdjListGraph());
		// boolean clusterGraphAvailable = clusterBackgroundGraph != null;
		// if (clusterGraphAvailable) {
		// for (Iterator<?> it = clusterBackgroundGraph.getNodesIterator(); it.hasNext();) {
		// Node clusterNode = (Node) it.next();
		// String clusterId = NodeTools.getClusterID(clusterNode, "");
		// if (clusterId.equals("")) {
		// throw new PreconditionException("Cluster-Graph-Node with no Cluster ID found!");
		// }
		// }
		// } else {
		// throw new PreconditionException("Error: No background cluster-graph available");
		// }
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		String cluster = "Cluster";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster = "Pathway";
		
		RunAlgorithmDialog rad = new RunAlgorithmDialog("Select " + cluster + " Layout",
							graph, selection, true, true);
		rad.setAlwaysOnTop(true);
		rad.setVisible(true);
		rad.requestFocusInWindow();
		MyActionListener al = new MyActionListener();
		final Timer t = new Timer(100, al);
		al.setAlgorithmDialog(rad);
		al.setOptions(getName(), t, currentOptionShowGraphs, currentOptionWaitForLayout);
		
		t.setRepeats(true);
		t.start();
	}
}

class MyActionListener implements ActionListener {
	RunAlgorithmDialog rad = null;
	
	Timer tref = null;
	
	String name;
	
	private boolean currentOptionShowGraphs;
	private boolean currentOptionWaitForLayout;
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (!rad.isVisible() && rad.getAlgorithm() != null) {
			tref.stop();
			MyLayoutService mcs = new MyLayoutService();
			mcs.currentOptionShowGraphs = currentOptionShowGraphs;
			mcs.currentOptionWaitForLayout = currentOptionWaitForLayout;
			mcs.setAlgorithm(rad.getAlgorithm());
			BackgroundTaskHelper bth = new BackgroundTaskHelper(mcs, mcs, name,
								name, true, false);
			bth.startWork(this);
		} else
			rad.setAlwaysOnTop(true);
	}
	
	public void setOptions(String name, Timer t,
						boolean currentOptionShowGraphs, boolean currentOptionWaitForLayout) {
		this.name = name;
		tref = t;
		this.currentOptionShowGraphs = currentOptionShowGraphs;
		this.currentOptionWaitForLayout = currentOptionWaitForLayout;
	}
	
	public void setAlgorithmDialog(RunAlgorithmDialog rad) {
		this.rad = rad;
	}
}

class MyLayoutService implements BackgroundTaskStatusProvider, Runnable {
	boolean userBreak = false;
	int statusInt = -1;
	boolean pleaseStop = false;
	public boolean currentOptionWaitForLayout = false;
	public boolean currentOptionShowGraphs;
	
	String status1, status2;
	
	private Algorithm layoutAlgorithm;
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	public int getCurrentStatusValue() {
		return statusInt;
	}
	
	public void setAlgorithm(Algorithm algorithm) {
		this.layoutAlgorithm = algorithm;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	public double getCurrentStatusValueFine() {
		return statusInt;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#pleaseStop()
	 */
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (checkStop())
			return;
		Graph mainGraph = null;
		try {
			mainGraph = GravistoService.getInstance().getMainFrame()
								.getActiveSession().getGraph();
		} catch (NullPointerException npe) {
			ErrorMsg.addErrorMessage(npe.getLocalizedMessage());
		}
		TreeSet<String> clusters = new TreeSet<String>();
		HashMap<Long, Point2D> newCoordinates = new HashMap<Long, Point2D>();
		HashMap<Long, Vector2d> newSizes = new HashMap<Long, Vector2d>();
		// HashMap<String, Point2D> centerForClusterGraph = new HashMap<String, Point2D>();
		if (mainGraph == null) {
			MainFrame.showMessageDialog("Error: Working-graph could not be determined", "Error");
		} else {
			status1 = "Set Node IDs for reference purposes...";
			int refID = 0;
			for (Node n : mainGraph.getNodes())
				n.setID(refID++);
			String cluster4 = "cluster";
			if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
				cluster4 = "pathway";
			status1 = "Read " + cluster4 + " information";
			for (Iterator<?> it = mainGraph.getNodesIterator(); it.hasNext();) {
				if (checkStop())
					return;
				Node n = (Node) it.next();
				String clusterId = NodeTools.getClusterID(n, "");
				if (!clusterId.equals(""))
					clusters.add(clusterId);
			}
			
			int cnt = 0;
			
			ArrayList<Graph> clusterGraphsToBeAnalyzed = new ArrayList<Graph>();
			
			for (String clusterID : clusters) {
				if (checkStop())
					break;
				status1 = "Apply " + layoutAlgorithm.getName()
									+ " to Subgraph (" + cluster4 + " " + clusterID + ")...";
				
				status2 = "Extract Subgraph...";
				final Graph clusterSubGraph = GraphHelper.getClusterSubGraph(mainGraph, clusterID);
				
				statusInt = (int) ((cnt + 0.5f) * 100f / clusters.size());
				status2 = "Do Layout...";
				layoutAlgorithm.attach(clusterSubGraph, new Selection());
				clusterSubGraph.setName(cluster4 + " " + clusterID);
				layoutAlgorithm.execute();
				
				while (BackgroundTaskHelper.isTaskWithGivenReferenceRunning(layoutAlgorithm)) {
					status2 = "Wait for layout to finish...";
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
						ErrorMsg.addErrorMessage(ie.getLocalizedMessage());
					}
				}
				layoutAlgorithm.reset();
				
				status2 = "Layout finished";
				
				clusterGraphsToBeAnalyzed.add(clusterSubGraph);
				statusInt = (int) (++cnt * 100f / clusters.size());
			}
			
			if (!checkStop()) {
				if (currentOptionShowGraphs)
					for (final Graph clusterSubGraph : clusterGraphsToBeAnalyzed)
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								MainFrame.getInstance().showGraph(clusterSubGraph, null);
							}
						});
				
				if (currentOptionWaitForLayout) {
					status1 = "It is now possible to modify the Layout";
					if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
						status2 = "of the Pathway-Subgraphs.";
					else
						status2 = "of the Cluster-Subgraphs.";
					userBreak = true;
					while (userBreak) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException ie) {
							ErrorMsg.addErrorMessage(ie.getLocalizedMessage());
							userBreak = false;
						}
					}
				}
				
				for (Graph clusterSubGraph : clusterGraphsToBeAnalyzed) {
					for (Node n : clusterSubGraph.getNodes()) {
						newCoordinates.put(new Long(n.getID()), AttributeHelper.getPosition(n));
						newSizes.put(new Long(n.getID()), AttributeHelper.getSize(n));
					}
				}
				
				if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
					status1 = "Pathway-Layout finished";
				else
					status1 = "Cluster-Layout finished";
				status2 = "Set node coordinates and size...";
				
				mainGraph.getListenerManager().transactionStarted(this);
				statusInt = -1;
				for (Node n : mainGraph.getNodes()) {
					Point2D newPosition = newCoordinates.get(new Long(n.getID()));
					Vector2d newSize = newSizes.get(new Long(n.getID()));
					String clusterNumber = NodeTools.getClusterID(n, "");
					if (clusterNumber.equals("")) {
						// not a cluster node, ignore this node for layout
					} else {
						if (newPosition != null) {
							newPosition = new Point2D.Double(
												newPosition.getX(),
												newPosition.getY());
							// System.out.println(" ClusterTarget: " + targetPointForCluster + ", Center of Subgraph: " + centerOfSubGraph + ", NewPos: " +
							// newPosition);
							AttributeHelper.setPosition(n, newPosition);
							if (newSize != null)
								AttributeHelper.setSize(n, newSize);
						} else {
							AttributeHelper.setBorderWidth(n, 5);
							ErrorMsg.addErrorMessage("No node position stored for nodeID " + n.getID());
						}
					}
				}
				mainGraph.getListenerManager().transactionFinished(this);
				
			} else {
				status1 = "Layouter aborted...";
				status2 = "";
			}
			statusInt = 100;
		}
	}
	
	/**
	 * @return
	 */
	private boolean checkStop() {
		if (pleaseStop) {
			status1 = "Layout not complete: aborted";
			status2 = "";
			statusInt = 100;
		}
		return pleaseStop;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	public boolean pluginWaitsForUser() {
		return userBreak;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	public void pleaseContinueRun() {
		userBreak = false;
	}
	
	public void setCurrentStatusValue(int value) {
		statusInt = value;
	}
}
