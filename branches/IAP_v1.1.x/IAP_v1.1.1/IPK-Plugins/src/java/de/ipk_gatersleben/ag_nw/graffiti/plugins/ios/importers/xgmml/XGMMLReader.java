/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: XGMMLReader.java,v 1.1 2011-01-31 09:00:24 klukas Exp $
 * Created on 25.10.2003 by Burkhard Sell
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.xgmml;

import java.io.InputStream;
import java.io.Reader;

import org.graffiti.graph.Graph;
import org.graffiti.graph.OptAdjListGraph;
import org.graffiti.plugin.io.AbstractIOSerializer;
import org.graffiti.plugin.io.AbstractInputSerializer;
import org.graffiti.plugin.io.ParserException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class provides a reader for praphs stored in XGMML format.
 * 
 * @see AbstractIOSerializer
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 */
public class XGMMLReader extends AbstractInputSerializer {
	/** The supported extensions. */
	private String[] extensions = { ".gr", ".xgmml" };
	
	/** Referenz to the parser to read the graph. */
	// private parser p;
	
	/**
	 * Constructs a <code>XGMMReader</code> instance.
	 */
	public XGMMLReader() {
		// Currently do nothing
	}
	
	/**
	 * Returns the extensions supported by this reader.
	 * 
	 * @return the extensions supported by this reader.
	 */
	public String[] getExtensions() {
		return this.extensions;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "XGMML", "XGMML" };
	}
	
	/**
	 * Reads a graph from the given input stream. <code>GraphElements</code> read are <b>cloned</b> when added to the
	 * graph. Consider using the <code>read(InputStream)</code> method when you
	 * start with an empty graph.
	 * 
	 * @param in_Stream
	 *           the <code>InputStream</code> to import the graph from.
	 * @param out_Graph
	 *           the graph to add the imported graph to.
	 * @exception ParserException
	 *               if an error occurs while parsing the stream.
	 */
	@Override
	public void read(InputStream in_Stream, Graph out_Graph)
						throws ParserException {
		
		XGMMLContentHandler contentHandler = new XGMMLContentHandler();
		XGMMLDelegatorHandler delegatorHandler = new XGMMLDelegatorHandler(contentHandler);
		
		try {
			
			XMLReader reader =
								XMLReaderFactory.createXMLReader(
													"org.apache.xerces.parsers.SAXParser");
			
			reader.setContentHandler(delegatorHandler);
			reader.setEntityResolver(new XGMMLDTDResolver());
			reader.parse(new InputSource(in_Stream));
		} catch (Exception e) {
			throw new ParserException(
								"Error parsing inputstream. Root exception = " + e);
		}
		
		Graph graph = contentHandler.getGraph();
		
		if (graph != null) {
			System.out.println("ading graph");
			out_Graph.addGraph(graph);
		} else
			System.out.println("graph is null");
	}
	
	/**
	 * Reads a graph from the given input stream.
	 * This implementation returns an instance of <code>OptAdjListGraph</code>
	 * 
	 * @param in
	 *           The input stream to read the graph from.
	 * @return The newly read graph (an instance of <code>OptAdjListGraph</code>).
	 * @exception ParserException
	 *               if an error occurs while parsing the stream.
	 */
	@Override
	public Graph read(InputStream in_Stream) throws ParserException {
		Graph graph = new OptAdjListGraph();
		read(in_Stream, graph);
		
		return graph;
	}
	
	public void read(Reader in, Graph out_Graph) throws Exception {
		XGMMLContentHandler contentHandler = new XGMMLContentHandler();
		XGMMLDelegatorHandler delegatorHandler = new XGMMLDelegatorHandler(contentHandler);
		
		try {
			
			XMLReader reader =
								XMLReaderFactory.createXMLReader(
													"org.apache.xerces.parsers.SAXParser");
			
			reader.setContentHandler(delegatorHandler);
			reader.setEntityResolver(new XGMMLDTDResolver());
			reader.parse(new InputSource(in));
		} catch (Exception e) {
			throw new ParserException(
								"Error parsing inputstream. Root exception = " + e);
		}
		
		Graph graph = contentHandler.getGraph();
		
		if (graph != null) {
			System.out.println("ading graph");
			out_Graph.addGraph(graph);
		} else
			System.out.println("graph is null");
	}
}
