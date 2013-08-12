// ==============================================================================
//
// AdjListGraph.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AdjListGraph.java,v 1.1 2011-01-31 09:04:44 klukas Exp $

package org.graffiti.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.event.ListenerManager;

// import org.graffiti.util.MultipleIterator;

/**
 * Implements the <code>Graph</code> -interface using an adjacency list
 * representation of the graph. Requires <code>AdjListNode</code> and <code>AdjListEdge</code> as implementations for nodes and edges. Every
 * method modifying the graph will inform the <code>ListenerManager</code> about the modification according to the description in <code>Graph</code>.
 * 
 * @version $Revision: 1.1 $
 * @see Graph
 * @see AbstractGraph
 * @see AdjListNode
 * @see AdjListEdge
 * @see AbstractNode
 * @see AbstractEdge
 */
public class AdjListGraph extends AbstractGraph implements Graph {
	// ~ Static fields/initializers =============================================
	
	// /** The logger for the current class. */
	// private static final Logger logger = Logger.getLogger(AdjListGraph.class
	// .getName());
	
	private int id;
	
	private static int graphCount = 0;
	
	private static long maxGraphElementId = 0; // Long.MIN_VALUE;
	
	private String idName = null;
	
	@Override
	public String toString() {
		return "Graph ID=" + id + " / " + getName();
	}
	
	// ~ Instance fields ========================================================
	
	/** The list containing the nodes of the graph. */
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<Edge> edges = new ArrayList<Edge>();
	
	/**
	 * set to True if graph has been modified.
	 */
	private boolean modified;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new instance of an <code>AdjListGraph</code>. Sets the <code>ListenerManager</code> of the new instance to the default
	 * <code>ListenerManager</code>.
	 */
	public AdjListGraph() {
		super();
		synchronized (AdjListGraph.class) {
			graphCount++;
			id = graphCount;
		}
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#getNumberOfNodes()
	 */
	@Override
	public int getNumberOfNodes() {
		return nodes.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#getNodes()
	 */
	@Override
	public List<Node> getNodes() {
		return nodes;
	}
	
	/**
	 * Constructs a new instance of an <code>AdjListGraph</code>. Sets the <code>ListenerManager</code> of the new instance to the default
	 * <code>ListenerManager</code>.
	 * 
	 * @param coll
	 *           the <code>CollectionAttribute</code> of the currently created <code>AdjListGraph</code> instance.
	 */
	public AdjListGraph(CollectionAttribute coll) {
		super(coll);
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
	}
	
	/**
	 * Constructs a new instance of an <code>AdjListGraph</code>. Sets the <code>ListenerManager</code> of the new instance to the specified one.
	 * 
	 * @param listenerManager
	 *           listener manager for the graph.
	 */
	public AdjListGraph(ListenerManager listenerManager) {
		super(listenerManager);
	}
	
	/**
	 * Constructs a new instance of an <code>AdjListGraph</code>. Sets the <code>ListenerManager</code> of the new instance to the specified one.
	 * 
	 * @param listenerManager
	 *           listener manager for the graph.
	 * @param coll
	 *           the <code>CollectionAttribute</code> of the currently created <code>AdjListGraph</code> instance.
	 */
	public AdjListGraph(ListenerManager listenerManager, CollectionAttribute coll) {
		super(listenerManager, coll);
	}
	
	/**
	 * Constructs a new instance of an <code>AdjListGraph</code> from an
	 * instance of any <code>Graph</code> implementation. Copies all nodes and
	 * edges from g into the new graph.
	 * 
	 * @param g
	 *           any <code>Graph</code> implementation out of which an <code>AdjListGraph</code> shall be generated.
	 * @param listenerManager
	 *           listener manager for the graph.
	 */
	public AdjListGraph(Graph g, ListenerManager listenerManager) {
		super(listenerManager);
		this.addGraph(g);
	}
	
	/**
	 * Constructs a new instance of an <code>AdjListGraph</code> from an
	 * instance of any <code>Graph</code> implementation. Copies all nodes and
	 * edges from g into the new graph.
	 * 
	 * @param g
	 *           any <code>Graph</code> implementation out of which an <code>AdjListGraph</code> shall be generated.
	 * @param listenerManager
	 *           listener manager for the graph.
	 * @param coll
	 *           the <code>CollectionAttribute</code> of the currently created <code>AdjListGraph</code> instance.
	 */
	public AdjListGraph(Graph g, ListenerManager listenerManager,
						CollectionAttribute coll) {
		super(listenerManager, coll);
		this.addGraph(g);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * The given node is moved to the front of the node list.
	 * 
	 * @param node
	 */
	public void setNodeFirst(Node node) {
		if (nodes.remove(node)) {
			nodes.add(0, node);
		}
	}
	
	/**
	 * The given node is moved to the end of the node list.
	 * 
	 * @param node
	 */
	public void setNodeLast(Node node) {
		if (nodes.remove(node)) {
			nodes.add(node);
		}
	}
	
	/**
	 * Returns an iterator over the nodes of the graph. Note that the remove
	 * operation is not supported by this iterator.
	 * 
	 * @return an iterator containing the nodes of the graph.
	 */
	public Iterator<Node> getNodesIterator() {
		return nodes.iterator();
	}
	
	/**
	 * Creates and returns a copy of the graph. The attributes are copied as well
	 * as all nodes and edges.
	 * 
	 * @return a copy of the graph.
	 */
	public Object copy() {
		AdjListGraph newGraph = new AdjListGraph((CollectionAttribute) this
							.getAttributes().copy());
		newGraph.addGraph(this);
		
		return newGraph;
	}
	
	/**
	 * Adds a new edge to the current graph. Informs the ListenerManager about
	 * the new node.
	 * 
	 * @param source
	 *           the source of the edge to add.
	 * @param target
	 *           the target of the edge to add.
	 * @param directed
	 *           <code>true</code> if the edge shall be directed, <code>false</code> otherwise.
	 * @return the new edge.
	 */
	@Override
	protected Edge doAddEdge(Node source, Node target, boolean directed) {
		assert (source != null) && (target != null);
		
		AdjListEdge edge = (AdjListEdge) createEdge(source, target, directed);
		((AdjListNode) source).addOutEdge(edge);
		((AdjListNode) target).addInEdge(edge);
		
		edges.add(edge);
		
		return edge;
	}
	
	/**
	 * Adds a new edge to the current graph. Informs the ListenerManager about
	 * the new node.
	 * 
	 * @param source
	 *           the source of the edge to add.
	 * @param target
	 *           the target of the edge to add.
	 * @param directed
	 *           <code>true</code> if the edge shall be directed, <code>false</code> otherwise.
	 * @param col
	 *           CollectionAttribute that will be added to the new Edge
	 * @return the new edge.
	 */
	@Override
	protected Edge doAddEdge(Node source, Node target, boolean directed,
						CollectionAttribute col) {
		assert (source != null) && (target != null) && (col != null);
		
		AdjListEdge edge = (AdjListEdge) createEdge(source, target, directed, col);
		((AdjListNode) source).addOutEdge(edge);
		
		((AdjListNode) target).addInEdge(edge);
		
		edges.add(edge);
		
		return edge;
	}
	
	/**
	 * Adds a new node to the graph. Informs the ListenerManager about the new
	 * node.
	 * 
	 * @param node
	 *           DOCUMENT ME!
	 */
	@Override
	protected void doAddNode(Node node) {
		assert node != null;
		setModified(true);
		nodes.add(node);
	}
	
	/**
	 * Deletes the current graph by resetting all its attributes. The graph is
	 * then equal to a new generated graph i.e. the list of nodes and edges will
	 * be empty. A special event for clearing the graph will be passed to the
	 * listener manager.
	 */
	@Override
	protected void doClear() {
		setModified(true);
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
	}
	
	/**
	 * Deletes the given edge from the current graph. Implicitly calls the
	 * ListenerManager by calling <code>AdjListNode.removeEdge()</code> in the
	 * source and target node of the edge.
	 * 
	 * @param e
	 *           the edge to delete.
	 */
	@Override
	protected void doDeleteEdge(Edge e) {
		assert e != null;
		if (((AdjListNode) (e.getSource())).getAllInEdges().contains(e))
			((AdjListNode) (e.getSource())).removeInEdge(e);
		if (((AdjListNode) (e.getTarget())).getAllInEdges().contains(e))
			((AdjListNode) (e.getTarget())).removeInEdge(e);
		
		if (((AdjListNode) (e.getSource())).getAllOutEdges().contains(e))
			((AdjListNode) (e.getSource())).removeOutEdge(e);
		if (((AdjListNode) (e.getTarget())).getAllOutEdges().contains(e))
			((AdjListNode) (e.getTarget())).removeOutEdge(e);
		
		((AdjListEdge) e).setGraphToNull();
		edges.remove(e);
		setModified(true);
	}
	
	/**
	 * Deletes the given node. First all in- and out-going edges will be deleted
	 * using <code>deleteEdge()</code> and thereby informs the ListenerManager
	 * implicitly.
	 * 
	 * @param n
	 *           the node to delete.
	 * @throws GraphElementNotFoundException
	 *            DOCUMENT ME!
	 */
	@Override
	protected void doDeleteNode(Node n) throws GraphElementNotFoundException {
		assert n != null;
		
		Collection<Edge> ce = n.getEdges();
		for (Edge e : ce) {
			deleteEdge(e);
		}
		
		((AdjListNode) n).setGraphToNull();
		nodes.remove(n);
		
		setModified(true);
	}
	
	/**
	 * Creates a new <code>AdjListNode</code> that is in the current graph.
	 * 
	 * @return the newly created node.
	 */
	@Override
	protected Node createNode() {
		setModified(true);
		return new AdjListNode(this);
	}
	
	/**
	 * Creates a new <code>AdjListNode</code> that is in the current graph. And
	 * initializes it with the given <code>CollectionAttribute</code>.
	 * 
	 * @param col
	 *           DOCUMENT ME!
	 * @return the newly created node.
	 */
	@Override
	protected Node createNode(CollectionAttribute col) {
		assert col != null;
		
		setModified(true);
		return new AdjListNode(this, col);
	}
	
	/**
	 * Creates a new <code>AdjListEdge</code> that is in the current graph.
	 * 
	 * @param source
	 *           the source of the edge to add.
	 * @param target
	 *           the target of the edge to add.
	 * @param directed
	 *           <code>true</code> if the edge shall be directed, <code>false</code> otherwise.
	 * @return the newly created edge.
	 */
	protected Edge createEdge(Node source, Node target, boolean directed) {
		setModified(true);
		return new AdjListEdge(this, source, target, directed);
	}
	
	/**
	 * Creates a new <code>AdjListEdge</code> that is in the current graph. And
	 * initializes it with the given <code>CollectionAttribute</code>.
	 * 
	 * @param source
	 *           the source of the edge to add.
	 * @param target
	 *           the target of the edge to add.
	 * @param directed
	 *           <code>true</code> if the edge shall be directed, <code>false</code> otherwise.
	 * @param col
	 *           CollectionAttribute that will be added to the new Edge
	 * @return the new edge.
	 */
	protected Edge createEdge(Node source, Node target, boolean directed,
						CollectionAttribute col) {
		assert col != null;
		
		setModified(true);
		return new AdjListEdge(this, source, target, directed, col);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#isModified()
	 */
	public boolean isModified() {
		return modified;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#setModified(boolean)
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#getEdges()
	 */
	@Override
	public Collection<Edge> getEdges() {
		return edges;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#getNumberOfEdges()
	 */
	@Override
	public int getNumberOfEdges() {
		return edges.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#getEdges(org.graffiti.graph.Node,
	 * org.graffiti.graph.Node)
	 */
	@Override
	public Collection<Edge> getEdges(Node n1, Node n2) {
		HashSet<Edge> result = new HashSet<Edge>();
		for (Edge edge : n1.getEdges())
			if (edge.getTarget() == n2 || edge.getSource() == n2)
				result.add(edge);
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#deleteAll(java.util.List)
	 */
	public void deleteAll(Collection<GraphElement> graphelements) {
		for (GraphElement ge : graphelements) {
			if ((ge instanceof Edge) && containsEdge((Edge) ge)) {
				deleteEdge((Edge) ge);
			}
			if ((ge instanceof Node) && containsNode((Node) ge)) {
				deleteNode((Node) ge);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#setName(java.lang.String)
	 */
	public void setName(String name) {
		idName = name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#getName()
	 */
	public String getName() {
		return getName(false);
	}
	
	public String getName(boolean fullName) {
		if (idName == null)
			return "[not saved " + id + "]";
		if (fullName)
			return idName;
		else {
			String res;
			if (idName.lastIndexOf(File.separator) > 0)
				res = idName.substring(idName.lastIndexOf(File.separator)
									+ File.separator.length());
			else
				if (idName.lastIndexOf("/") > 0)
					res = idName.substring(idName.lastIndexOf("/")
										+ "/".length());
				else
					res = idName;
			return res;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#numberNodes()
	 */
	public void numberGraphElements() {
		long startNumber = maxGraphElementId; // avoid recursive modification
		for (Edge e : getEdges())
			e.setID(++startNumber);
		for (Node n : getNodes())
			n.setID(++startNumber);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.graph.Graph#setMaxId(long)
	 */
	public void checkMaxGraphElementId(long id) {
		if (id > maxGraphElementId)
			maxGraphElementId = id;
	}
	
	public void setListenerManager(ListenerManager l) {
		listenerManager = l;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
