// ==============================================================================
//
// Queue.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Queue.java,v 1.1 2011-01-31 09:04:59 klukas Exp $

package org.graffiti.util;

import java.util.LinkedList;

/**
 * A simple queue.
 * 
 * @version $Revision: 1.1 $
 */
public class Queue {
	// ~ Instance fields ========================================================
	
	/** The queue's data structure. */
	private LinkedList<Object> list;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new queue.
	 */
	public Queue() {
		list = new LinkedList<Object>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns <code>true</code>, if the queue is empty.
	 * 
	 * @return <code>true</code>, if the queue is empty.
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	/**
	 * Adds the given object to the end of the queue.
	 * 
	 * @param o
	 *           the object to add to the end of the queue.
	 */
	public void addLast(Object o) {
		list.addLast(o);
	}
	
	/**
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		list.clear();
	}
	
	/**
	 * Returns and removes the first element in the queue.
	 * 
	 * @see java.util.LinkedList#removeFirst()
	 */
	public Object removeFirst() {
		return list.removeFirst();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
