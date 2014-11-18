/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.event.AttributeEvent;

public class CompoundPositionAttribute
					extends StringAttribute {
	private String value;
	
	public CompoundPositionAttribute() {
		super();
	}
	
	public CompoundPositionAttribute(String id) {
		super(id);
		setDescription("Image Position Attribute: Specify the position of the KEGG Compound image relative to the node"); // tooltip
	}
	
	public CompoundPositionAttribute(String id, String value) {
		super(id);
		this.value = value;
	}
	
	@Override
	public void setDefaultValue() {
		value = "DEFAULT VALUE TODO";
	}
	
	@Override
	public void setString(String value) {
		assert value != null;
		
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		this.value = value;
		callPostAttributeChanged(ae);
	}
	
	@Override
	public String getString() {
		return value;
	}
	
	@Override
	public Object getValue() {
		return value;
	}
	
	@Override
	public Object copy() {
		return new CompoundPositionAttribute(this.getId(), this.value);
	}
	
	@Override
	public String toString(int n) {
		return getSpaces(n) + getId() + " = \"" + value + "\"";
	}
	
	@Override
	public String toXMLString() {
		return getStandardXML(value);
	}
	
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
}