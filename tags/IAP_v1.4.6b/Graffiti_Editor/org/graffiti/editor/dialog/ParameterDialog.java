// ==============================================================================
//
// ParameterDialog.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ParameterDialog.java,v 1.1 2011-01-31 09:04:30 klukas Exp $

package org.graffiti.editor.dialog;

import java.util.Collection;
import java.util.List;

import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.session.Session;

/**
 * <code>ParameterDialog</code> provides an interface for dialogs displaying
 * name-value pairs where the value can be edited.
 * 
 * @see org.graffiti.plugin.editcomponent.ValueEditComponent
 */
public interface ParameterDialog {
	// ~ Methods ================================================================
	
	/**
	 * Returns the array of edited parameters.
	 * 
	 * @return the array of edited parameters.
	 */
	Parameter[] getEditedParameters();
	
	/**
	 * Returns <code>true</code>, if the user selected the ok button.
	 * 
	 * @return DOCUMENT ME!
	 */
	boolean isOkSelected();
	
	/**
	 * Sets the <code>ValueEditContainer</code> of this <code>ParameterDialog</code> to the specified value.
	 * 
	 * @param vec
	 *           the <code>ValueEditContainer</code> to be set.
	 */
	void setValueEditContainer(ValueEditContainer vec);
	
	/**
	 * Checks if all the edit components have a syntactically correct input.
	 * 
	 * @return a <code>java.util.List</code> of <code>EditComponent</code>s
	 *         which have a syntactically incorrect input.
	 */
	List<?> validateComponents();
	
	Collection<Session> getTargetSessions();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
