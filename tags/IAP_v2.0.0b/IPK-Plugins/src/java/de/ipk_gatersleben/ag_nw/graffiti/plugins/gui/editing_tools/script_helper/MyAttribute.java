package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import org.jdom.Attribute;

/**
 * @author Christian Klukas
 */
public class MyAttribute {
	
	private final String key;
	private String s;
	
	public MyAttribute(Attribute a) {
		this.key = a.getName();
		this.s = a.getValue();
	}
	
	public MyAttribute(String key, String s) {
		this.key = key;
		this.s = s;
	}
	
	public String getValue() {
		return s;
	}
	
	public void setValue(String htmlToUnicode) {
		s = htmlToUnicode;
	}
	
	public String getName() {
		return key;
	}
	
}
