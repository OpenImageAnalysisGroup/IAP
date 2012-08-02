// ==============================================================================
//
// GraphMLParser.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraphMLParser.java,v 1.2 2011-02-04 15:41:18 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.graffiti.plugins.ios.importers.TypedAttributeService;
import org.graffiti.plugins.ios.importers.graphml.GraphMLException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Class <code>GraphMLParser</code> is responsible for setting up the XML
 * parsing environment. It instantiates an XML parser, sets the desired
 * properties and attatches the event handlers to the parser.
 * 
 * @author ruediger
 */
public class GraphMLParser {
	// ~ Static fields/initializers =============================================
	
	/** The validation feature property string. */
	private static final String VALIDATION_FEATURE = "http://apache.org/xml/" +
			"features/validation/schema";
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GraphMLParser</code>.
	 */
	public GraphMLParser() {
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Parses the given <code>InputStream</code> and adds the read in data to
	 * the given <code>Graph</code>.
	 * 
	 * @param in
	 *           the <code>InputStream</code> from which to read.
	 * @param g
	 *           the <code>Graph</code> to which to add the parsed data.
	 * @throws IOException
	 *            if something fails during parsing.
	 * @throws GraphMLException
	 *            if something fails during parsing.
	 */
	public void parse(InputStream in, Graph g)
			throws IOException {
		parse(new InputSource(in), g, false);
	}
	
	private void parse(InputSource in, Graph g, boolean validate) throws GraphMLException, IOException {
		parseWithSettings(in, g, validate);
	}
	
	private void parseWithSettings(InputSource in, Graph g, boolean validate) throws GraphMLException, IOException {
		// instantiate a SAXParserFactory that creates SAX parsers that are
		// validating, support namespaces and XML schema validation
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(validate);
		
		// make sure the features are supported
		// assert spf.isNamespaceAware() : "parser is not namespace aware.";
		// assert spf.isValidating() : "parser is not validating.";
		
		// instantiate a parser
		SAXParser saxParser;
		
		try {
			saxParser = spf.newSAXParser(); // new org.apache.xerces.jaxp.SAXParserFactoryImpl().newSAXParser();
			// spf.newSAXParser();
		} catch (Exception pe) {
			throw new GraphMLException(pe);
		}
		// catch(SAXException se)
		// {
		// throw new GraphMLException(se);
		// }
		
		System.out.println("SAX Parser: " + saxParser.getClass().getCanonicalName());
		
		// configure the different readers
		XMLReader parser;
		
		try {
			parser = saxParser.getXMLReader();
		} catch (SAXException se) {
			throw new GraphMLException(se);
		}
		
		System.out.println("XML Reader: " + parser.getClass().getCanonicalName());
		
		// create a filter for filtering character events
		XMLFilterImpl charFilter = new CharFilter(parser);
		
		// create a filter to filter the content supported by Gravisto
		// XMLFilterImpl gravistoFilter = new GraphMLGravistoFilter(parser, g);
		XMLFilterImpl gravistoFilter = new GraphMLGravistoFilter(charFilter);
		
		// create a filter for processing the relevant content
		XMLFilterImpl graphMLParser = new GraphMLFilter(gravistoFilter, g);
		graphMLParser.setEntityResolver(new GraphMLEntityResolver());
		
		graphMLParser.setErrorHandler(new ErrorHandler() {
			
			public void error(SAXParseException exception) throws SAXException {
				ErrorMsg.addErrorMessage(exception);
			}
			
			public void fatalError(SAXParseException exception) throws SAXException {
				ErrorMsg.addErrorMessage(exception);
			}
			
			public void warning(SAXParseException exception) throws SAXException {
				ErrorMsg.addErrorMessage(exception);
			}
		});
		
		// run the parser
		try {
			graphMLParser.parse(in);
			g = TypedAttributeService.createTypedHashMapAttributes(g);
		} catch (SAXException se) {
			throw new GraphMLException(se);
		}
	}
	
	public void parse(Reader reader, Graph g) throws GraphMLException, IOException {
		parse(new InputSource(reader), g, false);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
