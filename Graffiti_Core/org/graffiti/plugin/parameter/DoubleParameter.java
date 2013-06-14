// ==============================================================================
//
// DoubleParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DoubleParameter.java,v 1.1 2011-01-31 09:05:03 klukas Exp $

package org.graffiti.plugin.parameter;

import java.util.ArrayList;
import java.util.Collection;

import scenario.ProvidesScenarioSupportCommand;

/**
 * Represents a double parameter.
 * 
 * @version $Revision: 1.1 $
 */
public class DoubleParameter
					extends AbstractLimitableParameter
					implements ProvidesScenarioSupportCommand {
	// ~ Instance fields ========================================================
	
	/** The value of this parameter. */
	private Double value = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new double parameter.
	 * 
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public DoubleParameter(String name, String description) {
		super(name, description);
	}
	
	/**
	 * Constructs a new double parameter.
	 * 
	 * @param val
	 *           the value of the parameter
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public DoubleParameter(double val, String name, String description) {
		super(name, description);
		value = new Double(val);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param val
	 *           DOCUMENT ME!
	 */
	public void setDouble(Double val) {
		this.value = val;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param val
	 *           DOCUMENT ME!
	 */
	public void setDouble(double val) {
		this.value = new Double(val);
	}
	
	/**
	 * Returns the value of this parameter as a <code>Double</code>.
	 * 
	 * @return the value of this parameter as a <code>Double</code>.
	 */
	public Double getDouble() {
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
		this.value = (Double) value;
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
		return "new DoubleParameter(" +
							getDouble() + ", \"" + getName() + "\", \"" + getDescription() + "\")";
	}
	
	public Collection<String> getScenarioImports() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("import org.graffiti.plugin.parameter.DoubleParameter;");
		return res;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
