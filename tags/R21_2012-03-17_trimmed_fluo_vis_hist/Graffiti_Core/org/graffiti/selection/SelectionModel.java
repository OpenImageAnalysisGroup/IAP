// ==============================================================================
//
// SelectionModel.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SelectionModel.java,v 1.1 2011-01-31 09:05:02 klukas Exp $

package org.graffiti.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Contains a list of selections and a reference to the current selection.
 * 
 * @author flierl
 * @version $Revision: 1.1 $
 */
public class SelectionModel {
	// ~ Static fields/initializers =============================================
	
	/** DOCUMENT ME! */
	public static final String ACTIVE = "active";
	
	// ~ Instance fields ========================================================
	
	/**
	 * The list of selections. Maps a <code>Selection.name</code> to a <code>Selection</code> instance.
	 */
	private Hashtable<String, Selection> selections;
	
	/**
	 * The list of listeners, that want to be informed about changes in the
	 * selection model.
	 * 
	 * @see SelectionListener
	 */
	private HashSet<SelectionListener> listeners;
	
	/** The current active selection. */
	private String activeSelection;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>SelectionModel</code>.
	 */
	public SelectionModel() {
		selections = new Hashtable<String, Selection>();
		listeners = new HashSet<SelectionListener>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the active selection to the given value. Informs all listeners
	 * about the change.
	 * 
	 * @param selectionName
	 *           the name of the new selection.
	 */
	public void setActiveSelection(String selectionName) {
		if (this.activeSelection != null) {
			Selection oldSel = this.selections.get(this.activeSelection);
			oldSel.clear();
		}
		
		this.activeSelection = selectionName;
		this.selectionChanged();
	}
	
	/**
	 * Sets the active selection to the given value. Informs all listeners
	 * about the change.
	 * 
	 * @param sel
	 *           the name of the new selection.
	 */
	public void setActiveSelection(Selection sel) {
		if (this.activeSelection != null) {
			Selection oldSel = this.selections.get(this.activeSelection);
			oldSel.clear();
			this.selectionChanged();
		}
		
		this.activeSelection = sel.getName();
		
		if (selections.put(sel.getName(), sel) == null) {
			SelectionEvent selectionEvent = new SelectionEvent(sel);
			selectionEvent.setAdded(true);
			
			for (Iterator<SelectionListener> it = listeners.iterator(); it.hasNext();) {
				((SelectionListener) it.next()).selectionListChanged(selectionEvent);
			}
		}
		
		this.selectionChanged();
	}
	
	/**
	 * Returns the active selection.
	 * 
	 * @return DOCUMENT ME!
	 */
	public Selection getActiveSelection() {
		if (activeSelection == null) {
			return null;
		} else {
			return selections.get(activeSelection);
		}
	}
	
	/**
	 * Adds the given selection to the list of selections.
	 * 
	 * @param selection
	 *           the selection object to add.
	 */
	public void add(Selection selection) {
		selections.put(selection.getName(), selection);
		
		SelectionEvent selectionEvent = new SelectionEvent(selection);
		selectionEvent.setAdded(true);
		
		for (Iterator<SelectionListener> it = listeners.iterator(); it.hasNext();) {
			((SelectionListener) it.next()).selectionListChanged(selectionEvent);
		}
	}
	
	/**
	 * Adds the given selection listener to the list of listeners.
	 * 
	 * @param listener
	 *           the selection listener to add.
	 */
	public void addSelectionListener(SelectionListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes the given selection for the list of selections.
	 * 
	 * @param selection
	 *           the selection to remove from the list.
	 */
	public void remove(Selection selection) {
		selections.remove(selection.getName());
		
		SelectionEvent selectionEvent = new SelectionEvent(selection);
		selectionEvent.setAdded(false);
		
		for (Iterator<SelectionListener> it = listeners.iterator(); it.hasNext();) {
			((SelectionListener) it.next()).selectionListChanged(selectionEvent);
		}
	}
	
	/**
	 * Removes the given selection listener from the list of listeners.
	 * 
	 * @param listener
	 *           the selection listener to remove.
	 */
	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Informs the registered listeners that the active session has changed.
	 */
	public void selectionChanged() {
		if (selections == null || activeSelection == null)
			return;
		Selection activeSel = selections.get(activeSelection);
		SelectionEvent selectionEvent = new SelectionEvent(activeSel);
		ArrayList<SelectionListener> list = new ArrayList<SelectionListener>();
		list.addAll(listeners);
		for (SelectionListener sl : list) {
			sl.selectionChanged(selectionEvent);
		}
		
		activeSel.committedChanges();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
