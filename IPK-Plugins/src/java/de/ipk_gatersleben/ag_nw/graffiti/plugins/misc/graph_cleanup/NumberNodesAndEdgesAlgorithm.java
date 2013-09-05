/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: NumberNodesAndEdgesAlgorithm.java,v 1.1 2011-01-31 09:00:59 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.graph_cleanup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesEdgeContextMenu;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class NumberNodesAndEdgesAlgorithm extends AbstractAlgorithm
					implements ProvidesEdgeContextMenu,
					ProvidesNodeContextMenu {
	
	JMenuItem numberNodes;
	JMenuItem numberEdges;
	
	Collection<Node> currentNodes;
	Collection<Edge> currentEdges;
	
	/**
	 * Constructs a new instance.
	 */
	public NumberNodesAndEdgesAlgorithm() {
		numberNodes = new JMenuItem("Number Nodes");
		numberEdges = new JMenuItem("Number Edges");
		numberNodes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int i = 1;
				if (currentNodes != null)
					for (Iterator<Node> it = currentNodes.iterator(); it.hasNext();) {
						Node n = (Node) it.next();
						AttributeHelper.setLabel(n, new Integer(i).toString());
						i++;
					}
			}
		});
		
		numberEdges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int i = 1;
				if (currentEdges != null)
					for (Iterator<Edge> it = currentEdges.iterator(); it.hasNext();) {
						Edge e = (Edge) it.next();
						AttributeHelper.setLabel(e, new Integer(i).toString());
						i++;
					}
			}
		});
	}
	
	@Override
	public String getCategory() {
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
		JOptionPane.showMessageDialog(null,
							"<html>Please use the context menu for numbering of nodes or edges.<p>");
		
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.AlgorithmWithEdgeContextMenu#getCurrentEdgeContextMenuItem(java.util.Collection)
	 */
	public JMenuItem[] getCurrentEdgeContextMenuItem(Collection<Edge> selectedEdges) {
		currentEdges = selectedEdges;
		if (selectedEdges.size() > 0)
			return new JMenuItem[] { numberEdges };
		else
			return new JMenuItem[] {};
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		//
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#attach(org.graffiti.graph.Graph)
	 */
	@Override
	public void attach(Graph g, Selection s) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#check()
	 */
	@Override
	public void check() {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.AlgorithmWithNodeContextMenu#getCurrentNodeContextMenuItem(java.util.Collection)
	 */
	public JMenuItem[] getCurrentNodeContextMenuItem(Collection<Node> selectedNodes) {
		currentNodes = selectedNodes;
		if (selectedNodes.size() > 0)
			return new JMenuItem[] { numberNodes };
		else
			return new JMenuItem[] {};
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#isLayoutAlgorithm()
	 */
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
}
