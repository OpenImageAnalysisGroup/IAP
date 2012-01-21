/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 27.2.2006
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.ratioview;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.KeyStroke;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * @author Christian Klukas
 */
public class ExchangeNodePositionsAlgorithm
					extends AbstractAlgorithm {
	public String getName() {
		return "Exchange Positions";
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null)
			throw new PreconditionException("No graph available");
		if (selection.getNodes().size() < 2)
			throw new PreconditionException("At least two nodes need to be selected");
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0);
	}
	
	public void execute() {
		final Collection<Node> myNodeList = selection.getNodes();
		final Graph myGraph = graph;
		
		AbstractUndoableEdit myExchangeCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			HashMap<Node, Point2D> oldPositions = new HashMap<Node, Point2D>();
			
			@Override
			public String getPresentationName() {
				return "Node position exchange";
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo node position exchange";
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo node position exchange";
			}
			
			@Override
			public void redo() throws CannotRedoException {
				myGraph.getListenerManager().transactionStarted(this);
				try {
					ArrayList<NodeHelper> nhl = new ArrayList<NodeHelper>();
					for (Node n : myNodeList) {
						if (n.getGraph() != null)
							nhl.add(new NodeHelper(n));
					}
					for (int i = 0; i < nhl.size() - 1; i++) {
						NodeHelper n1 = nhl.get(i);
						NodeHelper n2 = nhl.get((i + 1) % nhl.size());
						Point2D oldPos = n1.getPosition();
						Point2D newPos = n2.getPosition();
						if (!oldPositions.containsKey(n1.getGraphNode()))
							oldPositions.put(n1.getGraphNode(), oldPos);
						if (!oldPositions.containsKey(n2.getGraphNode()))
							oldPositions.put(n2.getGraphNode(), newPos);
						n1.setPosition(newPos);
						n2.setPosition(oldPos);
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				myGraph.getListenerManager().transactionStarted(this);
				try {
					ArrayList<NodeHelper> nhl = new ArrayList<NodeHelper>();
					for (Node n : myNodeList) {
						if (n.getGraph() != null)
							nhl.add(new NodeHelper(n));
					}
					for (int i = 0; i < nhl.size(); i++) {
						NodeHelper n = nhl.get(i);
						Point2D oldPos = oldPositions.get(n.getGraphNode());
						if (oldPos != null)
							n.setPosition(oldPos);
					}
				} finally {
					myGraph.getListenerManager().transactionFinished(this);
				}
				oldPositions.clear();
			}
			
		};
		
		myExchangeCmd.redo();
		
		UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
		undo.beginUpdate();
		undo.postEdit(myExchangeCmd);
		undo.endUpdate();
	}
}
