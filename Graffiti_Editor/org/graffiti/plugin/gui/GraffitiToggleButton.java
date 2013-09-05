// ==============================================================================
//
// GraffitiToggleButton.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiToggleButton.java,v 1.1 2011-01-31 09:04:32 klukas Exp $

package org.graffiti.plugin.gui;

import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * Abstract class for all ToggleButtons that should be used as <code>GraffitiComponents</code>. Provides an implementation for the
 * <code>getPreferredComponent()</code> method.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class GraffitiToggleButton
					extends JToggleButton
					implements GraffitiComponent {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The preferred component of this button. */
	protected String preferredComponent;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new GraffitiToggleButton object.
	 */
	public GraffitiToggleButton() {
	}
	
	/**
	 * Creates a new GraffitiToggleButton object.
	 * 
	 * @param preferredComp
	 *           DOCUMENT ME!
	 */
	public GraffitiToggleButton(String preferredComp) {
		preferredComponent = preferredComp;
	}
	
	/**
	 * Creates a new GraffitiToggleButton object.
	 * 
	 * @param preferredComp
	 *           DOCUMENT ME!
	 * @param text
	 *           DOCUMENT ME!
	 */
	public GraffitiToggleButton(String preferredComp, String text) {
		super(text);
	}
	
	/**
	 * Creates a new GraffitiToggleButton object.
	 * 
	 * @param preferredComp
	 *           DOCUMENT ME!
	 * @param i
	 *           DOCUMENT ME!
	 */
	public GraffitiToggleButton(String preferredComp, Icon i) {
		super(i);
		preferredComponent = preferredComp;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the id of the component the button prefers to be inserted in.
	 * 
	 * @return the id of the component the button prefers to be inserted in.
	 */
	public String getPreferredComponent() {
		return preferredComponent;
	}
	/*
	 * (non-Javadoc)
	 * @see javax.swing.AbstractButton#doClick()
	 */
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
