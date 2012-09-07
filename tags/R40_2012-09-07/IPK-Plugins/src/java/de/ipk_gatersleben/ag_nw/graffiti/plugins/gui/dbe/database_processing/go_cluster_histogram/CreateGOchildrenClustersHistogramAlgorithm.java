/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.ErrorMsg;
import org.FolderPanel;
import org.StringManipulationTools;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class CreateGOchildrenClustersHistogramAlgorithm
					extends AbstractAlgorithm
					implements AlgorithmWithComponentDescription {
	
	private boolean setLabel = true;
	private boolean removeExistingDatamapping = true;
	private boolean processUpstreamOfSelection = true;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Add Cluster-Histogram";
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"This command enumerates all<br>" +
							"leaf nodes with mapping data, connected<br>" +
							"to a given Hierarchy-Node.<br>" +
							"A histogram of the number of <br>" +
							"occurrences of a specific cluster <br>" +
							"ID is created. A data mapping<br>" +
							"representing this data is created <br>" +
							"and shown as a bar-chart.<br><br>" +
							"A similar command which processes<br>" +
							"the cluster distribution of neighbour-<br>" +
							"nodes of nodes with mapping-data is<br>" +
							"available from the &quot;Analysis&quot; menu.";
	}
	
	public JComponent getDescriptionComponent() {
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/images/cluster_hist_cmd_desc_scaled.png"));
		return FolderPanel.getBorderedComponent(new JLabel(icon), 5, 5, 5, 5);
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new BooleanParameter(setLabel, "Show Frequency Information", "If enabled, the frequency information will be added to the node labels."),
							new BooleanParameter(removeExistingDatamapping, "Remove existing data",
												"If disabled, a additional mapping is performed, existing data is not removed."),
							new BooleanParameter(processUpstreamOfSelection, "Process upstream",
												"If enabled, not only the selection is processed, but also all nodes upstream.")

		};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		setLabel = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		removeExistingDatamapping = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		processUpstreamOfSelection = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		try {
			graph.getListenerManager().transactionStarted(this);
			Collection<Node> workingSet = getSelectedOrAllNodes();
			final TreeSet<String> knownClusterIDs = new TreeSet<String>();
			HashSet<Node> processedNodes = new HashSet<Node>();
			for (Node n : workingSet) {
				knownClusterIDs.addAll(getLeafNodesClusterIDs(processedNodes, new NodeHelper(n)));
			}
			
			final boolean fsl = setLabel;
			
			/**
			 * Used in case upstream-processing is used.
			 */
			final HashSet<Node> specialModeValidLeafNodes = new HashSet<Node>(workingSet);
			
			ExecutorService run = Executors.newFixedThreadPool(1); // SystemAnalysis.getNumberOfCPUs());
			
			HashSet<Node> hierarchyNodesToBeProcessed = new HashSet<Node>();
			if (processUpstreamOfSelection) {
				// add upstream nodes
				for (Node n : graph.getNodes())
					if (n.getOutDegree() > 0)
						processUpstreamNodes(n, hierarchyNodesToBeProcessed);
			}
			
			hierarchyNodesToBeProcessed.addAll(workingSet);
			
			for (Node n : hierarchyNodesToBeProcessed) {
				final NodeHelper nh = new NodeHelper(n);
				String clusterId = nh.getClusterID(null);
				if (clusterId == null || clusterId.length() <= 0 || !knownClusterIDs.contains(clusterId)) {
					run.submit(new Runnable() {
						public void run() {
							processNode(nh, knownClusterIDs, fsl, removeExistingDatamapping, processUpstreamOfSelection, specialModeValidLeafNodes);
						}
					});
				}
			}
			run.shutdown();
			while (!run.isTerminated()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					ErrorMsg.addErrorMessage(e);
					break;
				}
			}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	private void processNode(NodeHelper nh, TreeSet<String> knownClusterIDs, boolean setLabel, boolean removeExistingDataMapping,
						boolean specialModeUpstreamProcessing, HashSet<Node> specialModeValidLeafNodes) {
		HashSet<Node> childNodes = nh.getAllOutChildNodes();
		
		if (specialModeUpstreamProcessing) {
			HashSet<Node> remove = new HashSet<Node>();
			for (Node n : childNodes)
				if (!specialModeValidLeafNodes.contains(n))
					remove.add(n);
			childNodes.removeAll(remove);
		}
		
		TreeMap<String, Integer> cluster2frequency = new TreeMap<String, Integer>();
		for (String ci : knownClusterIDs)
			cluster2frequency.put(ci, new Integer(0));
		
		for (Node n : childNodes) {
			NodeHelper nnh = new NodeHelper(n);
			if (nnh.getOutDegree() == 0) {
				String clusterID = nnh.getClusterID(null);
				if (clusterID != null) {
					Integer value = cluster2frequency.get(clusterID);
					if (value == null)
						value = new Integer(0);
					cluster2frequency.put(clusterID, new Integer(value + 1));
				}
			}
		}
		
		if (removeExistingDataMapping)
			nh.removeDataMapping();
		StringBuilder sb = null;
		if (setLabel)
			sb = new StringBuilder();
		
		for (String clusterID : cluster2frequency.keySet()) {
			int plantID = nh.memGetPlantID(clusterID, "", "", "", "");
			Integer value = cluster2frequency.get(clusterID);
			if (value == null) {
				nh.memSample(new Double(0), -1, plantID, "frequency", "-1", new Integer(-1));
				if (sb != null) {
					if (sb.length() > 0)
						sb.append(", 0");
					else
						sb.append("0");
				}
			} else {
				nh.memSample(new Double(value), -1, plantID, "frequency", "-1", new Integer(-1));
				if (sb != null) {
					if (sb.length() > 0)
						sb.append(";" + (int) value);
					else
						sb.append((int) value);
				}
			}
		}
		if (sb != null) {
			if (sb.length() > 0)
				nh.setLabel(nh.getLabel() + " (" + sb.toString() + ")");
			else
				nh.setLabel(nh.getLabel() + " (n/a)");
			String lbl = nh.getLabel();
			lbl = StringManipulationTools.stringReplace(lbl, ") (", "/");
			nh.setLabel(lbl);
		}
		nh.memAddDataMapping("Cluster Distribution for " + nh.getLabel(),
							"cluster frequency", null, "calculated analysis", "system", "Frequency of clusters in child nodes of a GO-Term-Hierarchy-Node", "");
		
		nh.setChartType(GraffitiCharts.PIE);
	}
	
	public static Collection<String> getLeafNodesClusterIDs(
						HashSet<Node> processedNodes,
						NodeHelper nh) {
		Collection<String> result = new ArrayList<String>();
		if (!processedNodes.contains(nh.getGraphNode())) {
			processedNodes.add(nh.getGraphNode());
			Collection<Node> childNodes = nh.getAllOutChildNodes();
			for (Node n : childNodes) {
				NodeHelper nnh = new NodeHelper(n);
				if (nnh.getOutDegree() > 0)
					continue;
				String clusterID = nnh.getClusterID(null);
				if (clusterID != null)
					result.add(clusterID);
			}
		}
		return result;
	}
	
	public void processUpstreamNodes(Node n, HashSet<Node> processedNodes) {
		if (!processedNodes.contains(n)) {
			processedNodes.add(n);
			Collection<Node> upstreamNodes = n.getAllInNeighbors();
			for (Node up : upstreamNodes) {
				processUpstreamNodes(up, processedNodes);
			}
		}
	}
}
