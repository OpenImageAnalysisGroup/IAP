// ==============================================================================
//
// FloatEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FloatEditComponent.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import org.graffiti.attributes.FloatAttribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.NumberEditComponent;

/**
 * An edit component for editing floats. Quite small since NumberEditComponent
 * does most of the work.
 * 
 * @see NumberEditComponent
 */
public class FloatEditComponent
					extends NumberEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>FloatEditComponent</code>.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public FloatEditComponent(Displayable disp) {
		super(disp);
	}
	
	/**
	 * Constructs a new <code>FloatEditComponent</code>.
	 * 
	 * @param attr
	 *           the <code>FloatAttribute</code> to be edited.
	 */
	public FloatEditComponent(FloatAttribute attr) {
		super(attr);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
