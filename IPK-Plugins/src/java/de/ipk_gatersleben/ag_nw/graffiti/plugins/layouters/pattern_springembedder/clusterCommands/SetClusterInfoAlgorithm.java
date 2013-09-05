/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.HashSet;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

public class SetClusterInfoAlgorithm extends AbstractAlgorithm {
	
	private String currentValue = "";
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Enter and set cluster-ID";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"With this command you may assign a new or modified cluster Id to<br>" +
							"the selected nodes (or all nodes, if no node is selected).";
	}
	
	@Override
	public Parameter[] getParameters() {
		try {
			HashSet<String> ids = new HashSet<String>();
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				ids.add(NodeTools.getClusterID(ge, "-"));
			}
			currentValue = "";
			for (String s : ids) {
				if (currentValue.length() > 0)
					currentValue = currentValue + " / " + s;
				else
					currentValue = s;
			}
		} catch (Exception e) {
			// ignore
		}
		return new Parameter[] { new StringParameter(currentValue, "New Cluster ID", null) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		currentValue = ((StringParameter) params[i++]).getString();
	}
	
	@Override
	public String getCategory() {
		return "Elements"; // "menu.edit";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getNumberOfNodes() <= 0)
			throw new PreconditionException("Graph contains no graph elements!");
	}
	
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		try {
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				NodeTools.setClusterID(ge, currentValue);
			}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}