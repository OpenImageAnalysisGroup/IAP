// ==============================================================================
//
// AbstractGraffitiContainer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractGraffitiContainer.java,v 1.1 2011-01-31 09:04:33 klukas Exp $

package org.graffiti.plugin.gui;

/**
 * Abstract class for default containers.
 * 
 * @version $Revision: 1.1 $
 */
public abstract class AbstractGraffitiContainer
					extends AbstractGraffitiComponent
					implements GraffitiContainer {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The id of the <code>AbstractGraffitiContainer</code>. */
	protected String id;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>AbstractGraffitiContainer</code>.
	 */
	protected AbstractGraffitiContainer() {
		super();
	}
	
	/**
	 * Constructs a new <code>AbstractGraffitiContainer</code>.
	 * 
	 * @param id
	 *           DOCUMENT ME!
	 * @param prefComp
	 *           DOCUMENT ME!
	 */
	protected AbstractGraffitiContainer(String id, String prefComp) {
		super(prefComp);
		this.id = id;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns an unique identifier for this <code>GraffitiContainer</code>.
	 * 
	 * @return an unique identifier for this <code>GraffitiContainer</code>.
	 */
	public String getId() {
		return this.id;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
