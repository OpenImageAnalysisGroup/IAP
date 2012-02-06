/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_settings;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.JLabel;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.event.AttributeEvent;

public class FontAttribute extends StringAttribute {
	private Font myFont = null;
	private static HashMap<String, Font> knownFonts = new HashMap<String, Font>();
	private Color myColor = getDefaultColor();
	
	private static Font defaultFont = null;
	
	public FontAttribute() {
		super();
	}
	
	public FontAttribute(String id) {
		super(id);
		setDescription("Modify Color and Font"); // tooltip
	}
	
	public FontAttribute(String id, String value) {
		super(id);
		setString(value);
	}
	
	@Override
	public void setDefaultValue() {
		myFont = getDefaultFont();
		myColor = getDefaultColor();
	}
	
	public Font getFont() {
		if (myFont == null)
			myFont = getDefaultFont();
		return myFont;
	}
	
	@Override
	public void setString(String value) {
		assert value != null;
		
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		
		String[] colorAndFont = value.split(";");
		
		String[] rgba_s = colorAndFont[0].split(",");
		int[] rgba = new int[rgba_s.length];
		for (int ir = 0; ir < rgba.length; ir++)
			rgba[ir] = Integer.parseInt(rgba_s[ir]);
		myColor = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
		
		myFont = knownFonts.get(colorAndFont[1]);
		if (myFont == null) {
			String fontNameSizeStyle[] = colorAndFont[1].split(",");
			int style = Integer.parseInt(fontNameSizeStyle[1]);
			int size = Integer.parseInt(fontNameSizeStyle[2]);
			myFont = new Font(fontNameSizeStyle[0], style, size);
			knownFonts.put(colorAndFont[1], myFont);
		}
		callPostAttributeChanged(ae);
	}
	
	@Override
	public String getString() {
		return myColor.getRed() + "," + myColor.getGreen() + "," + myColor.getBlue() + "," + myColor.getAlpha() +
							";" + myFont.getName() + "," + myFont.getStyle() + "," + myFont.getSize();
	}
	
	@Override
	public Object getValue() {
		return getString();
	}
	
	@Override
	public Object copy() {
		return new FontAttribute(this.getId(), this.getString());
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
	
	public static Font getDefaultFont() {
		if (defaultFont == null) {
			JLabel jl = new JLabel("test");
			Font f = new Font(jl.getFont().getFamily(), jl.getFont().getStyle(), jl.getFont().getSize());
			defaultFont = f;
		}
		return defaultFont;
	}
	
	public Color getColor() {
		return myColor;
	}
	
	public static Color getDefaultColor() {
		return Color.BLACK;
	}
	
	public void setFont(Font font) {
		this.myFont = font;
	}
	
	public void setColor(Color color) {
		this.myColor = color;
	}
}