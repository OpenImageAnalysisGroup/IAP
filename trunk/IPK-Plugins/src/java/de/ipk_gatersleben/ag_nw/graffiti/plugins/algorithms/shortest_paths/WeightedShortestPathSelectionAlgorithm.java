/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: WeightedShortestPathSelectionAlgorithm.java,v 1.2 2012-11-07 14:47:54 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.KeyStroke;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.apache.commons.collections.set.ListOrderedSet;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * @author klukas
 */
public class WeightedShortestPathSelectionAlgorithm
					extends AbstractAlgorithm {
	
	Selection selection;
	
	private boolean settingDirected = true;
	private boolean considerEdgeWeight = true;
	private boolean considerNodeWeight = true;
	private boolean setAttribute = false;
	private boolean setLabel = false;
	private boolean putWeightOnEdges = false;
	private AttributePathNameSearchType weightattribute = null;
	
	/**
	 * Constructs a new instance.
	 */
	public WeightedShortestPathSelectionAlgorithm() {
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (selection == null || selection.getNumberOfNodes() < 2)
			throw new PreconditionException("at least one start and one end node has to be selected");
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		ArrayList<AttributePathNameSearchType> possibleAttributes = new ArrayList<AttributePathNameSearchType>();
		SearchAndSelecAlgorithm.enumerateAllAttributes(possibleAttributes, graph, SearchType.getSetOfNumericSearchTypes());
		return new Parameter[] {
							new BooleanParameter(settingDirected, "Consider Edge Direction", "If selected, the direction of a path is considered."),
							new BooleanParameter(considerNodeWeight, "Consider Node Weight",
												"If selected, the specified attribute will be evaluated during the processing."),
							new BooleanParameter(considerEdgeWeight, "Consider Edge Weight",
												"If selected, the specified attribute will be evaluated during the processing."),
							new ObjectListParameter(null, "Weight-Attribute", "The value of this attribute influences the weight of a path", possibleAttributes),
							new BooleanParameter(putWeightOnEdges, "Put Weight on Edges", "<html>" +
												"If no attribute value should be considered, the weight of a path may<br>" +
												"either be based on the number of edges or on the number of nodes."),
							new BooleanParameter(setAttribute, "Add Distance Attribute",
												"If enabled, a attribute will be added, which contains calculated distance information."),
							new BooleanParameter(setLabel, "Replace Label with Distance",
												"If enabled, edge and node labels will show calculated distance information."), };
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		settingDirected = ((BooleanParameter) params[i++]).getBoolean();
		considerNodeWeight = ((BooleanParameter) params[i++]).getBoolean();
		considerEdgeWeight = ((BooleanParameter) params[i++]).getBoolean();
		weightattribute = (AttributePathNameSearchType) ((ObjectListParameter) params[i++]).getValue();
		putWeightOnEdges = ((BooleanParameter) params[i++]).getBoolean();
		setAttribute = ((BooleanParameter) params[i++]).getBoolean();
		setLabel = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		Selection sel = new Selection("id");
		ArrayList<GraphElement> currentSelElements = new ArrayList<GraphElement>();
		selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
		graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		graph.numberGraphElements();
		if (selection != null)
			currentSelElements.addAll(selection.getElements());
		ListOrderedSet targetGraphElementsToBeProcessed = new ListOrderedSet();
		for (GraphElement ge : currentSelElements) {
			targetGraphElementsToBeProcessed.add(ge);
		}
		for (GraphElement ge : currentSelElements) {
			Collection<GraphElement> shortestPathNodesAndEdges = getShortestPathElements(
								graph.getGraphElements(),
								ge,
								targetGraphElementsToBeProcessed,
								settingDirected,
								considerNodeWeight,
								considerEdgeWeight,
								Double.MAX_VALUE,
								weightattribute,
								putWeightOnEdges,
								setAttribute,
								setLabel);
			sel.addAll(shortestPathNodesAndEdges);
			if (!settingDirected)
				targetGraphElementsToBeProcessed.remove(ge);
		}
		sel.addAll(currentSelElements);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<GraphElement> getShortestPathElements(
						Collection<GraphElement> validGraphElements,
						GraphElement startGraphElement,
						ListOrderedSet targetGraphElements,
						boolean directed,
						boolean considerNodeWeight,
						boolean considerEdgeWeight,
						double maxDistance,
						AttributePathNameSearchType weightattribute,
						boolean putWeightOnEdges,
						boolean setAttribute,
						boolean setLabel) {
		
		Queue<GraphElement> findTheseGraphElements = new LinkedList<GraphElement>();
		findTheseGraphElements.addAll(targetGraphElements);
		
		HashSet<GraphElement> elementsOfShortestPaths = new HashSet<GraphElement>();
		
		Queue<WeightedDistanceInfo> toDo = new LinkedList<WeightedDistanceInfo>();
		
		HashMap<GraphElement, WeightedDistanceInfo> node2distanceinfo = new HashMap<GraphElement, WeightedDistanceInfo>();
		
		WeightedDistanceInfo di = new WeightedDistanceInfo(
							0, startGraphElement, startGraphElement, considerNodeWeight, considerEdgeWeight,
							weightattribute, putWeightOnEdges, setAttribute);
		toDo.add(di);
		node2distanceinfo.put(startGraphElement, di);
		
		do {
			WeightedDistanceInfo currentProcessingUnit = toDo.remove();
			GraphElement currentGraphElement = currentProcessingUnit.getGraphElement();
			
			Collection<GraphElement> connectedGraphElements = currentProcessingUnit.getConnectedGraphElements(directed);
			
			for (GraphElement neighbour : connectedGraphElements) {
				if (!validGraphElements.contains(neighbour))
					continue;
				if (node2distanceinfo.containsKey(neighbour)) {
					WeightedDistanceInfo neighbourProcessingUnit = node2distanceinfo.get(neighbour);
					neighbourProcessingUnit.checkDistanceAndMemorizePossibleSourceElement(currentGraphElement, currentProcessingUnit.getMinDistance());
				} else {
					WeightedDistanceInfo newInfo = new WeightedDistanceInfo(
										currentProcessingUnit.getMinDistance(),
										currentGraphElement,
										neighbour,
										considerNodeWeight,
										considerEdgeWeight,
										weightattribute,
										putWeightOnEdges,
										setAttribute);
					if (newInfo.getMinDistance() <= maxDistance)
						toDo.add(newInfo);
					node2distanceinfo.put(neighbour, newInfo);
				}
				if (targetGraphElements.contains(neighbour)) {
					WeightedDistanceInfo thisEntity = node2distanceinfo.get(neighbour);
					if (thisEntity.allPossibleSourcePathsTraversed(directed))
						findTheseGraphElements.remove(neighbour);
				}
			}
		} while ((!toDo.isEmpty() && !findTheseGraphElements.isEmpty()));
		
		if (setLabel)
			for (GraphElement ge : node2distanceinfo.keySet()) {
				WeightedDistanceInfo pdi = node2distanceinfo.get(ge);
				AttributeHelper.setLabel(ge, (int) pdi.getMinDistance() + "");
			}
		if (setAttribute)
			for (GraphElement ge : node2distanceinfo.keySet()) {
				WeightedDistanceInfo pdi = node2distanceinfo.get(ge);
				AttributeHelper.setAttribute(ge, "properties", "shortestdistance", pdi.getMinDistance());
			}
		
		for (Object o : targetGraphElements) {
			GraphElement targetNode = (GraphElement) o;
			elementsOfShortestPaths.add(targetNode);
			if (node2distanceinfo.containsKey(targetNode)) {
				WeightedDistanceInfo distInfo = node2distanceinfo.get(targetNode);
				processDistanceInfoFromTargetToSource(
									node2distanceinfo,
									elementsOfShortestPaths,
									distInfo,
									directed,
									weightattribute);
			}
		}
		return elementsOfShortestPaths;
	}
	
	private static void processDistanceInfoFromTargetToSource(
						HashMap<GraphElement, WeightedDistanceInfo> node2distanceinfo,
						HashSet<GraphElement> elementsOfShortestPath,
						WeightedDistanceInfo distInfo,
						boolean directed,
						AttributePathNameSearchType weightAttribute) {
		elementsOfShortestPath.add(distInfo.getGraphElement());
		Collection<GraphElement> graphElementsWithMinimalDistance = distInfo.getSourceGraphElementsWithMinimalDistance();
		for (GraphElement sourceElement : graphElementsWithMinimalDistance) {
			if (elementsOfShortestPath.contains(sourceElement))
				continue;
			elementsOfShortestPath.add(sourceElement);
			if (sourceElement != distInfo.getGraphElement())
				if (node2distanceinfo.containsKey(sourceElement)) {
					WeightedDistanceInfo distanceInfoForSourceElement = node2distanceinfo.get(sourceElement); // process distance
					// recursively visit source elements from target to source
					processDistanceInfoFromTargetToSource(
										node2distanceinfo,
										elementsOfShortestPath,
										distanceInfoForSourceElement,
										directed,
										weightAttribute);
				}
		}
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		graph = null;
		selection = null;
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Use this command, to find the shortest path(s) between any<br>" +
							"selected graph elements (Nodes or Edges).<br><br>" +
							"If enabled, the &quot;weight&quot; of edges and nodes is considered.<br><br>" +
							"<small>" +
							"If neither node nor edge weight attribute values should be evaluated, each edge<br>" +
							"adds a weight of 1 to the path, if the setting &quot;Put Weight on Edges&quot;<br>" +
							"is selected. If this setting is unselected, each node in the path adds a weight<br>" +
							" of 1. Use the &quot;Add Attribute&quot; or &quot;Set Label&quot; settings, to enable a<br>" +
							"review of the calculated distances.<br><br>" +
							"Hint: Use the simpler shortest path selection command to select nodes or edges,<br>" +
							"for situations where graph element attribute values should not be considered.";
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Find Weighted Shortest Path...";
		else
			return null;
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
	}
	
	@Override
	public String getCategory() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "menu.edit";
		else
			return "Analysis";
	}
	
	public void setSelection(Selection selection) {
		this.selection = selection;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}
}
