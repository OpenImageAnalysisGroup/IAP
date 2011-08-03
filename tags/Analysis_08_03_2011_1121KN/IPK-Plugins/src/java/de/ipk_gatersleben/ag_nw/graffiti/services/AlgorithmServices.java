/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 30.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.HelperClass;
import org.StringManipulationTools;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class AlgorithmServices implements HelperClass {
	
	/**
	 * @param sortedNodes
	 * @param object
	 * @param runnable
	 */
	public static void doCircularEdgeCrossingsMinimization(
						Object referenceObject, ArrayList<Node> nodes, Runnable threadUnsafePostTask) {
		MyEdgeCrossingReduction mecr = new AlgorithmServices().new MyEdgeCrossingReduction(nodes, threadUnsafePostTask);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(mecr, mecr, "Reduce Edge Crossings", "Reduce Edge Crossings", true, false);
		bth.startWork(referenceObject);
	}
	
	public static int getNumberOfCircularEdgeCrossings(Collection<Node> nodes) {
		Stack<Node> s = new Stack<Node>();
		s.addAll(nodes);
		return new AlgorithmServices().new MyEdgeCrossingReduction(null, null).calculateEdgeCrossings(s);
	}
	
	class MyEdgeCrossingReduction implements BackgroundTaskStatusProvider, Runnable {
		Runnable threadUnsafePostTask;
		double statusProgress = -1;
		String status1, status2;
		boolean pleaseStop = false;
		int nodeCount = -1;
		ArrayList<Node> nodes;
		
		ArrayList<Node> bestResult = new ArrayList<Node>();
		int edgeCrossingsForBestResult = Integer.MAX_VALUE;
		int edgeCrossingsForInitalSet;
		double currIteration;
		double maxIteration;
		private long lastUpdate = 0;
		
		HashMap<Edge, Integer> multiplicators = new HashMap<Edge, Integer>();
		
		public MyEdgeCrossingReduction(ArrayList<Node> nodes, Runnable threadUnsafePostTask) {
			this.threadUnsafePostTask = threadUnsafePostTask;
			this.nodes = nodes;
		}
		
		/*
		 * (non-Javadoc)
		 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusValue()
		 */
		public int getCurrentStatusValue() {
			return (int) Math.round(statusProgress);
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
			// work
			HashMap<Integer, Node> nodeAndCirclePosition = new HashMap<Integer, Node>();
			multiplicators.clear();
			nodeCount = nodes.size();
			for (int i = 0; i < nodes.size(); i++)
				nodeAndCirclePosition.put(new Integer(i), nodes.get(i));
			
			Stack<Node> nodeStack = new Stack<Node>();
			Stack<Node> currentResult = new Stack<Node>();
			nodeStack.addAll(nodes);
			status1 = "Calculate inital crossings...";
			edgeCrossingsForInitalSet = calculateEdgeCrossings(nodeStack);
			status1 = "Iterate possibilities (" + nodeCount + " nodes)...";
			maxIteration = fak(nodeStack.size()) / 2;
			doIteration(currentResult, nodeStack);
			statusProgress = 100;
			if (!pleaseStop)
				status1 = "Search complete: minimum is calculated.";
			else
				status1 = "Search aborted: use best known result.";
			nodes.clear();
			nodes.addAll(bestResult);
			// post task
			if (threadUnsafePostTask != null)
				SwingUtilities.invokeLater(threadUnsafePostTask);
		}
		
		/**
		 * @param i
		 * @return
		 */
		private double fak(int f) {
			double res = 1;
			for (int i = 1; i <= f; i++)
				res = res * i;
			return res;
		}
		
		private void doIteration(Stack<Node> currentResult, Stack<Node> nodeStack) {
			if (pleaseStop)
				return;
			currIteration++;
			if (nodeStack.size() <= 0) {
				int edgeCrossings = calculateEdgeCrossings(currentResult);
				if (edgeCrossings < edgeCrossingsForBestResult) {
					edgeCrossingsForBestResult = edgeCrossings;
					bestResult.clear();
					bestResult.addAll(currentResult);
					statusProgress = (100.0 * currIteration) / maxIteration;
					setStatus1();
					status2 = "Edge crossings: " + edgeCrossingsForInitalSet + " -> " + edgeCrossingsForBestResult + " (" + nodeCount + " Nodes)";
				}
			} else {
				for (int i = 0; i < nodeStack.size(); i++) {
					if (System.currentTimeMillis() - lastUpdate > 200) {
						statusProgress = (100.0 * currIteration) / maxIteration;
						setStatus1();
						lastUpdate = System.currentTimeMillis();
					}
					if (currentResult.size() <= 0 && i > 0)
						continue;
					Node n = nodeStack.get(i);
					currentResult.push(n);
					nodeStack.remove(i);
					doIteration(currentResult, nodeStack);
					currentResult.pop();
					nodeStack.insertElementAt(n, i);
				}
			}
		}
		
		private void setStatus1() {
			String s = new Double(currIteration).toString();
			s = StringManipulationTools.stringReplace(s, ".0", "");
			status1 = "Iterate possibilities (" + s + "/" + maxIteration + ")";
		}
		
		/**
		 * @param currentResult
		 * @return
		 */
		int calculateEdgeCrossings(Stack<Node> currentResult) {
			int result = 0;
			for (int i = 0; i < currentResult.size(); i++) {
				if (pleaseStop)
					break;
				for (int i2 = i; i2 < currentResult.size(); i2++) {
					if (i != i2) {
						Node a = currentResult.get(i);
						Collection<Edge> eA = a.getAllOutEdges();
						Node b = currentResult.get(i2);
						Collection<Edge> eB = b.getAllOutEdges();
						for (Iterator<Edge> it = eA.iterator(); it.hasNext();) {
							Edge edgA = it.next();
							for (Iterator<Edge> it2 = eB.iterator(); it2.hasNext();) {
								Edge edgB = it2.next();
								if (cross(edgA, edgB, currentResult)) {
									Integer mA = multiplicators.get(edgA);
									if (mA == null) {
										Integer edgeDoublingA = (Integer) AttributeHelper.getAttributeValue(
															edgA, "cluster", "edgecount", new Integer(1), null);
										multiplicators.put(edgA, edgeDoublingA);
										mA = edgeDoublingA;
									}
									Integer mB = multiplicators.get(edgB);
									if (mB == null) {
										Integer edgeDoublingB = (Integer) AttributeHelper.getAttributeValue(
															edgB, "cluster", "edgecount", new Integer(1), null);
										multiplicators.put(edgB, edgeDoublingB);
										mB = edgeDoublingB;
									}
									
									result += mA.intValue() * mB.intValue();
								}
							}
						}
					}
				}
			}
			return result;
		}
		
		private boolean cross(Edge edgA, Edge edgB, Stack<Node> currentResult) {
			int a0 = currentResult.indexOf(edgA.getSource());
			int a1 = currentResult.indexOf(edgA.getTarget());
			int b0 = currentResult.indexOf(edgB.getSource());
			int b1 = currentResult.indexOf(edgB.getTarget());
			if (a0 > a1) {
				int t = a0;
				a0 = a1;
				a1 = t;
			}
			if (b0 > b1) {
				int t = b0;
				b0 = b1;
				b1 = t;
			}
			
			if (b0 < a0) {
				int t0 = b0;
				int t1 = b1;
				b0 = a0;
				b1 = a1;
				a0 = t0;
				a1 = t1;
			}
			// System.out.print("Kreuz-Test ["+a0+"->"+a1+" mit "+b0+"->"+b1+"]: ");
			boolean result;
			boolean noNodeTheSame = a0 != b0 && a0 != b1 && a1 != b0 && a1 != b1;
			if (noNodeTheSame && a1 > b0 && a1 < b1)
				result = true;
			else
				result = false;
			// System.out.println(result);
			return result;
		}
		
		/*
		 * (non-Javadoc)
		 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
		 */
		public double getCurrentStatusValueFine() {
			return statusProgress;
		}
		
		/*
		 * (non-Javadoc)
		 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
		 */
		public boolean pluginWaitsForUser() {
			return false;
		}
		
		/*
		 * (non-Javadoc)
		 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
		 */
		public void pleaseContinueRun() {
			// empty
		}
		
		public void setCurrentStatusValue(int value) {
			statusProgress = value;
		}
	}
	
	/**
	 * This method calls a user defined layout algorithm for the given graph.
	 * 
	 * @param clusterReferenceGraph
	 * @param selection
	 */
	public static void selectAndRunLayoutAlgorithm(Graph clusterReferenceGraph, Selection selection, String commandTitle,
						boolean executeMoveToTopAfterwards) {
		RunAlgorithmDialog rad = new RunAlgorithmDialog(commandTitle, clusterReferenceGraph, selection, false, executeMoveToTopAfterwards);
		rad.setModal(true);
		rad.setAlwaysOnTop(true);
		rad.setVisible(true);
	}
}
