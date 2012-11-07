/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.tree_simple;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.EdgeShapeAttribute;
import org.graffiti.attributes.LinkedHashMapAttribute;
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
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.TreeContainer;

/**
 * An implementation of a tree layout algorithm.
 * 
 * @author Joerg Bartelheimer
 */

/* TODO: Layout of non-horizontal/vertical grids */
@SuppressWarnings("unchecked")
public class GraphTreeLayout extends AbstractAlgorithm {
	
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
	 * Dynamical defined edge shape attribute.
	 */
	private final String SHAPE =
			GraphicAttributeConstants.GRAPHICS
					+ Attribute.SEPARATOR
					+ GraphicAttributeConstants.SHAPE;
	
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
	 * The minimum x spanning of the actual tree
	 */
	private double xSpanningMin = 0.0;
	
	/**
	 * The maximum x spanning of the actual tree
	 */
	private double xSpanningMax = 0.0;
	
	/**
	 * The maximum y spanning of the actual tree
	 */
	private double ySpanningMax = 0.0;
	
	/**
	 * Horizontal distance between leaves
	 */
	private double xNodeDistance = 10;
	
	/**
	 * Vertical distance between leaves
	 */
	private double yNodeDistance = 10;
	
	/**
	 * Horizontal distance between trees
	 */
	private double xDistance = 100;
	
	/**
	 * Vertical distance between trees
	 */
	private double yDistance = 100;
	
	/**
	 * x coordinate of start point
	 */
	private double xStart = 0;
	
	/**
	 * y coordinate of start point
	 */
	private double yStart = 0;
	
	/**
	 * x coordinate of start point
	 */
	private double xStartParam = 100;
	
	/**
	 * y coordinate of start point
	 */
	private double yStartParam = 100;
	
	/**
	 * Height of nodes in each level of the tree.
	 */
	private HashMap depthOffsets = new HashMap();
	
	/**
	 * All trees are initialized by this variable.
	 */
	private HashMap forest;
	
	/**
	 * The roots in the forest.
	 */
	private HashMap sourceNodes = new HashMap();
	
	/**
	 * The maximum y dimension for each node.
	 */
	private HashMap maxNodeHeight = new HashMap();
	
	/**
	 * The depth for each node .
	 */
	private HashMap bfsNum;
	
	/**
	 * Put all trees in a row
	 */
	private boolean horizontalLayout = true;
	
	/**
	 * Move all tree in the right direction either 0, 90, 180 or 270 degree
	 */
	private int treeDirection = 0;
	
	/**
	 * Activate bus layout
	 */
	private boolean isBusLayout = false;
	
	/**
	 * Put all trees in a row
	 */
	private boolean isRemoveBends = true;
	
	/**
	 * The root node of the tree.
	 */
	private Node sourceNode = null;
	
	/**
	 * The depth for each node upside down.
	 */
	HashMap bfsNumUpsideDown = new HashMap();
	
	/**
	 * If there are a circle edges, save them here
	 */
	private LinkedList tempEdges = new LinkedList();
	
	/**
	 * All leaves have the position 0.
	 */
	boolean zeroLeaves = false;
	
	/*************************************************************/
	/* Declarations of methods */
	/*************************************************************/
	
	/**
	 * Constructs a new GraphTreeLayout algorithm instance.
	 */
	public GraphTreeLayout() {
		// does nothing
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
		/*
		 * g.getListenerManager().transactionStarted(this);
		 * try {
		 * ((BooleanAttribute) g.getAttribute(".directed")).setBoolean(false);
		 * } catch (Exception e) {
		 * e.printStackTrace();
		 * }
		 * g.getListenerManager().transactionFinished(this);
		 * g.getListenerManager().transactionStarted(this);
		 * try {
		 * ((BooleanAttribute) g.getAttribute(".directed")).setBoolean(true);
		 * } catch (Exception e) {
		 * e.printStackTrace();
		 * }
		 * g.getListenerManager().transactionFinished(this);
		 */
		forest = new HashMap();
		
		sourceNodes = new HashMap();
		
		tempEdges = new LinkedList();
		
		for (Iterator iterator = graph.getNodesIterator(); iterator.hasNext();) {
			forest.put(iterator.next(), null);
		}
		
		/* check all trees with selected nodes, whether they have one root */
		for (Iterator iterator = selection.getNodes().iterator(); iterator.hasNext();) {
			
			sourceNode = (Node) iterator.next();
			/* ignore multiple selection */
			if (forest.containsKey(sourceNode)) {
				/* check circle connection by using the depth of each node */
				computeAppropriateDepth(sourceNode);
				sourceNodes.put(
						sourceNode,
						new TreeContainer(
								depthOffsets,
								bfsNum,
								maxNodeHeight,
								bfsNumUpsideDown));
				
				if (!rootedTree(sourceNode)) {
					throw new PreconditionException("The given graph is not a tree.");
				}
			}
		}
		/* check the trees which are left, whether they have one root */
		while (forest.keySet().iterator().hasNext()) {
			
			sourceNode = (Node) forest.keySet().iterator().next();
			
			/* check circle connection by using the depth of each node */
			computeAppropriateDepth(sourceNode);
			sourceNodes.put(
					sourceNode,
					new TreeContainer(
							depthOffsets,
							bfsNum,
							maxNodeHeight,
							bfsNumUpsideDown));
			
			if (!rootedTree(sourceNode)) {
				throw new PreconditionException("The given graph is not a tree.");
			}
			
			/* in case of arrows, try to find the root */
			Node node = null;
			for (Iterator iterator = preorder(sourceNode).iterator(); iterator.hasNext();) {
				
				node = (Node) iterator.next();
				
				// System.out.println(/*"node  "+((IntegerAttribute)
				// sourceNode.getAttribute("id")).getValue()+*/"  in "+node.getInDegree()+"  out  "+node.getOutDegree());
				
				if ((node.getInDegree() == 0) && (node.getOutDegree() > 0)) {
					
					sourceNodes.remove(sourceNode);
					
					sourceNode = node;
					
					computeAppropriateDepth(sourceNode);
					sourceNodes.put(
							sourceNode,
							new TreeContainer(
									depthOffsets,
									bfsNum,
									maxNodeHeight,
									bfsNumUpsideDown));
					
					break;
				}
			}
			
		}
		
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
	 * Initialize the appropriate level of each node
	 */
	public void computeAppropriateDepth(Node startNode) {
		if (zeroLeaves) {
			computeDepthUpsideDown(sourceNode);
		} else {
			computeDepth(sourceNode);
		}
	}
	
	/**
	 * Initialize the level of each node upside down
	 */
	private void computeDepthUpsideDown(Node startNode) {
		
		LinkedList queue = new LinkedList();
		
		depthOffsets = new HashMap();
		
		bfsNum = new HashMap();
		
		maxNodeHeight = new HashMap();
		
		bfsNumUpsideDown = new HashMap();
		
		queue.addLast(startNode);
		
		LinkedList leaves = new LinkedList();
		
		queue.addLast(startNode);
		bfsNum.put(startNode, new Integer(0));
		bfsNumUpsideDown.put(startNode, new Integer(0));
		forest.remove(startNode);
		
		/* BreadthFirstSearch algorithm which calculates the depth of the tree */
		while (!queue.isEmpty()) {
			
			Node v = (Node) queue.removeFirst();
			
			boolean isLeaf = true;
			
			/* Walk through all neighbours of the last node */
			for (Iterator neighbours = v.getNeighborsIterator(); neighbours.hasNext();) {
				
				Node neighbour = (Node) neighbours.next();
				
				/* Not all neighbours, just the neighbours not visited yet */
				if (!bfsNum.containsKey(neighbour)) {
					Integer depth =
							new Integer(((Integer) bfsNum.get(v)).intValue() + 1);
					
					forest.remove(neighbour);
					isLeaf = false;
					bfsNum.put(neighbour, depth);
					queue.addFirst(neighbour);
				}
			}
			
			if (isLeaf) {
				leaves.add(v);
				bfsNumUpsideDown.put(v, new Integer(0));
			}
			
		}
		
		/* BreadthFirstSearch algorithm which calculates the depth of the tree */
		while (!leaves.isEmpty()) {
			
			Node v = (Node) leaves.removeFirst();
			
			/* Walk through all neighbours of the last node */
			for (Iterator neighbours = v.getNeighborsIterator(); neighbours.hasNext();) {
				
				Node neighbor = (Node) neighbours.next();
				
				if (((Integer) bfsNum.get(v)).intValue() > ((Integer) bfsNum.get(neighbor)).intValue()) {
					Integer depth =
							new Integer(
									((Integer) bfsNumUpsideDown.get(v)).intValue()
									+ 1);
					if (!((bfsNumUpsideDown.containsKey(neighbor)) && ((Integer) bfsNumUpsideDown.get(neighbor)).intValue() > depth.intValue())) {
						
						leaves.add(neighbor);
						bfsNumUpsideDown.put(neighbor, depth);
					}
				}
			}
		}
		
		for (Iterator iterator = bfsNum.keySet().iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			
			Integer depth =
					new Integer(((Integer) bfsNumUpsideDown.get(node)).intValue());
			
			double nodeHeight = getNodeHeight(node);
			
			/* Compute the maximum height of nodes in each level of the tree */
			Double maxNodeHeightValue = (Double) maxNodeHeight.get(depth);
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
			
		}
		
		/* Compute the maximum height of the root node */
		depthOffsets.put(
				new Integer(maxNodeHeight.size() - 1),
				new Double(getNodeHeight(sourceNode) / 2.0));
		maxNodeHeight.put(
				new Integer(maxNodeHeight.size() - 1),
				new Double(getNodeHeight(sourceNode)));
		
		for (int depth = maxNodeHeight.size() - 2; depth >= 0; depth--) {
			
			double nodeHeight =
					((Double) maxNodeHeight.get(new Integer(depth))).doubleValue();
			double nodeHeightAncestor =
					((Double) maxNodeHeight.get(new Integer(depth + 1)))
							.doubleValue();
			double yOffsetAncestor =
					((Double) depthOffsets.get(new Integer(depth + 1)))
							.doubleValue();
			double yOffset =
					yOffsetAncestor
							+ nodeHeight / 2.0
							+ yNodeDistance
							+ nodeHeightAncestor / 2.0;
			
			depthOffsets.put(new Integer(depth), new Double(yOffset));
		}
		
	}
	
	/**
	 * Initialize the level of each node
	 */
	
	private void computeDepth(Node startNode) {
		
		LinkedList queue = new LinkedList();
		
		maxNodeHeight = new HashMap();
		
		depthOffsets = new HashMap();
		
		bfsNum = new HashMap();
		
		queue.addLast(startNode);
		bfsNum.put(startNode, new Integer(0));
		forest.remove(startNode);
		
		/* Compute the maximum height of the root node */
		depthOffsets.put(
				new Integer(0),
				new Double(getNodeHeight(sourceNode) / 2.0));
		maxNodeHeight.put(
				new Integer(0),
				new Double(getNodeHeight(sourceNode)));
		
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
		
		for (int depth = 1; depth < maxNodeHeight.size(); depth++) {
			
			double nodeHeight =
					((Double) maxNodeHeight.get(new Integer(depth))).doubleValue();
			double nodeHeightAncestor =
					((Double) maxNodeHeight.get(new Integer(depth - 1)))
							.doubleValue();
			double yOffsetAncestor =
					((Double) depthOffsets.get(new Integer(depth - 1)))
							.doubleValue();
			double yOffset =
					yOffsetAncestor
							+ nodeHeight / 2.0
							+ yNodeDistance
							+ nodeHeightAncestor / 2.0;
			
			depthOffsets.put(new Integer(depth), new Double(yOffset));
		}
		
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		Node initNode = null;
		try {
			initNode = selection.getNodes().iterator().next();
		} catch (NoSuchElementException nse) {/* empty */
		}
		NodeParameter nodeParam =
				new NodeParameter(
						graph, initNode,
						"Start-Node",
						"Tree layouter will start only with a selected node.");
		
		DoubleParameter xDistanceParam =
				new DoubleParameter(
						"X distance",
						"The distance between nodes in horizontal direction.");
		
		DoubleParameter yDistanceParam =
				new DoubleParameter(
						"Y distance",
						"The distance between nodes in vertical direction.");
		
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
		
		IntegerParameter treeDirectionParam =
				new IntegerParameter(
						treeDirection,
						"Tree Direction (0,90,180,270)",
						"Move all trees in 0, 90, 180 or 270 degree");
		
		BooleanParameter zeroLeavesParam =
				new BooleanParameter(
						zeroLeaves,
						"Bottom Weighted",
						"Place all leaves on the same level");
		
		BooleanParameter busLayoutParam =
				new BooleanParameter(
						isBusLayout,
						"Bus Layout",
						"Layout the trees in bus format");
		
		BooleanParameter removeBendParam =
				new BooleanParameter(
						isRemoveBends,
						"Remove Bends",
						"Remove all bends in the forest");
		
		xDistanceParam.setDouble(xNodeDistance);
		yDistanceParam.setDouble(yNodeDistance);
		xStartParam.setDouble(this.xStartParam);
		yStartParam.setDouble(this.yStartParam);
		
		return new Parameter[] {
				nodeParam,
				xDistanceParam,
				yDistanceParam,
				xStartParam,
				yStartParam,
				horizontalParam,
				zeroLeavesParam,
				removeBendParam,
				busLayoutParam,
				treeDirectionParam };
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		Node nsel = ((NodeParameter) params[0]).getNode();
		Selection ts = new Selection();
		ts.add(nsel);
		selection = ts;
		
		xNodeDistance =
				((DoubleParameter) params[1]).getDouble().doubleValue();
		yNodeDistance =
				((DoubleParameter) params[2]).getDouble().doubleValue();
		xStart = ((DoubleParameter) params[3]).getDouble().doubleValue();
		yStart = ((DoubleParameter) params[4]).getDouble().doubleValue();
		xStartParam = ((DoubleParameter) params[3]).getDouble().doubleValue();
		yStartParam = ((DoubleParameter) params[4]).getDouble().doubleValue();
		horizontalLayout =
				((BooleanParameter) params[5]).getBoolean().booleanValue();
		zeroLeaves =
				((BooleanParameter) params[6]).getBoolean().booleanValue();
		isRemoveBends =
				((BooleanParameter) params[7]).getBoolean().booleanValue();
		isBusLayout =
				((BooleanParameter) params[8]).getBoolean().booleanValue();
		treeDirection = ((IntegerParameter) params[9]).getInteger().intValue();
		if (!((treeDirection == 0) || (treeDirection == 180) || (treeDirection == 270) || (treeDirection == 90))) {
			treeDirection = 0;
		}
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute(Graph)
	 *      The given graph must have at least one node.
	 */
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		if (isRemoveBends) {
			removeAllBends();
		}
		for (Iterator iterator = sourceNodes.keySet().iterator(); iterator.hasNext();) {
			
			sourceNode = (Node) iterator.next();
			
			depthOffsets =
					((TreeContainer) sourceNodes.get(sourceNode)).getDepthOffset();
			bfsNum = ((TreeContainer) sourceNodes.get(sourceNode)).getBfsNum();
			bfsNumUpsideDown =
					((TreeContainer) sourceNodes.get(sourceNode))
							.getBfsNumUpsideDown();
			maxNodeHeight =
					((TreeContainer) sourceNodes.get(sourceNode))
							.getMaxNodeHeight();
			
			/* Define a queue q which enables the algorithm to traverse the tree. */
			LinkedList q = new LinkedList();
			
			/* d contains a mapping from node to an integer, the x position of the node */
			Map d = new HashMap();
			
			/* calulated contains a mapping from a node so that we know that the node has a x position */
			HashMap calculated = new HashMap();
			
			/* Seperate the ordered nodes in leaves and nodes (degree > 1) */
			LinkedList leaves = new LinkedList();
			LinkedList nodes = new LinkedList();
			
			/* Start the algorithm with the selected node. */
			q.addLast(sourceNode);
			
			/* Root node has the x position 0. */
			d.put(sourceNode, new Integer(0));
			
			/* First leaf has the x position 0. */
			int xPosLeaf = 0;
			
			double minNodeSizeWidth = getNodeWidth(sourceNode);
			
			double maxNodeSizeWidth = 0.0;
			
			/*
			 * The loop traverses the tree starting from the selected node (root)
			 * and seperates the nodes from the leaves. The tree is traversed from
			 * left to right.
			 */
			while (!q.isEmpty()) {
				Node v = (Node) q.removeLast();
				
				boolean isLeaf = true;
				
				/* Walk through all neighbours of the last node */
				for (Iterator neighbours = v.getNeighborsIterator(); neighbours.hasNext();) {
					Node neighbour = (Node) neighbours.next();
					/* Neighbour already traversed ? */
					if (!d.containsKey(neighbour)) {
						xPosLeaf++;
						d.put(neighbour, new Integer(xPosLeaf));
						q.addLast(neighbour);
						isLeaf = false;
					}
				}
				
				minNodeSizeWidth = Math.min(getNodeWidth(v), minNodeSizeWidth);
				maxNodeSizeWidth = Math.max(getNodeWidth(v), maxNodeSizeWidth);
				
				/* Seperate the leaves from nodes. */
				if (isLeaf) {
					leaves.addFirst(v);
					calculated.put(v, null);
				} else {
					nodes.addFirst(v);
				}
				
			}
			
			/* Set the x position of the leaves here. */
			for (int i = 0; i < leaves.size(); i++) {
				Node node = (Node) leaves.get(i);
				double nodeWidth = getNodeWidth(node);
				double leftBrotherWidth = 0.0;
				double leftBrotherCoord = xStart;
				if (i != 0) {
					Node leftBrother = (Node) leaves.get(i - 1);
					leftBrotherWidth = getNodeWidth(leftBrother);
					leftBrotherCoord = getX(leftBrother);
				}
				
				xSpanningMax =
						Math.max(
								xSpanningMax,
								leftBrotherCoord
										+ nodeWidth
										+ leftBrotherWidth / 2.0
										+ xNodeDistance
										- xStart
										+ (maxNodeSizeWidth - minNodeSizeWidth) / 2);
				
				setX(
						node,
						leftBrotherCoord
								+ nodeWidth / 2.0
								+ leftBrotherWidth / 2.0
								+ xNodeDistance
								+ (maxNodeSizeWidth - minNodeSizeWidth) / 2);
			}
			
			/* Define a queue q which enables the algorithm to traverse the tree. */
			q = new LinkedList();
			
			/* Root node has the depth 0. */
			q.addLast(sourceNode);
			d.put(sourceNode, new Integer(0));
			HashMap bfs = null;
			
			if (zeroLeaves) {
				bfs = bfsNumUpsideDown;
			} else {
				bfs = bfsNum;
			}
			
			/* Set the y position of each node */
			for (Iterator nodesIterator = preorder(sourceNode).iterator(); nodesIterator.hasNext();) {
				Node n = (Node) nodesIterator.next();
				if (d.containsKey(n)) {
					
					int level = ((Integer) bfs.get(n)).intValue();
					
					setY(
							n,
							yStart
									+ ((Double) depthOffsets.get(new Integer(level)))
											.doubleValue());
					
					ySpanningMax =
							Math.max(
									ySpanningMax,
									((Double) depthOffsets.get(new Integer(level)))
											.doubleValue());
					
				}
			}
			
			/* Calculate for each node the x position from the bounds of its neighbour */
			for (Iterator nodesIterator = nodes.iterator(); nodesIterator.hasNext();) {
				
				Node node = (Node) nodesIterator.next();
				Rectangle rect = null;
				
				for (Iterator neighbours = node.getNeighborsIterator(); neighbours.hasNext();) {
					Node neighbour = (Node) neighbours.next();
					
					if (calculated.containsKey(neighbour)) {
						if (rect == null) {
							rect =
									new Rectangle(
											(int) (getX(neighbour) - getNodeWidth(neighbour) / 2),
											0,
											0,
											0);
						}
						rect.add(getX(neighbour) + getNodeWidth(neighbour) / 2, 0);
					}
				}
				calculated.put(node, null);
				
				xSpanningMax =
						Math.max(
								xSpanningMax,
								(rect.x + (rect.width >> 1))
										+ getNodeWidth(node) / 2.0
										- xStart);
				
				xSpanningMin =
						Math.min(
								xSpanningMin,
								
								((rect.x + (rect.width >> 1))
										- getNodeWidth(node) / 2.0) - xStart);
				
				/*
				 * Rectangle subTree = null;
				 * // System.out.println(rect.width);
				 * for (Iterator it = preorder(node).iterator(); it.hasNext();) {
				 * Node n = (Node)it.next();
				 * if (subTree == null) {
				 * subTree =
				 * new Rectangle(
				 * (int) (getX(n)-getNodeWidth(n)/2),
				 * 0,
				 * 0,
				 * 0);
				 * }
				 * System.out.print("  "+((IntegerAttribute) n.getAttribute("id")).getValue());
				 * subTree.add(getX(n)+getNodeWidth(n)/2, 0);
				 * }
				 * System.out.println(((IntegerAttribute) node.getAttribute("id")).getValue()+"   "+subTree.getWidth()+"   "+subTree+"      "+getNodeWidth(node));
				 * if (getNodeWidth(node)>subTree.width) {
				 * System.out.println(((IntegerAttribute) node.getAttribute("id")).getValue());
				 * for (Iterator it = calculated.keySet().iterator(); it.hasNext();) {
				 * Node correctionNode=(Node) it.next();
				 * if (!usedNodes.containsKey(correctionNode) /*&& getX(correctionNode)>getX(node)*) {
				 * setX(correctionNode, getX(correctionNode)+(getNodeWidth(node)));
				 * System.out.println("correcting"+((IntegerAttribute) correctionNode.getAttribute("id")).getValue());
				 * }
				 * }
				 * setX(node, (int) (rect.x + (rect.width >> 1))+(getNodeWidth(node)-subTree.width)/2);
				 * } else {
				 * setX(node, (int) (rect.x + (rect.width >> 1)));
				 * }
				 */
				setX(node, (rect.x + (rect.width >> 1)));
				
			}
			
			/* Adjust the start coordinates for each tree */
			adjustCoordinates();
			
			for (Iterator nodesIterator = preorder(sourceNode).iterator(); nodesIterator.hasNext();) {
				Node node = (Node) nodesIterator.next();
				if ((treeDirection == 180) || (treeDirection == 90)) {
					setY(node, ySpanningMax + yStart * 2 - getY(node));
				}
			}
			
			if (isBusLayout) {
				formatBusLayout();
			}
			
			if (horizontalLayout) {
				if ((treeDirection == 180) || (treeDirection == 0)) {
					xStart += xSpanningMax + xDistance + xNodeDistance;
				} else {
					yStart += ySpanningMax + yDistance + yNodeDistance;
				}
			} else {
				if ((treeDirection == 180) || (treeDirection == 0)) {
					yStart += ySpanningMax + yDistance + yNodeDistance;
				} else {
					xStart += xSpanningMax + xDistance + xNodeDistance;
				}
			}
			
			xSpanningMax = 0;
			xSpanningMin = 0;
			ySpanningMax = 0;
			
		}
		
		for (Iterator iterator = tempEdges.iterator(); iterator.hasNext();) {
			Edge edge = (Edge) iterator.next();
			graph.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
		}
		
		graph.getListenerManager().transactionFinished(this);
		/*
		 * graph.getListenerManager().transactionStarted(this);
		 * for (Iterator it = graph.getNodesIterator(); it.hasNext();) {
		 * Node node = (Node) it.next();
		 * setLabel(
		 * node,
		 * "" + ((IntegerAttribute) node.getAttribute("id")).getValue());
		 * }
		 * graph.getListenerManager().transactionFinished(this);
		 */
		
	}
	
	/**
	 * Bus layout format
	 */
	private void formatBusLayout() {
		for (Iterator iterator = graph.getEdgesIterator(); iterator.hasNext();) {
			
			Edge edge = ((Edge) iterator.next());
			
			Node node1 = edge.getSource();
			
			HashMap bfs = null;
			
			if (zeroLeaves) {
				bfs = bfsNumUpsideDown;
			} else {
				bfs = bfsNum;
			}
			
			if (bfs.get(node1) != null) {
				Node node2 = edge.getTarget();
				double yCoord1 =
						((Double) (depthOffsets.get(bfs.get(node1))))
								.doubleValue()
								+ ((Double) (maxNodeHeight.get(bfs.get(node1))))
										.doubleValue()
								/ 2
								+ yNodeDistance / 2;
				double yCoord2 =
						((Double) (depthOffsets.get(bfs.get(node2))))
								.doubleValue()
								+ ((Double) (maxNodeHeight.get(bfs.get(node2))))
										.doubleValue()
								/ 2
								+ yNodeDistance / 2;
				
				double yCoord = yStart + Math.min(yCoord1, yCoord2);
				
				double xCoord1 = getX(node1);
				double xCoord2 = getX(node2);
				
				EdgeShapeAttribute edgeShape = (EdgeShapeAttribute) (edge.getAttribute(SHAPE));
				edgeShape.setValue("org.graffiti.plugins.views.defaults.PolyLineEdgeShape");
				
				HashMap bends = new HashMap();
				if (treeDirection == 270) {
					bends.put("bend1", new CoordinateAttribute("bend1", yCoord, xCoord1));
					bends.put("bend2", new CoordinateAttribute("bend2", yCoord, xCoord2));
				} else
					if (treeDirection == 0) {
						bends.put("bend1", new CoordinateAttribute("bend1", xCoord1, yCoord));
						bends.put("bend2", new CoordinateAttribute("bend2", xCoord2, yCoord));
					} else
						if (treeDirection == 90) {
							bends.put("bend1", new CoordinateAttribute("bend1", ySpanningMax + yStart * 2 - yCoord, xCoord1));
							bends.put("bend2", new CoordinateAttribute("bend2", ySpanningMax + yStart * 2 - yCoord, xCoord2));
						} else {
							bends.put("bend1", new CoordinateAttribute("bend1", xCoord1, ySpanningMax + yStart * 2 - yCoord));
							bends.put("bend2", new CoordinateAttribute("bend2", xCoord2, ySpanningMax + yStart * 2 - yCoord));
						}
				
				((LinkedHashMapAttribute) edge.getAttribute(BENDS)).setCollection(bends);
			}
			
		}
	}
	
	/*
	 * Adjust the start coordinates for each tree
	 */
	private void adjustCoordinates() {
		
		for (Iterator nodesIterator = preorder(sourceNode).iterator(); nodesIterator.hasNext();) {
			Node node = (Node) nodesIterator.next();
			setX(node, getX(node) + Math.abs(xSpanningMin));
			// setY(node, getY(node)+Math.abs(ySpanningMin));
		}
		
		xSpanningMax += Math.abs(xSpanningMin);
		// ySpanningMax+=Math.abs(ySpanningMin);
	}
	
	/**
	 * Remove all bends
	 */
	private void removeAllBends() {
		for (Iterator iterator = graph.getEdgesIterator(); iterator.hasNext();) {
			try {
				((LinkedHashMapAttribute) ((Edge) iterator.next()).getAttribute(BENDS)).setCollection(
						new HashMap());
			} catch (AttributeNotFoundException atnfe) {
			};
		}
		
		for (Iterator iterator = tempEdges.iterator(); iterator.hasNext();) {
			try {
				((LinkedHashMapAttribute) ((Edge) iterator.next()).getAttribute(
						BENDS)).setCollection(
						new HashMap());
			} catch (AttributeNotFoundException atnfe) {
			};
		}
		
	}
	
	/**
	 * Return the witdth dimension of the given node n
	 * 
	 * @param n
	 *           node
	 * @return
	 */
	private double getNodeWidth(Node n) {
		DimensionAttribute dimAttr =
				(DimensionAttribute) n.getAttribute(DIMENSIONSTR);
		double result = 0.0;
		
		if ((treeDirection == 90) || (treeDirection == 270)) {
			result = dimAttr.getDimension().getHeight();
		} else {
			result = dimAttr.getDimension().getWidth();
		}
		
		return result;
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
	 * Returns the x position of the given node n
	 * 
	 * @param n
	 *           node
	 * @return
	 */
	private double getX(Node n) {
		double res = 0;
		CoordinateAttribute coordAttr =
				(CoordinateAttribute) n.getAttribute(COORDSTR);
		
		if (coordAttr != null) {
			if ((treeDirection == 90) || (treeDirection == 270)) {
				res = coordAttr.getY();
			} else {
				res = coordAttr.getX();
			}
		}
		return res;
	}
	
	/**
	 * Returns the x position of the given node n
	 * 
	 * @param n
	 *           node
	 * @return
	 */
	private double getY(Node n) {
		double res = 0;
		CoordinateAttribute coordAttr =
				(CoordinateAttribute) n.getAttribute(COORDSTR);
		
		if (coordAttr != null) {
			if ((treeDirection == 90) || (treeDirection == 270)) {
				res = coordAttr.getX();
			} else {
				res = coordAttr.getY();
			}
		}
		return res;
	}
	
	/**
	 * Sets the y position of the given node n
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
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Tree";
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
