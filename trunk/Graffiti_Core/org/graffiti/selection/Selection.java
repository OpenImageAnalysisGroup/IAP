// ==============================================================================
//
// Selection.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Selection.java,v 1.3 2012-11-07 14:42:00 klukas Exp $

package org.graffiti.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.set.ListOrderedSet;
import org.graffiti.attributes.FieldAlreadySetException;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

/**
 * Contains selected nodes and edges.
 * <p>
 * </p>
 * <p>
 * Even if there are fundamental changes to the selection, don't use something like that:
 * </p>
 * <p>
 * <code>Selection newSel = new Selection(SelectionModel.ACTIVE);
 * editorSession. getSelectionModel().add(SelectionModel.ACTIVE);
 * editorSession.
 * getSelectionModel().setActiveSelection(SelectionModel.ACTIVE); </code>
 * </p>
 * <p>
 * Instead, remove all entries within the selection by calling <code>clear()</code> on the active selection (<code>editorSession.
 * getSelectionModel().getActiveSelection()</code>) and add the new selection elements to it. After all changes have been made and the system should be updated,
 * call <code>editorSession.getSelectionmodel().
 * selectionChanged()</code>
 * </p>
 * 
 * @version $Revision: 1.3 $
 */
public class Selection {
	// ~ Instance fields ========================================================
	
	/**
	 * The list of selected edges.
	 * 
	 * @see org.graffiti.graph.Edge
	 */
	private Set<Edge> edges;
	
	/**
	 * The list of selected nodes.
	 * 
	 * @see org.graffiti.graph.Node
	 */
	private Set<Node> nodes;
	
	/**
	 * Map of graph elements that changed state from unmarked to marked. This
	 * map is cleared after a selectionChanged event has been fired.
	 */
	private Map<GraphElement, GraphElement> newMarked;
	
	/**
	 * Map of graph elements that changed state from marked to unmarked. This
	 * map is cleared after a selectionChanged event has been fired.
	 */
	private Map<GraphElement, GraphElement> newUnmarked;
	
	/** The name of this selection. */
	private String name;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>Selection</code> instance with the given name.
	 */
	@SuppressWarnings("unchecked")
	public Selection() {
		this.nodes = new ListOrderedSet();
		this.edges = new ListOrderedSet();
		this.newMarked = new HashMap<GraphElement, GraphElement>();
		this.newUnmarked = new HashMap<GraphElement, GraphElement>();
	}
	
	/**
	 * Constructs a new <code>Selection</code> instance with the given name.
	 * 
	 * @param name
	 *           the name of this selection.
	 */
	@SuppressWarnings("unchecked")
	public Selection(String name) {
		this();
		this.name = name;
	}
	
	public Selection(String name, Collection<?> newElements) {
		this(name);
		addAll(newElements);
	}
	
	public Selection(Collection<?> newElements) {
		this();
		addAll(newElements);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the list of selected edges.
	 * 
	 * @see org.graffiti.graph.Edge
	 */
	public Collection<Edge> getEdges() {
		return this.edges;
	}
	
	/**
	 * Returns a list containing all edges and nodes in this selection.
	 * 
	 * @return a list containing all edges and nodes in this selection.
	 */
	public List<GraphElement> getElements() {
		List<GraphElement> all = new LinkedList<GraphElement>();
		all.addAll(this.edges);
		all.addAll(this.nodes);
		
		return all;
	}
	
	/**
	 * Returns <code>true</code> if no nodes or edges are selected.
	 * 
	 * @return <code>true</code> if no nodes or edges are selected.
	 */
	public boolean isEmpty() {
		return (this.nodes.isEmpty() && this.edges.isEmpty());
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name
	 *           The name to set
	 * @throws FieldAlreadySetException
	 *            DOCUMENT ME!
	 */
	public void setName(String name)
						throws FieldAlreadySetException {
		if (this.name == null) {
			this.name = name;
		} else {
			throw new FieldAlreadySetException(
								"Name of a selection may not be changed. Create new one.");
		}
	}
	
	/**
	 * Returns the name of this selection.
	 * 
	 * @return the name of this selection.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets the list of graph elements that have been marked but the selection
	 * listeners have not yet been notified.
	 * 
	 * @param newMarked
	 *           list of newly marked graph elements
	 */
	public void setNewMarked(Map<GraphElement, GraphElement> newMarked) {
		this.newMarked = newMarked;
	}
	
	/**
	 * Returns the map holding graph elements that have been marked since the
	 * last selectionChanged event.
	 * 
	 * @return map holding graph elements that have been marked since the last
	 *         selectionChanged event.
	 */
	public Map<GraphElement, GraphElement> getNewMarked() {
		return newMarked;
	}
	
	/**
	 * Sets the list of graph elements that have been unmarked but the
	 * selection listeners have not yet been notified.
	 * 
	 * @param newUnmarked
	 *           list of newly unmarked graph elements
	 */
	public void setNewUnmarked(Map<GraphElement, GraphElement> newUnmarked) {
		this.newUnmarked = newUnmarked;
	}
	
	/**
	 * Returns the map holding graph elements that have been unmarked since the
	 * last selectionChanged event.
	 * 
	 * @return the map holding graph elements that have been unmarked since the
	 *         last selectionChanged event.
	 */
	public Map<GraphElement, GraphElement> getNewUnmarked() {
		return newUnmarked;
	}
	
	/**
	 * Returns the list of selected nodes.
	 * 
	 * @see org.graffiti.graph.Node
	 */
	public Collection<Node> getNodes() {
		if (nodes == null)
			return new ArrayList<Node>();
		else
			return this.nodes;
	}
	
	/**
	 * Adds the given node or edge to the selection.
	 * 
	 * @param ge
	 *           the node or edge to add to the selection.
	 */
	public void add(GraphElement ge) {
		assert ge != null;
		if (ge instanceof Node) {
			this.add((Node) ge);
		} else {
			this.add((Edge) ge);
		}
	}
	
	/**
	 * Adds the given node to the list of selected nodes.
	 * 
	 * @param node
	 *           the node to add to the list of selected nodes.
	 */
	public void add(Node node) {
		assert node != null;
		if (!nodes.contains(node)) {
			this.nodes.add(node);
			newUnmarked.remove(node);
			this.newMarked.put(node, null);
		}
	}
	
	/**
	 * Adds the given edge to the list of selected edges.
	 * 
	 * @param edge
	 *           the edge to add to the list of selected edges.
	 */
	public void add(Edge edge) {
		if (!edges.contains(edge)) {
			assert edge != null;
			this.edges.add(edge);
			
			newUnmarked.remove(edge);
			this.newMarked.put(edge, null);
		}
	}
	
	/**
	 * Adds all (graph)elements of the given collection to this selection.
	 * 
	 * @param newElements
	 */
	public void addAll(Collection<?> newElements) {
		for (Object ge : newElements)
			add((GraphElement) ge);
	}
	
	public void removeAll(Collection<?> elements) {
		for (Object ge : elements) {
			remove((GraphElement) ge);
		}
	}
	
	// /**
	// * Adds all (graph)elements of the given collection to this selection.
	// *
	// * @param newElements
	// */
	// public void addAll(Collection<Node> nodes)
	// {
	// for(Node n : nodes)
	// add(n);
	// }
	
	/**
	 * Adds all elements from the given selection to this selection.
	 * 
	 * @param sel
	 */
	public void addSelection(Selection sel) {
		for (Iterator<?> it = sel.getElements().iterator(); it.hasNext();) {
			add((GraphElement) it.next());
		}
	}
	
	/**
	 * Remove all elements from this selection.
	 */
	@SuppressWarnings("unchecked")
	public void clear() {
		for (Iterator<Node> it = nodes.iterator(); it.hasNext();) {
			newUnmarked.put(it.next(), null);
		}
		
		for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
			newUnmarked.put(it.next(), null);
		}
		
		nodes = new ListOrderedSet();
		edges = new ListOrderedSet();
		newMarked = new HashMap<GraphElement, GraphElement>();
	}
	
	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		Selection newSel = new Selection();
		
		for (Iterator<?> it = getElements().iterator(); it.hasNext();) {
			GraphElement ge = (GraphElement) it.next();
			newSel.add(ge);
		}
		
		return newSel;
	}
	
	/**
	 * Removes the given node or edge from the selection.
	 * 
	 * @param ge
	 *           the node or edge to remove from the selection.
	 */
	public void remove(GraphElement ge) {
		if (ge instanceof Node) {
			this.nodes.remove(ge);
		} else {
			this.edges.remove(ge);
		}
		
		newMarked.remove(ge);
		this.newUnmarked.put(ge, null);
	}
	
	/**
	 * Gets a string describing the selection. Default: number of selected
	 * nodes and edges in a sentence.
	 * 
	 * @return a string describing the selection. Default: number of selected
	 *         nodes and edges in a sentence.
	 */
	@Override
	public String toString() {
		return nodes.size() + " nodes and " + edges.size() + " edges selected";
	}
	
	/**
	 * Clears the maps holding any changes since the last selectionChanged
	 * event. Should be called whenever a selectionChanged event has been
	 * generated with this selection.
	 */
	protected void committedChanges() {
		this.newMarked = new HashMap<GraphElement, GraphElement>();
		this.newUnmarked = new HashMap<GraphElement, GraphElement>();
	}
	
	public boolean contains(GraphElement ge) {
		if (ge instanceof Node)
			return this.nodes.contains(ge);
		if (ge instanceof Edge)
			return this.edges.contains(ge);
		return false;
	}
	
	public int getNumberOfNodes() {
		return nodes.size();
	}
	
	public Collection<Graph> getGraph() {
		Collection<Graph> res = new ArrayList<Graph>();
		if (nodes.size() > 0) {
			res.add(nodes.iterator().next().getGraph());
			return res;
		}
		if (edges.size() > 0) {
			res.add(edges.iterator().next().getGraph());
			return res;
		}
		return null;
	}
	
	public int getNumberOfEdges() {
		return edges.size();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
