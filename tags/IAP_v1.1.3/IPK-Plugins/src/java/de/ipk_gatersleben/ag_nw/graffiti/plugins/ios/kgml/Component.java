/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;

public class Component {
	
	public static Collection<IdRef> getComponentElementsFromKgmlElement(List<?> componentKgmlElements) {
		Collection<IdRef> result = new ArrayList<IdRef>();
		
		for (Object o : componentKgmlElements) {
			Element component = (Element) o;
			String id = component.getAttributeValue("id");
			IdRef r = new IdRef(id);
			result.add(r);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static Element getKgmlComponentElement(IdRef compRef) {
		Element result = new Element("component");
		result.getAttributes().add(new Attribute("id", compRef.getValue()));
		return result;
	}
	
}
