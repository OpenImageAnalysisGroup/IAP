/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.event.AttributeEvent;

public class KeggIdAttribute extends StringAttribute {
	private String myValue;
	
	public KeggIdAttribute() {
		super();
	}
	
	public KeggIdAttribute(String id) {
		super(id);
		setDescription("KEGG ID"); // tooltip
	}
	
	public KeggIdAttribute(String id, String value) {
		super(id);
		setString(value);
	}
	
	@Override
	public void setDefaultValue() {
		myValue = null;
	}
	
	@Override
	public void setString(String value) {
		assert value != null;
		
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		myValue = value;
		callPostAttributeChanged(ae);
	}
	
	@Override
	public String getString() {
		return myValue;
	}
	
	@Override
	public Object getValue() {
		return getString();
	}
	
	@Override
	public Object copy() {
		return new KeggIdAttribute(this.getId(), this.getString());
	}
	
	@Override
	public String toString(int n) {
		return getSpaces(n) + getId() + " = \"" + getString() + "\"";
	}
	
	@Override
	public String toXMLString() {
		return getStandardXML(getString());
	}
	
	@Override
	protected void doSetValue(Object o) throws IllegalArgumentException {
		assert o != null;
		
		try {
			setString((String) o);
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
}