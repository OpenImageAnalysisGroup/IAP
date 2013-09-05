// ==============================================================================
//
// ObjectParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ObjectParameter.java,v 1.1 2011-01-31 09:05:03 klukas Exp $

package org.graffiti.plugin.parameter;

/**
 * Parameter that contains an <code>Object</code> value.
 * 
 * @version $Revision: 1.1 $
 */
public class ObjectParameter
					extends AbstractSingleParameter {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private Object object;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new integer parameter.
	 * 
	 * @param value
	 *           DOCUMENT ME!
	 * @param name
	 *           DOCUMENT ME!
	 * @param description
	 *           DOCUMENT ME!
	 */
	public ObjectParameter(Object value, String name, String description) {
		super(name, description);
		
		this.object = value;
	}
	
	/**
	 * Constructs a new integer parameter.
	 * 
	 * @param name
	 *           DOCUMENT ME!
	 * @param description
	 *           DOCUMENT ME!
	 */
	public ObjectParameter(String name, String description) {
		super(name, description);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.Displayable#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object val)
						throws IllegalArgumentException {
		object = val;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.Displayable#getValue()
	 */
	@Override
	public Object getValue() {
		return object;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
