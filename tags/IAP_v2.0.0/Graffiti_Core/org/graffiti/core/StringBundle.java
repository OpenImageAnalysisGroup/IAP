// ==============================================================================
//
// StringBundle.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StringBundle.java,v 1.1 2011-01-31 09:04:46 klukas Exp $

package org.graffiti.core;

/**
 * The localized resource for the the labels used in the whole system as for
 * label, buttons etc. This class is implemented using the singlton pattern
 * such that there will always be just on instance of a <code>Bundle</code> created.
 * 
 * @see GenericBundle
 */
public class StringBundle
					extends GenericBundle {
	// ~ Static fields/initializers =============================================
	
	/** The only instance which will be created and returned. */
	private static StringBundle instance = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>StringBundle</code>.
	 */
	protected StringBundle() {
		super();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only instance of a <code>Bundle</code> this class generates.
	 */
	public static StringBundle getInstance() {
		if (instance == null) {
			instance = new StringBundle();
		}
		
		return instance;
	}
	
	/**
	 * Returns the location of this <code>Bundle</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	protected String getBundleLocation() {
		return "org/graffiti/core/StringBundle";
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
