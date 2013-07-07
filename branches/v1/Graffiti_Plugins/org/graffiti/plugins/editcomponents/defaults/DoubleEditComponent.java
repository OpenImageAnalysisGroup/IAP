// ==============================================================================
//
// DoubleEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DoubleEditComponent.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.NumberEditComponent;

/**
 * Represents a gui component, which handles double values. Can be left empty
 * because superclass handles all primitive types.
 * 
 * @see NumberEditComponent
 */
public class DoubleEditComponent
					extends NumberEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new DoubleEditComponent object.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public DoubleEditComponent(Displayable disp) {
		super(disp);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
