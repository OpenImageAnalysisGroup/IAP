package org.graffiti.graphics;

import org.graffiti.attributes.DoubleAttribute;

public class ThicknessAttribute extends DoubleAttribute {
	
	public ThicknessAttribute(String id) {
		super(id);
	}
	
	public ThicknessAttribute(String id, double value) {
		super(id, value);
	}
	
	public ThicknessAttribute(String id, Double value) {
		super(id, value);
	}
	
	@Override
	public Object copy() {
		return new ThicknessAttribute(this.getId(), this.value);
	}
	
}
