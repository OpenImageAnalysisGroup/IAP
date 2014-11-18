// ==============================================================================
//
// Displayable.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Displayable.java,v 1.1 2011-01-31 09:05:04 klukas Exp $

package org.graffiti.plugin;

import javax.swing.JComponent;

/**
 * DOCUMENT ME!
 * 
 * @author ph
 */
public interface Displayable {
	// ~ Methods ================================================================
	
	/**
	 * Sets a short description of this object.
	 * 
	 * @param desc
	 */
	public void setDescription(String desc);
	
	/**
	 * Returns a short description of this object.
	 * 
	 * @return String
	 */
	public String getDescription();
	
	/**
	 * Returns the name of this object.
	 * 
	 * @return String
	 */
	public String getName();
	
	/**
	 * Sets the encapsulated object.
	 * 
	 * @param val
	 * @exception IllegalArgumentException
	 *               thrown if val is not of the
	 *               apropriate type.
	 */
	public void setValue(Object val)
						throws IllegalArgumentException;
	
	/**
	 * Returns the encapsulated object.
	 * 
	 * @return Object
	 */
	public Object getValue();
	
	/**
	 * Returns a well-formed XML string representing the Displayable. The
	 * Displayable should be reconstructable via this representation.
	 * Therefore it must at least include the type of Displayable (classname)
	 * and a representation of its value.
	 * <i>PROBABLE FUTURE DESIGN</i>:
	 * The Displayables themselves will provide a method to reconstruct their
	 * value from the XML representation they provided.
	 * 
	 * @return string holding an XML representation of this Displayable
	 */
	public String toXMLString();
	
	public JComponent getIcon();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
