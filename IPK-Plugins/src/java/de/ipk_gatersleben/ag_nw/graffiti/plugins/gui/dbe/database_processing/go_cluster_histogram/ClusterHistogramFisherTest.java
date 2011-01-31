/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 16.02.2007 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.FolderPanel;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.exact_fisher_test.ContTable;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.exact_fisher_test.FisherProbability;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ClusterHistogramFisherTest
					extends AbstractAlgorithm
					implements AlgorithmWithComponentDescription {
	
	private String groupA, groupB;
	private boolean store1minusP, findClusters, addMatrixInfo, addDataMapping;
	private double alpha = 0.05;
	
	private FisherOperationMode modeOfOperation = FisherOperationMode.selectSignificantNodes2s;
	
	private static String notInA = "[not in group A]";
	
	public void execute() {
		if (groupA == null || groupB == null || groupA.equals(groupB)) {
			MainFrame.showMessageDialog("Please select two different cluster IDs!", "Can not proceed");
			return;
		}
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Init", "Please wait");
		final Graph gg = graph;
		final Collection<Node> workingSet = getSelectedOrAllNodes();
		BackgroundTaskHelper.issueSimpleTask(getName(), "Init",
							new Runnable() {
								public void run() {
									processCommand(gg, workingSet, status, SystemAnalysis.getNumberOfCPUs());
									if (addDataMapping)
										GraphHelper.issueCompleteRedrawForGraph(gg);
								}
							}, null, status);
	}
	
	private void processCommand(Graph graph, Collection<Node> workingSet, final BackgroundTaskStatusProviderSupportingExternalCall status,
						int threadCount) {
		status.setCurrentStatusValue(-1);
		status.setCurrentStatusText1("Analyze Cluster Frequency...");
		status.setCurrentStatusText2("Please wait");
		final TreeSet<String> knownClusterIDs = new TreeSet<String>();
		HashSet<Node> processedNodes = new HashSet<Node>();
		final TreeMap<String, Integer> cluster2frequencyGlobal = new TreeMap<String, Integer>();
		int frequencyGlobalA = 0;
		int frequencyGlobalB = 0;
		int frequencyGlobalAll = 0;
		
		for (Node n : workingSet) {
			if (n == null)
				continue;
			knownClusterIDs.addAll(
								CreateGOchildrenClustersHistogramAlgorithm.getLeafNodesClusterIDs(processedNodes, new NodeHelper(n)));
			if (n.getOutDegree() == 0) {
				String cluster = NodeTools.getClusterID(n, "");
				frequencyGlobalAll++;
				if (cluster.equals(groupA))
					frequencyGlobalA++;
				else {
					if (cluster.equals(groupB) || (groupB.equals(notInA)))
						frequencyGlobalB++;
				}
				if (cluster.length() >= 0) {
					if (!cluster2frequencyGlobal.containsKey(cluster))
						cluster2frequencyGlobal.put(cluster, new Integer(1));
					else
						cluster2frequencyGlobal.put(cluster, cluster2frequencyGlobal.get(cluster) + 1);
				}
				if (status.wantsToStop())
					break;
			}
		}
		status.setCurrentStatusText1("Perform Probability Calculation...");
		status.setCurrentStatusText2("One and two sided Fisher test is running");
		int belowAlpha = 0;
		int nodeCnt = 0;
		ArrayList<Node> selNodes = new ArrayList<Node>();
		
		int iWorkLoad = 0;
		
		for (Node n : workingSet) {
			NodeHelper nh = new NodeHelper(n);
			String clusterId = nh.getClusterID(null);
			if (clusterId == null || nh.getOutDegree() > 0) {
				iWorkLoad++;
			}
		}
		
		HashSet<Node> significantNodes = new HashSet<Node>();
		
		HashMap<Node, Integer> result = new HashMap<Node, Integer>();
		
		final int ffrequencyGlobalA = frequencyGlobalA;
		final int ffrequencyGlobalB = frequencyGlobalB;
		final int ffrequencyGlobalAll = frequencyGlobalAll;
		
		ExecutorService run = Executors.newFixedThreadPool(threadCount);
		
		ArrayList<Future<Entry<Node, Integer>>> results = new ArrayList<Future<Entry<Node, Integer>>>();
		
		status.setCurrentStatusText2("Calculate Probabilities (1/2)");
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int fiWorkLoad = iWorkLoad;
		if (!status.wantsToStop())
			for (Node n : workingSet) {
				final NodeHelper nh = new NodeHelper(n);
				String clusterId = nh.getClusterID(null);
				if ((clusterId == null || nh.getOutDegree() > 0)) {
					Callable<Entry<Node, Integer>> cmd = new Callable<Entry<Node, Integer>>() {
						public Entry<Node, Integer> call() throws Exception {
							int res;
							boolean twoSidedAlphaComparison = modeOfOperation == FisherOperationMode.selectInsignificant2s ||
												modeOfOperation == FisherOperationMode.selectSignificantNodes2s ||
												modeOfOperation == FisherOperationMode.pruneTree2s;
							String sss = "One-sided";
							if (twoSidedAlphaComparison)
								sss = "Two-sided";
							status.setCurrentStatusText1(sss + " Fisher test (" + nh.getLabel() + "), " + tso.getInt() + "/" + fiWorkLoad);
							if (!status.wantsToStop())
								res = processNode(twoSidedAlphaComparison, alpha, nh, knownClusterIDs, ffrequencyGlobalA, ffrequencyGlobalB, ffrequencyGlobalAll,
													cluster2frequencyGlobal);
							else
								res = 0;
							tso.addInt(1);
							status.setCurrentStatusValueFine(100d * tso.getInt() / fiWorkLoad);
							return new MyEntry(nh.getGraphNode(), res);
						}
					};
					results.add(run.submit(cmd));
				}
			}
		run.shutdown();
		while (!run.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			}
			if (status.wantsToStop())
				break;
		}
		status.setCurrentStatusText1("Calculation Finished");
		status.setCurrentStatusText2("Process Results (2/2)");
		status.setCurrentStatusValue(-1);
		for (Future<Entry<Node, Integer>> r : results) {
			Entry<Node, Integer> calcRes;
			try {
				calcRes = r.get();
				result.put(calcRes.getKey(), calcRes.getValue());
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		
		nodeCnt = 0;
		
		if (!status.wantsToStop())
			for (Node n : workingSet) {
				status.setCurrentStatusValueFine(100d * nodeCnt / iWorkLoad);
				NodeHelper nh = new NodeHelper(n);
				String clusterId = nh.getClusterID(null);
				AttributeHelper.deleteAttribute(nh.getGraphNode(), "Fisher", "p_below_alpha");
				if ((clusterId == null || clusterId.length() <= 0) && nh.getOutDegree() > 0) {
					status.setCurrentStatusText1("Process " + nh.getLabel() + ", " + nodeCnt + "/" + iWorkLoad);
					nodeCnt++;
					status.setCurrentStatusValueFine(100d * nodeCnt / iWorkLoad);
					if (!result.containsKey(n))
						continue;
					int res = result.get(n);
					if (res > 0) {
						if (modeOfOperation == FisherOperationMode.selectSignificantNodes1s || modeOfOperation == FisherOperationMode.selectSignificantNodes2s)
							selNodes.add(nh.getGraphNode());
						if (modeOfOperation == FisherOperationMode.pruneTree1s || modeOfOperation == FisherOperationMode.pruneTree2s)
							significantNodes.add(nh.getGraphNode());
						AttributeHelper.setAttribute(n, "Fisher", "p_below_alpha", "yes");
					} else {
						AttributeHelper.setAttribute(n, "Fisher", "p_below_alpha", "no");
						if (modeOfOperation == FisherOperationMode.selectInsignificant1s || modeOfOperation == FisherOperationMode.selectInsignificant2s)
							selNodes.add(nh.getGraphNode());
					}
					belowAlpha += res;
				}
				if (status.wantsToStop())
					break;
			}
		status.setCurrentStatusValue(100);
		status.setCurrentStatusText1("Calculation");
		if (status.wantsToStop())
			status.setCurrentStatusText2("Not Complete!");
		else
			status.setCurrentStatusText2("Complete");
		MainFrame.showMessage(belowAlpha + " out of " + nodeCnt
							+ " hierarchy nodes seem to be related to a significant cluster distribution (alpha<=0.05), check node attribute values for details.",
							MessageType.INFO, 30000);
		if (modeOfOperation == FisherOperationMode.selectInsignificant1s || modeOfOperation == FisherOperationMode.selectInsignificant2s ||
							modeOfOperation == FisherOperationMode.selectSignificantNodes1s || modeOfOperation == FisherOperationMode.selectSignificantNodes2s)
			GraphHelper.selectNodes(selNodes);
		if (modeOfOperation == FisherOperationMode.pruneTree1s || modeOfOperation == FisherOperationMode.pruneTree2s)
			PruneTreeAlgorithm.pruneFromTheseNodes(significantNodes);
	}
	
	@SuppressWarnings("deprecation")
	private int processNode(boolean twoSidedAlphaComparison, double alpha, NodeHelper nh, TreeSet<String> knownClusterIDs,
						int frequencyGlobalA, int frequencyGlobalB, int frequencyGlobalAll, TreeMap<String, Integer> cluster2frequencyGlobal) {
		AttributeHelper.deleteAttribute(nh.getGraphNode(), "Fisher", "m_*");
		AttributeHelper.deleteAttribute(nh.getGraphNode(), "Fisher", "s*");
		AttributeHelper.deleteAttribute(nh.getGraphNode(), "Fisher", "p_*");
		AttributeHelper.deleteAttribute(nh.getGraphNode(), "Fisher", "_*");
		boolean foundBelowAlpha = false;
		Collection<Node> childNodes = nh.getAllOutChildNodes();
		TreeMap<String, Integer> cluster2frequencyAB = new TreeMap<String, Integer>();
		for (String ci : knownClusterIDs)
			cluster2frequencyAB.put(ci, new Integer(0));
		
		cluster2frequencyAB.put(notInA, new Integer(0));
		
		TreeMap<String, Integer> cluster2frequency = new TreeMap<String, Integer>();
		for (String ci : knownClusterIDs)
			cluster2frequency.put(ci, new Integer(0));
		
		int allChildrenCount = 0;
		for (Node n : childNodes) {
			if (n.getOutDegree() == 0) {
				NodeHelper nnh = new NodeHelper(n);
				allChildrenCount++;
				String clusterID = nnh.getClusterID("");
				if (clusterID.length() > 0) {
					cluster2frequency.put(clusterID, cluster2frequency.get(clusterID) + 1);
					if (!clusterID.equals(groupA) && groupB.equals(notInA))
						clusterID = notInA;
					if (clusterID.equals(groupA) || clusterID.equals(groupB) || clusterID.equals(notInA)) {
						Integer value = cluster2frequencyAB.get(clusterID);
						if (value == null)
							value = new Integer(0);
						cluster2frequencyAB.put(clusterID, new Integer(value + 1));
					}
				}
			}
		}
		if (!findClusters) {
			int a = cluster2frequencyAB.get(groupA);
			int b = cluster2frequencyAB.get(groupB);
			int c = frequencyGlobalA - a;
			int d = frequencyGlobalB - b;
			if (!(frequencyGlobalA == 0 && frequencyGlobalB == 0)) {
				ContTable ct = new ContTable(a, b, c, d, true);
				FisherProbability p = ct.getOneAndTwoSidedFisherProbability(twoSidedAlphaComparison);
				System.out.println("ct: " + ct.toString() + " -> " + p.getResultString());
				if (p.getOneSidedD() <= 0.05)
					foundBelowAlpha = true;
				if (store1minusP) {
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "s_fisher", 1 - p.getOneSidedD());
					if (twoSidedAlphaComparison)
						AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "s_fisher2", 1 - p.getTwoSidedD());
				} else {
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "p_fisher", p.getOneSidedD());
					if (twoSidedAlphaComparison)
						AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "p_fisher2", p.getTwoSidedD());
				}
				if (addMatrixInfo) {
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "m_a", a);
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "m_b", b);
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "m_c", c);
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "m_d", d);
				}
			}
		}
		if (findClusters) {
			// evaluate all clusters for significance
			// number of nodes belonging to current cluster ID vs. to not belonging to that cluster ID
			
			for (String currCluster : cluster2frequencyGlobal.keySet()) {
				if (currCluster.length() <= 0)
					continue;
				int a = cluster2frequency.get(currCluster);
				int b = allChildrenCount - a;
				int c = cluster2frequencyGlobal.get(currCluster) - a;
				int d = frequencyGlobalAll - b - a - c;
				ContTable ct = new ContTable(a, b, c, d, true);
				FisherProbability p = ct.getOneAndTwoSidedFisherProbability(twoSidedAlphaComparison);
				if (twoSidedAlphaComparison) {
					if (p.getTwoSidedD() <= alpha)
						foundBelowAlpha = true;
				} else {
					if (p.getOneSidedD() <= alpha)
						foundBelowAlpha = true;
				}
				if (addMatrixInfo) {
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "_" + AttributeHelper.getSaveAttributeName(currCluster) + "_m_a", a);
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "_" + AttributeHelper.getSaveAttributeName(currCluster) + "_m_b", b);
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "_" + AttributeHelper.getSaveAttributeName(currCluster) + "_m_c", c);
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "_" + AttributeHelper.getSaveAttributeName(currCluster) + "_m_d", d);
				}
				if (store1minusP) {
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "_" + AttributeHelper.getSaveAttributeName(currCluster) + "_s", 1 - p.getOneSidedD());
					if (twoSidedAlphaComparison)
						AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "_" + AttributeHelper.getSaveAttributeName(currCluster) + "_s2", 1 - p
											.getTwoSidedD());
				} else {
					AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "_" + AttributeHelper.getSaveAttributeName(currCluster) + "_p", p.getOneSidedD());
					if (twoSidedAlphaComparison)
						AttributeHelper.setAttribute(nh.getGraphNode(), "Fisher", "_" + AttributeHelper.getSaveAttributeName(currCluster) + "_p2", p.getTwoSidedD());
				}
				if (addDataMapping) {
					int plantID = nh.memGetPlantID(currCluster, "", "", "", "");
					if (twoSidedAlphaComparison) {
						if (store1minusP)
							nh.memSample(1 - p.getTwoSidedD(), 1, plantID, "1-alpha", "-1", -1);
						else
							nh.memSample(p.getTwoSidedD(), 1, plantID, "alpha", "-1", -1);
					} else {
						if (store1minusP)
							nh.memSample(1 - p.getOneSidedD(), 1, plantID, "1-alpha", "-1", -1);
						else
							nh.memSample(p.getOneSidedD(), 1, plantID, "alpha", "-1", -1);
					}
				}
			}
			if (addDataMapping) {
				if (store1minusP)
					nh.memAddDataMapping("Fisher Statistics", "s", new Date().toGMTString(), "Fisher Statistics", ReleaseInfo.getRunningReleaseStatus().toString(),
										"no remark", "no sequence info");
				else
					nh.memAddDataMapping("Fisher Statistics", "p", new Date().toGMTString(), "Fisher Statistics", ReleaseInfo.getRunningReleaseStatus().toString(),
										"no remark", "no sequence info");
			}
		}
		if (foundBelowAlpha)
			return 1;
		else
			return 0;
	}
	
	@Override
	public String getDescription() {
		return "<html>"
							+
							"This command evaluates the frequency of assigned clustered leaf nodes.<br>"
							+
							"<br><u>This command is not fully tested and should be used with care.<br>"
							+
							"Double-check the results.</u><br>"
							+
							"<br>"
							+
							"The probability is calculated, that the frequency of the selected cluster groups is observerd in<br>"
							+
							"context of the particular hierarchy node by chance. The Fisher exact test is used to calculate<br>"
							+
							" a p-value. The p value is the sum of the probability of observing the actual frequencies and the<br>"
							+
							"probabilities of observing more extreme distributions.<br>"
							+
							"For convenience purposes the frequency matrix values a, b, c, d are added to the<br>"
							+
							"hierarchy nodes as well as the calculated p-value.<br><br>"
							+
							"The two dimensional contingency matrix contains the following values:<br>"
							+
							"<table border=1>"
							+
							"<tr><td><small>a = Number of leaf nodes which are connected<br>to the current hierarchy node and whose cluster<br>ID equals group A</td>"
							+
							"<td><small>b = Number of leaf nodes which are connected<br>to current hierarchy node and whose cluster<br>ID equals group B</td></tr>"
							+
							"<tr>"
							+
							"<td><small>c = Number of leaf nodes which are not connected<br>to current hierarchy node and whose cluster<br>ID equals group A</td>"
							+
							"<td><small>d = Number of leaf nodes which are not connected<br> to current hierarchy node and whose cluster<br>ID equals group B</td></table><br><br>"
							+
							"Select two different cluster IDs:";
	}
	
	@Override
	public Parameter[] getParameters() {
		
		Collection<Node> workingSet = getSelectedOrAllNodes();
		TreeSet<String> knownClusterIDs = new TreeSet<String>();
		HashSet<Node> processedNodes = new HashSet<Node>();
		for (Node n : workingSet) {
			knownClusterIDs.addAll(
								CreateGOchildrenClustersHistogramAlgorithm.getLeafNodesClusterIDs(processedNodes, new NodeHelper(n)));
		}
		TreeSet<String> knownClusterIDsB = new TreeSet<String>();
		knownClusterIDsB.add(notInA);
		knownClusterIDsB.addAll(knownClusterIDs);
		return new Parameter[] {
							new ObjectListParameter(modeOfOperation, "Mode of Operation", "Specify operation result handling", FisherOperationMode.values()),
							new ObjectListParameter(knownClusterIDs.iterator().next(), "Group A", "Group A", knownClusterIDs),
							new ObjectListParameter(notInA, "Group B", "Group B", knownClusterIDsB),
							new BooleanParameter(store1minusP, "Store Frequency Info", "If selected, the matrix information a, b, c, d will be stored"),
							new BooleanParameter(store1minusP, "Store 1-p", "If selected, 1-p = s is stored instead of p"),
							new BooleanParameter(findClusters, "Check all clusters",
												"If selected the significance of cluster distribution of any cluster ID is additionally evaluated"),
							new BooleanParameter(addDataMapping, "Add Data-Mapping",
												"(requires 'check all clusters' to be enabled!) If selected, a new data mapping, containing the<br>" +
																	"significance or p values is added to the nodes."),
							new DoubleParameter(alpha, "Alpha", "Nodes with at least one p value smaller than alpha will be attributed and selected") };
	}
	
	@Override
	public void check() throws PreconditionException {
		Collection<Node> workingSet = getSelectedOrAllNodes();
		TreeSet<String> knownClusterIDs = new TreeSet<String>();
		HashSet<Node> processedNodes = new HashSet<Node>();
		for (Node n : workingSet) {
			knownClusterIDs.addAll(
								CreateGOchildrenClustersHistogramAlgorithm.getLeafNodesClusterIDs(processedNodes, new NodeHelper(n)));
		}
		if (knownClusterIDs.size() < 2)
			throw new PreconditionException("<html>" +
								"Hierarchy nodes need to be connected to leaf nodes<br>" +
								"which are attributed with at least two different cluster IDs.");
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		modeOfOperation = (FisherOperationMode) ((ObjectListParameter) params[i++]).getValue();
		groupA = (String) ((ObjectListParameter) params[i++]).getValue();
		groupB = (String) ((ObjectListParameter) params[i++]).getValue();
		addMatrixInfo = ((BooleanParameter) params[i++]).getBoolean();
		store1minusP = ((BooleanParameter) params[i++]).getBoolean();
		findClusters = ((BooleanParameter) params[i++]).getBoolean();
		addDataMapping = ((BooleanParameter) params[i++]).getBoolean();
		alpha = ((DoubleParameter) params[i++]).getDouble();
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	public String getName() {
		return "Fisher Test for Evaluation of Cluster Distribution";
	}
	
	public JComponent getDescriptionComponent() {
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/images/fisher_demo.png"));
		return FolderPanel.getBorderedComponent(new JLabel(icon), 5, 5, 5, 5);
	}
}
