//==============================================================================
//
//   GraphMLReader.java
//
//   Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
//==============================================================================
// $Id: GraphMLReader.java,v 1.1 2011-01-31 09:03:24 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractInputSerializer;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugins.ios.importers.graphml.parser.GraphMLParser;

/**
 * This class implements the interface to invoke the reading of graphML files.
 *
 * @author ruediger
 */
public class GraphMLReader
    extends AbstractInputSerializer
    implements InputSerializer
{
    //~ Static fields/initializers =============================================

    //~ Instance fields ========================================================

    /** The parser for reading the graphml input. */
    private GraphMLParser graphmlParser;

    /** The supported extension. */
    private String[] extensions = { ".graphml" /*, ".xml" */};

    //~ Constructors ===========================================================

    /**
     * Constructs a new <code>GraphMLReader</code>.
     */
    public GraphMLReader()
    {
        super();
        this.graphmlParser = new GraphMLParser();
    }

    //~ Methods ================================================================

    /*
     *
     */
    public String[] getExtensions()
    {
        return this.extensions;
    }
    
    /* (non-Javadoc)
     * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
     */
    public String[] getFileTypeDescriptions() {
        return new String[] { "GraphML" /* , "GraphXML"*/};
    }

    /*
     *
     */
    @Override
	public void read(InputStream in, Graph g)
        throws IOException
    {
        this.graphmlParser.parse(in, g);
        in.close();
    }

	public void read(Reader reader, Graph newGraph) throws Exception {
		  this.graphmlParser.parse(reader, newGraph);
	}
}

//------------------------------------------------------------------------------
//   end of file
//------------------------------------------------------------------------------
