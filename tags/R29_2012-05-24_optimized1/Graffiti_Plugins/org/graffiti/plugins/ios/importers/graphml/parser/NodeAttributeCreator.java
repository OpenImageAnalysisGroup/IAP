//==============================================================================
//
//   NodeAttributeCreator.java
//
//   Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
//==============================================================================
// $Id: NodeAttributeCreator.java,v 1.1 2011-01-31 09:03:36 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml.parser;

import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;

/**
 * Class <code>NodeAttributeCreator</code> is used for creating
 * <code>Node</code> attributes.
 *
 * @author ruediger
 */
class NodeAttributeCreator
    extends AttributeCreator
{
    //~ Constructors ===========================================================

    /**
     * Constructs a new <code>NodeAttributeCreator</code>.
     */
    NodeAttributeCreator()
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
        return new NodeGraphicAttribute();
    }
}

//------------------------------------------------------------------------------
//   end of file
//------------------------------------------------------------------------------
