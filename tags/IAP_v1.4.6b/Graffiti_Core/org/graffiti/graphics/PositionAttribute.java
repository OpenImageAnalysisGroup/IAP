// ==============================================================================
//
// PositionAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PositionAttribute.java,v 1.1 2011-01-31 09:04:48 klukas Exp $

package org.graffiti.graphics;

import java.util.Map;

import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.HashMapAttribute;

/**
 * Contains properties of the attribute position for a label
 * 
 * @version $Revision: 1.1 $
 */
public abstract class PositionAttribute
					extends HashMapAttribute
					implements GraphicAttributeConstants {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for NodeLabelPositionAttribute.
	 * 
	 * @param id
	 */
	public PositionAttribute(String id) {
		super(id);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the collection of attributes contained within this <tt>CollectionAttribute</tt>
	 * 
	 * @param attrs
	 *           the map that contains all attributes.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	@Override
	public void setCollection(Map<String, Attribute> attrs) {
		if (!attrs.keySet().isEmpty()) {
			throw new IllegalArgumentException("Invalid map.");
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
