// ==============================================================================
//
// PluginXMLParserErrorHandler.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PluginXMLParserErrorHandler.java,v 1.1 2011-01-31 09:04:52 klukas Exp $

package org.graffiti.managers.pluginmgr;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An error handler class for the plugin xml parser.
 * 
 * @version $Revision: 1.1 $
 */
public class PluginXMLParserErrorHandler
					implements ErrorHandler {
	// ~ Methods ================================================================
	
	/**
	 * Error Event Handler.
	 * 
	 * @param e
	 *           The SAXException, which was thrown by the parser.
	 * @exception SAXException
	 *               if this method is called by the parser.
	 */
	public void error(SAXParseException e)
						throws SAXException {
		throw e;
	}
	
	/**
	 * Fatal Error Event Handler.
	 * 
	 * @param e
	 *           The SAXException, which was thrown by the parser.
	 * @exception SAXException
	 *               if this method is called by the parser.
	 */
	public void fatalError(SAXParseException e)
						throws SAXException {
		throw e;
	}
	
	/**
	 * Warning Event Handler.
	 * 
	 * @param e
	 *           The SAXException, which was thrown by the parser.
	 * @exception SAXException
	 *               if this method is called by the parser.
	 */
	public void warning(SAXParseException e)
						throws SAXException {
		throw e;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
