/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: WeightedShortestPathSelectionAlgorithm.java,v 1.8 2013-02-04 23:38:29 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * @author klukas
 */
public class WeightedShortestPathSelectionAlgorithm
		extends AbstractAlgorithm {
	
	private boolean settingDirected = true;
	private boolean considerEdgeWeight = true;
	private boolean considerNodeWeight = true;
	private boolean setAttribute = false;
	private boolean setLabel = false;
	private boolean putWeightOnEdges = false;
	private boolean allowMultiplePathsWithSameDistance = true;
	private AttributePathNameSearchType weightattribute = null;
	
	/**
	 * Constructs a new instance.
	 */
	public WeightedShortestPathSelectionAlgorithm() {
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
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
						"If enabled, edge and node labels will show calculated distance information."),
				new BooleanParameter(allowMultiplePathsWithSameDistance, "Allow Multiple Paths",
						"If enabled, multiple paths with the same minimum distance are found.") };
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
		allowMultiplePathsWithSameDistance = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	@Override
	public void execute() {
		Selection sel = new Selection("id");
		ArrayList<GraphElement> currentSelElements = new ArrayList<GraphElement>();
		selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
		graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		graph.numberGraphElements();
		if (selection != null)
			currentSelElements.addAll(selection.getElements());
		if (currentSelElements.size() < 2) {
			ArrayList<GraphElement> possibleSourceElements = new ArrayList<GraphElement>();
			possibleSourceElements.addAll(graph.getNodes());
			if (currentSelElements.size() == 1)
				currentSelElements.addAll(findLongestShortestPathStartAndEndPoints(currentSelElements.iterator().next(),
						possibleSourceElements, weightattribute, null));
			else
				currentSelElements.addAll(findLongestShortestPathStartAndEndPoints(possibleSourceElements,
						weightattribute, null));
		}
		
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
					setLabel,
					allowMultiplePathsWithSameDistance);
			sel.addAll(shortestPathNodesAndEdges);
			if (!settingDirected)
				targetGraphElementsToBeProcessed.remove(ge);
		}
		
		sel.addAll(currentSelElements);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
	}
	
	public static List<GraphElement> findLongestShortestPathStartAndEndPoints(
			Collection<GraphElement> possibleSourceElements, AttributePathNameSearchType wa,
			ThreadSafeOptions optLengthReturn) {
		return findLongestShortestPathStartAndEndPoints(null, possibleSourceElements, wa,
				optLengthReturn);
	}
	
	public static List<GraphElement> findLongestShortestPathStartAndEndPoints(
			GraphElement optStart, Collection<GraphElement> possibleSourceElements,
			AttributePathNameSearchType wa,
			ThreadSafeOptions optLengthReturn) {
		WeightedShortestPathSelectionAlgorithm wsp = new WeightedShortestPathSelectionAlgorithm();
		Selection sel = new Selection();
		wsp.considerEdgeWeight = true;
		wsp.considerNodeWeight = false;
		wsp.putWeightOnEdges = true;
		wsp.settingDirected = false;
		wsp.setSelection(sel);
		wsp.weightattribute = wa;
		wsp.allowMultiplePathsWithSameDistance = false;
		ArrayList<GraphElement> currentSelElements = new ArrayList<GraphElement>();
		if (optStart != null)
			currentSelElements.add(optStart);
		wsp.findLongestShortestPathElements(currentSelElements, possibleSourceElements, optLengthReturn);
		return currentSelElements;
	}
	
	public static List<GraphElement> findLongestShortestPathElements(
			Collection<GraphElement> possibleSourceElements, AttributePathNameSearchType wa,
			ThreadSafeOptions optLengthReturn) {
		WeightedShortestPathSelectionAlgorithm wsp = new WeightedShortestPathSelectionAlgorithm();
		Selection sel = new Selection();
		wsp.setSelection(sel);
		wsp.considerEdgeWeight = true;
		wsp.considerNodeWeight = false;
		wsp.putWeightOnEdges = true;
		wsp.settingDirected = false;
		wsp.weightattribute = wa;
		sel.addAll(findLongestShortestPathStartAndEndPoints(possibleSourceElements, wa, optLengthReturn));
		wsp.setSelection(sel);
		ListOrderedSet targetGraphElementsToBeProcessed = new ListOrderedSet();
		for (GraphElement ge : sel.getElements()) {
			targetGraphElementsToBeProcessed.add(ge);
		}
		for (GraphElement ge : new ArrayList<GraphElement>(sel.getElements())) {
			Collection<GraphElement> shortestPathNodesAndEdges = getShortestPathElements(
					possibleSourceElements,
					ge,
					targetGraphElementsToBeProcessed,
					false,
					false,
					true,
					Double.MAX_VALUE,
					wa,
					true,
					false,
					false,
					false);
			sel.addAll(shortestPathNodesAndEdges);
		}
		return sel.getElements();
	}
	
	private void findLongestShortestPathElements(
			Collection<GraphElement> currentSelElements, Collection<GraphElement> possibleSourceElements,
			ThreadSafeOptions optLengthReturn) {
		ArrayList<GraphElement> longestDistanceElements = new ArrayList<GraphElement>();
		double maximumShortestLen = -1;
		HashSet<GraphElement> startElements = new HashSet<GraphElement>();
		for (GraphElement sourceElement : possibleSourceElements) {
			startElements.add(sourceElement);
			for (GraphElement target : possibleSourceElements) {
				if (sourceElement == target)
					continue;
				if (startElements.contains(target))
					continue;
				ListOrderedSet targetGraphElementsToBeProcessed = new ListOrderedSet();
				targetGraphElementsToBeProcessed.add(target);
				
				ThreadSafeOptions retDist = new ThreadSafeOptions();
				retDist.setDouble(0);
				getShortestPathElements(
						possibleSourceElements,
						sourceElement,
						targetGraphElementsToBeProcessed,
						settingDirected,
						considerNodeWeight,
						considerEdgeWeight,
						Double.MAX_VALUE,
						weightattribute,
						putWeightOnEdges,
						false,
						false,
						false,
						retDist);
				// process shortestPathNodesAndEdges
				double pathLen = retDist.getDouble();
				boolean printDistance = false;
				if (printDistance)
					System.out.println("DISTANCE " + new GraphElementHelper(sourceElement).getLabel() + "/" +
							sourceElement + " <==> " + new GraphElementHelper(target).getLabel() + "/" + target
							+ " : "
							+ pathLen);
				if (pathLen > 0 && pathLen < Double.MAX_VALUE) {
					if (Math.abs(pathLen - maximumShortestLen) < 0.000001 && allowMultiplePathsWithSameDistance) {
						maximumShortestLen = pathLen;
						longestDistanceElements.add(sourceElement);
						longestDistanceElements.add(target);
						if (printDistance)
							System.out.println("RES DISTANCE " + new GraphElementHelper(sourceElement).getLabel() + " <==> "
									+ new GraphElementHelper(target).getLabel()
									+ " : "
									+ pathLen);
					} else
						if (pathLen > maximumShortestLen) {
							maximumShortestLen = pathLen;
							longestDistanceElements.clear();
							longestDistanceElements.add(sourceElement);
							longestDistanceElements.add(target);
							if (printDistance)
								System.out.println("RES DISTANCE " + new GraphElementHelper(sourceElement).getLabel() + " <==> "
										+ new GraphElementHelper(target).getLabel() + " : "
										+ pathLen);
						}
				}
			}
		}
		if (optLengthReturn != null)
			optLengthReturn.setDouble(maximumShortestLen);
		if (longestDistanceElements.size() > 0)
			currentSelElements.addAll(longestDistanceElements);
	}
	
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
			boolean setLabel,
			boolean allowMultiplePathsWithSameDistance) {
		return getShortestPathElements(validGraphElements, startGraphElement, targetGraphElements, directed,
				considerNodeWeight, considerEdgeWeight, maxDistance, weightattribute, putWeightOnEdges, setAttribute, setLabel,
				allowMultiplePathsWithSameDistance, null);
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
			boolean setLabel,
			boolean allowMultiplePathsWithSameDistance,
			ThreadSafeOptions optShortestDistanceReturn) {
		
		Queue<GraphElement> findTheseGraphElements = new LinkedList<GraphElement>();
		findTheseGraphElements.addAll(targetGraphElements);
		
		HashSet<GraphElement> elementsOfShortestPaths = new HashSet<GraphElement>();
		
		Queue<WeightedDistanceInfo> toDo = new LinkedList<WeightedDistanceInfo>();
		
		HashMap<GraphElement, WeightedDistanceInfo> graphelement2distanceinfo = new HashMap<GraphElement, WeightedDistanceInfo>();
		
		WeightedDistanceInfo di = new WeightedDistanceInfo(
				0, startGraphElement, startGraphElement, considerNodeWeight, considerEdgeWeight,
				weightattribute, putWeightOnEdges, setAttribute);
		toDo.add(di);
		graphelement2distanceinfo.put(startGraphElement, di);
		
		do {
			WeightedDistanceInfo currentProcessingUnit = toDo.remove();
			GraphElement currentGraphElement = currentProcessingUnit.getGraphElement();
			
			Collection<GraphElement> connectedGraphElements = currentProcessingUnit.getConnectedGraphElements(directed);
			
			for (GraphElement neighbour : connectedGraphElements) {
				if (!validGraphElements.contains(neighbour))
					continue;
				if (graphelement2distanceinfo.containsKey(neighbour)) {
					WeightedDistanceInfo neighbourProcessingUnit = graphelement2distanceinfo.get(neighbour);
					neighbourProcessingUnit.checkDistanceAndMemorizePossibleSourceElement(currentGraphElement, currentProcessingUnit.getMinDistance(),
							allowMultiplePathsWithSameDistance);
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
					graphelement2distanceinfo.put(neighbour, newInfo);
				}
				if (targetGraphElements.contains(neighbour)) {
					WeightedDistanceInfo thisEntity = graphelement2distanceinfo.get(neighbour);
					if (thisEntity.allPossibleSourcePathsTraversed(directed))
						findTheseGraphElements.remove(neighbour);
				}
			}
		} while ((!toDo.isEmpty() && !findTheseGraphElements.isEmpty()));
		
		if (setLabel)
			for (GraphElement ge : graphelement2distanceinfo.keySet()) {
				WeightedDistanceInfo pdi = graphelement2distanceinfo.get(ge);
				AttributeHelper.setLabel(ge, (int) pdi.getMinDistance() + "");
			}
		if (setAttribute)
			for (GraphElement ge : graphelement2distanceinfo.keySet()) {
				WeightedDistanceInfo pdi = graphelement2distanceinfo.get(ge);
				AttributeHelper.setAttribute(ge, "properties", "shortestdistance", pdi.getMinDistance());
			}
		
		for (Object o : targetGraphElements) {
			GraphElement targetNode = (GraphElement) o;
			elementsOfShortestPaths.add(targetNode);
			if (graphelement2distanceinfo.containsKey(targetNode)) {
				WeightedDistanceInfo distInfo = graphelement2distanceinfo.get(targetNode);
				processDistanceInfoFromTargetToSource(
						graphelement2distanceinfo,
						elementsOfShortestPaths,
						distInfo,
						directed,
						weightattribute,
						optShortestDistanceReturn);
			}
		}
		
		return elementsOfShortestPaths;
	}
	
	private static void processDistanceInfoFromTargetToSource(
			HashMap<GraphElement, WeightedDistanceInfo> node2distanceinfo,
			HashSet<GraphElement> elementsOfShortestPath,
			WeightedDistanceInfo distInfo,
			boolean directed,
			AttributePathNameSearchType weightAttribute, ThreadSafeOptions optShortestDistanceReturn) {
		elementsOfShortestPath.add(distInfo.getGraphElement());
		Collection<GraphElement> graphElementsWithMinimalDistance = distInfo.getSourceGraphElementsWithMinimalDistance();
		for (GraphElement sourceElement : graphElementsWithMinimalDistance) {
			if (elementsOfShortestPath.contains(sourceElement))
				continue;
			elementsOfShortestPath.add(sourceElement);
			if (sourceElement != distInfo.getGraphElement())
				if (node2distanceinfo.containsKey(sourceElement)) {
					WeightedDistanceInfo distanceInfoForSourceElement = node2distanceinfo.get(sourceElement); // process distance
					if (optShortestDistanceReturn != null)
						optShortestDistanceReturn.addDouble(distanceInfoForSourceElement.getMyDistancePenality());
					// recursively visit source elements from target to source
					processDistanceInfoFromTargetToSource(
							node2distanceinfo,
							elementsOfShortestPath,
							distanceInfoForSourceElement,
							directed,
							weightAttribute, optShortestDistanceReturn);
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
				"for situations where graph element attribute values should not be considered.<br><br>" +
				"Hint: Special functions: If only one node is selected, the most far ones are located.<br>" +
				"If no node is selected, the longest shortest path is determined.";
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	@Override
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
