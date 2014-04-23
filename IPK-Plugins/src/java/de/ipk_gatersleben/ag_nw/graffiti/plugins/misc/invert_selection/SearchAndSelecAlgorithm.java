/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: SearchAndSelecAlgorithm.java,v 1.1 2011-01-31 08:59:35 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Stack;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.SystemInfo;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.ByteAttribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.FloatAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.graffiti.selection.Selection;

import scenario.ScenarioServiceHandlesStoredParametersOption;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class SearchAndSelecAlgorithm extends AbstractEditorAlgorithm implements ScenarioServiceHandlesStoredParametersOption {
	
	Selection selection;
	
	private static ArrayList<String> discardedSearchIDs = getDiscardedSearchIDs();
	
	/**
	 * Constructs a new instance.
	 */
	public SearchAndSelecAlgorithm() {
	}
	
	private static ArrayList<String> getDiscardedSearchIDs() {
		ArrayList<String> result = new ArrayList<String>();
		// result.add("red");
		// result.add("green");
		// result.add("blue");
		result.add("transparency");
		return result;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return null;
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke('L', SystemInfo.getAccelModifier());
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		ArrayList<AttributePathNameSearchType> possibleAttributes = new ArrayList<AttributePathNameSearchType>();
		HashSet<SearchType> validSearchTypes = SearchType.getSetOfSearchTypes();
		
		enumerateAllAttributes(possibleAttributes, graph, validSearchTypes);
		
		final SearchDialog sd = new SearchDialog(getMainFrame(),
							possibleAttributes, false);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sd.setLocationRelativeTo(MainFrame.getInstance());
				sd.setVisible(true);
			}
		});
		return;
	}
	
	@SuppressWarnings("unchecked")
	public static void enumerateAllAttributes(
						ArrayList<AttributePathNameSearchType> possibleAttributes,
						Graph graph,
						HashSet<SearchType> validSearchTypes) {
		
		SearchAttributeHelper sah = new SearchAttributeHelper();
		
		try {
			sah.prepareSearch();
			for (Node n : graph.getNodes()) {
				enumerateAttributes(possibleAttributes, n, validSearchTypes);
			}
			for (Edge e : graph.getEdges()) {
				enumerateAttributes(possibleAttributes, e, validSearchTypes);
			}
			AttributePathNameSearchType[] sortedPossibleAttributes = possibleAttributes.toArray(new AttributePathNameSearchType[] {});
			Arrays.sort(sortedPossibleAttributes, new Comparator() {
				public int compare(Object o1, Object o2) {
					AttributePathNameSearchType a1 = (AttributePathNameSearchType) o1;
					AttributePathNameSearchType a2 = (AttributePathNameSearchType) o2;
					return a1.getNiceID().compareTo(a2.getNiceID());
				}
			});
			possibleAttributes.clear();
			for (AttributePathNameSearchType a : sortedPossibleAttributes)
				possibleAttributes.add(a);
		} finally {
			sah.restoreDefintions();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void enumerateAttributes(
						ArrayList<AttributePathNameSearchType> possibleAttributes,
						Collection graphElements,
						HashSet<SearchType> validSearchTypes) {
		for (GraphElement ge : (Collection<GraphElement>) graphElements) {
			enumerateAttributes(possibleAttributes, ge, validSearchTypes);
		}
		AttributePathNameSearchType[] sortedPossibleAttributes = possibleAttributes.toArray(new AttributePathNameSearchType[] {});
		Arrays.sort(sortedPossibleAttributes, new Comparator() {
			public int compare(Object o1, Object o2) {
				AttributePathNameSearchType a1 = (AttributePathNameSearchType) o1;
				AttributePathNameSearchType a2 = (AttributePathNameSearchType) o2;
				return a1.getNiceID().compareTo(a2.getNiceID());
			}
		});
		possibleAttributes.clear();
		for (AttributePathNameSearchType a : sortedPossibleAttributes)
			possibleAttributes.add(a);
	}
	
	public static void enumerateAttributes(
						ArrayList<AttributePathNameSearchType> possibleAttributes,
						Attributable attr, HashSet<SearchType> validSearchTypes) {
		CollectionAttribute ca = attr.getAttributes();
		Stack<CollectionAttribute> catts = new Stack<CollectionAttribute>();
		catts.push(ca);
		boolean searchColors = validSearchTypes.contains(SearchType.searchColor);
		while (!catts.empty()) {
			CollectionAttribute ccc = catts.pop();
			for (Object o : ccc.getCollection().values()) {
				Attribute a = (Attribute) o;
				if (a instanceof CollectionAttribute && !(a instanceof ColorAttribute && searchColors)) {
					catts.push((CollectionAttribute) o);
				} else {
					SearchType st = null;
					if (a instanceof StringAttribute)
						st = SearchType.searchString;
					if (a instanceof ByteAttribute)
						st = SearchType.searchInteger;
					if (a instanceof IntegerAttribute)
						st = SearchType.searchInteger;
					if (a instanceof BooleanAttribute)
						st = SearchType.searchBoolean;
					if (a instanceof DoubleAttribute)
						st = SearchType.searchDouble;
					if (a instanceof FloatAttribute)
						st = SearchType.searchDouble;
					if (a instanceof ColorAttribute)
						st = SearchType.searchColor;
					
					if (!(validSearchTypes.contains(st)))
						continue;
					if (st != null && a != null && a.getId() != null) {
						if (!DefaultEditPanel.getDiscardedRowIDs().contains(a.getId())
											&& !discardedSearchIDs.contains(a.getId())) {
							String desc = AttributeHelper.getDefaultAttributeDescriptionFor(a.getId(),
												(attr instanceof Node) ? "Node" : "Edge", a);
							if (desc != null && desc.trim().length() > 0) {
								desc = StringManipulationTools.stringReplace(desc, ":", ":  ");
								desc = StringManipulationTools.stringReplace(desc, "  ", " ");
								if (!listContains(possibleAttributes, desc,
													attr instanceof Node,
													attr instanceof Edge)) {
									AttributePathNameSearchType newAtt = new AttributePathNameSearchType(
														a.getParent().getPath(), a
																			.getName(), st, desc);
									newAtt.setInEdge(attr instanceof Edge);
									newAtt.setInNode(attr instanceof Node);
									possibleAttributes.add(newAtt);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static boolean listContains(
						ArrayList<AttributePathNameSearchType> possibleAttributes,
						String desc, boolean inNode, boolean inEdge) {
		boolean result = false;
		for (AttributePathNameSearchType a : possibleAttributes) {
			if (a.getNiceID().equals(desc)) {
				result = true;
				if (inNode)
					a.setInNode(true);
				if (inEdge)
					a.setInEdge(true);
				break;
			}
		}
		return result;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		graph = null;
		selection = null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Search...";
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
	/**
	 * Sets the selection on which the algorithm works.
	 * 
	 * @param selection
	 *           the selection
	 */
	public void setSelection(Selection selection) {
		this.selection = selection;
	}
	
	public boolean activeForView(View v) {
		return v != null;
	}
	
}
