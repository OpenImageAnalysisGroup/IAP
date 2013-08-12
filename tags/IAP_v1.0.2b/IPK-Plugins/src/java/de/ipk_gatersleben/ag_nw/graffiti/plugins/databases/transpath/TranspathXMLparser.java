/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

import java.util.Map;

import org.ErrorMsg;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class TranspathXMLparser extends DefaultHandler {
	
	Map<String, TranspathEntityType> entries;
	
	String activeEnvironment = "";
	
	String entitystartEndTag = null;
	
	Class<?> entityType;
	
	TranspathEntityType currentEntity = null;
	
	public TranspathXMLparser(Map<String, TranspathEntityType> entries,
						Class<?> entityType, String entitystartEndTag) {
		this.entries = entries;
		this.entityType = entityType;
		this.entitystartEndTag = entitystartEndTag;
	}
	
	@Override
	public void startElement(String uri, String localName, String qname,
						Attributes attr) {
		// System.out.println("Start element: local name: " + localName
		// + " qname: " + qname + " uri: " + uri);
		int attrCount = attr.getLength();
		if (attrCount > 0) {
			// System.out.println("Attributes:");
			// for (int i = 0; i < attrCount; i++) {
			// System.out.println("  Name : " + attr.getQName(i));
			// System.out.println("  Type : " + attr.getType(i));
			// System.out.println("  Value: " + attr.getValue(i));
			// }
		}
		
		if (entitystartEndTag.equals(qname)) {
			activeEnvironment = "";
			try {
				currentEntity = (TranspathEntityType) entityType.newInstance();
			} catch (InstantiationException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (IllegalAccessException e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			activeEnvironment += "/" + qname;
	}
	
	@Override
	public void endElement(String uri, String localName, String qname) {
		// System.out.println("End element: local name: " + localName + " qname: "
		// + qname + " uri: " + uri);
		if (activeEnvironment.endsWith("/" + qname))
			activeEnvironment = activeEnvironment.substring(0, activeEnvironment.length() - "/".length() - qname.length());
		if (currentEntity != null && qname.equals(entitystartEndTag)) {
			entries.put(currentEntity.getKey(), currentEntity);
			currentEntity = null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) {
		String s = new String(ch, start, length).trim();
		// System.out.println("Characters: " + s);
		if (currentEntity != null && activeEnvironment != null && activeEnvironment.length() > 0 && s.length() > 0)
			currentEntity.processXMLentityValue(activeEnvironment, s);
	}
}
