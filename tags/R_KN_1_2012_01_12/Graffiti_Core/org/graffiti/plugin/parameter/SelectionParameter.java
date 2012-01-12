// ==============================================================================
//
// SelectionParameter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SelectionParameter.java,v 1.1 2011-01-31 09:05:02 klukas Exp $

package org.graffiti.plugin.parameter;

import org.graffiti.selection.Selection;

/**
 * This class contains a single <code>Node</code>.
 * 
 * @version $Revision: 1.1 $
 */
public class SelectionParameter
					extends AbstractSingleParameter {
	// ~ Instance fields ========================================================
	
	/** The value of this parameter. */
	private Selection value = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new selection parameter.
	 * 
	 * @param name
	 *           the name of the parameter.
	 * @param description
	 *           the description of the parameter.
	 */
	public SelectionParameter(String name, String description) {
		super(name, description);
	}
	
	/**
	 * Constructs a new selection parameter.
	 * 
	 * @param name
	 * @param description
	 * @param sel
	 */
	public SelectionParameter(String name, String description, Selection sel) {
		super(name, description);
		setSelection(sel);
	}
	
	/**
	 * Constructs a new selection parameter.
	 * 
	 * @param sel
	 * @param name
	 * @param description
	 */
	public SelectionParameter(Selection sel, String name, String description) {
		super(name, description);
		setSelection(sel);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the selection.
	 * 
	 * @param selection
	 *           DOCUMENT ME!
	 */
	public void setSelection(Selection selection) {
		this.value = selection;
	}
	
	/**
	 * Returns the selection encapsulated in this parameter.
	 * 
	 * @return DOCUMENT ME!
	 */
	public Selection getSelection() {
		return this.value;
	}
	
	/**
	 * Sets the value of the <code>AttributeParameter</code>.
	 * 
	 * @param value
	 *           the new value of the <code>AttributeParameter</code>.
	 */
	@Override
	public void setValue(Object value) {
		this.value = (Selection) value;
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
