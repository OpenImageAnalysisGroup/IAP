/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_color;

import java.awt.Color;

import org.color.ColorUtil;
import org.graffiti.attributes.ColorSetAndGetSupport;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;

public class LabelColorAttribute extends StringAttribute implements ColorSetAndGetSupport {
	
	public LabelColorAttribute() {
		super();
	}
	
	public LabelColorAttribute(String id) {
		super(id);
		if (id.equals(GraphicAttributeConstants.TEXTCOLOR))
			setDescription("Modify Label Color"); // tooltip
		if (id.startsWith(GraphicAttributeConstants.CHARTBACKGROUNDCOLOR))
			setDescription("Chart Background-Color (black=translucent)"); // tooltip
		if (id.equals(GraphicAttributeConstants.SHADOWCOLOR))
			setDescription("Label Drop Shadow Color"); // tooltip
	}
	
	public LabelColorAttribute(String id, String value) {
		super(id, value);
		if (id.equals(GraphicAttributeConstants.TEXTCOLOR))
			setDescription("Modify Label Color"); // tooltip
		if (id.equals(GraphicAttributeConstants.CHARTBACKGROUNDCOLOR))
			setDescription("Chart Background-Color (black=translucent)"); // tooltip
		if (id.equals(GraphicAttributeConstants.SHADOWCOLOR))
			setDescription("Label Drop Shadow Color"); // tooltip
	}
	
	@Override
	public void setDefaultValue() {
		this.value = "plain";
	}
	
	@Override
	public Object copy() {
		return new LabelColorAttribute(this.getId(), this.getString());
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
	
	public void setColor(Color newColor) {
		setString(ColorUtil.getHexFromColor(newColor));
	}
	
	public Color getColor() {
		return ColorUtil.getColorFromHex(getString());
	}
}