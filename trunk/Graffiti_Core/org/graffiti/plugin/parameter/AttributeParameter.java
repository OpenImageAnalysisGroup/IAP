// ==============================================================================
//
// AttributeParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeParameter.java,v 1.1 2011-01-31 09:05:03 klukas Exp $

package org.graffiti.plugin.parameter;

import org.graffiti.attributes.Attribute;

/**
 * This class is used for <code>Parameters</code> that satisfy the <code>org.graffiti.attributes.Attribte</code> interface.
 * 
 * @version $Revision: 1.1 $
 * @see Attribute
 */
public class AttributeParameter
					extends AbstractSingleParameter {
	// ~ Instance fields ========================================================
	
	/** The value of the <code>AttributeParameter</code>. */
	private Attribute value = null;
	
	/**
	 * The path to the attribute this <code>AttributeParameter</code> represents.
	 */
	private String path;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new attribute parameter.
	 * 
	 * @param name
	 *           the name of the attribute.
	 * @param description
	 *           the description of the attribute.
	 */
	public AttributeParameter(String name, String description) {
		super(name, description);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the <code>Attribute</code> the <code>AttributeParameter</code> represents. <b>Implementation Note</b>: has to update the <code>path</code>.
	 * 
	 * @param value
	 *           the new <code>Attribute</code> the <code>AttributeParameter</code> represents.
	 */
	public void setAttribute(Attribute value) {
		// 
	}
	
	/**
	 * Returns the <code>Attribute</code> the <code>AttributeParameter</code> represents.
	 * 
	 * @return the <code>Attribute</code> the <code>AttributeParameter</code> represents.
	 */
	public Attribute getAttribute() {
		return value;
	}
	
	/**
	 * Returns the path to the <code>Attribute</code> the <code>AttributeParameter</code> represents.
	 * 
	 * @return the path to the <code>Attribute</code> the <code>AttributeParameter</code> represents.
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Sets the value of the <code>AttributeParameter</code>.
	 * 
	 * @param value
	 *           the new value of the <code>AttributeParameter</code>.
	 */
	@Override
	public void setValue(Object value) {
		//
	}
	
	/**
	 * Returns the value of the <code>AttributeParameter</code>.
	 * 
	 * @return the value of the <code>AttributeParameter</code>.
	 */
	@Override
	public Object getValue() {
		return value;
	}
	
	/**
	 * @see org.graffiti.plugin.parameter.Parameter#toXMLString()
	 */
	@Override
	public String toXMLString() {
		return getStandardXML(value.toXMLString());
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
