/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.event.AttributeEvent;

public class CompoundAttribute
					extends StringAttribute {
	private String value;
	
	public CompoundAttribute() {
		super();
	}
	
	public CompoundAttribute(String id) {
		super(id);
		setDescription("Image Attribute: Specify a KEGG Compound ID (e.g. C00001) or another image file"); // tooltip
	}
	
	public CompoundAttribute(String id, String value) {
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
		return new CompoundAttribute(this.getId(), this.value);
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