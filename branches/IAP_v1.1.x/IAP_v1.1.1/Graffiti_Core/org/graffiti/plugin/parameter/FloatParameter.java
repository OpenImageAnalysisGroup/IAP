// ==============================================================================
//
// FloatParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FloatParameter.java,v 1.1 2011-01-31 09:05:02 klukas Exp $

package org.graffiti.plugin.parameter;

/**
 * Parameter that contains a float value.
 * 
 * @version $Revision: 1.1 $
 */
public class FloatParameter
					extends AbstractLimitableParameter {
	// ~ Instance fields ========================================================
	
	/** The value of this parameter. */
	private Float value = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new float parameter.
	 * 
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public FloatParameter(String name, String description) {
		super(name, description);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the value of this parameter as a <code>Float</code>.
	 * 
	 * @return the value of this parameter as a <code>Float</code>.
	 */
	public Float getFloat() {
		return value;
	}
	
	@Override
	public Comparable<?> getMax() {
		return null;
	}
	
	@Override
	public Comparable<?> getMin() {
		return null;
	}
	
	@Override
	public boolean isValid() {
		return false;
	}
	
	/**
	 * Sets the value of the <code>AttributeParameter</code>.
	 * 
	 * @param value
	 *           the new value of the <code>AttributeParameter</code>.
	 */
	@Override
	public void setValue(Object value) {
		// 
	}
	
	/**
	 * Returns the value of this parameter.
	 * 
	 * @return the value of this parameter.
	 */
	@Override
	public Object getValue() {
		return value;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
