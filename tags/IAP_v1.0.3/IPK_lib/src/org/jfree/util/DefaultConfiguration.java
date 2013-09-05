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
 * -------------------------
 * DefaultConfiguration.java
 * -------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultConfiguration.java,v 1.1 2011-01-31 09:01:41 klukas Exp $
 * Changes
 * -------
 * 04.06.2003 : Initial version (TM);
 */

package org.jfree.util;

import java.util.Properties;

/**
 * Default configuration.
 * 
 * @author Thomas Morgner.
 */
public class DefaultConfiguration extends Properties implements Configuration {

	/**
	 * Creates an empty property list with no default values.
	 */
	public DefaultConfiguration() {
		super();
	}

	/**
	 * Returns the configuration property with the specified key.
	 * 
	 * @param key
	 *           the property key.
	 * @return the property value.
	 */
	public String getConfigProperty(final String key) {
		return getProperty(key);
	}

	/**
	 * Returns the configuration property with the specified key (or the specified default value
	 * if there is no such property).
	 * <p>
	 * If the property is not defined in this configuration, the code will lookup the property in the parent configuration.
	 * 
	 * @param key
	 *           the property key.
	 * @param defaultValue
	 *           the default value.
	 * @return the property value.
	 */
	public String getConfigProperty(final String key, final String defaultValue) {
		return getProperty(key, defaultValue);
	}
}
