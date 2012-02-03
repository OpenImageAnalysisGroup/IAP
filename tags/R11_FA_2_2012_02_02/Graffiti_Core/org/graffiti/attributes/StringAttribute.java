// ==============================================================================
//
// StringAttribute.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StringAttribute.java,v 1.1 2011-01-31 09:04:43 klukas Exp $

package org.graffiti.attributes;

import java.lang.reflect.Constructor;

import org.ErrorMsg;
import org.graffiti.event.AttributeEvent;

/**
 * Contains a String.
 * 
 * @version $Revision: 1.1 $
 */
public class StringAttribute
					extends AbstractAttribute {
	// ~ Instance fields ========================================================
	
	/** The value of this <code>StringAttribute</code>. */
	protected String value;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new instance of a <code>StringAttribute</code>.
	 * 
	 * @param id
	 *           the id of the <code>Attribute</code>.
	 */
	protected StringAttribute(String id) {
		super(id);
	}
	
	/**
	 * Constructs a new instance of a <code>StringAttribute</code> with the
	 * given value.
	 * 
	 * @param id
	 *           the id of the attribute.
	 * @param value
	 *           the value of the <code>Attribute</code>.
	 */
	public StringAttribute(String id, String value) {
		super(id);
		this.value = value;
	}
	
	public StringAttribute() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public static Attribute getTypedStringAttribute(String id) {
		Attribute newInstance = null;
		Class ct = typedAttributesID2TypeForNodes.get(id);
		if (ct != null) {
			try {
				newInstance = (Attribute) ct.newInstance();
				newInstance.setId(id);
			} catch (InstantiationException e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			} catch (IllegalAccessException e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			}
		} else
			newInstance = new StringAttribute(id);
		assert newInstance != null;
		return newInstance;
	}
	
	@SuppressWarnings("unchecked")
	public static Attribute getTypedStringAttribute(String id, String value) {
		Attribute newInstance = null;
		Class ct = typedAttributesID2TypeForNodes.get(id);
		if (ct != null) {
			try {
				Object res = ct.newInstance();
				newInstance = (Attribute) res;
				newInstance.setId(id);
				newInstance.setValue(value);
			} catch (InstantiationException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (IllegalAccessException e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			newInstance = new StringAttribute(id, value);
		assert newInstance != null;
		return newInstance;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.attributes.Attribute#setDefaultValue()
	 */
	public void setDefaultValue() {
		value = "";
	}
	
	/**
	 * Sets the value of this object. The <code>ListenerManager</code> is
	 * informed by the method <code>setValue()</code>.
	 * 
	 * @param value
	 *           the new value of this object.
	 */
	public void setString(String value) {
		// assert value != null;
		
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		this.value = value;
		callPostAttributeChanged(ae);
	}
	
	/**
	 * Returns the value of this object.
	 * 
	 * @return the value of this object.
	 */
	public String getString() {
		return value;
	}
	
	/**
	 * Returns the value of this attribute, i.e. contained Sting object.
	 * 
	 * @return the value of the attribute, i.e. contained String object.
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Returns a deep copy of this instance.
	 * 
	 * @return a deep copy of this instance.
	 */
	@SuppressWarnings("unchecked")
	public Object copy() {
		if (this.getClass() == StringAttribute.class)
			return new StringAttribute(getId(), value);
		else {
			try {
				Constructor con = this.getClass().getConstructor(new Class[] { String.class, String.class });
				StringAttribute result = (StringAttribute) con.newInstance(new Object[] { getId(), value });
				return result;
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		}
	}
	
	/**
	 * @see org.graffiti.attributes.Attribute#toString(int)
	 */
	@Override
	public String toString(int n) {
		return getSpaces(n) + getId() + " = \"" + value + "\"";
	}
	
	/**
	 * Sets the value of the <code>Attribute</code>. The <code>ListenerManager</code> is informed by the method <code>setValue()</code>.
	 * 
	 * @param o
	 *           the new value of the attribute.
	 * @exception IllegalArgumentException
	 *               if the parameter has not the
	 *               appropriate class for this <code>Attribute</code>.
	 */
	@Override
	protected void doSetValue(Object o)
						throws IllegalArgumentException {
		assert o != null;
		
		try {
			value = (String) o;
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	/**
	 * @see org.graffiti.plugin.Displayable#toXMLString()
	 */
	@Override
	public String toXMLString() {
		return getStandardXML(value);
	}
	
	/**
	 * @author klukas
	 */
	@SuppressWarnings("unchecked")
	public static void putAttributeType(String id, Class attributeType) {
		if (typedAttributesID2TypeForNodes.containsKey(id))
			typedAttributesID2TypeForNodes.remove(id);
		typedAttributesID2TypeForNodes.put(id, attributeType);
		
		if (typedAttributesID2TypeForEdges.containsKey(id))
			typedAttributesID2TypeForEdges.remove(id);
		typedAttributesID2TypeForEdges.put(id, attributeType);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
