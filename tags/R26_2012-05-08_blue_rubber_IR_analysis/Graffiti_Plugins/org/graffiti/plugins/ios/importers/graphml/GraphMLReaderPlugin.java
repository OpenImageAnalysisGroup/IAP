//==============================================================================
//
//   GraphMLReaderPlugin.java
//
//   Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
//==============================================================================
// $Id: GraphMLReaderPlugin.java,v 1.1 2011-01-31 09:03:24 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml;

import org.graffiti.plugin.GenericPluginAdapter;

/**
 * This plugin provides the functionality for reading graphML files.
 *
 * @author ruediger
 */
public class GraphMLReaderPlugin
    extends GenericPluginAdapter
{
    //~ Constructors ===========================================================

    /**
     * Constructs a new <code>GraphMLReaderPlugin</code>.
     */
    public GraphMLReaderPlugin()
    {
        super();
        this.inputSerializers = new GraphMLReader[1];
        this.inputSerializers[0] = new GraphMLReader();
    }
}

//------------------------------------------------------------------------------
//   end of file
//------------------------------------------------------------------------------
