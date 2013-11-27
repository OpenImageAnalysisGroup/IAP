package org.graffiti.plugin.io;

import org.graffiti.graph.Graph;

/**
 * Interfaces a "graph post-processor", which is called once a graph is newly
 * loaded from file and once a graph is newly created. This makes it possible to
 * initialize all graphs automatically e.g. with default attributes. Another
 * use case is, the post processing of newly loaded graphs, that are read from
 * file.
 * 
 * @author Christian Klukas
 * @version $Revision: 1.1 $
 */
public interface GraphPostProcessor
					extends Serializer {
	// ~ Methods ================================================================
	
	/**
	 * Call back for processing newly created graphs and newly loaded graphs.
	 * After loading of a file this method will be called. Also
	 * during creation of a new graph this method will be called.
	 * 
	 * @param stream
	 *           The output stream to save the graph to.
	 * @param g
	 *           The graph to save.
	 */
	public void processNewGraph(Graph g);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
