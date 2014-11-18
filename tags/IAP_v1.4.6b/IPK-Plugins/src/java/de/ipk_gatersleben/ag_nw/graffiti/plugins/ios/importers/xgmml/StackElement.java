/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: StackElement.java,v 1.1 2011-01-31 09:00:25 klukas Exp $
 * Created on 28.10.2003 by Burkhard Sell
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml;

import org.xml.sax.Attributes;

/**
 * Used by parser to push elements to a stack.
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 */
class StackElement {
	
	/** Name of the element */
	private String elementName;
	
	/** Attributes of the element */
	private Attributes elementAttributes;
	
	/**
	 * Creates StackElement instance.
	 * 
	 * @param name
	 *           = element name
	 * @param attribs
	 *           = element attributes
	 */
	public StackElement(String name, Attributes attribs) {
		this.elementName = name;
		this.elementAttributes = attribs;
	}
	
	/**
	 * Get the element name.
	 * 
	 * @return the element name
	 */
	public String getElementName() {
		return this.elementName;
	}
	
	/**
	 * Get the element attributes.
	 * 
	 * @return the element attributes
	 */
	public Attributes getElementAttributes() {
		return this.elementAttributes;
	}
	
}