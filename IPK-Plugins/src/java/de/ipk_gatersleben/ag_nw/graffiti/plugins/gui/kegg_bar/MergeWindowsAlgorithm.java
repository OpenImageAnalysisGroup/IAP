/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 13.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.session.Session;

public class MergeWindowsAlgorithm extends AbstractAlgorithm {
	
	private boolean setClusterInformation = true;
	private boolean closeSourceGraphs = false;
	private boolean setFileSrcAttribute = false;
	
	public String getName() {
		// if (ReleaseInfo.getRunningReleaseStatus()!=Release.KGML_EDITOR)
		return "Combine Open Networks";
		// else
		// return null;
	}
	
	@Override
	public Parameter[] getParameters() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return new Parameter[] {
								new BooleanParameter(setClusterInformation,
													"Set Node-Cluster Information",
													"<html>" +
																		"If enabled, the source node cluster information will be<br>" +
																		"overwritten with the corresponding graph identifiers.<br>" +
																		"This is useful, if the source graphs do not have cluster information<br>" +
																		"assigned, and if it is of desire to recognize the source graph in the<br>" +
																		"constructed super graph."),
								new BooleanParameter(closeSourceGraphs,
													"Close Graph Windows",
													"<html>" +
																		"If enabled, the graph windows will be closed."),
								new BooleanParameter(setFileSrcAttribute,
													"Set File-Src Attribute",
													"<html>" +
																		"If enabled, a new attribute, containing the elements<br>" +
																		"file-source will be added to the graph elements.")

			};
		else
			return new Parameter[] { new BooleanParameter(closeSourceGraphs,
								"Close Graph Windows",
								"<html>" +
													"If enabled, the graph windows will be closed.")

			};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			setClusterInformation = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		else
			setClusterInformation = false;
		closeSourceGraphs = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			setFileSrcAttribute = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		else
			setFileSrcAttribute = false;
	}
	
	@Override
	public String getCategory() {
		return "menu.window";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Create a new graph which contains all graph<br>" +
							"elements from the open graph windows";
	}
	
	public void execute() {
		// MainFrame.
		Graph newGraph = new AdjListGraph();
		newGraph.setName("Super Graph");
		Set<Session> session = new HashSet<Session>(MainFrame.getSessions());
		for (Session s : session) {
			Graph tempGraph = new AdjListGraph();
			tempGraph.addGraph(s.getGraph());
			String name = null;
			if (setClusterInformation || setFileSrcAttribute)
				name = s.getGraph().getName();
			if (setClusterInformation)
				for (GraphElement ge : tempGraph.getGraphElements())
					AttributeHelper.setAttribute(ge, "cluster", "cluster", name);
			
			if (setFileSrcAttribute)
				for (GraphElement ge : tempGraph.getGraphElements())
					AttributeHelper.setAttribute(ge, "src", "fileName", name);
			
			newGraph.addGraph(tempGraph);
			if (closeSourceGraphs) {
				MainFrame.getInstance().getSessionManager().closeSession(s);
			}
		}
		MainFrame.getInstance().showGraph(newGraph, getActionEvent());
	}
}
