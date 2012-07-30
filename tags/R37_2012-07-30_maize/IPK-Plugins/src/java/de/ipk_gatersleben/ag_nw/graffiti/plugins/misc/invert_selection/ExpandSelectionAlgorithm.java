/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: ExpandSelectionAlgorithm.java,v 1.1 2011-01-31 08:59:35 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.ArrayList;

import javax.swing.KeyStroke;

import org.SystemInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class ExpandSelectionAlgorithm
					extends AbstractAlgorithm {
	
	Selection selection;
	private boolean directed, inverseDirected;
	
	/**
	 * Constructs a new instance.
	 */
	public ExpandSelectionAlgorithm(boolean directed, boolean inverseDirected) {
		this.directed = directed;
		this.inverseDirected = inverseDirected;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return null;
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
		Selection sel = new Selection("id");
		ArrayList<GraphElement> currentSelElements = new ArrayList<GraphElement>();
		selection = MainFrame.getInstance().getActiveEditorSession().
							getSelectionModel().getActiveSelection();
		graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		if (selection != null)
			currentSelElements.addAll(selection.getElements());
		for (GraphElement ge : currentSelElements) {
			if (ge instanceof Node) {
				Node n = (Node) ge;
				if (directed) {
					if (!inverseDirected) {
						for (Node neigh : n.getOutNeighbors())
							sel.add(neigh);
						for (Node neigh : n.getUndirectedNeighbors())
							sel.add(neigh);
						for (Edge e : n.getAllOutEdges())
							sel.add(e);
					} else {
						for (Node neigh : n.getInNeighbors())
							sel.add(neigh);
						for (Node neigh : n.getUndirectedNeighbors())
							sel.add(neigh);
						for (Edge e : n.getAllInEdges())
							sel.add(e);
					}
				} else {
					for (Node neigh : n.getNeighbors())
						sel.add(neigh);
					for (Edge e : n.getEdges())
						sel.add(e);
				}
			}
		}
		sel.addAll(currentSelElements);
		MainFrame.getInstance().getActiveEditorSession().
							getSelectionModel().setActiveSelection(sel);
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
		if (!inverseDirected || !directed)
			return "Extend Selection " + (directed ? "(Downstream)" : "(Undirected)");
		else
			return "Extend Selection (Upstream)";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		if (directed) {
			if (!inverseDirected)
				return KeyStroke.getKeyStroke('D', SystemInfo.getAccelModifier());
			else
				return KeyStroke.getKeyStroke('U', SystemInfo.getAccelModifier());
			// return KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.ALT_DOWN_MASK);
		} else
			// return KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
			return KeyStroke.getKeyStroke('E', SystemInfo.getAccelModifier());
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
