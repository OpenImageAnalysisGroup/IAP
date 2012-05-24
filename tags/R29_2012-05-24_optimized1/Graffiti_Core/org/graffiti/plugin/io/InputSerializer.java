// ==============================================================================
//
// InputSerializer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: InputSerializer.java,v 1.1 2011-01-31 09:04:57 klukas Exp $

package org.graffiti.plugin.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import org.graffiti.graph.Graph;

/**
 * Interfaces a serializer, which is able to reconstruct a graph from a given
 * input.
 * 
 * @version $Revision: 1.1 $
 */
public interface InputSerializer extends Serializer {
	// ~ Methods ================================================================
	
	/**
	 * Reads in a graph from the given filename.
	 * 
	 * @param filename
	 *           The name of the file to read the graph from.
	 * @param g
	 *           The graph to add the newly read graph to.
	 * @exception IOException
	 *               If an IO error occurs.
	 */
	public void read(String filename, Graph g) throws IOException;
	
	/**
	 * @param reader
	 *           Warning: The Inputstream-Length may be limited in size, e.g. may
	 *           only provide access to the first 5000 bytes.
	 * @return
	 */
	public boolean validFor(InputStream reader);
	
	/**
	 * Reads in the graph from the given url.
	 * 
	 * @param url
	 *           The URL to read the graph from.
	 * @param g
	 *           The graph to add the newly read graph to.
	 * @exception IOException
	 *               If an IO error occurs.
	 */
	public void read(URL url, Graph g) throws IOException;
	
	/**
	 * Reads in a graph from the given input stream.
	 * 
	 * @param in
	 *           The input stream to read the graph from.
	 * @param g
	 *           The graph to add the newly read graph to.
	 * @exception IOException
	 *               If an IO error occurs.
	 */
	public void read(InputStream in, Graph g) throws IOException;
	
	/**
	 * Reads in a graph from the given input stream.
	 * 
	 * @param in
	 *           The input stream to read the graph from.
	 * @return The newly read graph.
	 * @exception IOException
	 *               If an IO error occurs.
	 */
	public Graph read(InputStream in) throws IOException;
	
	public void read(Reader reader, Graph newGraph) throws Exception;
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
