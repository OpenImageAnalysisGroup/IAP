/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.11.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.fish_eye;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.graffiti.attributes.Attribute;
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
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.SelectionParameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree.TreeContainer;

/**
 * An implementation of a fish eye layout algorithm.
 * 
 * @author Joerg Bartelheimer
 */

@SuppressWarnings("unchecked")
public class FishEyeLayout extends AbstractAlgorithm {
	
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
	 * Distortion of the fish eye
	 */
	private float distortion = 0;
	
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
	 * Remove all bends
	 */
	private boolean isRemoveBends = true;
	
	/**
	 * Polar coordinate system mapping
	 */
	private boolean isPolar = false;
	
	/**
	 * x coordinate of start point
	 */
	private double xStart = 100;
	
	/**
	 * y coordinate of start point
	 */
	private double yStart = 100;
	
	/**
	 * The currently registered graph.
	 */
	private Graph g;
	
	/**
	 * All trees are initialized by this variable.
	 */
	private HashMap forest;
	
	/**
	 * The roots in the forest.
	 */
	private HashMap sourceNodes = new HashMap();
	
	/**
	 * Currently selected node.
	 */
	private Selection selection;
	
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
	 * Save all edges of the actual tree here
	 */
	HashSet<Edge> edges = new HashSet<Edge>();
	
	/**
	 * x coordinate of start point
	 */
	private double xStartParam = 100;
	
	/**
	 * y coordinate of start point
	 */
	private double yStartParam = 100;
	
	/**
	 * Construct a new FishEyeLayout algorithm instance.
	 */
	public FishEyeLayout() {
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#check()
	 */
	@Override
	public void check() throws PreconditionException {
		if (g.getNumberOfNodes() <= 0) {
			throw new PreconditionException("The graph is empty. Cannot run tree layouter.");
		}
		
		tempEdges = new LinkedList();
		
		sourceNodes = new HashMap();
		
		forest = new HashMap();
		for (Iterator iterator = g.getNodesIterator(); iterator.hasNext();) {
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
									new TreeContainer(bfsNum, maxNodeHeight, edges));
				
				if (!rootedTree(sourceNode)) {
					throw new PreconditionException("The given graph is not a tree.");
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
								new TreeContainer(bfsNum, maxNodeHeight, edges));
			
			if (!rootedTree(sourceNode)) {
				for (Iterator it = tempEdges.iterator(); it.hasNext();) {
					Edge edge = (Edge) it.next();
					g.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
				}
				throw new PreconditionException("The given graph has trees with multiple roots.");
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
										new TreeContainer(bfsNum, maxNodeHeight, edges));
					
					break;
				}
			}
			
		}
		
	}
	
	/**
	 * Check whether the tree is rooted.
	 */
	public boolean rootedTree(Node rootNode) {
		
		edges = new HashSet<Edge>();
		
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
				
				/* save the eges of the tree for later transformations */
				edges.add(neighbourEdge);
				
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
						g.deleteEdge(neighbourEdge);
					}
				}
				/* any links from same level nodes ? */
				if (((Integer) bfsNum.get(node)).intValue() == ((Integer) bfsNum.get(neighbour)).intValue()) {
					
					tempEdges.add(neighbourEdge);
					g.deleteEdge(neighbourEdge);
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
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		SelectionParameter selParam =
							new SelectionParameter(
												"Start node",
												"Tree layouter will start only with a selected node.");
		
		DoubleParameter distortionParam =
							new DoubleParameter("distortion", "The distortion factor (0-9)");
		
		DoubleParameter xStartParam =
							new DoubleParameter(
												"x base",
												"The x coordinate of the starting point of the grid horizontal direction.");
		
		DoubleParameter yStartParam =
							new DoubleParameter(
												"y base",
												"The y coordinate of the starting point of the grid horizontal direction.");
		
		BooleanParameter polarParam =
							new BooleanParameter(
												isPolar,
												"polar mapping (cartesian)",
												"Polar or cartesian coordinate system mapping");
		
		BooleanParameter horizontalParam =
							new BooleanParameter(
												horizontalLayout,
												"trees in a row",
												"Place all trees in a row");
		
		BooleanParameter removeBendParam =
							new BooleanParameter(
												isRemoveBends,
												"remove bends",
												"Remove all bends in the forest");
		
		distortionParam.setDouble(distortion);
		xStartParam.setDouble(this.xStartParam);
		yStartParam.setDouble(this.yStartParam);
		
		return new Parameter[] {
							selParam,
							distortionParam,
							xStartParam,
							yStartParam,
							horizontalParam,
							removeBendParam,
							polarParam };
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		selection = ((SelectionParameter) params[0]).getSelection();
		distortion = ((DoubleParameter) params[1]).getDouble().floatValue();
		xStart = ((DoubleParameter) params[2]).getDouble().doubleValue();
		yStart = ((DoubleParameter) params[3]).getDouble().doubleValue();
		xStartParam = ((DoubleParameter) params[2]).getDouble().doubleValue();
		yStartParam = ((DoubleParameter) params[3]).getDouble().doubleValue();
		horizontalLayout =
							((BooleanParameter) params[4]).getBoolean().booleanValue();
		isRemoveBends =
							((BooleanParameter) params[5]).getBoolean().booleanValue();
		isPolar = ((BooleanParameter) params[6]).getBoolean().booleanValue();
		
	}
	
	/**
	 * Transform the coordinates in cartesian fish eye coordinates
	 * 
	 * @param focusX
	 * @param focusY
	 * @param x
	 *           position
	 * @param y
	 *           position
	 * @return transformed coordinates
	 */
	protected Point2D.Float transformCartesian(float focusX, float focusY, float x, float y) {
		
		/* scale to a smaller distortion number */
		float k = distortion / 1000;
		
		/* subtract the distance from the point of interest */
		float nx = Math.abs(focusX - x);
		float ny = Math.abs(focusY - y);
		
		/* transform the coordinates */
		float tx = ((k + 1.0F) * nx) / (k * nx + 1.0F);
		float ty = ((k + 1.0F) * ny) / (k * ny + 1.0F);
		
		if (x < focusX)
			tx = -tx;
		if (y < focusY)
			ty = -ty;
		
		/* add the distance from the point of interest again */
		float px = tx + focusX;
		float py = ty + focusY;
		
		return new Point2D.Float(px, py);
		
	}
	
	/**
	 * Transform the coordinates in polar fish eye coordinates
	 * 
	 * @param focusX
	 * @param focusY
	 * @param x
	 *           position
	 * @param y
	 *           position
	 * @return transformed coordinates
	 */
	protected Point2D.Float transformPolar(float focusX, float focusY, float x, float y) {
		
		/* scale to a smaller distortion number */
		float k = distortion / 1000;
		
		/* subtract the distance from the point of interest */
		float nx = Math.abs(focusX - x);
		float ny = Math.abs(focusY - y);
		
		/* transform the coordinates */
		float r = (float) Math.sqrt(nx * nx + ny * ny);
		float tr = ((k + 1.0F) * r) / (k * r + 1.0F);
		
		float tx = tr * (nx / r);
		float ty = tr * (ny / r);
		
		if (x < focusX)
			tx = -tx;
		if (y < focusY)
			ty = -ty;
		
		/* add the distance from the point of interest again */
		float px = tx + focusX;
		float py = ty + focusY;
		
		return new Point2D.Float(px, py);
		
	}
	
	/**
	 * Start computing the fish eye in a cartesian/polar coordinate system
	 */
	protected void computePositions() {
		
		Iterator nodesIterator = postorder(sourceNode).iterator();
		
		/* avoid division by zero +1 */
		float focusX = (float) getX(sourceNode) + 1;
		float focusY = (float) getY(sourceNode) + 1;
		
		/* transform each coordinate of a node by using the cartesian/polar distortion */
		while (nodesIterator.hasNext()) {
			Node node = (Node) nodesIterator.next();
			
			float x = (float) getX(node);
			float y = (float) getY(node);
			
			Point2D.Float p = null;
			/* transform the coordinate into the appropriate coordinate system */
			if (isPolar)
				p = transformPolar(focusX, focusY, x, y);
			else
				p = transformCartesian(focusX, focusY, x, y);
			
			setX(node, p.getX());
			setY(node, p.getY());
			
		}
		
		Iterator edgesIterator = edges.iterator();
		
		/* transform each coordinate of an edge by using the cartesian/polar distortion */
		while (edgesIterator.hasNext()) {
			Edge edge = (Edge) edgesIterator.next();
			
			Map bends = ((LinkedHashMapAttribute) edge.getAttribute(BENDS))
								.getCollection();
			
			Iterator bendsIterator = bends.values().iterator();
			while (bendsIterator.hasNext()) {
				
				CoordinateAttribute coord = (CoordinateAttribute) bendsIterator.next();
				
				float x = (float) coord.getX();
				float y = (float) coord.getY();
				
				Point2D.Float p = null;
				/* transform the coordinate into the appropriate coordinate system */
				if (isPolar)
					p = transformPolar(focusX, focusY, x, y);
				else
					p = transformCartesian(focusX, focusY, x, y);
				
				coord.setX(p.getX());
				coord.setY(p.getY());
				
			}
		}
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute(Graph)
	 *      The given graph must have at least one node.
	 */
	public void execute() {
		
		g.getListenerManager().transactionStarted(this);
		
		if (isRemoveBends) {
			removeAllBends();
		}
		
		for (Iterator iterator = sourceNodes.keySet().iterator(); iterator.hasNext();) {
			
			/* get a new tree by using the source node */
			sourceNode = (Node) iterator.next();
			/* get the depth of each node in the tree */
			edges = ((TreeContainer) sourceNodes.get(sourceNode)).getEdges();
			/* get the depth of each node in the tree */
			bfsNum = ((TreeContainer) sourceNodes.get(sourceNode)).getBfsNum();
			/* get the y poisition of each node in the tree */
			maxNodeHeight =
								((TreeContainer) sourceNodes.get(sourceNode))
													.getMaxNodeHeight();
			
			Iterator nodesIterator = postorder(sourceNode).iterator();
			
			/* the size variable for the actual tree */
			Rectangle rectSize = null;
			
			/* get the original size of the tree before the tree transformation */
			/* by adding the nodes into the rectangle */
			while (nodesIterator.hasNext()) {
				
				Node node = (Node) nodesIterator.next();
				
				if (rectSize == null) {
					rectSize =
										new Rectangle(
															(int) (getX(node) - getNodeWidth(node) / 2),
															(int) (getY(node) - getNodeHeight(node) / 2),
															0,
															0);
				}
				rectSize.add(
									getX(node) - getNodeWidth(node) / 2,
									getY(node) - getNodeHeight(node) / 2);
				rectSize.add(
									getX(node) + getNodeWidth(node) / 2,
									getY(node) + getNodeHeight(node) / 2);
			}
			
			/* start the tree transformation */
			computePositions();
			
			nodesIterator = postorder(sourceNode).iterator();
			
			Rectangle rectSizeComputed = null;
			
			/* get the transformed size of the tree after the tree transformation */
			/* by adding the nodes into the rectangle */
			while (nodesIterator.hasNext()) {
				Node node = (Node) nodesIterator.next();
				if (rectSizeComputed == null) {
					rectSizeComputed =
										new Rectangle(
															(int) (getX(node) - getNodeWidth(node) / 2),
															(int) (getY(node) - getNodeHeight(node) / 2),
															0,
															0);
				}
				
				rectSizeComputed.add(
									getX(node) - getNodeWidth(node) / 2,
									getY(node) - getNodeHeight(node) / 2);
				rectSizeComputed.add(
									getX(node) + getNodeWidth(node) / 2,
									getY(node) + getNodeHeight(node) / 2);
			}
			
			/* compute the x and y scale factors */
			float xs =
								(float) (rectSize.width) / (float) (rectSizeComputed.width);
			float ys =
								(float) (rectSize.height) / (float) (rectSizeComputed.height);
			
			/* compute the starting position of the tree */
			double dx =
								Math.abs(rectSizeComputed.getX() * xs - rectSize.getX());
			double dy =
								Math.abs(rectSizeComputed.getY() * ys - rectSize.getY());
			
			nodesIterator = postorder(sourceNode).iterator();
			
			/* put the tree at the right starting position and scale it to the original size */
			while (nodesIterator.hasNext()) {
				/* adjust all nodes of the actual tree */
				Node node = (Node) nodesIterator.next();
				
				/* scale coordinates */
				setX(node, (getX(node) * xs) - dx - rectSize.getX() + xStart);
				setY(node, (getY(node) * ys) - dy - rectSize.getY() + yStart);
				
			}
			
			Iterator edgesIterator = edges.iterator();
			
			while (edgesIterator.hasNext()) {
				/* adjust all edges of the actual tree */
				Edge edge = (Edge) edgesIterator.next();
				
				Map bends = ((LinkedHashMapAttribute) edge.getAttribute(BENDS))
									.getCollection();
				
				Iterator bendsIterator = bends.values().iterator();
				while (bendsIterator.hasNext()) {
					
					CoordinateAttribute coord = (CoordinateAttribute) bendsIterator.next();
					
					float x = (float) coord.getX();
					float y = (float) coord.getY();
					
					/* scale coordinates */
					coord.setX((x * xs) - dx - rectSize.getX() + xStart);
					coord.setY((y * ys) - dy - rectSize.getY() + yStart);
					
				}
			}
			
			if (horizontalLayout) {
				xStart += rectSize.width + xDistance;
			} else {
				yStart += rectSize.height + yDistance;
			}
			
		}
		for (Iterator iterator = tempEdges.iterator(); iterator.hasNext();) {
			Edge edge = (Edge) iterator.next();
			g.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
		}
		
		g.getListenerManager().transactionFinished(this);
		
	}
	
	/**
	 * Remove all bends
	 */
	private void removeAllBends() {
		for (Iterator iterator = g.getEdgesIterator(); iterator.hasNext();) {
			
			((LinkedHashMapAttribute) ((Edge) iterator.next()).getAttribute(
								BENDS)).setCollection(
									new HashMap());
		}
		
		for (Iterator iterator = tempEdges.iterator(); iterator.hasNext();) {
			
			((LinkedHashMapAttribute) ((Edge) iterator.next()).getAttribute(
								BENDS)).setCollection(
									new HashMap());
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
		
		result = dimAttr.getDimension().getWidth();
		
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
		
		result = dimAttr.getDimension().getHeight();
		
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
			coordAttr.setX(x);
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
			res = coordAttr.getX();
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
			res = coordAttr.getY();
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
			coordAttr.setY(y);
		}
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Fish Eye";
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#attach(Graph)
	 */
	public void attach(Graph g) {
		this.g = g;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		g = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Layout";
	}
}
