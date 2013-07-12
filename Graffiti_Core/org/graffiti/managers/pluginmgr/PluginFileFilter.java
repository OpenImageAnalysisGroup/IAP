// ==============================================================================
//
// PluginFileFilter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PluginFileFilter.java,v 1.1 2011-01-31 09:04:52 klukas Exp $

package org.graffiti.managers.pluginmgr;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.graffiti.core.StringBundle;

/**
 * Represents a file filter for graffiti plugins.
 * 
 * @version $Revision: 1.1 $
 */
public class PluginFileFilter
					extends FileFilter {
	// ~ Instance fields ========================================================
	
	/** The <code>StringBundle</code> of the file filter. */
	protected StringBundle sBundle = StringBundle.getInstance();
	
	/** The description of this file filter. */
	private String description;
	
	/** The list of extensions of this file to filter. */
	private String[] extensions = null;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for PluginFileFilter.
	 * 
	 * @param extension
	 *           DOCUMENT ME!
	 */
	public PluginFileFilter(String extension) {
		this(new String[] { extension });
	}
	
	/**
	 * Constructs a new plugin file filter from the given array of extensions.
	 * 
	 * @param extensions
	 *           the array of extensions (<tt>String</tt>) to filter.
	 */
	public PluginFileFilter(String[] extensions) {
		super();
		
		this.extensions = extensions;
		
		StringBuffer exts = new StringBuffer();
		
		for (int i = 0; i < extensions.length; i++) {
			exts.append(extensions[i]);
		}
		
		description = sBundle.getString("plugin.filter.description." +
							exts.toString());
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the extension of the selected file.
	 * 
	 * @param f
	 *           DOCUMENT ME!
	 * @return the extension of the selected file.
	 */
	public String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		
		int i = s.lastIndexOf('.');
		
		if ((i > 0) && (i < (s.length() - 1))) {
			ext = s.substring(i + 1).toLowerCase();
		}
		
		return ext;
	}
	
	/**
	 * @see javax.swing.filechooser.FileFilter#accept(File)
	 */
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		
		String extension = getExtension(f);
		
		if (extension == null) {
			return false;
		}
		
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i].compareTo(extension) == 0) {
				return true;
			}
		}
		
		return false;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
