// ==============================================================================
//
// PluginManagerException.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PluginManagerException.java,v 1.1 2011-01-31 09:04:51 klukas Exp $

package org.graffiti.managers.pluginmgr;

import org.graffiti.core.StringBundle;

/**
 * <code>PluginManagerException</code> is thrown, iff an error occured during
 * the loading of a plugin.
 * 
 * @version $Revision: 1.1 $
 */
public class PluginManagerException
					extends Exception {
	// ~ Static fields/initializers =============================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The <code>StringBundle</code> of the exception. */
	protected static StringBundle sBundle = StringBundle.getInstance();
	
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private Dependency dependency;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for PluginManagerException.
	 * 
	 * @param key
	 *           the error message of this exception.
	 */
	public PluginManagerException(String key) {
		super(sBundle.getString(key) != null ? sBundle.getString(key) : key);
	}
	
	/**
	 * Constructs a plugin manager exception from the given parameters.
	 * 
	 * @param key
	 *           the property key in the plugin manager's resource bundle.
	 * @param message
	 *           the additional message of the exception.
	 */
	public PluginManagerException(String key, String message) {
		super((sBundle.getString(key) != null ? sBundle.getString(key) : key) + message);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the dependency.
	 * 
	 * @param dependency
	 *           The dependency to set
	 */
	public void setDependency(Dependency dependency) {
		this.dependency = dependency;
	}
	
	/**
	 * Returns the dependency.
	 * 
	 * @return Dependency
	 */
	public Dependency getDependency() {
		return dependency;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
