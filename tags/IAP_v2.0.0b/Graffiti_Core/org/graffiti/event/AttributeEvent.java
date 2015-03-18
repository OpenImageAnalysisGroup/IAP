// ==============================================================================
//
// AttributeEvent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeEvent.java,v 1.1 2011-01-31 09:05:00 klukas Exp $

package org.graffiti.event;

import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;

/**
 * Contains an attribute event.
 * 
 * @version $Revision: 1.1 $
 */
public class AttributeEvent
					extends AbstractEvent {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The path that has been assigned to the attribute by the event. */
	private String path;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Contructor that is called when one attribute is concerned.
	 * 
	 * @param attribute
	 *           the attribute, which was altered.
	 */
	public AttributeEvent(Attribute attribute) {
		super(attribute);
	}
	
	/**
	 * Contructor that is called when one composite attribute is concerned,
	 * where it is comfortable to pass the path of attribute, too.
	 * 
	 * @param path
	 *           the path to the attribute that was altered.
	 * @param attribute
	 *           the attribute, which was altered.
	 */
	public AttributeEvent(String path, Attribute attribute) {
		super(attribute);
		this.path = path;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the attribute that has been changed by this event.
	 * 
	 * @return the attribute that has been changed by this event.
	 */
	public Attribute getAttribute() {
		return (Attribute) getSource();
	}
	
	/**
	 * Returns the path to the attribute that has been changed by this event.
	 * 
	 * @return the path to the attribute that has been changed by this event.
	 */
	public String getPath() {
		return path;
	}
	
	public Attributable getAttributeable() {
		return getAttribute().getAttributable();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
