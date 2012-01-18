// ==============================================================================
//
// PluginHelper.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: PluginHelper.java,v 1.1 2011-01-31 09:04:58 klukas Exp $

package org.graffiti.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.HelperClass;
import org.graffiti.core.StringBundle;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.managers.pluginmgr.PluginManagerException;
import org.graffiti.managers.pluginmgr.PluginXMLParser;

/**
 *
 */
@SuppressWarnings("unchecked")
public class PluginHelper implements HelperClass {
	// ~ Methods ================================================================
	
	/**
	 * Reads and returns the plugin description of the plugin from the given
	 * URL.
	 * 
	 * @param pluginLocation
	 *           the URL to the plugin.
	 * @return DOCUMENT ME!
	 * @exception PluginManagerException
	 *               if an error occurrs while loading the
	 *               plugin description.
	 */
	public static PluginDescription readPluginDescription(URL pluginLocation)
						throws PluginManagerException {
		if (pluginLocation == null) {
			throw new PluginManagerException("exception.MalformedURL", "null");
		}
		
		String fileName = pluginLocation.toString();
		InputStream input;
		
		if (fileName.toLowerCase().endsWith(".xml")) {
			try {
				if (fileName.startsWith("jar:")) {
					JarURLConnection juc = (JarURLConnection) pluginLocation.openConnection();
					input = juc.getInputStream();
				} else {
					URLConnection uc;
					uc = pluginLocation.openConnection();
					// %5c
					try {
						fileName = fileName.replaceAll("%5c", "/");
						fileName = fileName.replaceAll("file://", "file:///");
						URLConnection uc2 = new URL(fileName).openConnection();
						input = uc2.getInputStream();
					} catch (IOException ioe) {
						input = uc.getInputStream();
					}
				}
			} catch (IOException ioe) {
				throw new PluginManagerException("exception.IO");
			}
			
			// directly read from the jar or zip file
		} else
			if (fileName.toLowerCase().endsWith(".jar") ||
								fileName.toLowerCase().endsWith(".zip")) {
				try {
					JarFile file = new JarFile(new File(
										new URI(pluginLocation.toString())));
					StringBundle sBundle = StringBundle.getInstance();
					ZipEntry entry = file.getEntry(sBundle.getString(
										"plugin.xml.filename"));
					
					if (entry != null) {
						// create an input stream from this entry.
						input = file.getInputStream(entry);
					} else {
						throw new PluginManagerException("exception.IO");
					}
				} catch (MalformedURLException mue) {
					throw new PluginManagerException("exception.MalformedURL");
				} catch (URISyntaxException use) {
					throw new PluginManagerException("exception.URISyntax");
				} catch (IOException ioe) {
					throw new PluginManagerException("exception.IO");
				}
			} else {
				throw new PluginManagerException("exception.unknownFileType",
									fileName);
			}
		
		PluginDescription description = null;
		
		try {
			PluginXMLParser parser = new PluginXMLParser();
			description = parser.parse(input);
		} catch (IOException ioe) {
			throw new PluginManagerException("exception.IO", ioe.getMessage());
		} finally {
			try {
				input.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		return description;
	}
	
	/**
	 * @return
	 */
	public static List<Class> getAvailableAttributes() {
		List<Class> result = new ArrayList<Class>();
		Collection plugins = DefaultPluginManager.lastInstance.getPluginEntries();
		for (Iterator it = plugins.iterator(); it.hasNext();) {
			PluginEntry e = (PluginEntry) it.next();
			Class[] at = e.getPlugin().getAttributes();
			if (at != null && at.length > 0) {
				for (int i = 0; i < at.length; i++)
					result.add(at[i]);
			}
		}
		return result;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
