// ==============================================================================
//
// AttributeComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeComponent.java,v 1.1 2011-01-31 09:04:24 klukas Exp $

package org.graffiti.plugin.view;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import org.graffiti.attributes.Attribute;

/**
 * This component represents a <code>org.graffiti.attributes.Attribute</code>.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AttributeComponent
					extends JComponent
					implements GraffitiViewComponent {
	// ~ Methods ================================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sets an instance of attribute which this component displays.
	 * 
	 * @param attr
	 */
	public abstract void setAttribute(Attribute attr);
	
	/**
	 * Returns the attribute that is displayed by this component.
	 * 
	 * @return the attribute that is displayed by this component.
	 */
	public abstract Attribute getAttribute();
	
	/**
	 * Sets shape of graph element to which the attribute of this component
	 * belongs.
	 * 
	 * @param geShape
	 */
	public abstract void setGraphElementShape(GraphElementShape geShape);
	
	public abstract void adjustComponentSize();
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param shift
	 *           DOCUMENT ME!
	 */
	public abstract void setShift(Point shift);
	
	/**
	 * Called when a graphics attribute of the attribute represented by this
	 * component has changed.
	 * 
	 * @param attr
	 *           the attribute that has triggered the event.
	 */
	public abstract void attributeChanged(Attribute attr)
						throws ShapeNotFoundException;
	
	/**
	 * Used when the shape changed in the datastructure. Makes the painter to
	 * create a new shape.
	 */
	public abstract void recreate()
						throws ShapeNotFoundException;
	
	public void highlight(boolean value, MouseEvent e) {
		
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
