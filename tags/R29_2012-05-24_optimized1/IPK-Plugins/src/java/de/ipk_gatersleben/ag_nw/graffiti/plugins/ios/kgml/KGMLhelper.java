/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import org.jdom.Attribute;
import org.jdom.Element;

public class KGMLhelper {
	
	public static String getAttributeValue(Element element, String attributeName, String defaultReturn) {
		if (element.getAttribute(attributeName) != null) {
			String attributeValue = element.getAttributeValue(attributeName);
			return attributeValue;
		} else
			return defaultReturn;
	}
	
	public static Attribute getNewAttribute(String attributeName, String value) {
		Attribute result = new Attribute(attributeName, value);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static void addNewAttribute(Element element, String attributeName, String value) {
		if (element != null && attributeName != null && value != null) {
			element.getAttributes().add(getNewAttribute(attributeName, value));
		}
	}
	
}
