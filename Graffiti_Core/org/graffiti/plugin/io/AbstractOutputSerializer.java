// ==============================================================================
//
// AbstractOutputSerializer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractOutputSerializer.java,v 1.1 2011-01-31 09:04:57 klukas Exp $

package org.graffiti.plugin.io;

import java.io.FileOutputStream;
import java.io.IOException;

import org.graffiti.graph.Graph;

/**
 * Provides additional methods to write a graph object.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AbstractOutputSerializer
					implements OutputSerializer {
	// ~ Methods ================================================================
	
	/**
	 * Writes the contents of the given graph to a file.
	 * 
	 * @param g
	 *           The graph to save.
	 * @param filename
	 *           The name of the file to save the graph to.
	 * @exception IOException
	 *               If an IO error occurs.
	 */
	public void write(Graph g, String filename)
						throws IOException {
		write(new FileOutputStream(filename), g);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
