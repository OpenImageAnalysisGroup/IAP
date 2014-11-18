/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: FindReplaceDialog.java,v 1.1 2011-01-31 08:59:36 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.SystemInfo;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.graffiti.selection.Selection;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class FindReplaceDialog
					extends AbstractAlgorithm {
	
	Selection selection;
	private static ArrayList<String> discardedSearchIDs = getDiscardedSearchIDs();
	
	/**
	 * Constructs a new instance.
	 */
	public FindReplaceDialog() {
	}
	
	private static ArrayList<String> getDiscardedSearchIDs() {
		ArrayList<String> result = new ArrayList<String>();
		result.add("red");
		result.add("green");
		result.add("blue");
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
		return KeyStroke.getKeyStroke('F', SystemInfo.getAccelModifier());
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
		
		enumerateAllAttributes(possibleAttributes, graph);
		
		final SearchDialog sd = new SearchDialog(MainFrame.getInstance(), possibleAttributes, true);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sd.setLocationRelativeTo(MainFrame.getInstance());
				sd.setVisible(true);
			}
		});
		return;
	}
	
	@SuppressWarnings("unchecked")
	private void enumerateAllAttributes(
						ArrayList<AttributePathNameSearchType> possibleAttributes, Graph graph) {
		for (Node n : graph.getNodes()) {
			enumerateAttributes(possibleAttributes, n);
		}
		for (Edge e : graph.getEdges()) {
			enumerateAttributes(possibleAttributes, e);
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
	
	private void enumerateAttributes(ArrayList<AttributePathNameSearchType> possibleAttributes, Attributable attr) {
		CollectionAttribute ca = attr.getAttributes();
		Stack<CollectionAttribute> catts = new Stack<CollectionAttribute>();
		catts.push(ca);
		while (!catts.empty()) {
			CollectionAttribute ccc = catts.pop();
			for (Object o : ccc.getCollection().values()) {
				if (o instanceof CollectionAttribute) {
					catts.push((CollectionAttribute) o);
				} else {
					Attribute a = (Attribute) o;
					SearchType st = null;
					if (a instanceof StringAttribute)
						st = SearchType.searchString;
					
					if (st != null && a.getId() != null) {
						if (!DefaultEditPanel.getDiscardedRowIDs().contains(a.getId())
											&& !discardedSearchIDs.contains(a.getId())) {
							String desc = AttributeHelper.getDefaultAttributeDescriptionFor(a.getId(),
												(attr instanceof Node) ? "Node" : "Edge", a);
							if (desc != null && desc.length() > 0) {
								if (!listContains(possibleAttributes, desc, attr instanceof Node, attr instanceof Edge)) {
									AttributePathNameSearchType newAtt = new AttributePathNameSearchType(a.getParent().getPath(), a.getName(),
														st, desc);
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
	
	private static boolean listContains(ArrayList<AttributePathNameSearchType> possibleAttributes,
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
		return "Find and Replace...";
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
	
}
