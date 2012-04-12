/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.radial_tree;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.NodeParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.TreeContainer;

/**
 * An implementation of a radial tree layout algorithm.
 * 
 * @author Joerg Bartelheimer
 */

/* TODO: Layout of non-horizontal/vertical grids */
@SuppressWarnings("unchecked")
public class RadialTreeLayout extends AbstractAlgorithm {
	
	/*************************************************************/
	/* Member variables */
	/*************************************************************/
	
	/**
	 * Dynamical defined edge bend attribute.
	 */
	private final String BENDS =
						GraphicAttributeConstants.GRAPHICS
											+ Attribute.SEPARATOR
											+ GraphicAttributeConstants.BENDS;
	/**
	 * Dynamical defined node coordinate attribute.
	 */
	private final String COORDSTR =
						GraphicAttributeConstants.GRAPHICS
											+ Attribute.SEPARATOR
											+ GraphicAttributeConstants.COORDINATE;
	
	/**
	 * Dynamical defined node dimension attribute.
	 */
	private final String DIMENSIONSTR =
						GraphicAttributeConstants.GRAPHICS
											+ Attribute.SEPARATOR
											+ GraphicAttributeConstants.DIMENSION;
	
	/**
	 * Distance of each node from the center
	 */
	private double nodeDistance = 100;
	
	/**
	 * Horizontal distance between trees
	 */
	private double xDistance = 30;
	
	/**
	 * Vertical distance between trees
	 */
	private double yDistance = 30;
	
	/**
	 * The maximum y dimension for each node.
	 */
	private HashMap maxNodeHeight = new HashMap();
	
	/**
	 * Put all trees in a row
	 */
	private boolean horizontalLayout = true;
	
	/**
	 * Move all tree in the right direction either 0, 90, 180 or 270 degree
	 */
	private int treeDirection = 0;
	
	/**
	 * Remove all bends
	 */
	private boolean doRemoveBends = true;
	
	/**
	 * Activate source node red backround
	 */
	private boolean doMarkSourceNode = true;
	
	/**
	 * x coordinate of start point
	 */
	private double xStart = 100;
	
	/**
	 * y coordinate of start point
	 */
	private double yStart = 100;
	
	/**
	 * All trees are initialized by this variable.
	 */
	private HashMap forest;
	
	/**
	 * The roots in the forest.
	 */
	private HashMap sourceNodes = new HashMap();
	
	/**
	 * The root node of the tree.
	 */
	private Node sourceNode = null;
	
	/**
	 * The depth for each node .
	 */
	private HashMap bfsNum = new HashMap();
	
	/**
	 * If there are a circle edges, save them here
	 */
	private LinkedList tempEdges = new LinkedList();
	
	/**
	 * Sum of all children
	 */
	private HashMap magnitude = new HashMap();
	
	/**
	 * x coordinate of start point
	 */
	private double xStartParam = 100;
	
	/**
	 * y coordinate of start point
	 */
	private double yStartParam = 100;
	
	/*************************************************************/
	/* Declarations of methods */
	/*************************************************************/
	
	/**
	 * Construct a new RadialTreeLayout algorithm instance.
	 */
	public RadialTreeLayout() {
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#check()
	 */
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getNumberOfNodes() <= 0) {
			throw new PreconditionException("The graph is empty. Cannot run tree layouter.");
		}
		
	}
	
	/**
	 * Check whether the tree is rooted.
	 */
	public boolean rootedTree(Node rootNode) {
		
		int roots = 0;
		for (Iterator iterator = bfsNum.keySet().iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			/* maybe there is a second */
			if ((node.getInDegree() == 0) && (node.getOutDegree() > 0)) {
				if (roots == 0) {
					roots++;
				} else {
					// return false;
				}
			}
			int ancestors = 0;
			for (Iterator neighbourEdges = node.getEdgesIterator(); neighbourEdges.hasNext();) {
				
				Edge neighbourEdge = (Edge) neighbourEdges.next();
				Node neighbour = null;
				if (neighbourEdge.getSource() == node) {
					neighbour = neighbourEdge.getTarget();
				} else {
					neighbour = neighbourEdge.getSource();
				}
				
				/* any links from upper level nodes more than once ? */
				if (((Integer) bfsNum.get(node)).intValue() > ((Integer) bfsNum.get(neighbour)).intValue()) {
					ancestors++;
					if (ancestors > 1) {
						tempEdges.add(neighbourEdge);
						graph.deleteEdge(neighbourEdge);
					}
				}
				/* any links from same level nodes ? */
				if (((Integer) bfsNum.get(node)).intValue() == ((Integer) bfsNum.get(neighbour)).intValue()) {
					
					tempEdges.add(neighbourEdge);
					graph.deleteEdge(neighbourEdge);
					/*
					 * old rooted check routine
					 * ancestors++;
					 * if (ancestors > 1) {
					 * return false;
					 * }
					 */
				}
			}
		}
		return true;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		Node initNode = null;
		if (!selection.getNodes().isEmpty())
			initNode = selection.getNodes().iterator().next();
		else
			initNode = graph.getNodes().iterator().next();
		
		NodeParameter nodeParam = new NodeParameter(graph, initNode, "Start-Node",
							"Tree layouter will start only with a selected node.");
		
		DoubleParameter distanceParam =
							new DoubleParameter(
												"Node Radius",
												"The distance from the center of each node");
		
		DoubleParameter xStartParam =
							new DoubleParameter(
												"X base",
												"The x coordinate of the starting point of the grid horizontal direction.");
		
		DoubleParameter yStartParam =
							new DoubleParameter(
												"Y base",
												"The y coordinate of the starting point of the grid horizontal direction.");
		
		BooleanParameter horizontalParam =
							new BooleanParameter(
												horizontalLayout,
												"Place Trees in a Row",
												"Place all trees in a row");
		
		BooleanParameter removeBendParam =
							new BooleanParameter(
												doRemoveBends,
												"Remove Bends",
												"Remove all bends in the forest");
		
		BooleanParameter markedSourceNodeParam =
							new BooleanParameter(
												doMarkSourceNode,
												"Mark Start-Node",
												"Mark each source Node");
		
		IntegerParameter treeDirectionParam =
							new IntegerParameter(
												treeDirection,
												"Tree Direction (0,90,180,270)",
												"Move all trees in 0, 90, 180 or 270 degree");
		
		distanceParam.setDouble(nodeDistance);
		xStartParam.setDouble(this.xStartParam);
		yStartParam.setDouble(this.yStartParam);
		
		return new Parameter[] {
							nodeParam,
							distanceParam,
							xStartParam,
							yStartParam,
							horizontalParam,
							removeBendParam,
							markedSourceNodeParam,
							treeDirectionParam };
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		
		Node n = ((NodeParameter) params[0]).getNode();
		selection.clear();
		selection.add(n);
		System.out.println("Node: " + AttributeHelper.getLabel(n, "- unnamed -"));
		
		nodeDistance = ((DoubleParameter) params[1]).getDouble().doubleValue();
		xStart = ((DoubleParameter) params[2]).getDouble().doubleValue();
		yStart = ((DoubleParameter) params[3]).getDouble().doubleValue();
		xStartParam = ((DoubleParameter) params[2]).getDouble().doubleValue();
		yStartParam = ((DoubleParameter) params[3]).getDouble().doubleValue();
		horizontalLayout =
							((BooleanParameter) params[4]).getBoolean().booleanValue();
		doRemoveBends =
							((BooleanParameter) params[5]).getBoolean().booleanValue();
		doMarkSourceNode =
							((BooleanParameter) params[6]).getBoolean().booleanValue();
		treeDirection =
							((IntegerParameter) params[7]).getInteger().intValue();
		if (!((treeDirection == 0) || (treeDirection == 180) || (treeDirection == 270) || (treeDirection == 90))) {
			treeDirection = 0;
		}
	}
	
	/**
	 * Return a postordered tree
	 * 
	 * @param root
	 * @return a postordered tree
	 */
	private LinkedList postorder(Node root) {
		LinkedList result = new LinkedList();
		postorderTraverse(null, root, result);
		return result;
	}
	
	/**
	 * Return a postordered tree
	 * 
	 * @param root
	 * @return a preordered tree
	 */
	private LinkedList preorder(Node root) {
		LinkedList result = new LinkedList();
		preorderTraverse(null, root, result);
		return result;
	}
	
	/**
	 * Traverse the tree in postorder
	 * 
	 * @param ancestor
	 *           - from node
	 * @param node
	 *           - start node
	 * @param lq
	 *           - result is a LinkedList
	 */
	private void postorderTraverse(Node ancestor, Node node, LinkedList lq) {
		for (Iterator neighbors = node.getNeighborsIterator(); neighbors.hasNext();) {
			Node neighbor = (Node) neighbors.next();
			if (neighbor != ancestor) {
				postorderTraverse(node, neighbor, lq);
			}
		}
		lq.addLast(node);
	}
	
	/**
	 * Traverse the tree in preorder
	 * 
	 * @param ancestor
	 *           - from node
	 * @param node
	 *           - start node
	 * @param lq
	 *           - result is a LinkedList
	 */
	private void preorderTraverse(Node ancestor, Node node, LinkedList lq) {
		lq.addLast(node);
		for (Iterator neighbors = node.getNeighborsIterator(); neighbors.hasNext();) {
			Node neighbor = (Node) neighbors.next();
			if (neighbor != ancestor) {
				preorderTraverse(node, neighbor, lq);
			}
		}
		
	}
	
	/**
	 * Get the successors of the given node
	 * 
	 * @param node
	 * @return
	 */
	private Iterator getSuccessors(Node node) {
		LinkedList result = new LinkedList();
		
		for (Iterator neighbors = node.getNeighborsIterator(); neighbors.hasNext();) {
			Node neighbor = (Node) neighbors.next();
			if (((Integer) bfsNum.get(node)).intValue() < ((Integer) bfsNum.get(neighbor)).intValue()) {
				result.add(neighbor);
			}
		}
		return result.iterator();
	}
	
	/**
	 * Get the predecessors of the given node
	 * 
	 * @param node
	 * @return
	 */
	private Iterator getPredecessors(Node node) {
		LinkedList result = new LinkedList();
		
		for (Iterator neighbors = node.getNeighborsIterator(); neighbors.hasNext();) {
			Node neighbor = (Node) neighbors.next();
			if (((Integer) bfsNum.get(node)).intValue() > ((Integer) bfsNum.get(neighbor)).intValue()) {
				result.add(neighbor);
			}
		}
		return result.iterator();
	}
	
	/**
	 * Init for each node the sum of all children and sub children
	 */
	protected void initMagnitude() {
		magnitude = new HashMap();
		
		for (Iterator it = postorder(sourceNode).iterator(); it.hasNext();) {
			
			Node node = (Node) it.next();
			int nodeValue = 1;
			if (magnitude.get(node) != null) {
				nodeValue = ((Integer) magnitude.get(node)).intValue();
			} else {
				magnitude.put(node, new Integer(1));
			}
			
			for (Iterator it2 = getPredecessors(node)/* node.getInNeighborsIterator() */; it2.hasNext();) {
				Node neighbour = (Node) it2.next();
				int sum = nodeValue;
				if (magnitude.get(neighbour) != null) {
					sum += ((Integer) magnitude.get(neighbour)).intValue();
				}
				
				magnitude.put(neighbour, new Integer(sum));
			}
			
		}
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute(Graph)
	 *      The given graph must have at least one node.
	 */
	public void execute() {
		GravistoService.getInstance().algorithmAttachData(this);
		
		tempEdges = new LinkedList();
		
		sourceNodes = new HashMap();
		
		forest = new HashMap();
		for (Iterator iterator = graph.getNodesIterator(); iterator.hasNext();) {
			forest.put(iterator.next(), null);
		}
		
		/* check all trees with selected nodes, whether they have one root */

		for (Iterator iterator = selection.getNodes().iterator(); iterator.hasNext();) {
			
			sourceNode = (Node) iterator.next();
			
			/* ignore multiple selection */
			if (forest.containsKey(sourceNode)) {
				/* check circle connection by using the depth of each node */
				computeDepth(sourceNode);
				sourceNodes.put(
									sourceNode,
									new TreeContainer(
														bfsNum,
														maxNodeHeight));
				
				if (!rootedTree(sourceNode)) {
					ErrorMsg.addErrorMessage("The given graph is not a tree.");
				}
			}
		}
		
		/* check the trees whether they have one root */
		while (forest.keySet().iterator().hasNext()) {
			
			sourceNode = (Node) forest.keySet().iterator().next();
			
			/* check circle connection by using the depth of each node */
			computeDepth(sourceNode);
			sourceNodes.put(
								sourceNode,
								new TreeContainer(
													bfsNum, maxNodeHeight));
			
			if (!rootedTree(sourceNode)) {
				for (Iterator it = tempEdges.iterator(); it.hasNext();) {
					Edge edge = (Edge) it.next();
					graph.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
				}
				ErrorMsg.addErrorMessage("The given graph has trees with multiple roots.");
			}
			
			/* in case of arrows, try to find the root */
			Node node = null;
			for (Iterator iterator = preorder(sourceNode).iterator(); iterator.hasNext();) {
				node = (Node) iterator.next();
				if ((node.getInDegree() == 0) && (node.getOutDegree() > 0)) {
					
					sourceNodes.remove(sourceNode);
					
					sourceNode = node;
					
					computeDepth(sourceNode);
					
					sourceNodes.put(
										sourceNode,
										new TreeContainer(
															bfsNum, maxNodeHeight));
					
					break;
				}
			}
			
		}
		
		graph.getListenerManager().transactionStarted(this);
		
		if (doRemoveBends) {
			removeAllBends();
		}
		
		for (Iterator iterator = sourceNodes.keySet().iterator(); iterator.hasNext();) {
			
			sourceNode = (Node) iterator.next();
			
			if (doMarkSourceNode)
				AttributeHelper.setFillColor(sourceNode, Color.RED);
			
			bfsNum = ((TreeContainer) sourceNodes.get(sourceNode)).getBfsNum();
			maxNodeHeight =
								((TreeContainer) sourceNodes.get(sourceNode))
													.getMaxNodeHeight();
			
			/* compute segments of the tree */
			initMagnitude();
			
			/* compute positions */
			computePositions();
			
			/* place the tree on its position */
			if (horizontalLayout) {
				if ((treeDirection == 180) || (treeDirection == 0)) {
					xStart += maxNodeHeight.size() * nodeDistance * 2 + xDistance;
				} else {
					yStart += maxNodeHeight.size() * nodeDistance * 2 + yDistance;
				}
			} else {
				if ((treeDirection == 180) || (treeDirection == 0)) {
					yStart += maxNodeHeight.size() * nodeDistance * 2 + yDistance;
				} else {
					xStart += maxNodeHeight.size() * nodeDistance * 2 + xDistance;
				}
			}
			
		}
		for (Iterator iterator = tempEdges.iterator(); iterator.hasNext();) {
			Edge edge = (Edge) iterator.next();
			graph.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
		}
		
		graph.getListenerManager().transactionFinished(this);
	}
	
	/**
	 * Remove all bends
	 */
	private void removeAllBends() {
		for (Iterator iterator = graph.getEdgesIterator(); iterator.hasNext();) {
			try {
				((LinkedHashMapAttribute) ((Edge) iterator.next()).getAttribute(BENDS)).setCollection(
									new HashMap());
			} catch (AttributeNotFoundException anfe) {
			};
		}
		
		for (Iterator iterator = tempEdges.iterator(); iterator.hasNext();) {
			try {
				((LinkedHashMapAttribute) ((Edge) iterator.next()).getAttribute(
									BENDS)).setCollection(
										new HashMap());
			} catch (AttributeNotFoundException anfe) {
			};
		}
		
	}
	
	/**
	 * Initialize the level of each node
	 */
	private void computeDepth(Node startNode) {
		
		LinkedList queue = new LinkedList();
		
		maxNodeHeight = new HashMap();
		
		bfsNum = new HashMap();
		
		queue.addLast(startNode);
		bfsNum.put(startNode, new Integer(0));
		forest.remove(startNode);
		
		/* BreadthFirstSearch algorithm which calculates the depth of the tree */
		while (!queue.isEmpty()) {
			
			Node v = (Node) queue.removeFirst();
			/* Walk through all neighbours of the last node */
			for (Iterator neighbours = v.getNeighborsIterator(); neighbours.hasNext();) {
				
				Node neighbour = (Node) neighbours.next();
				
				/* Not all neighbours, just the neighbours not visited yet */
				if (!bfsNum.containsKey(neighbour)) {
					Integer depth =
										new Integer(((Integer) bfsNum.get(v)).intValue() + 1);
					
					double nodeHeight = getNodeHeight(neighbour);
					
					/* Compute the maximum height of nodes in each level of the tree */
					Double maxNodeHeightValue =
										(Double) maxNodeHeight.get(depth);
					if (maxNodeHeightValue != null) {
						maxNodeHeight.put(
											depth,
											new Double(
																Math.max(
																					maxNodeHeightValue.doubleValue(),
																					nodeHeight)));
					} else {
						maxNodeHeight.put(depth, new Double(nodeHeight));
					}
					
					forest.remove(neighbour);
					bfsNum.put(neighbour, depth);
					queue.addFirst(neighbour);
				}
			}
		}
		
	}
	
	/**
	 * Start computing the root
	 */
	protected void computePositions() {
		
		double rho = 0.0;
		double alpha1 = 0.0;
		double alpha2 = 2 * Math.PI;
		
		/* compute the coordinates (alpha1 + alpha2) / 2 */
		double nodeCoordX = polarToCartesianX(rho, (alpha1 + alpha2) / 2);
		double nodeCoordY = polarToCartesianY(rho, (alpha1 + alpha2) / 2);
		
		setX(sourceNode, nodeCoordX);
		setY(sourceNode, nodeCoordY);
		
		/* give every kid a sector proportional to its width / width of the whole subtree */
		double rootWidth = magnitude(sourceNode);
		rho++;
		Iterator succIterator = getSuccessors(sourceNode);
		/* launch the RadialSubtree method on its leaves */
		while (succIterator.hasNext()) {
			Node successor = (Node) succIterator.next();
			double succWidth = magnitude(successor);
			
			alpha2 = alpha1 + (2 * Math.PI * succWidth / rootWidth);
			/* start computing the children */
			radialSubTree(successor, succWidth, rho, alpha1, alpha2);
			alpha1 = alpha2;
		}
	}
	
	/**
	 * Compute the x coordinate
	 * 
	 * @param rho
	 * @param alpha
	 * @return
	 */
	protected double polarToCartesianX(double rho, double alpha) {
		double result = xStart
							+ rho * Math.cos(alpha) * nodeDistance +
							maxNodeHeight.size() * nodeDistance;
		
		if (treeDirection == 270) {
			result = xStart -
								rho * Math.cos(alpha) * nodeDistance +
								maxNodeHeight.size() * nodeDistance;
		}
		
		return result;
	}
	
	/**
	 * Compute the y coordinate
	 * 
	 * @param rho
	 * @param alpha
	 * @return y coordinate
	 */
	protected double polarToCartesianY(double rho, double alpha) {
		double result = yStart
							+ rho * Math.sin(alpha) * nodeDistance +
							maxNodeHeight.size() * nodeDistance;
		
		if (treeDirection == 180) {
			result = yStart -
								rho * Math.sin(alpha) * nodeDistance +
								maxNodeHeight.size() * nodeDistance;
		}
		
		return result;
	}
	
	/**
	 * Compute all subtrees recursively
	 * 
	 * @param node
	 * @param width
	 * @param rho
	 * @param alpha1
	 * @param alpha2
	 */
	protected void radialSubTree(
						Node node,
						double width,
						double rho,
						double alpha1,
						double alpha2) {
		
		/* compute the coordinates (alpha1 + alpha2) / 2 */
		double nodeCoordX = polarToCartesianX(rho, (alpha1 + alpha2) / 2);
		double nodeCoordY = polarToCartesianY(rho, (alpha1 + alpha2) / 2);
		
		setX(node, nodeCoordX);
		setY(node, nodeCoordY);
		
		double tau = 2 * Math.acos(rho / (rho + 1));
		double alpha = 0.0;
		double s = 0.0;
		if (tau < (alpha2 - alpha1)) {
			alpha = (alpha1 + alpha2 - tau) / 2.0;
			s = tau / width;
		} else {
			alpha = alpha1;
			s = (alpha2 - alpha1) / width;
		}
		Iterator succIterator = getSuccessors(node);
		/* launch the RadialSubtree method on its leaves */
		while (succIterator.hasNext()) {
			Node successor = (Node) succIterator.next();
			double succWidth = magnitude(successor);
			
			radialSubTree(
								successor,
								succWidth,
								rho + 1,
								alpha,
								alpha += s * succWidth);
		}
	}
	
	/**
	 * Get the magnitude for the given node
	 * 
	 * @param node
	 * @return the sum of all children and sub children
	 */
	protected double magnitude(Node node) {
		
		return ((Integer) magnitude.get(node)).intValue();
	}
	
	/**
	 * Return the height dimension of the given node n
	 * 
	 * @param n
	 *           node
	 * @return
	 */
	private double getNodeHeight(Node n) {
		DimensionAttribute dimAttr =
							(DimensionAttribute) n.getAttribute(DIMENSIONSTR);
		
		double result = 0.0;
		
		if ((treeDirection == 90) || (treeDirection == 270)) {
			result = dimAttr.getDimension().getWidth();
		} else {
			result = dimAttr.getDimension().getHeight();
		}
		
		return result;
	}
	
	/**
	 * Sets the x position of the given node n
	 * 
	 * @param n
	 *           node
	 * @param x
	 *           position
	 */
	private void setX(Node n, double x) {
		CoordinateAttribute coordAttr =
							(CoordinateAttribute) n.getAttribute(COORDSTR);
		
		if (coordAttr != null) {
			if ((treeDirection == 90) || (treeDirection == 270)) {
				coordAttr.setY(x);
			} else {
				coordAttr.setX(x);
			}
		}
	}
	
	/**
	 * Set the y position of the given node n
	 * 
	 * @param n
	 *           node
	 * @param y
	 *           position
	 */
	private void setY(Node n, double y) {
		CoordinateAttribute coordAttr =
							(CoordinateAttribute) n.getAttribute(COORDSTR);
		
		if (coordAttr != null) {
			if ((treeDirection == 90) || (treeDirection == 270)) {
				coordAttr.setX(y);
			} else {
				coordAttr.setY(y);
			}
		}
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return null;
		// return "Radial Tree";
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
}
