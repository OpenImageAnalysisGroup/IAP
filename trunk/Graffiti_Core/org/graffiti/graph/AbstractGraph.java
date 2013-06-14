// ==============================================================================
//
// AbstractGraph.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractGraph.java,v 1.2 2011-09-11 05:15:09 klukas Exp $
package org.graffiti.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.AbstractAttributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeConsumer;
import org.graffiti.attributes.AttributeExistsException;
import org.graffiti.attributes.AttributeTypesManager;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.UnificationException;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.ListenerManager;

/**
 * Provides further functionality for graphs.
 * 
 * @version $Revision: 1.2 $
 * @see Graph
 * @see AdjListGraph
 */
public abstract class AbstractGraph extends AbstractAttributable implements
					Graph {
	// ~ Static fields/initializers =============================================
	
	/** The logger for the current class. */
	private static final Logger logger = Logger.getLogger(AbstractGraph.class
						.getName());
	
	// ~ Instance fields ========================================================
	
	/** The <code> AttributeTypesManager</code> for handling attribute types. */
	protected AttributeTypesManager attTypesManager;
	
	/**
	 * The <code>ListenerManager</code> for handling events modifying the
	 * graph.
	 */
	protected ListenerManager listenerManager;
	
	boolean isDirected = true;
	
	/**
	 * The attribute, which will be (deep-)copied and added to every new edge.
	 * This attribute is extended by the <code>addAttributeConsumer</code> method.
	 */
	private CollectionAttribute defaultEdgeAttribute;
	
	/**
	 * The attribute, which will be (deep-)copied and added to every new node.
	 * This attribute is extended by the <code>addAttributeConsumer</code> method.
	 */
	private CollectionAttribute defaultNodeAttribute;
	
	/** Contains a set of attribute consumers. */
	private final Set<AttributeConsumer> attributeConsumers;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new instance of an <code>AbstractGraph</code>. Sets the <code>ListenerManager</code> of the new instance to the default
	 * <code>ListenerManager</code>.
	 */
	public AbstractGraph() {
		this.listenerManager = new ListenerManager();
		this.attributeConsumers = new HashSet<AttributeConsumer>();
		BooleanAttribute a = (BooleanAttribute) getAttributes().getCollection().get("direced");
		if (a == null)
			setBoolean("directed", true);
		else
			a.setBoolean(true);
	}
	
	/**
	 * Constructs a new instance of an <code>AbstractGraph</code>. Sets the <code>ListenerManager</code> of the new instance to the default
	 * <code>ListenerManager</code>.
	 * 
	 * @param coll
	 *           the <code>CollectionAttribute</code> of the currently
	 *           created <code>AbstractGraph</code> instance.
	 */
	public AbstractGraph(CollectionAttribute coll) {
		super(coll);
		this.listenerManager = new ListenerManager();
		this.attributeConsumers = new HashSet<AttributeConsumer>();
		setBoolean("directed", true);
	}
	
	/**
	 * Constructs a new instance of an <code>AdjListGraph</code>. Sets the <code>ListenerManager</code> of the new instance to the specified one.
	 * 
	 * @param listenerManager
	 *           listener manager for the graph.
	 */
	public AbstractGraph(ListenerManager listenerManager) {
		this.listenerManager = listenerManager;
		this.attributeConsumers = new HashSet<AttributeConsumer>();
		setBoolean("directed", true);
	}
	
	/**
	 * Constructs a new instance of an <code>AdjListGraph</code>. Sets the <code>ListenerManager</code> of the new instance to the specified one.
	 * 
	 * @param listenerManager
	 *           listener manager for the graph.
	 * @param coll
	 *           the <code>CollectionAttribute</code> of the currently
	 *           created <code>AbstractGraph</code> instance.
	 */
	public AbstractGraph(ListenerManager listenerManager,
						CollectionAttribute coll) {
		super(coll);
		this.listenerManager = listenerManager;
		this.attributeConsumers = new HashSet<AttributeConsumer>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>AttributeTypesManager</code> of the graph.
	 * 
	 * @return the <code>AttributeTypesManager</code> of the graph.
	 */
	public AttributeTypesManager getAttTypesManager() {
		return this.attTypesManager;
	}
	
	/**
	 * Indicates whether the graph is directed.
	 * A graph is directed if the graph setting states this.
	 * 
	 * @return a boolean indicating whether the graph is directed.
	 */
	public boolean isDirected() {
		return isDirected;
		/*
		 * if (getEdges().size() == 0)
		 * return isDirected;
		 * else
		 * return getEdges().size() == getNumberOfDirectedEdges();
		 */
	}
	
	/**
	 * Sets all edges to be <code>directed</code>.
	 * <p>
	 * If <code>directed</code> is <code>true</code>, standard arrows are set, if it is <code>false</code>, all arrows of all edges are removed.
	 * 
	 * @see org.graffiti.graph.Graph#setDirected(boolean)
	 */
	public void setDirected(boolean directed) {
		isDirected = directed;
		for (Iterator<Edge> it = getEdgesIterator(); it.hasNext();) {
			Edge edge = it.next();
			if (directed != edge.isDirected()) {
				edge.setDirected(directed);
			}
		}
		setBoolean("directed", directed);
		ArrayList<Edge> el = new ArrayList<Edge>(getEdges());
		for (Edge e : el)
			deleteEdge(e);
		for (Edge e : el)
			addEdgeCopy(e, e.getSource(), e.getTarget());
	}
	
	/**
	 * When passing a true value, all undirected edges in the graph will be
	 * set to be directed. V.v. for a false value.
	 * A second parameter indicates that if the edge arrows should be corrected.
	 * 
	 * @param directed
	 * @param adjustArrows
	 */
	public void setDirected(boolean directed, boolean adjustArrows) {
		isDirected = directed;
		if (adjustArrows) {
			for (Iterator<Edge> it = getEdgesIterator(); it.hasNext();) {
				Edge edge = it.next();
				if (directed != edge.isDirected()) {
					edge.setDirected(directed);
				}
				if (directed) {
					edge.setString("graphics.arrowtail", "");
					edge.setString("graphics.arrowhead",
										"org.graffiti.plugins.views.defaults.StandardArrowShape");
				} else {
					edge.setString("graphics.arrowtail", "");
					edge.setString("graphics.arrowhead", "");
				}
			}
		}
		setBoolean("directed", directed);
		ArrayList<Edge> el = new ArrayList<Edge>(getEdges());
		for (Edge e : el)
			deleteEdge(e);
		for (Edge e : el)
			addEdgeCopy(e, e.getSource(), e.getTarget());
	}
	
	/**
	 * Returns a <code>java.util.Collection</code> containing all the edges of
	 * the current graph.
	 * 
	 * @return a <code>java.util.Collection</code> containing all the edges of
	 *         the current graph.
	 */
	public Collection<Edge> getEdges() {
		Set<Edge> h = new HashSet<Edge>();
		
		for (Iterator<Node> nodeIt = getNodesIterator(); nodeIt.hasNext();) {
			Node n = (nodeIt.next());
			
			h.addAll(n.getEdges());
		}
		
		return h;
	}
	
	/**
	 * Returns a collection containing all the edges between n1 and n2. There
	 * can be more than one edge between two nodes. The edges returned by this
	 * method can go from n1 to n2 or vice versa, be directed or not.
	 * 
	 * @param n1
	 *           the first node.
	 * @param n2
	 *           the second node.
	 * @return a <code>Collection</code> containing all edges between n1 and
	 *         n2, an empty collection if there is no edge between the two
	 *         nodes.
	 * @exception GraphElementNotFoundException
	 *               if one of the nodes is not
	 *               contained in the graph.
	 */
	public Collection<Edge> getEdges(Node n1, Node n2)
						throws GraphElementNotFoundException {
		assert (n1 != null) && (n2 != null);
		
		Collection<Edge> col = new LinkedList<Edge>();
		
		if ((this == n1.getGraph()) && (this == n2.getGraph())) {
			for (Iterator<Edge> it = n1.getEdgesIterator(); it.hasNext();) {
				Edge e = it.next();
				
				if ((n2 == e.getSource()) || (n2 == e.getTarget())) {
					col.add(e);
				}
			}
		} else {
			throw new GraphElementNotFoundException(
								"one of the nodes is not in the graph");
		}
		
		return col;
	}
	
	/**
	 * Returns an iterator over the edges of the graph.
	 * 
	 * @return an iterator over the edges of the graph.
	 */
	public Iterator<Edge> getEdgesIterator() {
		return getEdges().iterator();
	}
	
	/**
	 * Returns <code>true</code> if the graph is empty. The graph is equal to a
	 * graph which has been cleared.
	 * 
	 * @return <code>true</code> if the graph is empty, <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return getNumberOfNodes() == 0;
	}
	
	/**
	 * Returns all nodes and all edges contained in this graph.
	 * 
	 * @return Collection
	 */
	public Collection<GraphElement> getGraphElements() {
		Collection<Node> nodes = getNodes();
		Collection<Edge> edges = getEdges();
		Collection<GraphElement> ges = new ArrayList<GraphElement>(nodes.size() + edges.size());
		ges.addAll(nodes);
		ges.addAll(edges);
		
		return ges;
	}
	
	/**
	 * Returns the ListenerManager of the current graph.
	 * 
	 * @return the ListenerManager of the current graph.
	 */
	public ListenerManager getListenerManager() {
		return this.listenerManager;
	}
	
	/**
	 * Returns a list containing a copy of the node list of the graph.
	 * Removing elements from this collection will have no effect on the graph
	 * whereas nodes can be modified.
	 * 
	 * @return a new <code>java.util.List</code> containing all the nodes
	 *         of the graph.
	 */
	public List<Node> getNodes() {
		return new LinkedList<Node>(getNodes());
	}
	
	/**
	 * Returns the number of directed edges of the graph.
	 * 
	 * @return the number of directed edges of the graph.
	 */
	public int getNumberOfDirectedEdges() {
		int numberOfDirectedEdges = 0;
		
		for (Iterator<Edge> edgeIt = getEdgesIterator(); edgeIt.hasNext();) {
			Edge testedEdge = edgeIt.next();
			
			if (testedEdge.isDirected()) {
				numberOfDirectedEdges++;
			}
		}
		
		logger.fine("this graph contains " + numberOfDirectedEdges
							+ " directed edge(s)");
		
		return numberOfDirectedEdges;
	}
	
	/**
	 * Returns the number of edges of the graph.
	 * 
	 * @return the number of edges of the graph.
	 */
	public int getNumberOfEdges() {
		return getEdges().size();
	}
	
	/**
	 * Returns the number of nodes in the graph.
	 * 
	 * @return the number of nodes of the graph.
	 */
	public int getNumberOfNodes() {
		return getNodes().size();
	}
	
	/**
	 * Returns the number of undirected edges in the graph.
	 * 
	 * @return the number of undirected edges in the graph.
	 */
	public int getNumberOfUndirectedEdges() {
		int numberOfUndirectedEdges = getEdges().size()
							- getNumberOfDirectedEdges();
		
		logger.fine("this graph contains " + numberOfUndirectedEdges
							+ " undirected edge(s)");
		
		return numberOfUndirectedEdges;
	}
	
	/**
	 * Indicates whether the graph is undirected. A graph is undirected if all
	 * the edges are undirected.
	 * 
	 * @return A boolean indicating whether the graph is undirected.
	 */
	public boolean isUndirected() {
		if (getEdges().size() == 0)
			return !isDirected;
		else
			return getEdges().size() == getNumberOfUndirectedEdges();
	}
	
	/**
	 * Adds the given attribute consumer to the list of attribute consumers.
	 * 
	 * @param attConsumer
	 *           the attribute consumer to add.
	 * @throws UnificationException
	 *            in the context of unification failures.
	 */
	public void addAttributeConsumer(AttributeConsumer attConsumer)
						throws UnificationException {
		unifyWithNodeAttribute(attConsumer.getNodeAttribute());
		unifyWithEdgeAttribute(attConsumer.getEdgeAttribute());
		addAttributeToExistingNodes(attConsumer.getNodeAttribute());
		addAttributeToExistingEdges(attConsumer.getEdgeAttribute());
		attributeConsumers.add(attConsumer);
	}
	
	/**
	 * Adds a new edge to the current graph. Informs the ListenerManager about
	 * the new node. This method adds a copy of the <code>defaultEdgeAttributes</code> after the <code>preEdgeAdded</code> and before the
	 * <code>postEdgeAdded</code> event.
	 * 
	 * @param source
	 *           the source of the edge to add.
	 * @param target
	 *           the target of the edge to add.
	 * @param directed
	 *           <code>true</code> if the edge shall be directed, <code>false</code> otherwise.
	 * @return the new edge.
	 * @exception GraphElementNotFoundException
	 *               if any of the nodes cannot be
	 *               found in the graph.
	 */
	public Edge addEdge(Node source, Node target, boolean directed)
						throws GraphElementNotFoundException {
		assert (source != null) && (target != null);
		// logger.info("adding a new edge to the graph");
		
		ListenerManager listMan = this.getListenerManager();
		
		if (this != source.getGraph()) {
			logger.severe("throwing GENFException, because the given source "
								+ "was not in the same graph");
			throw new GraphElementNotFoundException(
								"source is not in the same graph as the edge");
		}
		
		if (this != target.getGraph()) {
			logger.severe("throwing GENFException, because the given target "
								+ "was not in the same graph");
			throw new GraphElementNotFoundException(
								"target is not in the same graph as the edge");
		}
		
		if (listMan != null)
			listMan.preEdgeAdded(new GraphEvent(source, target));
		
		Edge edge = doAddEdge(source, target, directed);
		
		// add the edge's default attribute
		if (defaultEdgeAttribute != null) {
			edge.addAttribute((Attribute) defaultEdgeAttribute.copy(), "");
		}
		
		if (listMan != null)
			listMan.postEdgeAdded(new GraphEvent(edge));
		
		return edge;
	}
	
	/**
	 * Adds a new edge to the current graph. Informs the ListenerManager about
	 * the new node. This method does not add any <code>defaultEdgeAttributes</code>.
	 * 
	 * @param source
	 *           the source of the edge to add.
	 * @param target
	 *           the target of the edge to add.
	 * @param directed
	 *           <code>true</code> if the edge shall be directed, <code>false</code> otherwise.
	 * @param col
	 *           the <code>CollectionAttribute</code> with which the edge is
	 *           initialized.
	 * @return the new edge.
	 * @exception GraphElementNotFoundException
	 *               if any of the nodes cannot be
	 *               found in the graph.
	 */
	public Edge addEdge(Node source, Node target, boolean directed,
						CollectionAttribute col) throws GraphElementNotFoundException {
		assert (source != null) && (target != null) && (col != null);
		// logger.info("adding a new edge with collection attributes to the graph");
		
		source.setGraph(this); // CK
		target.setGraph(this); // CK
		
		// System.out.println("Add edge from "+source.toString()+" to "+target.toString());
		
		if (this != source.getGraph()) {
			logger.severe("throwing GENFException, because the given source "
								+ "was not in the same graph");
			throw new GraphElementNotFoundException(
								"source is not in the same graph as the edge");
		}
		
		if (this != target.getGraph()) {
			logger.severe("throwing GENFException, because the given target "
								+ "was not in the same graph");
			throw new GraphElementNotFoundException(
								"target is not in the same graph as the edge");
		}
		
		// logger.info("adding a new edge to the graph");
		ListenerManager listMan = this.getListenerManager();
		if (listMan != null)
			listMan.preEdgeAdded(new GraphEvent(source, target));
		
		Edge edge = doAddEdge(source, target, directed, col);
		if (listMan != null)
			listMan.postEdgeAdded(new GraphEvent(edge));
		
		return edge;
	}
	
	/**
	 * Adds a copy of the specified edge to the graph as a new edge between the
	 * specified source and target node. Informs the ListenerManager about the
	 * newly added edge through the call to <code>addEdge()</code>. Also
	 * informs the ListenerManager about the copy of the attributes added to
	 * the edge by adding them separatly throug <code>CollectionAttribute.add(Attribute)</code>.
	 * 
	 * @param edge
	 *           the <code>Egde</code> which to copy and add.
	 * @param source
	 *           the source <code>Node</code> of the copied and added edge.
	 * @param target
	 *           the target <code>Node</code> of the copied and added edge.
	 * @return DOCUMENT ME!
	 */
	public Edge addEdgeCopy(Edge edge, Node source, Node target) {
		assert (edge != null) && (source != null) && (target != null);
		
		CollectionAttribute col = (CollectionAttribute) edge.getAttributes()
							.copy();
		Edge newEdge = addEdge(source, target, edge.isDirected(), col);
		newEdge.setID(edge.getID()); // copied edges share the same edge id
		newEdge.setViewID(edge.getViewID());
		newEdge.setGraph(this);
		return newEdge;
	}
	
	/**
	 * Adds a Graph g to the current graph. Graph g will be copied and then all
	 * its nodes and edges will be added to the current graph. Like this g
	 * will not be destroyed.
	 * 
	 * @param g
	 *           the Graph to be added.
	 */
	public Collection<GraphElement> addGraph(Graph g) {
		assert g != null;
		// setDirected(g.isDirected(), true);
		Collection<GraphElement> newElements = new ArrayList<GraphElement>();
		for (Attribute a : g.getAttributes().getCollection().values()) {
			try {
				// try {
				addAttribute((Attribute) a.copy(), "");
				/*
				 * } catch(AttributeNotFoundException e) {
				 * attributes.getAttribute(a.getId()).setValue(a.getValue());
				 * }
				 */
			} catch (AttributeExistsException aee) {
				Attribute b = getAttribute(a.getPath()); // +Attribute.SEPARATOR+a.getId()
				b.setValue(a.getValue());
			}
		}
		
		Map<Node, Node> hm = new HashMap<Node, Node>();
		
		List<Node> nc = new ArrayList<Node>(g.getNodes());
		
		for (Node oldNode : nc) {
			Node newNode = addNodeCopy(oldNode);
			if (newNode == null) {
				ErrorMsg.addErrorMessage("Node is NULL");
			}
			hm.put(oldNode, newNode);
			newElements.add(newNode);
		}
		
		for (Edge oldEdge : g.getEdges()) {
			CollectionAttribute col = (CollectionAttribute) oldEdge.getAttributes().copy();
			
			Node source = hm.get(oldEdge.getSource());
			Node target = hm.get(oldEdge.getTarget());
			if (source == null || target == null) {
				ErrorMsg.addErrorMessage("Src or Tgt node is NULL");
			} else {
				try {
					Edge newEdge = this.addEdge(source, target, oldEdge.isDirected(), col);
					newEdge.setViewID(oldEdge.getViewID());
					newElements.add(newEdge);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		return newElements;
	}
	
	/**
	 * Adds a new node to the graph. Informs the ListenerManager about the new
	 * node. This method adds a copy of the <code>defaultNodeAttribute</code> to the newly created node (after the <code>preNodeAdded</code> event
	 * and before the <code>postNodeAdded</code> event).
	 * 
	 * @return the new node.
	 */
	public Node addNode() {
		// logger.info("adding a new node to the graph");
		
		Node node = createNode();
		GraphEvent ga = new GraphEvent(node);
		
		listenerManager.preNodeAdded(ga);
		doAddNode(node);
		
		// add the node's default attribute
		if (defaultNodeAttribute != null) {
			node.addAttribute((Attribute) defaultNodeAttribute.copy(), "");
		}
		
		listenerManager.postNodeAdded(ga);
		
		// logger.fine("returning the created node and exiting addNode()");
		
		return node;
	}
	
	/**
	 * Adds a new node to the graph. Informs the ListenerManager about the new
	 * node. Default node attributes (<code>defaultNodeAttribute</code>) are
	 * not added by this method.
	 * 
	 * @param col
	 *           the <code>CollectionAttribute</code> the node is initialized
	 *           with.
	 * @return the new node.
	 */
	public Node addNode(CollectionAttribute col) {
		// assert col != null;
		if (col == null)
			col = AttributeHelper.getDefaultGraphicsAttributeForNode(100, 100);
		// logger.info("adding a new node to the graph");
		
		Node node = createNode(col);
		
		GraphEvent ga = new GraphEvent(node);
		
		if (listenerManager != null)
			listenerManager.preNodeAdded(ga);
		doAddNode(node);
		if (listenerManager != null)
			listenerManager.postNodeAdded(ga);
		
		// logger.fine("returning the created node and exiting addNode()");
		
		return node;
	}
	
	/**
	 * Adds a copy of the specified node to the graph and returns the copy.
	 * Informs the ListenerManager about the newly added node in the same way
	 * as if a completely new node was added. Also informs the ListenerManager
	 * about the addition of attributes by using the <code>add(Attribute)</code> method of <code>CollectionAttribute</code>.
	 * 
	 * @param node
	 *           the <code>Node</code> which to copy and to add.
	 * @return the newly created node.
	 */
	public Node addNodeCopy(Node node) {
		assert node != null;
		
		CollectionAttribute col = (CollectionAttribute) node.getAttributes()
							.copy();
		Node newNode = this.addNode(col);
		newNode.setID(node.getID()); // copied nodes share the same ID
		newNode.setViewID(node.getViewID());
		newNode.setGraph(this);
		return newNode;
	}
	
	/**
	 * Returns <code>true</code>, if the graph contains an edge between the
	 * nodes n1 and n2, <code>false</code> otherwise.
	 * 
	 * @param n1
	 *           first node of the edge to search for.
	 * @param n2
	 *           second node of the edge to search for.
	 * @return <code>true</code>, if the graph contains an edge between the
	 *         nodes n1 and n2 <code>false</code> otherwise.
	 * @exception GraphElementNotFoundException
	 *               if any of the nodes cannot be
	 *               found in the graph.
	 */
	public boolean areConnected(Node n1, Node n2)
						throws GraphElementNotFoundException {
		assert (n1 != null) && (n2 != null);
		
		return getEdges(n1, n2).size() > 0;
	}
	
	/**
	 * Deletes the current graph by resetting all its attributes. The graph is
	 * then equal to a new generated graph i.e. the list of nodes and edges
	 * will be empty. A special event for clearing the graph will be passed to
	 * the listener manager.
	 */
	public void clear() {
		ListenerManager listMan = getListenerManager();
		if (listMan != null)
			listMan.preGraphCleared(new GraphEvent(this));
		doClear();
		if (listMan != null)
			listMan.postGraphCleared(new GraphEvent(this));
	}
	
	/**
	 * Returns <code>true</code>, if the graph contains the specified edge, <code>false</code> otherwise.
	 * 
	 * @param e
	 *           the edge to search for.
	 * @return <code>true</code>, if the graph contains the edge e, <code>false</code> otherwise.
	 */
	public boolean containsEdge(Edge e) {
		assert e != null;
		
		return getEdges().contains(e);
	}
	
	/**
	 * Returns <code>true</code>, if the graph contains the specified node, <code>false</code> otherwise.
	 * 
	 * @param n
	 *           the node to search for.
	 * @return <code>true</code>, if the graph contains the node n, <code>false</code> otherwise.
	 */
	public boolean containsNode(Node n) {
		assert n != null;
		
		return getNodes().contains(n);
	}
	
	/**
	 * Deletes the given edge from the current graph. Informs the
	 * ListenerManager about the deletion.
	 * 
	 * @param e
	 *           the edge to delete.
	 * @exception GraphElementNotFoundException
	 *               if the edge to delete cannot be
	 *               found in the graph.
	 */
	public void deleteEdge(Edge e) throws GraphElementNotFoundException {
		assert e != null;
		// if (e.getGraph()==null) return;
		
		if (!getEdges().contains(e)) {
			return;
		}
		
		// logger.info("deleting edge e from this graph");
		
		ListenerManager listMan = this.getListenerManager();
		GraphEvent ga = new GraphEvent(e);
		
		if (listMan != null)
			listMan.preEdgeRemoved(ga);
		doDeleteEdge(e);
		if (listMan != null)
			listMan.postEdgeRemoved(ga);
	}
	
	/**
	 * Deletes the given node. First all in- and out-going edges will be
	 * deleted using <code>deleteEdge()</code> and thereby informs the
	 * ListenerManager implicitly. Then deletes the node and informs the
	 * ListenerManager about the deletion.
	 * 
	 * @param n
	 *           the node to delete.
	 * @exception GraphElementNotFoundException
	 *               if the node to delete cannot be
	 *               found in the graph.
	 */
	public void deleteNode(Node n) throws GraphElementNotFoundException {
		assert n != null;
		assert n.getGraph() != null;
		if (n.getGraph() != this) {
			ErrorMsg.addErrorMessage("the node was not found in this graph");
			/*
			 * throw new GraphElementNotFoundException(
			 * "the node was not found in this graph");
			 */
			return;
		}
		
		// logger.info("deleting a node from the graph");
		
		ListenerManager listMan = this.getListenerManager();
		GraphEvent ga = new GraphEvent(n);
		
		if (listMan != null)
			listMan.preNodeRemoved(ga);
		doDeleteNode(n);
		if (listMan != null)
			listMan.postNodeRemoved(ga);
		n = null;
	}
	
	/**
	 * Returns <code>true</code>, if the given attribute consumer was in the
	 * list of attribute consumers and could be removed.
	 * 
	 * @param attConsumer
	 *           DOCUMENT ME!
	 * @return <code>true</code>, if the given attribute consumer was in the
	 *         list of attribute consumers and could be removed.
	 */
	public boolean removeAttributeConsumer(AttributeConsumer attConsumer) {
		return attributeConsumers.remove(attConsumer);
	}
	
	/**
	 * Adds a new edge to the current graph.
	 * 
	 * @param source
	 *           the source of the edge to add.
	 * @param target
	 *           the target of the edge to add.
	 * @param directed
	 *           <code>true</code> if the edge shall be directed, <code>false</code> otherwise.
	 * @return the new edge.
	 * @exception GraphElementNotFoundException
	 *               if any of the nodes cannot be
	 *               found in the graph.
	 */
	protected abstract Edge doAddEdge(Node source, Node target, boolean directed)
						throws GraphElementNotFoundException;
	
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
	 *           the <code>CollectionAttribute</code> with which the edge is
	 *           initialized.
	 * @return the new edge.
	 * @exception GraphElementNotFoundException
	 *               if any of the nodes cannot be
	 *               found in the graph.
	 */
	protected abstract Edge doAddEdge(Node source, Node target,
						boolean directed, CollectionAttribute col)
						throws GraphElementNotFoundException;
	
	/**
	 * Adds the node to the graph.
	 * 
	 * @param node
	 *           the node to add
	 */
	protected abstract void doAddNode(Node node);
	
	/**
	 * Deletes the current graph by resetting all its attributes. The graph is
	 * then equal to a new generated graph i.e. the list of nodes and edges
	 * will be empty.
	 */
	protected abstract void doClear();
	
	/**
	 * Deletes the given edge from the current graph.
	 * 
	 * @param e
	 *           the edge to delete.
	 * @exception GraphElementNotFoundException
	 *               if the edge to delete cannot be
	 *               found in the graph.
	 */
	protected abstract void doDeleteEdge(Edge e)
						throws GraphElementNotFoundException;
	
	/**
	 * Deletes the given node. First all in- and out-going edges will be
	 * deleted using <code>deleteEdge()</code> and thereby informs the
	 * ListenerManager implicitly.
	 * 
	 * @param n
	 *           the node to delete.
	 * @exception GraphElementNotFoundException
	 *               if the node to delete cannot be
	 *               found in the graph.
	 */
	protected abstract void doDeleteNode(Node n)
						throws GraphElementNotFoundException;
	
	/**
	 * Creates a new <code>Node</code>.
	 * 
	 * @return the newly created node.
	 */
	abstract Node createNode();
	
	/**
	 * Creates a new <code>Node</code> that is initialize with the given <code>CollectionAttribute</code>.
	 * 
	 * @return the newly created node.
	 */
	abstract Node createNode(CollectionAttribute col);
	
	/**
	 * Tries to add the given attribute to every edge in this graph.
	 * 
	 * @param att
	 *           the attribute to add to every edge.
	 */
	private void addAttributeToExistingEdges(CollectionAttribute att) {
		if (att == null) {
			return;
		}
		
		for (Edge e : getEdges()) {
			try {
				e.addAttribute((Attribute) att.copy(), "");
			} catch (AttributeExistsException aee) {
				//
			} catch (Exception err) {
				ErrorMsg.addErrorMessage(err);
			}
		}
	}
	
	/**
	 * Tries to add the given attribute to every node in this graph.
	 * 
	 * @param att
	 *           the attribute to add to every node.
	 */
	private void addAttributeToExistingNodes(CollectionAttribute att) {
		if (att == null) {
			return;
		}
		
		for (Node n : getNodes()) {
			try {
				n.addAttribute((Attribute) att.copy(), "");
			} catch (AttributeExistsException aee) {
				//
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	/**
	 * Unifies a given collection attribute with the default edge attribute.
	 * 
	 * @param c
	 *           the new collection attribute to add.
	 * @throws UnificationException
	 *            DOCUMENT ME!
	 */
	private void unifyWithEdgeAttribute(CollectionAttribute c)
						throws UnificationException {
		if (c == null) { // base case
		
			return;
		} else {
			if (defaultEdgeAttribute == null) { // base case
				defaultEdgeAttribute = c;
			} else {
				// check, if the types of c and defaultAttribute are compatible
				// (maybe throw an UnificationException)
				// check, if it is possible to add another attribute to the
				// current attribute hierarchy
				// and add the given collection attributes sub attributes to the
				// default attribute
				String attClazName = defaultEdgeAttribute.getClass().getName();
				
				if (defaultEdgeAttribute instanceof HashMapAttribute) {
					// if (attClazName
					// .equals("org.graffiti.attributes.HashMapAttribute")) {
					for (Iterator<String> i = c.getCollection().keySet().iterator(); i
										.hasNext();) {
						String id = i.next();
						
						try {
							defaultEdgeAttribute.add((Attribute) c.getAttribute(id)
												.copy());
						} catch (AttributeExistsException aee) {
							// ErrorMsg.addErrorMessage(aee.getLocalizedMessage());
						}
					}
				} else
					if (defaultEdgeAttribute instanceof HashMapAttribute) {
						// c.getClass().getName().equals(
						// "org.graffiti.attributes.HashMapAttribute")) {
						CollectionAttribute tmp = (CollectionAttribute) c.copy();
						
						for (Iterator<String> i = defaultEdgeAttribute.getCollection().keySet()
											.iterator(); i.hasNext();) {
							String id = i.next();
							
							try {
								tmp.add((Attribute) defaultEdgeAttribute.getAttribute(id)
													.copy());
							} catch (AttributeExistsException aee) {
								// ErrorMsg.addErrorMessage(aee.getLocalizedMessage());
							}
						}
						
						defaultEdgeAttribute = tmp;
					} else {
						throw new UnificationException("Cannot unify " + attClazName
											+ " and " + c.getClass().getName());
					}
			}
		}
	}
	
	/**
	 * Unifies a given collection attribute with the default node attribute.
	 * 
	 * @param c
	 *           the new collection attribute to add.
	 */
	private void unifyWithNodeAttribute(CollectionAttribute c) {
		if (c == null) { // base case
		
			return;
		} else {
			if (defaultNodeAttribute == null) { // base case
				defaultNodeAttribute = c;
			} else {
				// check, if the types of c and defaultAttribute are compatible
				// (maybe throw an UnificationException)
				// check, if it is possible to add another attribute to the
				// current attribute hierarchy
				// and add the given collection attributes sub attributes to the
				// default attribute
				// String attClazName = defaultNodeAttribute.getClass().getName();
				//
				// if(attClazName.equals(
				// "org.graffiti.attributes.HashMapAttribute"))
				// if(defaultNodeAttribute instanceof CollectionAttribute)
				{
					for (Iterator<String> i = c.getCollection().keySet().iterator(); i
										.hasNext();) {
						String id = i.next();
						
						try {
							defaultNodeAttribute.add((Attribute) c.getAttribute(id)
												.copy());
						} catch (AttributeExistsException aee) {
						}
					}
				}
				
				// // else if(c.getClass().getName().equals("org.graffiti.attributes.HashMapAttribute"))
				// else if(c instanceof CollectionAttribute)
				{
					CollectionAttribute tmp = (CollectionAttribute) c.copy();
					
					for (Iterator<String> i = defaultNodeAttribute.getCollection().keySet()
										.iterator(); i.hasNext();) {
						String id = i.next();
						
						try {
							tmp.add((Attribute) defaultNodeAttribute.getAttribute(id)
												.copy());
						} catch (AttributeExistsException aee) {
						}
					}
					
					defaultNodeAttribute = tmp;
				}
				/*
				 * else
				 * {
				 * throw new UnificationException("Cannot unify " +
				 * defaultNodeAttribute.getClass().getName() + " and " +
				 * c.getClass().getName());
				 * }
				 */
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
