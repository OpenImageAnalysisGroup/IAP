/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.FolderPanel;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.services.RunAlgorithmDialog;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK-Gatersleben
 */
public class ClusterGraphLayout extends AbstractAlgorithm
					implements AlgorithmWithComponentDescription {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Apply Pathway-Overview Layout";
		else
			return "Apply Layout of Overview-Graph to Nodes";// Re-Layout based on Cluster-Graph Layout";
	}
	
	@Override
	public String getDescription() {
		String cluster = "overview";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster = "pathway-overview";
		String cluster2 = "cluster";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster2 = "pathway";
		return "<html>" +
							"Using this command<br>" +
							"a " + cluster + " graph is<br>" +
							"created (2) from<br>" +
							"the source graph (1), " +
							"<br>which needs to<br>" +
							"contain nodes with<br>" +
							"different " + cluster2 + " IDs.<br>" +
							"<br>The " + cluster + " graph<br>" +
							"may be automatically<br>" +
							"layouted, or manually.<br>" +
							"For that you need to<br>" +
							"select the<br>" +
							"&quot;Null-Layout&quot;.<br>" +
							"<br>After layouting the<br>" +
							cluster + " graph (3),<br>" +
							"apply the layout of<br>" +
							"the " + cluster + " graph to<br>" +
							"the source graph (4),<br>" +
							"by choosing <b>OK</b><br>" +
							"from the progress<br>" +
							"panel in the<br>" +
							"lower-right of the<br>" +
							"application window.<br>";
	}
	
	public JComponent getDescriptionComponent() {
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/images/clusterrelayout.png"));
		return FolderPanel.getBorderedComponent(new JLabel(icon), 5, 5, 5, 5);
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false; // recursive run should be avoided, thus it is not labeled as a layout algorithm
	}
	
	@Override
	public void reset() {
	}
	
	@Override
	public void setParameters(Parameter[] params) {
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
		String cluster = "overview";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster = "cluster/pathway";
		if (clusters.size() <= 0)
			throw new PreconditionException(
								"No " + cluster + " information available for this graph!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		String cluster = "overview-graph";
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			cluster = "Pathway-Overview Graph";
		
		RunAlgorithmDialog rad = new RunAlgorithmDialog("Select " + cluster + " Layout",
							graph, selection, true, false);
		rad.setAlwaysOnTop(true);
		rad.setVisible(true);
		rad.requestFocusInWindow();
		ActionListenerForClusterGraphBasedLayout al = new ActionListenerForClusterGraphBasedLayout();
		final Timer t = new Timer(100, al);
		al.setAlgorithmDialog(rad);
		al.setOptions(getName(), t, false, false, graph);
		
		t.setRepeats(true);
		t.start();
	}
	
}

class ActionListenerForClusterGraphBasedLayout implements ActionListener {
	RunAlgorithmDialog rad = null;
	
	Timer tref = null;
	
	String name;
	
	private Graph graph;
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (!rad.isVisible() && rad.getAlgorithm() != null) {
			tref.stop();
			MyClusterGraphBasedReLayoutService mcs = new MyClusterGraphBasedReLayoutService(true, graph);
			mcs.setAlgorithm(rad.getAlgorithm(), null);
			BackgroundTaskHelper bth = new BackgroundTaskHelper(mcs, mcs, name,
								name, true, false);
			bth.startWork(this);
		} else
			rad.setAlwaysOnTop(true);
	}
	
	public void setOptions(String name, Timer t,
						boolean currentOptionShowGraphs, boolean currentOptionWaitForLayout, Graph graph) {
		this.name = name;
		this.graph = graph;
		tref = t;
	}
	
	public void setAlgorithmDialog(RunAlgorithmDialog rad) {
		this.rad = rad;
	}
}
