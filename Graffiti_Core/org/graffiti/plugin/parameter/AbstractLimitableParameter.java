// ==============================================================================
//
// AbstractLimitableParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractLimitableParameter.java,v 1.1 2011-01-31 09:05:03 klukas Exp $

package org.graffiti.plugin.parameter;

/**
 * This abstract class provides an implementation for the <code>isValid</code> method, using the <code>compareTo</code> method of the <code>Comparable</code>
 * interface.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AbstractLimitableParameter
					extends AbstractSingleParameter
					implements LimitableParameter {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new abstract limitable parameter.
	 * 
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public AbstractLimitableParameter(String name, String description) {
		super(name, description);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.parameter.Parameter#toXMLString()
	 */
	@Override
	public String toXMLString() {
		return getStandardXML(getValue().toString());
	}
	
	/**
	 * Returns the maximum of the intervall.
	 * 
	 * @return the maximum of the intervall.
	 */
	public abstract Comparable<?> getMax();
	
	/**
	 * Returns the minimum of the intervall.
	 * 
	 * @return the minimum of the intervall.
	 */
	public abstract Comparable<?> getMin();
	
	/**
	 * Returns <code>true</code> if the value is between the minimum and the
	 * maximum, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the value is between the minimum and the
	 *         maximum, <code>false</code> otherwise.
	 */
	public abstract boolean isValid();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
