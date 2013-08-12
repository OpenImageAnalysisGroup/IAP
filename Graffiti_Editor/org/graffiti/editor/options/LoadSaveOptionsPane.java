// ==============================================================================
//
// LoadSaveOptionsPane.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LoadSaveOptionsPane.java,v 1.1 2011-01-31 09:04:26 klukas Exp $

package org.graffiti.editor.options;

import javax.swing.JComponent;

import org.graffiti.options.AbstractOptionPane;

/**
 * Handles some loading and saving stuff, e.g.: autosave, backups,
 * backupDirectory, backupSuffix.
 * 
 * @version $Revision: 1.1 $
 */
public class LoadSaveOptionsPane
					extends AbstractOptionPane {
	// ~ Constructors ===========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for LoadSaveOptionsPane.
	 */
	public LoadSaveOptionsPane() {
		super("loadsave");
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.options.AbstractOptionPane#initDefault()
	 */
	@Override
	protected void initDefault() {
		// 
	}
	
	/*
	 * @see org.graffiti.options.AbstractOptionPane#saveDefault()
	 */
	@Override
	protected void saveDefault() {
		// 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getCategory()
	 */
	public String getCategory() {
		//
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getOptionName()
	 */
	public String getOptionName() {
		//
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#init(javax.swing.JComponent)
	 */
	public void init(JComponent options) {
		//
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#save(javax.swing.JComponent)
	 */
	public void save(JComponent options) {
		//
		
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
