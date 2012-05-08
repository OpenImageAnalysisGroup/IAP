// ==============================================================================
//
// ValueEditContainer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ValueEditContainer.java,v 1.1 2011-01-31 09:04:30 klukas Exp $

package org.graffiti.editor.dialog;

import java.util.List;

import org.graffiti.plugin.editcomponent.ValueEditComponent;

/**
 * <code>ValueEditContainer</code> is an interface for an arbitrary component
 * containing a set of <code>ValueEditComponent</code>s. A class implementing
 * this interface can be used either within a dialog or within a separate
 * frame etc.
 * 
 * @see org.graffiti.plugin.editcomponent.ValueEditComponent
 */
public interface ValueEditContainer {
	// ~ Methods ================================================================
	
	/**
	 * Returns a <code>java.util.List</code> containing all the edit components
	 * of this <code>ValueEditContainer</code>.
	 * 
	 * @return a <code>java.util.List</code> containing all the edit components
	 *         of this <code>ValueEditContainer</code>.
	 */
	public List<?> getEditComponents();
	
	/**
	 * Adds another <code>ValueEditComponent</code> to the dialog.
	 * 
	 * @param vec
	 *           the <code>ValueEditComponent</code> to be added to the
	 *           dialog.
	 */
	public void addValueEditComponent(ValueEditComponent vec);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
