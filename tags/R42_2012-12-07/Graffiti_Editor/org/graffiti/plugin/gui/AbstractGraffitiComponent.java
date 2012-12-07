// ==============================================================================
//
// AbstractGraffitiComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractGraffitiComponent.java,v 1.1 2011-01-31 09:04:32 klukas Exp $

package org.graffiti.plugin.gui;

import javax.swing.JPanel;

import org.graffiti.editor.MainFrame;

/**
 * Abstract class for default containers.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AbstractGraffitiComponent
					extends JPanel
					implements GraffitiComponent {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The component wherer the current <code>AbstractGraffitiContainer</code> prefers to be inserted.
	 */
	protected String preferredComponent;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>AbstractGraffitiContainer</code>.
	 */
	protected AbstractGraffitiComponent() {
		super();
	}
	
	/**
	 * Constructs a new <code>AbstractGraffitiContainer</code>.
	 * 
	 * @param prefComp
	 *           DOCUMENT ME!
	 */
	protected AbstractGraffitiComponent(String prefComp) {
		super();
		this.preferredComponent = prefComp;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#setMainFrame(org.graffiti.editor.MainFrame)
	 */
	public void setMainFrame(MainFrame mf) {
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getPreferredComponent() {
		return this.preferredComponent;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
