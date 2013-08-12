package org.graffiti.attributes;

import java.util.HashSet;

public class AttributeManager {
	
	private HashSet<String> unwrittenAttributes;
	
	private static AttributeManager instance;
	
	public static AttributeManager getInstance() {
		if (instance == null)
			instance = new AttributeManager();
		return instance;
	}
	
	private AttributeManager() {
		super();
		unwrittenAttributes = new HashSet<String>();
	}
	
	public HashSet<String> getUnwrittenAttributes() {
		return unwrittenAttributes;
	}
	
	public void addUnwrittenAttribute(String pathAndName) {
		unwrittenAttributes.add(pathAndName);
	}
	
}
