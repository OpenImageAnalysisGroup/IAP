package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import org.HelperClass;

public class PrettyObject implements HelperClass {
	
	private String name;
	public Object obj;
	
	public PrettyObject(Object o, String name) {
		obj = o;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
