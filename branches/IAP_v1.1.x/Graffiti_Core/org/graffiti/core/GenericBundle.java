// ==============================================================================
//
// GenericBundle.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GenericBundle.java,v 1.2 2011-02-05 20:33:31 klukas Exp $

package org.graffiti.core;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A generalized localized resource bundle.
 */
public abstract class GenericBundle {
	// ~ Instance fields ========================================================
	
	/** Resource bundle. */
	protected ResourceBundle resources;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs new <code>GenericBundle</code>.
	 */
	protected GenericBundle() {
		try {
			resources = ResourceBundle.getBundle(getBundleLocation(),
								Locale.getDefault());
		} catch (Throwable mre) {
			System.err.println(getBundleLocation() + ".properties not found.");
			mre.printStackTrace(System.err);
		}
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the relative location of the specified resource.
	 * 
	 * @param s
	 *           the name of the resource.
	 * @return the relative location of the specified resource.
	 */
	public URL getRes(String s) {
		if (s == null) {
			return null;
		}
		
		String res = getString(s);
		
		if (res == null) {
			return null;
		}
		
		return GenericBundle.class.getClassLoader().getResource(res);
	}
	
	/**
	 * Returns the specified String from the properties, <code>null</code> if
	 * there is no such key.
	 * 
	 * @param id
	 *           the key of the String to look up.
	 * @return the value of the looked up key.
	 */
	public String getString(String id) {
		try {
			return resources.getString(id);
		} catch (Throwable mre) {
			return null;
		}
	}
	
	/**
	 * Returns the location of the bundle. This is a String like <code>package/subpackage/classname</code>.
	 * 
	 * @return the location of the bundle.
	 */
	protected abstract String getBundleLocation();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
