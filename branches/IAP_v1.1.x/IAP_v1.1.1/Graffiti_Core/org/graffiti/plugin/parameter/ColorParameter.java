/**
 * @author klukas
 */
package org.graffiti.plugin.parameter;

import java.awt.Color;

import org.color.ColorUtil;

public class ColorParameter
					extends AbstractSingleParameter {
	private Color value = null;
	
	public ColorParameter(Color value, String name, String description) {
		super(name, description);
		this.value = value;
	}
	
	public Color getColor() {
		return value;
	}
	
	public boolean isValid() {
		if (value == null) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void setValue(Object value) {
		try {
			this.value = (Color) value;
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
	/**
	 * Returns the value of this parameter.
	 * 
	 * @return the value of this parameter.
	 */
	@Override
	public Object getValue() {
		return value;
	}
	
	/**
	 * @see org.graffiti.plugin.parameter.Parameter#toXMLString()
	 */
	@Override
	public String toXMLString() {
		return getStandardXML(ColorUtil.getHexFromColor(value));
	}
	
}