/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: XGMMLDelegatorHandler.java,v 1.1 2011-01-31 09:00:24 klukas Exp $
 * Created on 05.11.2003 by Burkhard Sell
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Delegator handler.
 * <p>
 * Handles the SAX parser events and invokes the <code>XGMMLHandler</code> methods uing refection API.
 * </p>
 * <br />
 * <p>
 * This handler handles the startDocument, endDocument, startElement and endElementEvents from SAX2 parser.<br />
 * If an event was received this handler uses a second handler to process the events. There for the second handler must implement startXElement and endXElement
 * where X is the tag name to be processed.
 * </p>
 * <p>
 * Example:<br />
 * If you want to process &lt;node&gt; tags, your handler must implement a <code>startNodeElement</code> and a <code>endNodeElement</code> method. These method
 * are automatically invoked using reflection API.
 * </p>
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml.XGMMLHandler
 * @see de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml.XGMMLContentHandler
 * @see org.xml.sax.helpers.DefaultHandler
 */

public class XGMMLDelegatorHandler extends DefaultHandler {
	
	private XGMMLHandler delegate;
	
	private String nodeElementName;
	private String edgeElementName;
	
	/**
	 * Creates a XGMMLDelegatorHandler instance.
	 * 
	 * @param handler
	 *           - handler to delegate the events to.
	 */
	public XGMMLDelegatorHandler(XGMMLHandler handler) {
		this.delegate = handler;
		this.nodeElementName = handler.getNodeElementName();
		this.edgeElementName = handler.getEdgeElementName();
	}
	
	/**
	 * Handle start document event.
	 */
	@Override
	public void startDocument() {
		this.delegate.startDocument();
	}
	
	/**
	 * Handle end document event.
	 */
	@Override
	public void endDocument() throws SAXException {
		this.delegate.endDocument();
	}
	
	/**
	 * Handle start tag event.
	 */
	@Override
	public void startElement(
						String uri,
						String localName,
						String qualifiedName,
						Attributes attribs) {
		
		if (localName.equals(this.nodeElementName)
							&& this.delegate.getGraph() == null) {
			
			try {
				this.delegate.instantiateGraph();
			} catch (Exception ex) {
				System.err.println("Error instantiating graph: " + ex);
			}
		}
		
		String methodName =
							"start"
												+ localName.substring(0, 1).toUpperCase()
												+ localName.substring(1)
												+ "Element";
		Class<?> attribInterface = new AttributesImpl().getClass().getInterfaces()[0];
		Class<?> paramClasses[] = { attribInterface };
		Method method;
		
		try {
			method = this.delegate.getClass().getMethod(methodName, paramClasses);
			
			// System.out.println("Dynamically invoking " + methodName);
			Object paramObjects[] = { attribs };
			method.invoke(this.delegate, paramObjects);
		} catch (NoSuchMethodException ex) {
		} catch (SecurityException ex) {
		} catch (InvocationTargetException ex) {
		} catch (Exception ex) {
		}
		
		this.delegate.startElement(uri, localName, qualifiedName, attribs);
	}
	
	/**
	 * Handle end tag event.
	 */
	@Override
	public void endElement(String uri, String localName, String qualifiedName) {
		String methodName =
							"end"
												+ localName.substring(0, 1).toUpperCase()
												+ localName.substring(1)
												+ "Element";
		Method method;
		
		try {
			method = this.delegate.getClass().getMethod(methodName, (Class<?>) null);
			method.invoke(this.delegate, (Class<?>) null);
		} catch (NoSuchMethodException ex) {
		} catch (SecurityException ex) {
		} catch (InvocationTargetException ex) {
		} catch (Exception ex) {
		}
		
		if (localName.equals(this.nodeElementName)) {
			try {
				this.delegate.instantiateNode();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
		} else
			if (localName.equals(this.edgeElementName)) {
				try {
					this.delegate.instantiateEdge();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		
		this.delegate.endElement(uri, localName, qualifiedName);
	}
	
}