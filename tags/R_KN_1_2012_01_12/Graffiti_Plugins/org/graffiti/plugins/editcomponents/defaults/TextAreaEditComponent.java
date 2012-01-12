// ==============================================================================
//
// TextAreaEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: TextAreaEditComponent.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import javax.swing.JTextArea;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.Displayable;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 */
public class TextAreaEditComponent
					extends StringEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for TextAreaEditComponent.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public TextAreaEditComponent(Displayable disp) {
		super(disp);
		this.textComp = new JTextArea();
	}
	
	/**
	 * Constructor for TextAreaEditComponent.
	 * 
	 * @param attr
	 *           DOCUMENT ME!
	 */
	public TextAreaEditComponent(StringAttribute attr) {
		super(attr);
		this.textComp = new JTextArea(attr.getString());
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
