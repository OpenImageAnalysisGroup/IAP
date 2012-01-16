// ==============================================================================
//
// AttributeListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeListener.java,v 1.1 2011-01-31 09:04:59 klukas Exp $

package org.graffiti.event;

/**
 * Interfaces an attribute events listener.
 * 
 * @version $Revision: 1.1 $
 * @see AttributeEvent
 */
public interface AttributeListener
					extends TransactionListener {
	// ~ Methods ================================================================
	
	/**
	 * Called after an attribute has been added.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	void postAttributeAdded(AttributeEvent e);
	
	/**
	 * Called after an attribute has been changed.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	void postAttributeChanged(AttributeEvent e);
	
	/**
	 * Called after an attribute has been removed.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	void postAttributeRemoved(AttributeEvent e);
	
	/**
	 * Called just before an attribute is added.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	void preAttributeAdded(AttributeEvent e);
	
	/**
	 * Called before a change of an attribute takes place.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	void preAttributeChanged(AttributeEvent e);
	
	/**
	 * Called just before an attribute is removed.
	 * 
	 * @param e
	 *           the AttributeEvent detailing the changes.
	 */
	void preAttributeRemoved(AttributeEvent e);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
