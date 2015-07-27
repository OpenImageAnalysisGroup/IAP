package org.graffiti.graphics;

import org.graffiti.attributes.StringAttribute;

public class SourceDockingAttribute extends StringAttribute {
	
	protected SourceDockingAttribute(String id) {
		super(id);
	}
	
	public SourceDockingAttribute(String id, String value) {
		super(id, value);
	}
	
	public SourceDockingAttribute() {
		super();
	}
	
	@Override
	public Object copy() {
		return new SourceDockingAttribute(this.getId(), this.value);
	}
	
}
