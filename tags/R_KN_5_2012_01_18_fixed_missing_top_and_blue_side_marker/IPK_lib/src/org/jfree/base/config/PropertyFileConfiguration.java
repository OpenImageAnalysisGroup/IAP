/*
 * ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jcommon/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ------------------------------------
 * PropertyFileReportConfiguration.java
 * ------------------------------------
 * (C)opyright 2003, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: PropertyFileConfiguration.java,v 1.1 2011-01-31 09:02:08 klukas Exp $
 * Changes
 * -------
 * 14-Jan-2003 : Initial version
 */
package org.jfree.base.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jfree.util.Log;

/**
 * A report configuration that reads its values from an arbitary property file.
 * 
 * @author Thomas Morgner
 */
public class PropertyFileConfiguration extends HierarchicalConfiguration {
	/**
	 * Loads the properties stored in the given file. This method does nothing if
	 * the file does not exist or is unreadable. Appends the contents of the loaded
	 * properties to the already stored contents.
	 * 
	 * @param fileName
	 *           the file name of the stored properties.
	 */
	public void load(final String fileName) {
		final InputStream in = this.getClass().getResourceAsStream(fileName);
		if (in != null) {
			load(in);
		} else {
			Log.debug("Report configuration file not found: " + fileName);
		}

	}

	/**
	 * Loads the properties stored in the given file. This method does nothing if
	 * the file does not exist or is unreadable. Appends the contents of the loaded
	 * properties to the already stored contents.
	 * 
	 * @param in
	 *           the input stream used to read the properties.
	 */
	public void load(final InputStream in) {
		if (in == null) {
			throw new NullPointerException();
		}

		try {
			final BufferedInputStream bin = new BufferedInputStream(in);
			final Properties p = new Properties();
			p.load(bin);
			this.getConfiguration().putAll(p);
			bin.close();
		} catch (IOException ioe) {
			Log.warn("Unable to read configuration", ioe);
		}

	}

	/**
	 * Default constructor.
	 */
	public PropertyFileConfiguration() {
		// nothing required
	}
}
