//==============================================================================
//
//   GraphMLException.java
//
//   Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
//==============================================================================
// $Id: GraphMLException.java,v 1.1 2011-01-31 09:03:24 klukas Exp $

package org.graffiti.plugins.ios.importers.graphml;

import java.io.IOException;

/**
 * This exception is thrown when errors occur during parsing.
 *
 * @author ruediger
 */
public class GraphMLException
    extends IOException
{
    //~ Constructors ===========================================================

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <code>GraphMLException</code> from a given
     * <code>Throwable</code>.
     *
     * @param cause the <code>Throwable</code> that caused the exception to be
     *        thrown.
     */
    public GraphMLException(Throwable cause)
    {
        super(cause.getMessage());
        this.initCause(cause);
    }
}

//------------------------------------------------------------------------------
//   end of file
//------------------------------------------------------------------------------
