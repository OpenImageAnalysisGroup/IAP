/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 15.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.hamming_distance;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugins.ios.importers.gml.GMLReader;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class HammingCalculator implements Runnable,
					BackgroundTaskStatusProvider {
	
	private Collection<Graph> listOfGraphs;
	private int[][] hammingDistances;
	private ArrayList<File> listOfGraphFileNames;
	private boolean consNodes;
	private boolean consEdges;
	private boolean consEdgeLabels;
	private int nodesDistance;
	private boolean computeOrder;
	private int edgesDistance;
	private int numberOfGraphs;
	private int[] newOrder;
	private double progress;
	private String message1 = "Please wait...";
	private String message2 = "";;
	private boolean stopWanted;
	private int validGraphIndex;
	private String outputDir;
	
	private static Integer fileLoader = new Integer(0); // dummy variable for synchronized file loading (only one file-load at a time)
	
	private static LinkedList<WorkTask> workQueue = new LinkedList<WorkTask>();
	
	public HammingCalculator(Collection<Graph> listOfGraphs, ArrayList<File> listOfGraphFileNames,
						boolean consNodes,
						boolean consEdges, boolean consEdgeLabels,
						int nodesDistance, int edgesDistance,
						boolean computeOrder, int validGraphIndex,
						String outputDir) {
		this.listOfGraphs = listOfGraphs;
		this.listOfGraphFileNames = listOfGraphFileNames;
		this.consNodes = consNodes;
		this.consEdges = consEdges;
		this.consEdgeLabels = consEdgeLabels;
		this.nodesDistance = nodesDistance;
		this.edgesDistance = edgesDistance;
		this.computeOrder = computeOrder;
		this.validGraphIndex = validGraphIndex;
		this.outputDir = outputDir;
		
		if (listOfGraphs != null)
			numberOfGraphs = listOfGraphs.size();
		else
			numberOfGraphs = listOfGraphFileNames.size();
		
		hammingDistances = new int[numberOfGraphs][numberOfGraphs];
		this.newOrder = new int[numberOfGraphs];
	}
	
	public void run() {
		/* Computes the hamming distances between all graphs */
		HashMap<Integer, String> graphNames = computeHammingDistances(hammingDistances, listOfGraphs, listOfGraphFileNames, validGraphIndex);
		
		/* Prints the hamming distances between all graphs */
		printDistanceMatrix(hammingDistances, graphNames, numberOfGraphs, validGraphIndex, outputDir);
		
		/* Computes the MIN SUM order depending on the hamming distances */
		if (computeOrder) {
			computeMinSumOrdering(newOrder, hammingDistances, numberOfGraphs, validGraphIndex);
		}
	}
	
	public int getCurrentStatusValue() {
		return (int) getCurrentStatusValueFine();
	}
	
	public void setCurrentStatusValue(int value) {
		
	}
	
	public double getCurrentStatusValueFine() {
		return progress;
	}
	
	public String getCurrentStatusMessage1() {
		return message1;
	}
	
	public String getCurrentStatusMessage2() {
		return message2;
	}
	
	public void pleaseStop() {
		stopWanted = true;
	}
	
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	public void pleaseContinueRun() {
	}
	
	/**
	 * Computes the hamming distances between graphs
	 * 
	 * @param hammingDistances
	 *           DOCUMENT ME!
	 * @param validGraphIndex2
	 * @return A map which contains the graph names for each index
	 */
	private HashMap<Integer, String> computeHammingDistances(int[][] hammingDistances, Collection<Graph> listOfGraphs, Collection<File> listOfGraphFileNames,
						int validGraphIndex2) {
		/* The iterators for the graph list */
		int i = 0;
		int j = 0;
		
		message1 = "Compute Hamming-Distances...";
		message2 = "";
		
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		
		WorkSettings ws = new WorkSettings(consNodes, nodesDistance, consEdges, consEdgeLabels, edgesDistance, validGraphIndex);
		
		if (listOfGraphFileNames == null) {
			/*
			 * The while loop computes the pairwise hamming distances. The
			 * distances are stored in the matrix <code>hammingDistance</code>.
			 */
			for (Graph graph1 : listOfGraphs) {
				if (stopWanted)
					break;
				HashSet<String> nodesIn1 = new HashSet<String>();
				for (Node n : graph1.getNodes())
					nodesIn1.add(AttributeHelper.getLabel(n, null));
				result.put(new Integer(i), graph1.getName());
				message2 = "Between " + graph1.getName() + " and remaining graphs (" + (i + 1) + "/" + numberOfGraphs + ")";
				for (Graph graph2 : listOfGraphs) {
					if (stopWanted)
						break;
					HashSet<String> nodesIn2 = new HashSet<String>();
					for (Node n : graph2.getNodes())
						nodesIn2.add(AttributeHelper.getLabel(n, null));
					hammingDistances[i][j] = compareTwoGraphs(graph1, graph2, nodesIn1, nodesIn2, ws);
					j++;
				}
				/*
				 * All graphs given by iterator <code>itJ</code> are considered,
				 * take the next graph given by iterator <code>itI</code>.
				 */
				i++;
				System.out.println("i=" + i);
				j = 0;
			}
			if (stopWanted)
				message1 = "User abort";
			else
				message1 = "Finished";
		} else {
			
			/*
			 * The while loop computes the pairwise hamming distances. The
			 * distances are stored in the matrix <code>hammingDistance</code>.
			 */
			double step = 1d / numberOfGraphs;
			for (File graph1fn : listOfGraphFileNames) {
				if (stopWanted)
					break;
				
				if (validGraphIndex != 0) {
					if (i + 1 != Math.abs(validGraphIndex)) {
						i++;
						continue;
					}
				}
				
				Graph graph1 = null;
				if (MainFrame.getInstance() != null) {
					try {
						graph1 = MainFrame.getInstance().getGraph(graph1fn);
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				} else
					try {
						graph1 = new GMLReader().read(new BufferedInputStream(new FileInputStream(graph1fn)));
						graph1.setName(graph1fn.getAbsolutePath());
					} catch (FileNotFoundException e1) {
						ErrorMsg.addErrorMessage(e1);
					} catch (IOException e1) {
						ErrorMsg.addErrorMessage(e1);
					}
				
				if (graph1 == null) {
					graph1 = new AdjListGraph();
					graph1.setName("Could not load " + graph1fn.getAbsolutePath());
				}
				result.put(new Integer(i), graph1.getName());
				HashSet<String> nodesIn1 = new HashSet<String>();
				for (Node n : graph1.getNodes())
					nodesIn1.add(AttributeHelper.getLabel(n, null));
				message1 = "Enqueue work-tasks - " + graph1.getName() + " (" + (i + 1) + "/" + numberOfGraphs + ")";
				ArrayList<WorkTask> runningThreads = new ArrayList<WorkTask>();
				for (File graph2fn : listOfGraphFileNames) {
					if (stopWanted)
						break;
					WorkTask t = new WorkTask(nodesIn1, graph2fn, i, j, message2, hammingDistances, graph1, ws, runningThreads);
					enqueueWorkTask(t);
					j = getTaskQueueSize();
					progress = 100d * ((double) i / numberOfGraphs + (double) j / numberOfGraphs * step);
				}
				int runSize;
				
				message1 = "Wait for work-tasks to be finished - " + graph1.getName() + " (" + (i + 1) + "/" + numberOfGraphs + ")";
				do {
					if (runningThreads.size() < 2) {
						final WorkTask wwtt = getNextWorkTask();
						if (wwtt != null) {
							Thread wt = new Thread(new Runnable() {
								public void run() {
									HammingCalculator.threadComputing(wwtt);
								}
							}, "Hamming Distance Calculation Thread") {};
							synchronized (runningThreads) {
								runningThreads.add(wwtt);
							}
							wt.start();
						}
					}
					message2 = "Queue-Size: " + getTaskQueueSize() + ", Active Threads: " + runningThreads.size();
					synchronized (runningThreads) {
						progress = 100d * ((double) i / numberOfGraphs + step
											* ((double) (numberOfGraphs - (getTaskQueueSize() + runningThreads.size())) / numberOfGraphs));
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
					synchronized (runningThreads) {
						runSize = runningThreads.size();
					}
				} while (getTaskQueueSize() > 0 || runSize > 0);
				/*
				 * All graphs given by iterator <code>itJ</code> are considered,
				 * take the next graph given by iterator <code>itI</code>.
				 */
				i++;
				progress = 100d * ((double) i / numberOfGraphs);
				System.out.println("i=" + i);
				j = 0;
			}
			if (stopWanted)
				message1 = "User abort";
			else
				message1 = "Finished";
			progress = 100;
		}
		return result;
	}
	
	private int getTaskQueueSize() {
		int result;
		synchronized (workQueue) {
			result = workQueue.size();
		}
		return result;
	}
	
	private void enqueueWorkTask(WorkTask t) {
		synchronized (workQueue) {
			workQueue.add(t);
		}
	}
	
	private static void threadComputing(WorkTask t) {
		if (t == null)
			return;
		Graph graph2 = null;
		synchronized (fileLoader) {
			
			if (MainFrame.getInstance() != null) {
				try {
					graph2 = MainFrame.getInstance().getGraph(t.graph2fn);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			} else
				try {
					graph2 = new GMLReader().read(new BufferedInputStream(new FileInputStream(t.graph2fn)));
					graph2.setName(t.graph2fn.getAbsolutePath());
				} catch (FileNotFoundException e1) {
					ErrorMsg.addErrorMessage(e1);
				} catch (IOException e1) {
					ErrorMsg.addErrorMessage(e1);
				}
			
			if (graph2 == null) {
				graph2 = new AdjListGraph();
				graph2.setName("Could not load " + t.graph2fn.getAbsolutePath());
			}
		}
		HashSet<String> nodesIn2 = new HashSet<String>();
		for (Node n : graph2.getNodes())
			nodesIn2.add(AttributeHelper.getLabel(n, null));
		t.hammingDistances[t.i][t.j] = compareTwoGraphs(t.graph1, graph2, t.nodesIn1, nodesIn2, t.ws);
		synchronized (t.runningThreads) {
			t.runningThreads.remove(t);
		}
	}
	
	private static WorkTask getNextWorkTask() {
		WorkTask result;
		synchronized (workQueue) {
			if (workQueue.size() > 0)
				result = workQueue.removeFirst();
			else
				result = null;
		}
		return result;
	}
	
	/**
	 * Prints the distance matrix
	 * 
	 * @param hammingDistances
	 *           DOCUMENT ME!
	 * @param graphNames
	 * @param validGraphIndex2
	 */
	private void printDistanceMatrix(int[][] hammingDistances, HashMap<Integer, String> graphNames, int numberOfGraphs, int validGraphIndex2, String outputDir) {
		/* Print graph names */
		int i = 0;
		if (validGraphIndex2 == 0) {
			/* Print matrix */
			for (i = 0; i < numberOfGraphs; i++) {
				System.err.print("   " + i);
			}
			
			System.err.println("");
			
			for (i = 0; i < numberOfGraphs; i++) {
				System.err.print(i + "  ");
				
				for (int j = 0; j < numberOfGraphs; j++) {
					System.err.print(hammingDistances[i][j] + "   ");
				}
				
				System.err.println("");
			}
		}
		// //////////////////////////////////////////////////////////////////////////////////////////////
		
		StringBuffer result = new StringBuffer();
		String home;
		if (outputDir == null && !(new File(outputDir).isDirectory()))
			home = ReleaseInfo.getAppFolder();
		else
			home = outputDir;
		String sep = System.getProperty("file.separator");
		if (Math.abs(validGraphIndex2) <= 1) {
			result.append("Species = " + numberOfGraphs + "\n");
			result.append("Width = 800\n");
			result.append("Height = 800\n");
			result.append("//\n");
			result.append("\n");
			result.append("# Distance Matrix\n");
		}
		for (i = 0; i < numberOfGraphs; i++) {
			if (validGraphIndex != 0) {
				if (i + 1 != Math.abs(validGraphIndex))
					continue;
			}
			String id = graphNames.get(new Integer(i));
			id = id.substring(id.lastIndexOf(sep) + sep.length());
			id = id.substring(0, id.lastIndexOf("."));
			result.append(id + "  ");
			
			for (int j = 0; j <= i; j++) {
				result.append(hammingDistances[i][j] + (j < i ? "   " : ""));
			}
			
			result.append(";\n");
		}
		new TextFile();
		try {
			TextFile.write(home + sep + "hamming_" + getIdx(validGraphIndex) + ".txt", result.toString());
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private String getIdx(int validGraphIndex2) {
		String idx = Math.abs(validGraphIndex2) + "";
		while (idx.length() < 4)
			idx = "0" + idx;
		return idx + "_err_" + ErrorMsg.getErrorMsgCount();
	}
	
	/**
	 * Computes a MIN SUM ordering of the graphs Note: This is a straight
	 * forward implementation and therefore probably quite slow
	 * 
	 * @param newOrder
	 *           DOCUMENT ME!
	 * @param hammingDistances
	 *           DOCUMENT ME!
	 * @param validGraphIndex2
	 */
	private void computeMinSumOrdering(int[] newOrder, int[][] hammingDistances, int numberOfGraphs, int validGraphIndex2) {
		if (validGraphIndex != 0) {
			ErrorMsg.addErrorMessage("Graph-Index need to be set to 0 (all graphs), otherwise MinSumOrder is not calculated!");
			return;
		}
		
		message1 = "Compute MinSumOrder";
		message2 = "Please Wait...";
		progress = -1;
		int minSum;
		int currentSum;
		boolean validCombination;
		int[] currentCombination;
		
		currentCombination = new int[numberOfGraphs];
		
		/* Initialize <code>minSum</code> */
		for (int i = 0; i < numberOfGraphs; i++) {
			currentCombination[i] = i;
		}
		
		minSum =
							computeSum(currentCombination, hammingDistances, numberOfGraphs);
		
		/* Initial combination of the graphs */
		for (int i = 0; i < numberOfGraphs; i++) {
			currentCombination[i] = 0;
		}
		
		while (currentCombination[0] < numberOfGraphs) {
			/* Checks if the current combination is a valid permutation */
			validCombination =
								isValid(true, currentCombination, (numberOfGraphs - 1),
													numberOfGraphs);
			
			if (validCombination) {
				currentSum =
									computeSum(currentCombination, hammingDistances,
														numberOfGraphs);
				
				// System.err.println("Valid permutation");
				// for (int j = 0; j < numberOfGraphs; j++) {
				// System.err.print(currentCombination[j] + " ");
				// }
				// System.err.println("currentSum " + currentSum);
				// System.err.println("");
				if (currentSum < minSum) {
					minSum = currentSum;
					
					for (int i = 0; i < numberOfGraphs; i++) {
						newOrder[i] = currentCombination[i];
					}
				}
			}
			
			nextCombination(currentCombination, (numberOfGraphs - 1),
								numberOfGraphs);
		}
		
		System.err.println("");
		for (int i = 0; i < numberOfGraphs; i++) {
			System.err.println(newOrder[i]);
		}
	}
	
	/**
	 * Computes the sum of the current permutation
	 * 
	 * @param permutation
	 *           DOCUMENT ME!
	 * @param hammingDistances
	 *           DOCUMENT ME!
	 * @param numberOfGraphs
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private int computeSum(int[] permutation, int[][] hammingDistances,
						int numberOfGraphs) {
		int sum = 0;
		
		for (int i = 0; i < (numberOfGraphs - 1); i++) {
			sum += hammingDistances[permutation[i]][permutation[i + 1]];
		}
		
		return sum;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param combination
	 *           DOCUMENT ME!
	 * @param position
	 *           DOCUMENT ME!
	 * @param size
	 *           DOCUMENT ME!
	 */
	private void nextCombination(int[] combination, int position, int size) {
		combination[position] += 1;
		
		if (combination[position] >= size) {
			if (position == 0) {
				return;
			}
			combination[position] = 0;
			nextCombination(combination, (position - 1), size);
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param valid
	 *           DOCUMENT ME!
	 * @param combination
	 *           DOCUMENT ME!
	 * @param position
	 *           DOCUMENT ME!
	 * @param size
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private boolean isValid(boolean valid, int[] combination, int position,
						int size) {
		if (position < 0) {
			return valid;
		}
		
		for (int i = position - 1; i >= 0; i--) {
			if (combination[position] == combination[i]) {
				return false;
			}
		}
		
		return isValid(valid, combination, (position - 1), size);
	}
	
	/**
	 * Computes the hamming distance between two given graphs
	 * 
	 * @param graph1
	 *           The first graph
	 * @param graph1
	 *           The second graph
	 * @return the hamming distance
	 */
	public static int compareTwoGraphs(Graph graph1, Graph graph2, HashSet<String> nodesGraph1, HashSet<String> nodesGraph2, WorkSettings t) {
		
		if (t.validGraphIndex < 0) {
			return (int) (Math.random() * 100d);
		}
		
		int hammingDistance = 0;
		
		/* Compare nodes of the two graphs */
		if (t.consNodes == true) {
			
			HashSet<String> nodesInGraph1and2 = new HashSet<String>();
			nodesInGraph1and2.addAll(nodesGraph1);
			nodesInGraph1and2.addAll(nodesGraph2);
			
			hammingDistance += t.nodesDistance * (nodesInGraph1and2.size() - nodesGraph1.size());
			hammingDistance += t.nodesDistance * (nodesInGraph1and2.size() - nodesGraph2.size());
			/*
			 * Hamming distance between nodes
			 * <code>graph1</code> -> <code>graph2</code>
			 */
			/*
			 * for (Iterator itNodes = graph1.getNodesIterator();
			 * itNodes.hasNext();) {
			 * Node n1 = (Node) itNodes.next();
			 * if (existsNode(n1, graph2) == false) {
			 * hammingDistance += nodesDistance;
			 * }
			 * }
			 */
			/*
			 * Hamming distance between nodes
			 * <code>graph2</code> -> <code>graph1</code>
			 */
			/*
			 * for (Iterator itNodes = graph2.getNodesIterator();
			 * itNodes.hasNext();) {
			 * Node n2 = (Node) itNodes.next();
			 * if (existsNode(n2, graph1) == false) {
			 * hammingDistance += nodesDistance;
			 * }
			 * }
			 */
		}
		/* Compare edges of the two graphs */
		if (t.consEdges == true) {
			/*
			 * Hamming distance between edges
			 * <code>graph1</code> -> <code>graph2</code>
			 */
			for (Iterator<?> itEdges = graph1.getEdgesIterator(); itEdges.hasNext();) {
				Edge e1 = (Edge) itEdges.next();
				
				if (existsEdge(e1, graph2, t) == false) {
					hammingDistance += t.edgesDistance;
				}
			}
			
			/*
			 * Hamming distance between edges
			 * <code>graph2</code> -> <code>graph1</code>
			 */
			for (Iterator<?> itEdges = graph2.getEdgesIterator(); itEdges.hasNext();) {
				Edge e2 = (Edge) itEdges.next();
				
				if (existsEdge(e2, graph1, t) == false) {
					hammingDistance += t.edgesDistance;
				}
			}
		}
		
		// System.err.println("The two graphs have the hamming distance "
		// + hammingDistance);
		return hammingDistance;
	}
	
	/**
	 * Checks if a given edge has a corresponding edge in the graph <code>graph</code>. Uses the node labels (source and target node of
	 * the edge) to find the corresponding edge in the graph.
	 * 
	 * @param edge
	 *           The edge which should be in the graph
	 * @param graph
	 *           The graph
	 * @return yes, if edge exists in the graph (as label of source and target
	 *         node).
	 */
	private static boolean existsEdge(Edge edge, Graph graph, WorkSettings t) {
		boolean exists = false;
		
		Node source = edge.getSource();
		Node target = edge.getTarget();
		
		String edgeLabel = "";
		
		if (t.consEdgeLabels == true) {
			/*
			 * EdgeLabelAttribute edgeLabelAttr =
			 * (EdgeLabelAttribute) edge.getAttribute("label");
			 * edgeLabel = edgeLabelAttr.getLabel();
			 */
			edgeLabel = AttributeHelper.getLabel(edge, "");
		}
		
		String sourceLabel = AttributeHelper.getLabel(source, "");
		String targetLabel = AttributeHelper.getLabel(target, "");
		
		/*
		 * NodeLabelAttribute sourceLabelAttr =
		 * (NodeLabelAttribute) source.getAttribute("label");
		 * String sourceLabel = sourceLabelAttr.getLabel();
		 * NodeLabelAttribute targetLabelAttr =
		 * (NodeLabelAttribute) target.getAttribute("label");
		 * String targetLabel = targetLabelAttr.getLabel();
		 */

		/*
		 * Look at all edges in the graph <code>graph</code> to find
		 * an edge with the source and target nodes (labels)
		 * as the edge <code>edge</code>.
		 * If <code>consEdgeLabels</code> is true,
		 * check also the label of the edge.
		 */
		for (Iterator<?> itEdges = graph.getEdgesIterator(); itEdges.hasNext() && exists == false;) {
			Edge tmpEdge = (Edge) itEdges.next();
			
			Node tmpSource = tmpEdge.getSource();
			Node tmpTarget = tmpEdge.getTarget();
			
			String tmpSourceLabel = AttributeHelper.getLabel(tmpSource, "");
			String tmpTargetLabel = AttributeHelper.getLabel(tmpTarget, "");
			/*
			 * NodeLabelAttribute tmpSourceLabelAttr =
			 * (NodeLabelAttribute) tmpSource.getAttribute("label");
			 * String tmpSourceLabel = tmpSourceLabelAttr.getLabel();
			 * NodeLabelAttribute tmpTargetLabelAttr =
			 * (NodeLabelAttribute) tmpTarget.getAttribute("label");
			 * String tmpTargetLabel = tmpTargetLabelAttr.getLabel();
			 */

			if (tmpSourceLabel.equals(sourceLabel)
								&& tmpTargetLabel.equals(targetLabel)) {
				/*
				 * Either consider edge labels
				 * or simply set <code>exists</code> true
				 */
				if (t.consEdgeLabels == true) {
					/*
					 * EdgeLabelAttribute tmpEdgeLabelAttr =
					 * (EdgeLabelAttribute) tmpEdge.getAttribute("label");
					 * String tmpEdgeLabel = tmpEdgeLabelAttr.getLabel();
					 */
					String tmpEdgeLabel = AttributeHelper.getLabel(tmpEdge, "");
					if (tmpEdgeLabel.equals(edgeLabel)) {
						exists = true;
					}
				} else {
					exists = true;
				}
			}
		}
		
		return exists;
	}
	
}
