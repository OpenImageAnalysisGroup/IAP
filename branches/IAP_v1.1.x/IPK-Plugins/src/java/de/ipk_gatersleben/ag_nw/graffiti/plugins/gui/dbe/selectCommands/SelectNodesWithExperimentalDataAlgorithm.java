/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class SelectNodesWithExperimentalDataAlgorithm extends AbstractAlgorithm {
	
	private boolean extendSelection = true;
	private boolean onlyWithMapping = false;
	private boolean onlyWithoutMapping = false;
	private int maximumDegree = -1;
	private int minimumDegree = -1;
	private boolean onlyConnectedToSelectedEdges = false;
	private boolean onlyVisibleNodes = false;
	private boolean onlyHiddenNodes = false;
	
	private int minDistance = -1;
	private int maxDistance = -1;
	private boolean directedDistanceCalculation = true;
	private boolean selectUnconnected = false;
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active graph editor window found!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Select Nodes...";
	}
	
	@Override
	public String getDescription() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING)) {
			return "<html>" +
								"Select at least one of the checkboxes in order to limit<br>" +
								"the node selection to nodes which meet the checked<br>" +
								"parameters.<br><br>" +
								"<b>Use default-settings (checkboxes unchecked and<br>" +
								"degree and distance limits (if avail.) = -1), to<br>" +
								"select all nodes.</b><br><br>";
		} else {
			return "<html><small>You may limit the node selection with a minimum and maximum<br>" +
								"node-degree setting (number of connections to other nodes).<br><br>" +
								"<b>Leave degree limits unchanged to select all nodes</b><br><br>";
		}
	}
	
	@Override
	public Parameter[] getParameters() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING)) {
			boolean distEvalOk = selection != null && selection.getNodes().size() > 0;
			if (!distEvalOk) {
				minDistance = -1;
				maxDistance = -1;
			}
			return new Parameter[] {
								selection.isEmpty() ? null : new BooleanParameter(extendSelection, "Extend selection", "<html>" +
													"If selected, the selection will be extended,<br>" +
													"leaving currently selected elements unaffected."),
								new BooleanParameter(onlyWithMapping, "With mapping-data", ""),
								new BooleanParameter(onlyWithoutMapping, "Without mapping-data", ""),
								new IntegerParameter(-1, "Degree >", "Degree greater than (-1 means no limit)"),
								new IntegerParameter(-1, "Degree <", "Degree smaller than (-1 means no limit)"),
								selection.getEdges().isEmpty() ? null : new BooleanParameter(onlyConnectedToSelectedEdges, "Connected to sel. edges", ""),
								new BooleanParameter(onlyVisibleNodes, "Visible nodes", ""),
								new BooleanParameter(onlyHiddenNodes, "Hidden nodes", ""),
								distEvalOk ? new IntegerParameter(-1, "Distance to sel. >", "Distance to selection greater than (-1 means not considered)") : null,
									distEvalOk ? new IntegerParameter(-1, "Distance to sel. <", "Distance to selection smaller than (-1 means not considered)") : null,
											distEvalOk ? new BooleanParameter(directedDistanceCalculation, "<html><small>^^^ consider edge-directions", "") : null,
													distEvalOk ? new BooleanParameter(selectUnconnected, "<html><small>^^^ process non-reachable (dist=&infin;)", "") : null, };
		} else {
			return new Parameter[] {
								new IntegerParameter(-1, "Degree greater than (-1 = no limit)", "Degree >"),
								new IntegerParameter(-1, "Degree smaller than (-1 = no limit)", "Degree <") };
		}
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING)) {
			extendSelection = params[i] == null ? (i++ > 0) : ((BooleanParameter) params[i++]).getBoolean();
			onlyWithMapping = ((BooleanParameter) params[i++]).getBoolean();
			onlyWithoutMapping = ((BooleanParameter) params[i++]).getBoolean();
			minimumDegree = ((IntegerParameter) params[i++]).getInteger();
			maximumDegree = ((IntegerParameter) params[i++]).getInteger();
			onlyConnectedToSelectedEdges = params[i] == null ? (i++ < 0) : ((BooleanParameter) params[i++]).getBoolean();
			onlyVisibleNodes = ((BooleanParameter) params[i++]).getBoolean();
			onlyHiddenNodes = ((BooleanParameter) params[i++]).getBoolean();
			if (params[i] == null) {
				minDistance = -1;
				i++;
			} else
				minDistance = ((IntegerParameter) params[i++]).getInteger();
			if (params[i] == null) {
				maxDistance = -1;
				i++;
			} else
				maxDistance = ((IntegerParameter) params[i++]).getInteger();
			if (params[i] == null) {
				i++;
				directedDistanceCalculation = false;
			} else
				directedDistanceCalculation = ((BooleanParameter) params[i++]).getBoolean();
			if (params[i] == null) {
				i++;
				selectUnconnected = false;
			} else
				selectUnconnected = ((BooleanParameter) params[i++]).getBoolean();
		} else {
			extendSelection = true;
			onlyWithMapping = false;
			onlyWithoutMapping = false;
			minimumDegree = ((IntegerParameter) params[i++]).getInteger();
			maximumDegree = ((IntegerParameter) params[i++]).getInteger();
			onlyConnectedToSelectedEdges = false;
			onlyVisibleNodes = false;
			onlyHiddenNodes = false;
			minDistance = -1;
			maxDistance = -1;
			selectUnconnected = false;
		}
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		ArrayList<Node> validNodes = new ArrayList<Node>();
		HashSet<Node> initalSelection = new HashSet<Node>();
		if (minDistance >= 0 || maxDistance >= 0)
			initalSelection.addAll(selection.getNodes());
		if (onlyWithMapping || onlyWithoutMapping || maximumDegree >= 0 || minimumDegree >= 0
							|| onlyHiddenNodes || onlyVisibleNodes || minDistance >= 0 || maxDistance >= 0) {
			HashMap<Node, Integer> node2distance = new HashMap<Node, Integer>();
			if (minDistance >= 0 || maxDistance >= 0)
				GraphHelper.getShortestDistances(node2distance, initalSelection, directedDistanceCalculation, 0);
			for (Node n : getNodesLimitedByEdgeSelectionSetting(graph)) {
				Integer dist = null;
				if (maxDistance >= 0 || minDistance >= 0)
					dist = node2distance.get(n);
				if (maxDistance >= 0 && (dist == null || dist >= maxDistance))
					continue;
				if (selectUnconnected) {
					if (minDistance >= 0 && (dist != null && dist <= minDistance))
						continue;
				} else {
					if (minDistance >= 0 && (dist == null || dist <= minDistance))
						continue;
				}
				
				if (maximumDegree >= 0 && n.getDegree() >= maximumDegree)
					continue;
				if (minimumDegree >= 0 && n.getDegree() <= minimumDegree)
					continue;
				if (!(onlyWithMapping || onlyWithoutMapping))
					validNodes.add(n);
				else {
					try {
						Attribute a = n.getAttribute(Experiment2GraphHelper.mapFolder + Attribute.SEPARATOR + Experiment2GraphHelper.mapVarName);
						if (a != null && onlyWithMapping)
							validNodes.add(n);
					} catch (AttributeNotFoundException anfe) {
						if (onlyWithoutMapping)
							validNodes.add(n);
					}
				}
			}
			int oldCnt = selection.getNodes().size();
			if (onlyVisibleNodes || onlyHiddenNodes) {
				HashSet<Node> invalidNodes = new HashSet<Node>();
				for (Node n : validNodes) {
					if (onlyVisibleNodes && AttributeHelper.isHiddenGraphElement(n))
						invalidNodes.add(n);
					if (onlyHiddenNodes && !AttributeHelper.isHiddenGraphElement(n))
						invalidNodes.add(n);
				}
				validNodes.removeAll(invalidNodes);
			}
			if (!extendSelection)
				selection.clear();
			selection.addAll(validNodes);
			MainFrame.showMessage("Added " + (selection.getNodes().size() - oldCnt) + " node(s) to selection (" + validNodes.size() + " met selected criteria)",
								MessageType.INFO);
		} else {
			int oldCnt = selection.getNodes().size();
			if (!extendSelection)
				selection.clear();
			selection.addAll(getNodesLimitedByEdgeSelectionSetting(graph));
			if (!extendSelection)
				MainFrame.showMessage("New selection: " + (selection.getNodes().size() - oldCnt) + " node(s)", MessageType.INFO);
			else
				MainFrame.showMessage("Added " + (selection.getNodes().size() - oldCnt) + " node(s) to selection", MessageType.INFO);
		}
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
	}
	
	private Collection<Node> getNodesLimitedByEdgeSelectionSetting(Graph graph) {
		if (!onlyConnectedToSelectedEdges)
			return graph.getNodes();
		else {
			HashSet<Node> result = new HashSet<Node>();
			for (Edge e : selection.getEdges()) {
				result.add(e.getSource());
				result.add(e.getTarget());
			}
			return result;
		}
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
