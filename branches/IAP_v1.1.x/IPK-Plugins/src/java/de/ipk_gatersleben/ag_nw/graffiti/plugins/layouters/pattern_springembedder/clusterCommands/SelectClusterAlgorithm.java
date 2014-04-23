/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

public class SelectClusterAlgorithm extends AbstractEditorAlgorithm {
	
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Select Pathway-Subgraph...";
		else
			return "Select Cluster...";
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		Set<String> clusters = new TreeSet<String>();
		for (GraphElement ge : graph.getGraphElements()) {
			String clusterId = NodeTools.getClusterID(ge, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
		}
		if (clusters.size() <= 0)
			throw new PreconditionException("There is no cluster information assigned to graph elements!");
	}
	
	public void execute() {
		TreeSet<String> clusters = new TreeSet<String>();
		for (GraphElement ge : graph.getGraphElements()) {
			String clusterId = NodeTools.getClusterID(ge, "");
			if (!clusterId.equals(""))
				clusters.add(clusterId);
			else
				clusters.add("[empty]");
		}
		Object result;
		String cluster = "Cluster";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster = "Pathway";
		
		if ((result = JOptionPane.showInputDialog(MainFrame.getInstance(),
							"Select the " + cluster + " to be selected (by ID)", "Select " + cluster,
							JOptionPane.OK_CANCEL_OPTION, null, clusters.toArray(), clusters
												.first())) != null) {
			String selCluster = (String) result;
			int cnt = 0;
			for (GraphElement ge : graph.getGraphElements()) {
				String clusterId = NodeTools.getClusterID(ge, "");
				if (clusterId.equals(selCluster) || (clusterId.length() <= 0 && selCluster.equals("[empty]"))) {
					selection.add(ge);
					cnt++;
				}
			}
			
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
			MainFrame.showMessage(cnt + " graph elements added to selection", MessageType.INFO);
		}
	}
	
	public boolean activeForView(View v) {
		return v != null;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}