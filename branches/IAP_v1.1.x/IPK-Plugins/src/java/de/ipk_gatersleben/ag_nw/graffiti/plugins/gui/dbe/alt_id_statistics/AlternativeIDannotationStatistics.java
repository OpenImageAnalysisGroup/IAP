package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.alt_id_statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class AlternativeIDannotationStatistics extends AbstractAlgorithm {
	
	private String filterText = "";
	private boolean createLogFile = false;
	
	public void execute() {
		final TextFile logFile = new TextFile();
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl sp = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
		
		final Graph fg = graph;
		final Selection fs = selection;
		
		Runnable bt = new Runnable() {
			public void run() {
				fg.getListenerManager().transactionStarted(AlternativeIDannotationStatistics.class);
				try {
					processData(logFile, sp, fg, fs);
				} finally {
					fg.getListenerManager().transactionFinished(AlternativeIDannotationStatistics.class);
				}
			}
		};
		
		Runnable st = new Runnable() {
			public void run() {
				if (createLogFile) {
					File f = OpenFileDialogService.getSaveFile(new String[] { ".txt" }, "Tab-separated text files");
					if (f != null) {
						try {
							logFile.write(f);
						} catch (IOException e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
				}
			}
		};
		
		BackgroundTaskHelper.issueSimpleTask("Alternative ID Statistics", "", bt, st, sp);
	}
	
	public String getName() {
		return "Alternative Identifier Statistics";
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Calculate frequency of alternative identifiers, connected to<br>" +
							"leaf nodes, reachable from hierarchy nodes.";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new StringParameter(filterText, "Filter", "Only alternative IDs, which contain the specified search filter are processed."),
							new BooleanParameter(createLogFile, "Create Log-File", "If selected, a CSV text file containing computation results is created.") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		this.filterText = ((StringParameter) params[i++]).getString();
		this.createLogFile = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	private void processData(final TextFile logFile,
						final BackgroundTaskStatusProviderSupportingExternalCallImpl sp,
						final Graph fg, final Selection fs) {
		attach(fg, fs);
		HashSet<String> clusterIDs = new HashSet<String>();
		for (Node n : GraphHelper.getLeafNodes(getSelectedOrAllNodes())) {
			String clusterID = NodeTools.getClusterID(n, "");
			clusterID = StringManipulationTools.stringReplace(clusterID, ":", "_");
			NodeHelper nh = new NodeHelper(n);
			if (!nh.hasDataMapping())
				clusterID = "";
			clusterIDs.add(clusterID);
		}
		String sep = "\t";
		String heading = "Label" + sep;
		boolean headingSet = false;
		
		double max = getSelectedOrAllNodes().size();
		
		sp.setCurrentStatusText1("Alternative ID filter: " + filterText + ", " + (int) max + " nodes");
		sp.setCurrentStatusText2("");
		
		double current = 0;
		sp.setCurrentStatusValueFine(0);
		for (Node n : getSelectedOrAllNodes()) {
			if (n.getOutDegree() == 0)
				continue;
			NodeHelper nh = new NodeHelper(n);
			sp.setCurrentStatusText2("Current node: " + nh.getLabel());
			AttributeHelper.deleteAttribute(nh, "hierarchy_" + filterText, "leaf-alt-id*");
			AttributeHelper.deleteAttribute(nh, "hierarchy-" + filterText, "leaf-alt-id*");
			TreeSet<String> idSet = new TreeSet<String>();
			HashMap<String, ArrayList<String>> geneNameAndClusterIDs = new HashMap<String, ArrayList<String>>();
			HashMap<String, HashSet<String>> geneNameAndClusterIDset = new HashMap<String, HashSet<String>>();
			HashMap<String, Integer> clusterIDandFrequency = new HashMap<String, Integer>();
			
			StringBuilder currentLine = new StringBuilder("");
			currentLine.append(nh.getLabel() + sep);
			
			if (!headingSet) {
				heading += "m_a" + sep + "m_b" + sep + "m_c" + sep + "m_d" + sep + "p (one sided)" + sep + "p (two sided)";
			}
			
			int a = (Integer) AttributeHelper.getAttributeValue(nh, "Fisher", "m_a", -1, -1);
			int b = (Integer) AttributeHelper.getAttributeValue(nh, "Fisher", "m_b", -1, -1);
			int c = (Integer) AttributeHelper.getAttributeValue(nh, "Fisher", "m_c", -1, -1);
			int d = (Integer) AttributeHelper.getAttributeValue(nh, "Fisher", "m_d", -1, -1);
			double p1 = (Double) AttributeHelper.getAttributeValue(nh, "Fisher", "p_fisher", -1d, -1d);
			double p2 = (Double) AttributeHelper.getAttributeValue(nh, "Fisher", "p_fisher2", -1d, -1d);
			currentLine.append(a + sep + b + sep + c + sep + d + sep + p1 + sep + p2 + sep);
			
			for (NodeHelper leafNode : nh.getReachableLeafNodes()) {
				String leafCluster = leafNode.getClusterID("");
				leafCluster = StringManipulationTools.stringReplace(leafCluster, ":", "_");
				
				if (!leafNode.hasDataMapping())
					leafCluster = "";
				
				ArrayList<String> altIDs = leafNode.getAlternativeIDs();
				String lbl = leafNode.getLabel();
				if (lbl != null && altIDs.size() == 0) {
					altIDs.add(lbl);
				}
				for (String id : altIDs) {
					if (filterText.length() <= 0 || id.contains(filterText)) {
						if (!geneNameAndClusterIDs.containsKey(id))
							geneNameAndClusterIDs.put(id, new ArrayList<String>());
						if (!geneNameAndClusterIDset.containsKey(id))
							geneNameAndClusterIDset.put(id, new HashSet<String>());
						geneNameAndClusterIDs.get(id).add(leafCluster);
						geneNameAndClusterIDset.get(id).add(leafCluster);
						idSet.add(id);
					}
				}
				
				if (!clusterIDandFrequency.containsKey(leafCluster))
					clusterIDandFrequency.put(leafCluster, 0);
				clusterIDandFrequency.put(leafCluster, clusterIDandFrequency.get(leafCluster) + 1);
			}
			if (!createLogFile)
				nh.setAttributeValue("hierarchy-" + filterText, "leaf-alt-id-values", AttributeHelper.getStringList(idSet, ","));
			nh.setAttributeValue("hierarchy-" + filterText, "leaf-alt-id-count", idSet.size());
			
			if (!headingSet) {
				heading += "leaf-alt-id-set" + sep + "leaf-alt-id-cnt" + sep;
			}
			currentLine.append(AttributeHelper.getStringList(idSet, ",") + sep + idSet.size() + sep);
			
			TreeSet<String> otherClusterGeneList = new TreeSet<String>();
			for (String clusterID : clusterIDs) {
				int frequency = 0;
				if (clusterIDandFrequency.containsKey(clusterID))
					frequency = clusterIDandFrequency.get(clusterID);
				if (clusterID.length() <= 0)
					clusterID = "empty";
				nh.setAttributeValue("hierarchy-" + filterText, "leaf-cluster-" + clusterID, frequency);
				TreeSet<String> geneList = new TreeSet<String>();
				for (Map.Entry<String, HashSet<String>> me : geneNameAndClusterIDset.entrySet()) {
					if (me.getValue().size() == 1 && me.getValue().iterator().next().equals(clusterID)) {
						geneList.add(me.getKey());
					} else {
						if (me.getValue().size() != 1) {
							otherClusterGeneList.add(me.getKey());
						}
					}
				}
				nh.setAttributeValue("hierarchy-" + filterText, "leaf-alt-id-frequency-for-cluster-" + clusterID, geneList.size());
				if (!createLogFile)
					nh.setAttributeValue("hierarchy-" + filterText, "leaf-alt-id-values-for-cluster-" + clusterID, AttributeHelper.getStringList(geneList, ","));
				if (!headingSet) {
					heading += "leaf-alt-id-set-for-cluster-" + clusterID + sep + "leaf-alt-id-cnt-for-cluster-" + clusterID + sep;
				}
				currentLine.append(AttributeHelper.getStringList(geneList, ",") + sep + geneList.size() + sep);
			}
			nh.setAttributeValue("hierarchy-" + filterText, "leaf-alt-id-frequency-for-cluster-other", otherClusterGeneList.size());
			if (!createLogFile)
				nh.setAttributeValue("hierarchy-" + filterText, "leaf-alt-id-values-for-cluster-other", AttributeHelper.getStringList(otherClusterGeneList, ","));
			if (!headingSet) {
				heading += "leaf-alt-id-set-for-cluster-other" + sep + "leaf-alt-id-cnt-for-cluster-other";
			}
			currentLine.append(AttributeHelper.getStringList(otherClusterGeneList, ",") + sep + otherClusterGeneList.size());
			
			if (!headingSet)
				logFile.add(heading);
			logFile.add(currentLine.toString());
			
			headingSet = true;
			current += 1;
			sp.setCurrentStatusValueFine(current / max * 100);
		}
	}
	
}
