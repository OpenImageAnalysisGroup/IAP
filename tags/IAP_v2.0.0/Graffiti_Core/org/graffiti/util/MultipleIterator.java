// ==============================================================================
//
// MultipleIterator.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MultipleIterator.java,v 1.1 2011-01-31 09:04:58 klukas Exp $

package org.graffiti.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class <code>UniqueMultipleIterator</code> encapsulates a number of instances
 * implementing the <code>java.util.Iterator</code> interface. It is possible
 * to iterate over all the iterators one after the other.
 * 
 * @version $Revision: 1.1 $
 */
@SuppressWarnings("unchecked")
public class MultipleIterator
					implements Iterator {
	// ~ Instance fields ========================================================
	
	/** The iterator that has only unique elements. */
	private Iterator uniqueIterator;
	
	/** The set used for duplicate removal. */
	private Set set = new LinkedHashSet(); // new HashSet();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>UniqueMultipleIterator</code> instance.
	 * 
	 * @param iters
	 *           the iterators over which to iterate.
	 */
	public MultipleIterator(Iterator[] iters) {
		for (int its = iters.length - 1; its >= 0; its--) {
			while (iters[its].hasNext()) {
				set.add(iters[its].next());
			}
		}
		
		this.uniqueIterator = set.iterator();
	}
	
	/**
	 * Constructs a new <code>UniqueMultipleIterator</code> instance.
	 * 
	 * @param itr
	 *           the iterator over which to iterate.
	 */
	public MultipleIterator(Iterator itr) {
		while (itr.hasNext()) {
			set.add(itr.next());
		}
		
		this.uniqueIterator = set.iterator();
	}
	
	/**
	 * Constructs a new <code>UniqueMultipleIterator</code> instance.
	 * 
	 * @param itr1
	 *           the first iterator over which to iterate.
	 * @param itr2
	 *           the second iterator over which to iterate.
	 */
	public MultipleIterator(Iterator itr1, Iterator itr2) {
		while (itr1.hasNext()) {
			set.add(itr1.next());
		}
		
		while (itr2.hasNext()) {
			set.add(itr2.next());
		}
		
		this.uniqueIterator = set.iterator();
	}
	
	/**
	 * Constructs a new <code>UniqueMultipleIterator</code> instance.
	 * 
	 * @param itr1
	 *           the first iterator over which to iterate.
	 * @param itr2
	 *           the second iterator over which to iterate.
	 * @param itr3
	 *           the third iterator over which to iterate.
	 */
	public MultipleIterator(Iterator itr1, Iterator itr2, Iterator itr3) {
		while (itr1.hasNext()) {
			set.add(itr1.next());
		}
		
		while (itr2.hasNext()) {
			set.add(itr2.next());
		}
		
		while (itr3.hasNext()) {
			set.add(itr3.next());
		}
		
		this.uniqueIterator = set.iterator();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns <code>true</code> if the iteration has not yet passed each of
	 * the iterators, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the iteration has not yet passed each of
	 *         the iterators, <code>false</code> otherwise.
	 */
	public boolean hasNext() {
		return this.uniqueIterator.hasNext();
	}
	
	/**
	 * Returns the next element of the iteration. If the end of one iterator
	 * has been reached, the iteration will be continued on the next one.
	 * 
	 * @return the next element of the iteration.
	 */
	public Object next() {
		return this.uniqueIterator.next();
	}
	
	/**
	 * The method <code>remove()</code> of the interface <code>java.util.Iterator</code> will not be supported in this
	 * implementation.
	 * 
	 * @exception UnsupportedOperationException
	 *               if the method is called.
	 */
	public void remove()
						throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Removing is not supported " +
							"on MultipleIterators.");
	}
}
