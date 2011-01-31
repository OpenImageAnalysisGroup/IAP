// ==============================================================================
//
// IntegerParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: IntegerParameter.java,v 1.1 2011-01-31 09:05:03 klukas Exp $

package org.graffiti.plugin.parameter;

import java.util.ArrayList;
import java.util.Collection;

import scenario.ProvidesScenarioSupportCommand;

/**
 * Parameter that contains an <code>Integer</code> value.
 * 
 * @version $Revision: 1.1 $
 */
public class IntegerParameter
					extends AbstractLimitableParameter
					implements ProvidesScenarioSupportCommand {
	// ~ Instance fields ========================================================
	
	/** The maximum valid value of this parameter. */
	private Integer max = null;
	
	/** The minimum valid value of this parameter. */
	private Integer min = null;
	
	/** The value of this parameter. */
	private Integer value = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new integer parameter.
	 * 
	 * @param value
	 *           the new integer value. May be null.
	 * @param min
	 *           the minimum value.
	 * @param max
	 *           the maximum value.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public IntegerParameter(Integer value, Integer min, Integer max,
						String name, String description) {
		super(name, description);
		
		this.value = value;
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Constructs a new integer parameter.
	 * 
	 * @param value
	 *           the new integer value. May be null.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public IntegerParameter(Integer value, String name, String description) {
		super(name, description);
		this.value = value;
	}
	
	/**
	 * Constructs a new integer parameter.
	 * 
	 * @param value
	 *           the new integer value.
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public IntegerParameter(int value, String name, String description) {
		super(name, description);
		this.value = new Integer(value);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the value of this parameter as an <code>Integer</code>.
	 * 
	 * @return the value of this parameter as an <code>Integer</code>.
	 */
	public Integer getInteger() {
		return value;
	}
	
	/**
	 * Returns the maximum of the intervall.
	 * 
	 * @return the maximum of the intervall.
	 */
	@Override
	public Comparable<?> getMax() {
		return max;
	}
	
	/**
	 * Returns the minimum of the intervall.
	 * 
	 * @return the minimum of the intervall.
	 */
	@Override
	public Comparable<?> getMin() {
		return min;
	}
	
	/**
	 * Returns <code>true</code>, if the current value is valid.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isValid() {
		if (value == null) {
			return false;
		}
		
		if ((min == null) && (max == null)) {
			return true;
		}
		
		return ((min.compareTo(value) < 0) && (max.compareTo(value) > 0));
	}
	
	/**
	 * Sets the value of the <code>AttributeParameter</code>.
	 * 
	 * @param value
	 *           the new value of the <code>AttributeParameter</code>.
	 * @exception IllegalArgumentException
	 *               thrown if <code>value</code> is not
	 *               of the correct type.
	 */
	@Override
	public void setValue(Object value) {
		try {
			this.value = (Integer) value;
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
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
	
	public String getScenarioCommand() {
		return "new IntegerParameter(" +
							getInteger().intValue() + ", \"" + getName() + "\", \"" + getDescription() + "\")";
	}
	
	public Collection<String> getScenarioImports() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("import org.graffiti.plugin.parameter.IntegerParameter;");
		return res;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
