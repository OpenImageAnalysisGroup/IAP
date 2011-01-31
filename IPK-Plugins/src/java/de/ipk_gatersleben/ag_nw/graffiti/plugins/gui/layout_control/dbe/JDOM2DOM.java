/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import org.graffiti.plugin.XMLHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */

public class JDOM2DOM {
	private static DOMOutputter domOut = new DOMOutputter();
	
	public static org.w3c.dom.Element getNodeForJDOMElement(Element e)
						throws JDOMException {
		Element clone = (Element) e.clone();
		Document jdomDoc = new Document();
		jdomDoc.setRootElement(clone);
		org.w3c.dom.Document domDoc = domOut.output(jdomDoc);
		return domDoc.getDocumentElement();
	}
	
	public static org.w3c.dom.Document getDOMfromJDOM(org.jdom.Document doc)
						throws JDOMException {
		Element e = doc.getRootElement();
		Element clone = (Element) e.clone();
		Document jdomDoc = new Document();
		jdomDoc.setRootElement(clone);
		org.w3c.dom.Document domDoc = domOut.output(jdomDoc);
		return domDoc;
		
	}
	
	public static org.jdom.Document getJDOMfromDOM(org.w3c.dom.Document doc) {
		return XMLHelper.getJDOMfromDOM(doc);
	}
}
