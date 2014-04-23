/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.expand_reduce_space;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JMenuItem;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.AttributeConstants;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NeedsSwingThread;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.ContextMenuHelper;

/**
 * Transforms the layout by doing a calculation like x=x*1.4 and y=y*1.4 for
 * the nodes and bends positions.
 * 
 * @author Christian Klukas
 */
public class ExpandReduceLayouterAlgorithm extends AbstractAlgorithm
					implements ProvidesNodeContextMenu, NeedsSwingThread {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.AlgorithmWithNodeContextMenu#getCurrentNodeContextMenuItem(java.util.Collection)
	 */
	public JMenuItem[] getCurrentNodeContextMenuItem(Collection<Node> selectedNodes) {
		String sel = "selected";
		if (selectedNodes == null || selectedNodes.isEmpty()) {
			if (MainFrame.getInstance().getActiveSession() != null)
				selectedNodes = MainFrame.getInstance().getActiveSession().getGraph().getNodes();
			if (selectedNodes == null || selectedNodes.isEmpty())
				return null;
			sel = "all";
		}
		if (selectedNodes.size() == 1)
			return null;
		JMenuItem increaseSpace =
							new JMenuItem("Increase space between " + sel + " nodes");
		JMenuItem decreaseSpace =
							new JMenuItem("Decrease space between " + sel + " nodes");
		JMenuItem modifySpace =
							new JMenuItem("Increase space between " + sel + " nodes (parameterized)");
		
		final double factor = 1.5;
		
		final Collection<Node> selectedNodesF = selectedNodes;
		
		increaseSpace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// increase space between nodes
				doOperation(selectedNodesF, factor, factor, "Increase Space");
			}
		});
		decreaseSpace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOperation(selectedNodesF, 1 / factor, 1 / factor, "Decrease Space");
			}
		});
		modifySpace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double factorX = factor;
				double factorY = factor;
				Object[] result = MyInputHelper.getInput(
									"Please enter the X and Y factors<br>for distortion:",
									"Distortion Parameters",
									new Object[] {
														"X-factor", new Double(factorX),
														"Y-factor", new Double(factorY),
						}
									);
				if (result == null)
					return;
				doOperation(selectedNodesF, (Double) result[0], (Double) result[1], "X/Y transformation");
			}
		});
		
		return new JMenuItem[] { increaseSpace, decreaseSpace, modifySpace };
	}
	
	/**
	 * Transforms the Bends of an Edge that starts at a given node.
	 * Only Edges are transformed, that have the target in the selection.
	 * 
	 * @param node
	 * @param selectedNodes
	 */
	@SuppressWarnings("unchecked")
	static void TransformEdgesForThisNode(HashMap<CoordinateAttribute, Vector2d> bends2newPositions,
						Node node, Collection<Node> selectedNodes, double factorX, double factorY, Vector2d center) {
		Collection<?> edges = node.getEdges();
		for (Iterator<?> it = edges.iterator(); it.hasNext();) {
			Edge e = (Edge) it.next();
			if (selectedNodes.contains(e.getSource()) &&
								selectedNodes.contains(e.getTarget()) && e.getSource() == node) {
				LinkedHashMapAttribute ha =
									((LinkedHashMapAttribute)
									e.getAttribute(
														AttributeConstants.BENDS));
				Map<?, ?> m = ha.getCollection();
				for (Iterator<?> bi = m.entrySet().iterator(); bi.hasNext();) {
					// transform bends
					Map.Entry en = (Entry<?, ?>) bi.next();
					CoordinateAttribute co = (CoordinateAttribute) en.getValue();
					double x = (co.getX() - center.x) * factorX + center.x;
					double y = (co.getY() - center.y) * factorY + center.y;
					bends2newPositions.put(co, new Vector2d(x, y));
				}
			}
		}
	}
	
	// public static void expandSpace(Collection<Node> nodes, double factor, Vector2d center, double raster, Graph graph) {
	// graph.getListenerManager().transactionStarted(graph);
	// for (Node currentNode : nodes) {
	// Vector2d currentPosition =
	// AttributeHelper.getPositionVec2d(currentNode);
	// double posX = (currentPosition.x - center.x) / factor + center.x;
	// double posY = (currentPosition.y - center.y) / factor + center.y;
	// double epsilon = 0.00001;
	// if (raster>epsilon || raster < -epsilon) {
	// posX = posX - (posX % Math.abs(raster));
	// posY = posY - (posY % Math.abs(raster));
	// }
	// AttributeHelper.setPosition(
	// currentNode,
	// posX,
	// posY);
	//
	// TransformEdgesForThisNode(currentNode, nodes, 1/factor, center);
	// }
	// graph.getListenerManager().transactionFinished(graph);
	// }
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Expand or Reduce Node-Spacing";
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#setParameters(org.graffiti.plugin.parameter.Parameter[])
	 */
	@Override
	public void setParameters(Parameter[] params) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return null;
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
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		ContextMenuHelper.createAndShowContextMenuForAlgorithm(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		//
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#isLayoutAlgorithm()
	 */
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	public static void doOperation(final Collection<Node> selectedNodes, final double factorX, double factorY, String description) {
		Vector2d center;
		if (!selectedNodes.isEmpty()) {
			Node node1 = selectedNodes.iterator().next();
			if (node1.getGraph().getNodes().size() == selectedNodes.size()) {
				// all nodes are selected, in this case the center is set to 0,0 in order
				// to avoid the movement of a selection out of view
				// this can happen also for a selection, but this not as bad
				center = new Vector2d(0, 0);
			} else
				center = NodeTools.getCenter(selectedNodes);
		} else
			center = NodeTools.getCenter(selectedNodes);
		
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		HashMap<CoordinateAttribute, Vector2d> bends2newPositions = new HashMap<CoordinateAttribute, Vector2d>();
		
		for (Iterator<Node> it = selectedNodes.iterator(); it.hasNext();) {
			Node currentNode = it.next();
			Vector2d currentPosition =
								AttributeHelper.getPositionVec2d(currentNode);
			double x = (currentPosition.x - center.x) * factorX + center.x;
			double y = (currentPosition.y - center.y) * factorY + center.y;
			
			nodes2newPositions.put(currentNode, new Vector2d(x, y));
			
			TransformEdgesForThisNode(bends2newPositions, currentNode, selectedNodes, factorX, factorY, center);
		}
		GraphHelper.applyUndoableNodeAndBendPositionUpdate(nodes2newPositions, bends2newPositions, description);
	}
}
