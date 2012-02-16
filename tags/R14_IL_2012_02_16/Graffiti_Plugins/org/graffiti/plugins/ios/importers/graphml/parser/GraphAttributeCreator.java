//==============================================================================
//
//   GraphAttributeCreator.java
//
//   Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
//==============================================================================
// $Id: GraphAttributeCreator.java,v 1.1 2011-01-31 09:03:36 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml.parser;

import org.graffiti.attributes.CollectionAttribute;

/**
 * Class <code>GraphAttributeCreator</code> is used for reading and creating
 * graph attributes.
 *
 * @author ruediger
 */
class GraphAttributeCreator
    extends AttributeCreator
{
    //~ Constructors ===========================================================

    /**
     * Constructs a new <code>GraphAttributeCreator</code>.
     */
    public GraphAttributeCreator()
    {
        super();
    }

    //~ Methods ================================================================

    /*
     *
     */
    @Override
	CollectionAttribute createDefaultAttribute()
    {
        return null;
    }
}

//------------------------------------------------------------------------------
//   end of file
//------------------------------------------------------------------------------
