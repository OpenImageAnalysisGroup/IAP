/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 08.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class AlignNodesCommand extends AbstractUndoableEdit {
	public enum Command {
		jbHorB, jbHorC, jbHorT, jbVertC, jbVertL, jbVertR
	};
	
	private Command cmd;
	private EditorSession session;
	private String desc;
	
	private LinkedHashMap<Node, Vector2d> oldPositions = new LinkedHashMap<Node, Vector2d>();
	
	/**
	 * Creates a Alignment Command, used for aligning nodes.
	 * 
	 * @param cmd
	 *           The command to be carried out
	 * @param nodes
	 *           The node list to operate on, at least two nodes must be in the list.
	 */
	public AlignNodesCommand(Command cmd, EditorSession session) {
		this.cmd = cmd;
		this.session = session;
		desc = "Align ";
		if (cmd == Command.jbHorB)
			desc += "horzontal - bottom";
		if (cmd == Command.jbHorT)
			desc += "horzontal - top";
		if (cmd == Command.jbHorC)
			desc += "horzontal - center";
		if (cmd == Command.jbVertL)
			desc += "vertical - bottom";
		if (cmd == Command.jbVertR)
			desc += "vertical - right";
		if (cmd == Command.jbVertC)
			desc += "vertical - center";
	}
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public String getPresentationName() {
		return desc;
	}
	
	@Override
	public String getRedoPresentationName() {
		return "redo " + desc;
	}
	
	@Override
	public String getUndoPresentationName() {
		return "undo " + desc;
	}
	
	@Override
	public void redo() throws CannotRedoException {
		
		Graph graph = session.getGraph();
		
		Selection selection = session.getSelectionModel()
							.getActiveSelection();
		Collection<Node> nodes;
		if (selection == null || selection.isEmpty()) {
			nodes = graph.getNodes();
		} else {
			nodes = selection.getNodes();
		}
		
		if (nodes.size() < 2) {
			MainFrame.showMessageDialog(
								"Selection or graph must contain more than one node. Can not proceed.",
								"Error");
			return;
		}
		
		double smallestX = Double.MAX_VALUE;
		double smallestY = Double.MAX_VALUE;
		double greatestX = Double.NEGATIVE_INFINITY;
		double greatestY = Double.NEGATIVE_INFINITY;
		double sumX = 0;
		double sumY = 0;
		int nodeCnt = 0;
		
		double avgX, avgY;
		
		nodeCnt = nodes.size();
		for (Iterator<Node> nodeIterator = nodes.iterator(); nodeIterator.hasNext();) {
			Node currentNode = (Node) nodeIterator.next();
			double cx = AttributeHelper.getPositionX(currentNode);
			double cy = AttributeHelper.getPositionY(currentNode);
			Vector2d size = AttributeHelper.getSize(currentNode);
			smallestX = min2(smallestX, cx - size.x / 2);
			smallestY = min2(smallestY, cy - size.y / 2);
			greatestX = max2(greatestX, cx + size.x / 2);
			greatestY = max2(greatestY, cy + size.y / 2);
			sumX += cx;
			sumY += cy;
		}
		
		avgX = sumX / nodeCnt;
		avgY = sumY / nodeCnt;
		
		(nodes.iterator().next()).getGraph().getListenerManager().transactionStarted(this);
		
		// undoSupport.postEdit(new Undoa)
		
		for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
			Node currentNode = (Node) it.next();
			Vector2d size = AttributeHelper.getSize(currentNode);
			Vector2d pos = AttributeHelper.getPositionVec2d(currentNode);
			
			oldPositions.put(currentNode, pos);
			
			if (cmd == Command.jbHorT)
				AttributeHelper.setPosition(currentNode, pos.x, smallestY + size.y / 2d);
			
			if (cmd == Command.jbHorC)
				AttributeHelper.setPosition(currentNode, pos.x, avgY);
			
			if (cmd == Command.jbHorB)
				AttributeHelper.setPosition(currentNode, pos.x, greatestY - size.y / 2d);
			
			if (cmd == Command.jbVertL)
				AttributeHelper.setPosition(currentNode, smallestX + size.x / 2d, pos.y);
			
			if (cmd == Command.jbVertC)
				AttributeHelper.setPosition(currentNode, avgX, pos.y);
			
			if (cmd == Command.jbVertR)
				AttributeHelper.setPosition(currentNode, greatestX - size.x / 2d, pos.y);
		}
		(nodes.iterator().next()).getGraph().getListenerManager().transactionFinished(this);
	}
	
	@Override
	public void undo() throws CannotUndoException {
		session.getGraph().getNodes().get(0).getGraph().getListenerManager().
							transactionStarted(this);
		for (Iterator<Node> it = oldPositions.keySet().iterator(); it.hasNext();) {
			Node node = (Node) it.next();
			Graph graph = session.getGraph();
			if (graph.containsNode(node)) {
				Vector2d oldPos = oldPositions.get(node);
				AttributeHelper.setPosition(node, oldPos);
			}
		}
		session.getGraph().getNodes().get(0).getGraph().getListenerManager().
							transactionFinished(this);
	}
	
	/**
	 * @param greatestX
	 *           Value 1
	 * @param cx
	 *           Value 2
	 * @return Value 1 if it is greater than Value 2, otherwise Value2
	 */
	private double max2(double greatestX, double cx) {
		return greatestX > cx ? greatestX : cx;
	}
	
	/**
	 * @param smallestX
	 *           Value 1
	 * @param cx
	 *           Value 2
	 * @return The smaller one of the parameters
	 */
	private double min2(double smallestX, double cx) {
		return smallestX < cx ? smallestX : cx;
	}
}
