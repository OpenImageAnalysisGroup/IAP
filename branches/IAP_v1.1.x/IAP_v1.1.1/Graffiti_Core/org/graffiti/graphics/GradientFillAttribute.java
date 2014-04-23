package org.graffiti.graphics;

import org.graffiti.attributes.DoubleAttribute;

public class GradientFillAttribute extends DoubleAttribute {
	
	public GradientFillAttribute(String id) {
		super(id);
	}
	
	public GradientFillAttribute(String id, double value) {
		super(id, value);
	}
	
	public GradientFillAttribute(String id, Double value) {
		super(id, value);
	}
	
	@Override
	public Object copy() {
		return new GradientFillAttribute(this.getId(), this.value);
	}
	
}
