/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_settings;

import javax.swing.JLabel;

import org.graffiti.attributes.StringAttribute;

public class LabelFontAttribute extends StringAttribute {
	
	public LabelFontAttribute() {
		super();
	}
	
	public LabelFontAttribute(String id) {
		super(id);
		setDescription("Modify Font"); // tooltip
	}
	
	public LabelFontAttribute(String id, String value) {
		super(id, value);
		setDescription("Modify Font"); // tooltip
	}
	
	@Override
	public void setDefaultValue() {
		this.value = new JLabel("").getFont().getFamily();
	}
	
	@Override
	public Object copy() {
		return new LabelFontAttribute(this.getId(), this.getString());
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