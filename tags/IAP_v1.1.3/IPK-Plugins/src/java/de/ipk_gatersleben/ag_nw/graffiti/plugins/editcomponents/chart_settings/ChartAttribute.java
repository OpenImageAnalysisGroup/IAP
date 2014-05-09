/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import org.graffiti.attributes.StringAttribute;

public class ChartAttribute
					extends StringAttribute {
	public static final String CHARTPOSITION = "chartposition";
	
	public ChartAttribute() {
		super();
	}
	
	public ChartAttribute(String id) {
		super(id);
		setDescription("Select one of the available charting types"); // tooltip
	}
	
	public ChartAttribute(String id, String value) {
		super(id, value);
	}
	
	@Override
	public Object copy() {
		return new ChartAttribute(this.getId(), this.value);
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
			value = ((String) o).intern();
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
}