// ==============================================================================
//
// AbstractEvent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractEvent.java,v 1.1 2011-01-31 09:05:00 klukas Exp $

package org.graffiti.event;

import java.util.EventObject;

/**
 * This class is merely meant to group all the Node-/Edge-/ etc. events. One
 * could imagine that common things could be added right there (for example
 * the timestamp of the event). Otherwise the class is empty.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AbstractEvent
					extends EventObject {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs an AbstractEvent with object o as source.
	 * 
	 * @param o
	 *           the object that is considered as source of the event.
	 */
	public AbstractEvent(Object o) {
		super(o);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
