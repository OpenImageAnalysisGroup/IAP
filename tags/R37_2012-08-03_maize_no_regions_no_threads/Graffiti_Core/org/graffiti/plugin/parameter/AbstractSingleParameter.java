// ==============================================================================
//
// AbstractSingleParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractSingleParameter.java,v 1.1 2011-01-31 09:05:02 klukas Exp $

package org.graffiti.plugin.parameter;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.graffiti.plugin.XMLHelper;

/**
 * Implements functions that are common in all SingleParameters.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AbstractSingleParameter
					implements SingleParameter {
	// ~ Instance fields ========================================================
	
	/** The image representing the parameter. */
	private BufferedImage image = null;
	
	/** The value of this paramater. */
	private Object value;
	
	/** The description of the parameter. */
	private String description;
	
	/** The name of the parameter. */
	private String name;
	
	private boolean left_aligned;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new abstract single parameter class.
	 * 
	 * @param val
	 *           DOCUMENT ME!
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public AbstractSingleParameter(Object val, String name, String description) {
		this.name = name;
		this.description = description;
		value = val;
	}
	
	/**
	 * Constructs a new abstract single parameter class.
	 * 
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public AbstractSingleParameter(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the description.
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the description of the parameter.
	 * 
	 * @return the description of the parameter.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the image representing the parameter.
	 * 
	 * @return the image representing the parameter.
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	/**
	 * Returns the name of the parameter.
	 * 
	 * @return the name of the parameter.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @see org.graffiti.plugin.Displayable#setValue(java.lang.Object)
	 */
	public void setValue(Object val)
						throws IllegalArgumentException {
		value = val;
	}
	
	/**
	 * @see org.graffiti.plugin.Displayable#getValue()
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (getValue() == null) {
			return "Parameter (" + name + "), value: null";
		} else {
			return "Parameter (" + name + "), value: " + getValue().toString();
		}
	}
	
	/**
	 * @see org.graffiti.plugin.parameter.Parameter#toXMLString()
	 */
	public String toXMLString() {
		String valStr = (value == null) ? "null" : value.toString();
		
		return "<parameter classname=\\\"" + getClass().getName() + "\\\">" +
							XMLHelper.getDelimiter() + XMLHelper.spc(2) + "<value><![CDATA[" +
							valStr + "]]>" + XMLHelper.getDelimiter() + XMLHelper.spc(2) +
							"</value>" + XMLHelper.getDelimiter() + "</parameter>";
	}
	
	/**
	 * Embeds the given String into an XML String. It includes the classname of
	 * the parameter and a "value" element that gets the given String <code>valueString</code> as content.
	 * 
	 * @param valueString
	 * @return DOCUMENT ME!
	 */
	protected String getStandardXML(String valueString) {
		return "<parameter classname=\\\"" + getClass().getName() +
							"\\\" name=\\\"" + getName() + "\\\" description=\\\"" +
							getDescription() + "\\\">" + XMLHelper.getDelimiter() +
							XMLHelper.spc(2) + "<value>" + XMLHelper.getDelimiter() +
							XMLHelper.spc(4) + valueString + XMLHelper.getDelimiter() +
							XMLHelper.spc(2) + "</value>" + XMLHelper.getDelimiter() +
							"</parameter>";
	}
	
	public JComponent getIcon() {
		return null;
	}
	
	public boolean isLeftAligned() {
		return left_aligned;
	}
	
	public void setLeftAligned(boolean left_aligned) {
		this.left_aligned = left_aligned;
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
