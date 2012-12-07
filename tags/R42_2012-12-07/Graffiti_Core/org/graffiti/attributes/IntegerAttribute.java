// ==============================================================================
//
// IntegerAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: IntegerAttribute.java,v 1.1 2011-01-31 09:04:42 klukas Exp $

package org.graffiti.attributes;

import org.graffiti.event.AttributeEvent;

/**
 * Contains an integer.
 * 
 * @version $Revision: 1.1 $
 */
public class IntegerAttribute
					extends AbstractAttribute {
	// ~ Instance fields ========================================================
	
	/** The value of this attribute. */
	private int value;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new instance of an <code>IntegerAttribute</code>.
	 * 
	 * @param id
	 *           the id of the attribute
	 */
	public IntegerAttribute(String id) {
		super(id);
	}
	
	/**
	 * Constructs a new instance of an <code>IntegerAttribute</code> with the
	 * given value.
	 * 
	 * @param id
	 *           the id of the attribute
	 * @param value
	 *           the value of the attribute
	 */
	public IntegerAttribute(String id, int value) {
		super(id);
		this.value = value;
	}
	
	/**
	 * Constructs a new instance of a <code>IntegerAttribute</code> with the
	 * given value.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param value
	 *           the value of the attribute.
	 */
	public IntegerAttribute(String id, Integer value) {
		super(id);
		this.value = value.intValue();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.attributes.Attribute#setDefaultValue()
	 */
	public void setDefaultValue() {
		value = 0;
	}
	
	/**
	 * Sets the value of this object. The <code>ListenerManager</code> is
	 * informed by the method <code>setValue()</code>.
	 * 
	 * @param value
	 *           The new value of this object.
	 */
	public void setInteger(int value) {
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		this.value = value;
		callPostAttributeChanged(ae);
	}
	
	/**
	 * Returns the value of this object.
	 * 
	 * @return The value of this object.
	 */
	public int getInteger() {
		return value;
	}
	
	/**
	 * Returns the value of the attribute wrapped in an <code>Integer</code> object.
	 * 
	 * @return The value of the attribute wrapped in an <code>Integer</code> object.
	 */
	public Object getValue() {
		return new Integer(value);
	}
	
	/**
	 * Returns a deep copy of this instance.
	 * 
	 * @return a deep copy of this instance.
	 */
	public Object copy() {
		return new IntegerAttribute(this.getId(), this.value);
	}
	
	/**
	 * Sets the value of the attribute. The <code>ListenerManager</code> is
	 * informed by the method <code>setValue()</code>.
	 * 
	 * @param o
	 *           The new value of the attribute.
	 * @exception IllegalArgumentException
	 *               if the parameter has not the
	 *               appropriate class for this attribute.
	 */
	@Override
	protected void doSetValue(Object o)
						throws IllegalArgumentException {
		assert o != null;
		
		try {
			value = ((Integer) o).intValue();
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * @see org.graffiti.plugin.Displayable#toXMLString()
	 */
	@Override
	public String toXMLString() {
		return getStandardXML(String.valueOf(value));
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
