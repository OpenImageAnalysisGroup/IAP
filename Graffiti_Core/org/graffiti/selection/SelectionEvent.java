// ==============================================================================
//
// SelectionEvent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SelectionEvent.java,v 1.1 2011-01-31 09:05:02 klukas Exp $

package org.graffiti.selection;

import org.graffiti.event.AbstractEvent;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 */
public class SelectionEvent
					extends AbstractEvent {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** DOCUMENT ME! */
	private boolean added;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>SelectionEvent</code>.
	 * 
	 * @param selection
	 *           the (new / updated) selection.
	 */
	public SelectionEvent(Selection selection) {
		super(selection);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the added.
	 * 
	 * @param added
	 *           The added to set
	 */
	public void setAdded(boolean added) {
		this.added = added;
	}
	
	/**
	 * Returns the selection contained in the event.
	 * 
	 * @return the selection contained in the event.
	 */
	public Selection getSelection() {
		return (Selection) getSource();
	}
	
	/**
	 * Returns the added.
	 * 
	 * @return boolean
	 */
	public boolean toBeAdded() {
		return added;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
