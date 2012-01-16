// ==============================================================================
//
// MatrixModel.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MatrixModel.java,v 1.1 2011-01-31 09:03:37 klukas Exp $

package org.graffiti.plugins.views.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.GraphListener;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

/**
 * Contains the matrix model, which is used by the JTable ui component.
 * 
 * @version $Revision: 1.1 $
 */
public class MatrixModel
					extends AbstractTableModel
					implements GraphListener {
	// ~ Static fields/initializers =============================================
	
	/** The logger for the current class. */
	// private static final Logger logger = Logger.getLogger(MatrixModel.class.getName());
	
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	
	/** The list of nodes. Maps from row/col index to <code>Node</code>. */
	private ArrayList<Node> nodes;
	
	/** The graph, this model refers to. */
	private Graph graph;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new matrix model.
	 * 
	 * @param graph
	 *           DOCUMENT ME!
	 */
	public MatrixModel(Graph graph) {
		super();
		this.graph = graph;
		this.nodes = new ArrayList<Node>();
		
		// add all nodes of the given graph to our row/col index to node mapping
		for (Iterator<Node> i = graph.getNodesIterator(); i.hasNext();) {
			nodes.add(i.next());
		}
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return row != col;
	}
	
	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class getColumnClass(int col) {
		return Boolean.class;
	}
	
	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return graph.getNumberOfNodes();
	}
	
	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return graph.getNumberOfNodes();
	}
	
	/**
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		Node source = (Node) nodes.get(row);
		Node target = (Node) nodes.get(col);
		
		Collection<Edge> c = graph.getEdges(source, target);
		
		boolean found = false;
		
		for (Iterator<Edge> i = c.iterator(); i.hasNext() && !found;) {
			Edge e = (Edge) i.next();
			
			if ((e.getSource() == source) && (e.getTarget() == target)) {
				found = true;
			}
		}
		
		// found an edge, so: remove it
		if (found) {
			graph.getListenerManager().transactionStarted(this);
			
			for (Iterator<Edge> i = c.iterator(); i.hasNext();) {
				Edge e = (Edge) i.next();
				
				if ((e.getSource() == source) && (e.getTarget() == target)) {
					graph.deleteEdge(e);
				}
			}
			
			graph.getListenerManager().transactionFinished(this, false, null);
		} else { // did not find an edge between source and target =>
		
			// add a new edge...
			addEdge(source, target);
		}
	}
	
	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		if ((row < nodes.size()) && (col < nodes.size())) {
			Node source = (Node) nodes.get(row);
			Node target = (Node) nodes.get(col);
			
			Collection<Edge> c = graph.getEdges(source, target);
			
			boolean found = false;
			
			for (Iterator<Edge> i = c.iterator(); i.hasNext() && !found;) {
				Edge e = (Edge) i.next();
				
				if ((e.getSource() == source) && (e.getTarget() == target)) {
					found = true;
				}
			}
			
			return new Boolean(found);
		} else {
			return null;
		}
	}
	
	@Override
	public String getColumnName(int col) {
		if (col < nodes.size()) {
			Node n = (Node) nodes.get(col);
			return AttributeHelper.getLabel(n, "Col " + col);
		} else
			return super.getColumnName(col);
	}
	
	public String getRowName(int row) {
		if (row < nodes.size()) {
			Node n = (Node) nodes.get(row);
			return AttributeHelper.getLabel(n, "Row " + row);
		} else
			return super.getColumnName(row);
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postEdgeAdded(GraphEvent)
	 */
	public void postEdgeAdded(GraphEvent e) {
		fireTableDataChanged();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postEdgeRemoved(GraphEvent)
	 */
	public void postEdgeRemoved(GraphEvent e) {
		fireTableDataChanged();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postGraphCleared(GraphEvent)
	 */
	public void postGraphCleared(GraphEvent e) {
		nodes.clear();
		fireTableStructureChanged();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postNodeAdded(GraphEvent)
	 */
	public void postNodeAdded(GraphEvent e) {
		nodes.add(e.getNode());
		fireTableStructureChanged();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#postNodeRemoved(GraphEvent)
	 */
	public void postNodeRemoved(GraphEvent e) {
		// the array list access may be a bit inefficient here...
		nodes.remove(e.getNode());
		fireTableStructureChanged();
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preEdgeAdded(GraphEvent)
	 */
	public void preEdgeAdded(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preEdgeRemoved(GraphEvent)
	 */
	public void preEdgeRemoved(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preGraphCleared(GraphEvent)
	 */
	public void preGraphCleared(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preNodeAdded(GraphEvent)
	 */
	public void preNodeAdded(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.GraphListener#preNodeRemoved(GraphEvent)
	 */
	public void preNodeRemoved(GraphEvent e) {
	}
	
	/**
	 * @see org.graffiti.event.TransactionListener#transactionFinished(TransactionEvent)
	 */
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		nodes.clear();
		
		// add all nodes of the given graph to our row/col index to node mapping
		for (Iterator<Node> i = graph.getNodesIterator(); i.hasNext();) {
			nodes.add(i.next());
		}
		
		fireTableStructureChanged();
	}
	
	/**
	 * @see org.graffiti.event.TransactionListener#transactionStarted(TransactionEvent)
	 */
	public void transactionStarted(TransactionEvent e) {
	}
	
	/**
	 * Adds an edge between the given source and target.
	 * 
	 * @param source
	 *           the source node.
	 * @param target
	 *           the target node.
	 */
	private void addEdge(Node source, Node target) {
		// TODO configure, whether the edge is directed or undirected
		// TODO dont do this here: perhaps in its own controller
		graph.addEdge(source, target, false);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
