/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_style;

import org.graffiti.attributes.StringAttribute;

public class LabelStyleAttribute extends StringAttribute {
	
	public LabelStyleAttribute() {
		super();
	}
	
	public LabelStyleAttribute(String id) {
		super(id);
		setDescription("Modify Font Style"); // tooltip
	}
	
	public LabelStyleAttribute(String id, String value) {
		super(id, value);
		setDescription("Modify Font Style"); // tooltip
	}
	
	@Override
	public void setDefaultValue() {
		this.value = "plain";
	}
	
	@Override
	public Object copy() {
		return new LabelStyleAttribute(this.getId(), this.getString());
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