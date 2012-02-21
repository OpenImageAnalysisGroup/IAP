/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: XGMMLHandler.java,v 1.1 2011-01-31 09:00:24 klukas Exp $
 * Created on 10.11.2003 by Burkhard Sell
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml;

import java.util.HashMap;
import java.util.Stack;

import org.graffiti.graph.Graph;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Abstract Handler for parsing an XGMML input source using SAX2.
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 */

public abstract class XGMMLHandler extends DefaultHandler {
	
	/** Stack to push elements. */
	Stack<StackElement> tagStack;
	@SuppressWarnings("unchecked")
	HashMap nodeIDMap;
	
	Graph graph;
	
	public XGMMLHandler() {
	}
	
	@Override
	public void startDocument() {
		this.tagStack = new Stack<StackElement>();
	}
	
	@Override
	public void startElement(
						String uri,
						String localName,
						String qualifiedName,
						Attributes attribs) {
		
		StackElement newEntry =
							new StackElement(localName, new AttributesImpl(attribs));
		
		// push to stack
		this.tagStack.push(newEntry);
	}
	
	/**
	 * Pops the last item of the internal stack
	 */
	@Override
	public void endElement(String uri, String localName, String qualifiedName) {
	}
	
	public Graph getGraph() {
		return this.graph;
	}
	
	public abstract void instantiateNode() throws Exception;
	
	public abstract void instantiateEdge() throws Exception;
	
	public abstract void instantiateGraph() throws Exception;
	
	public abstract String getNodeElementName();
	
	public abstract String getEdgeElementName();
	
}