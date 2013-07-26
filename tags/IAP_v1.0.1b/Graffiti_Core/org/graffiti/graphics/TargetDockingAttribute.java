package org.graffiti.graphics;

import org.graffiti.attributes.StringAttribute;

public class TargetDockingAttribute extends StringAttribute {
	
	protected TargetDockingAttribute(String id) {
		super(id);
	}
	
	public TargetDockingAttribute(String id, String value) {
		super(id, value);
	}
	
	public TargetDockingAttribute() {
		super();
	}
	
	@Override
	public Object copy() {
		return new TargetDockingAttribute(this.getId(), this.value);
	}
	
}
