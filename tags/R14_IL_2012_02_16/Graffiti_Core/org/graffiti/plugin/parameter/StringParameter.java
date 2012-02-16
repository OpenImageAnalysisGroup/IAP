// ==============================================================================
//
// StringParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StringParameter.java,v 1.1 2011-01-31 09:05:02 klukas Exp $

package org.graffiti.plugin.parameter;

import java.util.ArrayList;
import java.util.Collection;

import scenario.ProvidesScenarioSupportCommand;

/**
 * Parameter that contains an <code>Integer</code> value.
 * 
 * @version $Revision: 1.1 $
 */
public class StringParameter
					extends AbstractSingleParameter
					implements ProvidesScenarioSupportCommand {
	// ~ Instance fields ========================================================
	
	/** The value of this parameter. */
	private String value = null;
	
	// ~ Constructors ===========================================================
	
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
	public StringParameter(String value, String name, String description) {
		super(name, description);
		this.value = value;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the value of this parameter as an <code>String</code>.
	 * 
	 * @return the value of this parameter as an <code>String</code>.
	 */
	public String getString() {
		return value;
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
			this.value = (String) value;
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
	
	/**
	 * @see org.graffiti.plugin.parameter.Parameter#toXMLString()
	 */
	@Override
	public String toXMLString() {
		return getStandardXML(value.toString());
	}
	
	public String getScenarioCommand() {
		return "new StringParameter(\"" +
							getString() + "\", \"" + getName() + "\", \"" + getDescription() + "\")";
	}
	
	public Collection<String> getScenarioImports() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("import org.graffiti.plugin.parameter.StringParameter;");
		return res;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
