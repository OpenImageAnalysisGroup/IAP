// ==============================================================================
//
// GraffitiButton.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiButton.java,v 1.1 2011-01-31 09:04:33 klukas Exp $

package org.graffiti.plugin.gui;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

import org.graffiti.editor.MainFrame;

public abstract class GraffitiButton
					extends JButton
					implements GraffitiComponent {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The preffered component of this button. */
	protected String preferredComponent;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new GraffitiButton object.
	 * 
	 * @param preferredComp
	 *           DOCUMENT ME!
	 */
	public GraffitiButton(String preferredComp) {
		preferredComponent = preferredComp;
	}
	
	/**
	 * Creates a new GraffitiButton object.
	 * 
	 * @param preferredComp
	 *           DOCUMENT ME!
	 * @param text
	 *           DOCUMENT ME!
	 */
	public GraffitiButton(String preferredComp, String text) {
		super(text);
		preferredComponent = preferredComp;
	}
	
	/**
	 * Creates a new GraffitiButton object.
	 * 
	 * @param preferredComp
	 *           DOCUMENT ME!
	 * @param i
	 *           DOCUMENT ME!
	 */
	public GraffitiButton(String preferredComp, Icon i) {
		super(i);
		preferredComponent = preferredComp;
	}
	
	/**
	 * Creates a new GraffitiButton object.
	 * 
	 * @param preferredComp
	 *           DOCUMENT ME!
	 * @param a
	 *           DOCUMENT ME!
	 */
	public GraffitiButton(String preferredComp, Action a) {
		super(a);
		preferredComponent = preferredComp;
	}
	
	/**
	 * Constructs a new <code>GraffitiButton</code>.
	 */
	protected GraffitiButton() {
		super();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#setMainFrame(org.graffiti.editor.MainFrame)
	 */
	public void setMainFrame(MainFrame mf) {
	}
	
	/**
	 * Returns the id of the component the button prefers to be inserted in.
	 * 
	 * @return the id of the component the button prefers to be inserted in.
	 */
	public String getPreferredComponent() {
		return this.preferredComponent;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
