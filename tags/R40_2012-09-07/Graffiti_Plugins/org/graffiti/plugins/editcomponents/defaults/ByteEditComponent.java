// ==============================================================================
//
// ByteEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ByteEditComponent.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.NumberEditComponent;

/**
 * Represents a gui component, which handles byte values. May be empty since
 * superclass handles all primitive types.
 * 
 * @see NumberEditComponent
 */
public class ByteEditComponent
					extends NumberEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new ByteEditComponent object.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public ByteEditComponent(Displayable disp) {
		super(disp);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
