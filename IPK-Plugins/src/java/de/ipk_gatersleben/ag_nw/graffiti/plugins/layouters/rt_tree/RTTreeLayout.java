/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
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
import org.graffiti.plugin.parameter.Parameter;

/**
 * An implementation of a tree layout algorithm.
 * 
 * @author Joerg Bartelheimer
 */

/* TODO: Layout of non-horizontal/vertical grids */
@SuppressWarnings("unchecked")
public class RTTreeLayout extends AbstractAlgorithm {
	
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
	 * Holds the tree in a linkedlist which is used by getLeftBrother
	 */
	LinkedList treeMap;
	
	/**
	 * The maximum x spanning of the actual tree
	 */
	private double xSpanningMax = 0.0;
	
	/**
	 * The maximum y spanning of the actual tree
	 */
	private double ySpanningMax = 0.0;
	
	/**
	 * The minimum x spanning of the actual tree
	 */
	private double xSpanningMin = 0.0;
	
	/**
	 * The minimum y spanning of the actual tree
	 */
	private double ySpanningMin = 0.0;
	
	/**
	 * Horizontal distance between leaves
	 */
	private double xNodeDistance = 15;
	
	/**
	 * Vertical distance between leaves
	 */
	private double yNodeDistance = 80;
	
	/**
	 * Horizontal distance between trees
	 */
	private double xDistance = 150;
	
	/**
	 * Vertical distance between trees
	 */
	private double yDistance = 150;
	
	/**
	 * Put all trees in a row
	 */
	private boolean horizontalLayout = true;
	
	/**
	 * Move all tree in the right direction either 0, 90, 180 or 270 degree
	 */
	private int treeDirection = 270;
	
	/**
	 * Remove all bends
	 */
	private boolean isRemoveBends = true;
	
	/**
	 * Activate bus layout
	 */
	private boolean isBusLayout = false;
	
	/**
	 * x coordinate of start point
	 */
	private double xStart = 0;
	
	/**
	 * y coordinate of start point
	 */
	private double yStart = 0;
	
	/**
	 * All trees are initialized by this variable.
	 */
	private HashMap forest;
	
	/**
	 * The roots in the forest.
	 */
	private HashMap<Node, TreeContainer> sourceNodes = new LinkedHashMap<Node, TreeContainer>();
	
	/**
	 * Height of nodes in each level of the tree.
	 */
	private HashMap<Integer, Double> depthOffsets = new LinkedHashMap<Integer, Double>();
	
	/**
	 * The depth for each node .
	 */
	private HashMap<Node, Integer> bfsNum = new LinkedHashMap<Node, Integer>();
	
	/**
	 * The maximum y dimension for each node.
	 */
	private HashMap<Integer, Double> maxNodeHeight = new LinkedHashMap<Integer, Double>();
	
	/**
	 * Relative coordinates computed in computeRelativeCoordinates
	 */
	private HashMap relativeCoords = new LinkedHashMap();
	
	/**
	 * Modifier fields to apply to succesors
	 */
	private HashMap modifierField = new LinkedHashMap();
	
	/**
	 * If there are a circle edges, save them here
	 */
	private LinkedList<Edge> tempEdges = new LinkedList<Edge>();
	
	/**
	 * The sum of all modifiers fields of ancestors which
	 * adjusts the successors.
	 */
	private HashMap cumulModifier = new LinkedHashMap();
	
	/*************************************************************/
	/* Declarations of methods */
	/*************************************************************/
	
	/**
	 * Construct a new GraphTreeLayout algorithm instance.
	 */
	public RTTreeLayout() {
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
		
	}
	
	@Override
	public String getDescription() {
		return "The node selection defines the starting point(s) for the layout.";
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
				
				if (bfsNum.get(neighbour) != null)
					if (bfsNum.get(node).intValue() > bfsNum.get(neighbour).intValue()) {
						ancestors++;
						if (ancestors > 1) {
							tempEdges.add(neighbourEdge);
							graph.deleteEdge(neighbourEdge);
						}
					}
				/* any links from same level nodes ? */
				if (bfsNum.get(neighbour) != null)
					if (bfsNum.get(node).intValue() == bfsNum.get(neighbour).intValue()) {
						
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
		/*
		 * SelectionParameter selParam =
		 * new SelectionParameter(
		 * "Start node",
		 * "Tree layouter will start only with a selected node.");
		 */
		DoubleParameter xDistanceParam =
				new DoubleParameter(
						"X distance",
						"The distance between nodes in horizontal direction.");
		
		DoubleParameter yDistanceParam =
				new DoubleParameter(
						"Y distance",
						"The distance between nodes in vertical direction.");
		
		DoubleParameter xStartParam =
				new DoubleParameter(100,
						"X base",
						"The x coordinate of the starting point of the grid horizontal direction.");
		
		DoubleParameter yStartParam =
				new DoubleParameter(100,
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
		
		BooleanParameter removeBendParam =
				new BooleanParameter(
						isRemoveBends,
						"Remove Bends",
						"Remove all bends in the forest");
		
		BooleanParameter busLayoutParam =
				new BooleanParameter(
						isBusLayout,
						"Bus Layout",
						"Layout the trees in bus format");
		
		xDistanceParam.setDouble(xNodeDistance);
		yDistanceParam.setDouble(yNodeDistance);
		
		return new Parameter[] {
				xDistanceParam,
				yDistanceParam,
				xStartParam,
				yStartParam,
				horizontalParam,
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
		int i = 0;
		xNodeDistance =
				((DoubleParameter) params[i++]).getDouble().doubleValue();
		yNodeDistance =
				((DoubleParameter) params[i++]).getDouble().doubleValue();
		xStart = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		yStart = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		horizontalLayout =
				((BooleanParameter) params[i++]).getBoolean().booleanValue();
		isRemoveBends =
				((BooleanParameter) params[i++]).getBoolean().booleanValue();
		isBusLayout =
				((BooleanParameter) params[i++]).getBoolean().booleanValue();
		treeDirection = ((IntegerParameter) params[i++]).getInteger().intValue();
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
		for (Iterator neighbors = node.getOutNeighborsIterator(); neighbors.hasNext();) {
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
		for (Iterator neighbors = node.getOutNeighborsIterator(); neighbors.hasNext();) {
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
	private Iterator<Node> getSuccessors(Node node) {
		LinkedList<Node> result = new LinkedList<Node>();
		
		for (Iterator neighbors = node.getNeighborsIterator(); neighbors.hasNext();) {
			Node neighbor = (Node) neighbors.next();
			if (bfsNum.get(neighbor) != null)
				if ((bfsNum.get(node)).intValue() < (bfsNum.get(neighbor)).intValue()) {
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
	private Iterator<Node> getPredecessors(Node node) {
		LinkedList<Node> result = new LinkedList<Node>();
		
		for (Iterator neighbors = node.getNeighborsIterator(); neighbors.hasNext();) {
			Node neighbor = (Node) neighbors.next();
			if (bfsNum.get(neighbor) != null)
				if ((bfsNum.get(node)).intValue() > (bfsNum.get(neighbor)).intValue()) {
					result.add(neighbor);
				}
		}
		return result.iterator();
	}
	
	/**
	 * Get the left brother at the actual level
	 * 
	 * @param node
	 * @return
	 */
	private Node getLeftBrother(Node node) {
		Node leftBrother = null;
		int depth = (bfsNum.get(node)).intValue();
		int pos = ((LinkedList) treeMap.get(depth)).indexOf(node);
		
		if (pos != 0) {
			leftBrother =
					(Node) ((LinkedList) treeMap.get(depth)).get(pos - 1);
		}
		
		return leftBrother;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute(Graph)
	 *      The given graph must have at least one node.
	 */
	public void execute() {
		tempEdges = new LinkedList<Edge>();
		
		sourceNodes = new LinkedHashMap<Node, TreeContainer>();
		
		forest = new LinkedHashMap<Node, TreeContainer>();
		
		ArrayList<Node> selNodes = new ArrayList<Node>(getSelectedOrAllNodes());
		
		if (selNodes.size() <= 0 || graph.getNodes().size() == selNodes.size()) {
			selNodes.clear();
			int minDegree = Integer.MAX_VALUE;
			for (Node n : graph.getNodes()) {
				if (n.getInDegree() <= minDegree) {
					if (n.getInDegree() < minDegree)
						selNodes.clear();
					selNodes.add(n);
					minDegree = n.getInDegree();
				}
			}
		}
		
		for (Node n : selNodes) {
			forest.put(n, null);
		}
		
		/* check all trees with selected nodes, whether they have one root */
		for (Node n : selNodes) {
			if (forest.containsKey(n)) {
				/* check circle connection by using the depth of each node */
				computeDepth(n);
				sourceNodes.put(
						n,
						new TreeContainer(
								treeMap,
								depthOffsets,
								bfsNum,
								maxNodeHeight));
				
				if (!rootedTree(n)) {
					ErrorMsg.addErrorMessage("The given graph is not a tree.");
				}
			}
		}
		/* check the trees which are left, whether they have one root */
		while (forest.keySet().iterator().hasNext()) {
			
			Node sourceNode = (Node) forest.keySet().iterator().next();
			// System.out.println("nodes left !!!   "+((IntegerAttribute) sourceNode.getAttribute("id")).getValue());
			
			/* check circle connection by using the depth of each node */
			computeDepth(sourceNode);
			sourceNodes.put(
					sourceNode,
					new TreeContainer(
							treeMap,
							depthOffsets,
							bfsNum,
							maxNodeHeight));
			
			if (!rootedTree(sourceNode)) {
				ErrorMsg.addErrorMessage("The given graph is not a tree.");
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
									treeMap,
									depthOffsets,
									bfsNum,
									maxNodeHeight));
					
					break;
				}
			}
			
		}
		
		try {
			graph.getListenerManager().transactionStarted(this);
			
			if (isRemoveBends) {
				for (Edge edge : graph.getEdges())
					AttributeHelper.removeEdgeBends(edge);
			}
			
			for (Node sourceNode : sourceNodes.keySet()) {
				treeMap = sourceNodes.get(sourceNode).getTreeMap();
				depthOffsets = sourceNodes.get(sourceNode).getDepthOffset();
				bfsNum = sourceNodes.get(sourceNode).getBfsNum();
				maxNodeHeight =
						(sourceNodes.get(sourceNode))
								.getMaxNodeHeight();
				
				initTreeMap(sourceNode);
				
				/* Computing the relative coordinates and the modifiers */
				computeRelativeCoordinates(sourceNode);
				
				/* Adjusting the relative coordinates from first step */
				adjustRelativeCoordinates(sourceNode);
				
				/* Computing the Y coordinates for each node */
				setTreeYCoordinates(sourceNode);
				
				/* Adjust the start coordinates for each tree */
				adjustCoordinates(sourceNode);
				
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
				ySpanningMax = 0;
				xSpanningMin = 0;
				ySpanningMin = 0;
				
				/*
				 * g.getListenerManager().transactionStarted(this);
				 * for (Iterator it = g.getNodesIterator(); it.hasNext();) {
				 * Node node = (Node) it.next();
				 * setLabel(
				 * node,
				 * "" + ((IntegerAttribute) node.getAttribute("id")).getValue());
				 * }
				 * g.getListenerManager().transactionFinished(this);
				 */
			}
			
			for (Edge edge : tempEdges) {
				graph.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
			}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
		
	}
	
	/**
	 * Bus layout format
	 */
	private void formatBusLayout() {
		for (Iterator iterator = graph.getEdgesIterator(); iterator.hasNext();) {
			
			Edge edge = ((Edge) iterator.next());
			
			Node node1 = edge.getSource();
			if (bfsNum.get(node1) != null) {
				Node node2 = edge.getTarget();
				double yCoord1 =
						((depthOffsets.get(bfsNum.get(node1))))
								.doubleValue()
								+ ((maxNodeHeight.get(bfsNum.get(node1))))
										.doubleValue()
								/ 2
								+ yNodeDistance / 2;
				double yCoord2 =
						((depthOffsets.get(bfsNum.get(node2))))
								.doubleValue()
								+ ((maxNodeHeight.get(bfsNum.get(node2))))
										.doubleValue()
								/ 2
								+ yNodeDistance / 2;
				
				double yCoord = yStart + Math.min(yCoord1, yCoord2);
				
				double xCoord1 = getX(node1);
				double xCoord2 = getX(node2);
				
				EdgeShapeAttribute edgeShape =
						(EdgeShapeAttribute) (edge.getAttribute(SHAPE));
				edgeShape.setValue(
						"org.graffiti.plugins.views.defaults.PolyLineEdgeShape");
				HashMap bends = new LinkedHashMap();
				
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
	
	/**
	 * Initialize the level of each node
	 */
	private void computeDepth(Node startNode) {
		
		LinkedList<Node> queue = new LinkedList<Node>();
		
		maxNodeHeight = new LinkedHashMap<Integer, Double>();
		
		treeMap = new LinkedList();
		
		depthOffsets = new LinkedHashMap<Integer, Double>();
		
		bfsNum = new LinkedHashMap<Node, Integer>();
		
		treeMap.add(new LinkedList());
		queue.addLast(startNode);
		bfsNum.put(startNode, new Integer(0));
		
		forest.remove(startNode);
		
		/* Compute the maximum height of the root node */
		depthOffsets.put(
				new Integer(0),
				new Double(getNodeHeight(startNode) / 2.0));
		maxNodeHeight.put(
				new Integer(0),
				new Double(getNodeHeight(startNode)));
		
		/* BreadthFirstSearch algorithm which calculates the depth of the tree */
		while (!queue.isEmpty()) {
			
			Node v = queue.removeFirst();
			/* Walk through all neighbours of the last node */
			for (Node neighbour : v.getOutNeighbors()) {
				
				/* Not all neighbours, just the neighbours not visited yet */
				if (!bfsNum.containsKey(neighbour)) {
					Integer depth =
							new Integer((bfsNum.get(v)).intValue() + 1);
					
					double nodeHeight = getNodeHeight(neighbour);
					
					/* Compute the maximum height of nodes in each level of the tree */
					Double maxNodeHeightValue =
							maxNodeHeight.get(depth);
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
					
					if (treeMap.size() <= depth.intValue()) {
						treeMap.add(new LinkedList());
					}
					bfsNum.put(neighbour, depth);
					
					queue.addFirst(neighbour);
				}
			}
		}
		
		for (int depth = 1; depth < maxNodeHeight.size(); depth++) {
			
			double nodeHeight =
					(maxNodeHeight.get(new Integer(depth))).doubleValue();
			double nodeHeightAncestor =
					(maxNodeHeight.get(new Integer(depth - 1)))
							.doubleValue();
			double yOffsetAncestor =
					(depthOffsets.get(new Integer(depth - 1)))
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
	 * Initialize the treeMap LinkedList
	 */
	private void initTreeMap(Node sourceNode) {
		
		/* Initializing the treeMap */
		for (Iterator postorder = postorder(sourceNode).iterator(); postorder.hasNext();) {
			Node node = (Node) postorder.next();
			int depth = (bfsNum.get(node)).intValue();
			((LinkedList) treeMap.get(depth)).add(node);
		}
		
	}
	
	/**
	 * Compute the y coordinates by bfsNum
	 */
	private void setTreeYCoordinates(Node sourceNode) {
		for (Iterator nodesIterator = postorder(sourceNode).iterator(); nodesIterator.hasNext();) {
			Node n = (Node) nodesIterator.next();
			if (bfsNum.containsKey(n)) {
				
				int level = (bfsNum.get(n)).intValue();
				
				ySpanningMax =
						Math.max(
								ySpanningMax,
								(depthOffsets.get(new Integer(level))).doubleValue()
										+ getNodeHeight(n) / 2.0);
				
				ySpanningMin =
						Math.min(
								ySpanningMin,
								(depthOffsets.get(new Integer(level)))
										.doubleValue()
										- getNodeHeight(n) / 2.0);
				
				setY(
						n,
						yStart
								+ (depthOffsets.get(new Integer(level)))
										.doubleValue());
			}
		}
		
	}
	
	/*
	 * Adjust the start coordinates for each tree
	 */
	private void adjustCoordinates(Node sourceNode) {
		
		for (Iterator nodesIterator = preorder(sourceNode).iterator(); nodesIterator.hasNext();) {
			Node node = (Node) nodesIterator.next();
			setX(node, getX(node) + Math.abs(xSpanningMin));
			setY(node, getY(node) + Math.abs(ySpanningMin));
		}
		
		xSpanningMax += Math.abs(xSpanningMin);
		ySpanningMax += Math.abs(ySpanningMin);
	}
	
	/**
	 * Walk through the tree and computes the relative coordinates und
	 * modifiers for each node
	 */
	private void computeRelativeCoordinates(Node sourceNode) {
		
		Iterator nodesIterator = postorder(sourceNode).iterator();
		
		modifierField = new LinkedHashMap();
		cumulModifier = new LinkedHashMap();
		relativeCoords = new LinkedHashMap();
		
		while (nodesIterator.hasNext()) {
			Node node = (Node) nodesIterator.next();
			double modifier = 0.0;
			double relativeCoord = 0.0;
			Iterator nodeSuccessors = getSuccessors(node);
			
			/* Is node a leaf ? */
			if (!nodeSuccessors.hasNext()) {
				double localXdist = xNodeDistance;
				Node leftBrother = getLeftBrother(node);
				if (AttributeHelper.isHiddenGraphElement(leftBrother))
					localXdist = 0;
				/* Has left brother ? */
				if (leftBrother != null) {
					double leftBrotherWidth = getNodeWidth(leftBrother);
					
					double nodeWidth = getNodeWidth(node);
					/* compute relative coordinates from neighbour */
					relativeCoord =
							leftOrientedCoordinates(leftBrother)
									+ (leftBrotherWidth / 2.0
											+ localXdist
											+ nodeWidth / 2.0);
				}
				/* no left brother, set relative coordinates to 0, modifier 0 */
			} else {
				/* Compute the relative coordinates from children's center. */
				Node leftMost = (Node) nodeSuccessors.next();
				double leftMostCoord =
						((Double) relativeCoords.get(leftMost)).doubleValue();
				double leftMostModif =
						((Double) modifierField.get(leftMost)).doubleValue();
				Node rightMost = leftMost;
				while (nodeSuccessors.hasNext()) {
					rightMost = (Node) nodeSuccessors.next();
				}
				double rightMostCoord =
						((Double) relativeCoords.get(rightMost)).doubleValue();
				double rightMostModif =
						((Double) modifierField.get(rightMost)).doubleValue();
				relativeCoord =
						((leftMostCoord + leftMostModif)
						+ (rightMostCoord + rightMostModif))
						/ 2;
				Node leftBrother = getLeftBrother(node);
				/* Has left brother ? */
				if (leftBrother != null) {
					/* yes left brother, set relative coordinates to center of children's position */
					double leftBrothCoord =
							leftOrientedCoordinates(leftBrother);
					double leftBrotherWidth = getNodeWidth(leftBrother);
					double nodeWidth = getNodeWidth(node);
					
					/*
					 * Looks for an overlap with an already positioned
					 * tree on the left. In this case, compute how much the tree needs to be adjusted
					 * (to the right) and adjust both relative coordinates and modifier fields.
					 */
					
					double localXdist = xNodeDistance;
					
					if (AttributeHelper.isHiddenGraphElement(leftBrother))
						localXdist = 0;
					
					modifier =
							Math.max(
									0.0,
									(leftBrotherWidth / 2.0
											+ localXdist
											+ nodeWidth / 2.0)
											- (relativeCoord - leftBrothCoord));
				}
				/* no left brother, set relative coordinates to center of children's position, modifier 0 */
			}
			
			modifierField.put(node, new Double(modifier));
			/* initialization of cumulative modifier field needed for adjustRelativeCoordinates */
			cumulModifier.put(node, new Double(0.0));
			relativeCoords.put(node, new Double(relativeCoord));
		}
		
	}
	
	/**
	 * Computes the real coordinates for the successors by using the sum of the modifiers
	 */
	private void adjustRelativeCoordinates(Node sourceNode) {
		
		Iterator nodesIterator = preorder(sourceNode).iterator();
		while (nodesIterator.hasNext()) {
			Node node = (Node) nodesIterator.next();
			double nodeModif =
					((Double) modifierField.get(node)).doubleValue();
			double nodeCumulModif =
					((Double) cumulModifier.get(node)).doubleValue();
			
			double nodeXCoord =
					((Double) relativeCoords.get(node)).doubleValue()
							+ nodeCumulModif
							+ nodeModif;
			
			xSpanningMax =
					Math.max(xSpanningMax, nodeXCoord + getNodeWidth(node) / 2.0);
			xSpanningMin =
					Math.min(xSpanningMin, nodeXCoord - getNodeWidth(node) / 2.0);
			
			/* Setting the computed x position */
			setX(node, nodeXCoord + xStart);
			
			/* Adjusting modifier fields for successors */
			Iterator nodeSuccessors = getSuccessors(node);
			while (nodeSuccessors.hasNext()) {
				Node succ = (Node) nodeSuccessors.next();
				((Double) cumulModifier.get(succ)).doubleValue();
				cumulModifier.put(
						succ,
						new Double(nodeModif + nodeCumulModif));
			}
		}
		
	}
	
	/**
	 * Computes the coordinates from the left side of the tree by
	 * using the coordinates of the given leftBrother and the modifiers of the
	 * predecessors.
	 * 
	 * @param node
	 * @return
	 */
	private double leftOrientedCoordinates(Node node) {
		
		Node ancestor = node;
		double nodeCoord = ((Double) relativeCoords.get(node)).doubleValue();
		nodeCoord += ((Double) modifierField.get(node)).doubleValue();
		Iterator predecessors = getPredecessors(ancestor);
		while (predecessors.hasNext()) {
			ancestor = (Node) predecessors.next();
			Double ancestorModField = ((Double) modifierField.get(ancestor));
			if (ancestorModField == null) {
				return nodeCoord;
			} else {
				nodeCoord += ancestorModField.doubleValue();
				predecessors = getPredecessors(ancestor);
			}
		}
		return nodeCoord;
	}
	
	/**
	 * Return the width dimension of the given node n
	 * 
	 * @param n
	 *           node
	 * @return
	 */
	private double getNodeWidth(Node n) {
		
		if (AttributeHelper.isHiddenGraphElement(n))
			return 0;
		
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
		if (AttributeHelper.isHiddenGraphElement(n))
			return 0;
		
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
		// return null;
		return "Tree Layout (RT)";
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
