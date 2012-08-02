// ==============================================================================
//
// StandardAttributeComponents.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StandardAttributeComponents.java,v 1.1 2011-01-31 09:03:39 klukas Exp $

package org.graffiti.plugins.attributecomponents.simplelabel;

import org.graffiti.graphics.EdgeLabelAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.graffiti.plugin.EditorPluginAdapter;

/**
 * This plugin contains the standard attribute-attributeComponent mappings.
 * 
 * @version $Revision: 1.1 $
 */
public class StandardAttributeComponents
					extends EditorPluginAdapter {
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new StandardAttributeComponents object.
	 */
	@SuppressWarnings("unchecked")
	public StandardAttributeComponents() {
		this.attributeComponents.put(NodeLabelAttribute.class,
							LabelComponent.class);
		this.attributeComponents.put(EdgeLabelAttribute.class,
							LabelComponent.class);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
