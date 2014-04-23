// ==============================================================================
//
// GMLReader.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GMLReader.java,v 1.1 2011-01-31 09:03:33 klukas Exp $

package org.graffiti.plugins.ios.importers.gml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractInputSerializer;
import org.graffiti.plugin.io.ParserException;
import org.graffiti.plugins.ios.importers.TypedAttributeService;

/**
 * This class provides a reader for graphs in gml format.
 * 
 * @see org.graffiti.plugin.io.AbstractIOSerializer
 */
public class GMLReader
					extends AbstractInputSerializer {
	// ~ Instance fields ========================================================
	
	/** The supported extensions. */
	private String[] extensions = { ".gml" };
	
	/** The parser for reading in the graph. */
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>GMLReader</code>
	 */
	public GMLReader() {
	}
	
	// ~ Methods ================================================================
	
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
		return new String[] { "GML" };
	}
	
	/**
	 * Reads in a graph from the given input stream. <code>GraphElements</code> read are <b>cloned</b> when added to the graph. Consider using the
	 * <code>read(InputStream)</code> method when you start with an empty
	 * graph.
	 * 
	 * @param in
	 *           the <code>InputStream</code> from which to read in the graph.
	 * @param g
	 *           the graph in which to read in the file.
	 * @throws IOException
	 */
	@Override
	public void read(InputStream in, Graph g)
						throws IOException {
		g.addGraph(read(in));
	}
	
	/**
	 * Reads in a graph from the given input stream. This implementation
	 * returns an instance of <code>OptAdjListGraph</code> (that's what the
	 * parser returns).
	 * 
	 * @param in
	 *           The input stream to read the graph from.
	 * @return The newly read graph (an instance of <code>OptAdjListGraph</code>).
	 * @exception IOException
	 *               If an IO error occurs.
	 * @throws ParserException
	 *            DOCUMENT ME!
	 */
	@Override
	public Graph read(InputStream in)
						throws IOException {
		parser p = null;
		try {
			p = new parser(new Yylex(new java.io.BufferedReader(new java.io.InputStreamReader(in, Charset.forName(StringManipulationTools.Unicode))))); // defaultCharset()))));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			p = new parser(new Yylex(new java.io.BufferedReader(new java.io.InputStreamReader(in, Charset.defaultCharset()))));
		}
		try {
			p.parse();
			Graph gg = p.getGraph();
			in.close();
			p = null;
			return TypedAttributeService.createTypedHashMapAttributes(gg);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			try {
				in.close();
			} catch (Exception err) {
				// empty
			}
			throw new ParserException(e.getMessage());
		}
	}
	
	public void read(Reader reader, Graph newGraph) {
		parser p = new parser(new Yylex(new java.io.BufferedReader(reader)));
		try {
			p.parse();
			Graph gg = TypedAttributeService.createTypedHashMapAttributes(p.getGraph());
			newGraph.addGraph(gg);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
