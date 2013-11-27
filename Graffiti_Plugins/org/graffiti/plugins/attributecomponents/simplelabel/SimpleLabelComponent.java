// ==============================================================================
//
// SimpleLabelComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SimpleLabelComponent.java,v 1.1 2011-01-31 09:03:39 klukas Exp $

package org.graffiti.plugins.attributecomponents.simplelabel;

import org.graffiti.graphics.GraphicAttributeConstants;

/**
 * This component represents a label for a node or an edge. It is displayed via
 * a JTextField.
 * 
 * @version $Revision: 1.1 $
 */
public class SimpleLabelComponent
					extends LabelComponent
					implements GraphicAttributeConstants {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates an <code>LabelComponent</code>
	 */
	public SimpleLabelComponent() {
		super();
		
		// done in recreate:
		// this.labelPanel = new JTextField(DEFAULT_WIDTH);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
