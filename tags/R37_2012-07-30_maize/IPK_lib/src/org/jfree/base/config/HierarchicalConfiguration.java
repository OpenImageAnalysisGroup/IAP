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
 * ------------------------------
 * HierarchicalConfiguration.java
 * ------------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: HierarchicalConfiguration.java,v 1.1 2011-01-31 09:02:08 klukas Exp $
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 * 29-Jul-2004 : Replaced 'enum' variable name (reserved word in JDK 1.5) (DG);
 */

package org.jfree.base.config;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

/**
 * A hierarchical configuration.
 */
public class HierarchicalConfiguration implements ModifiableConfiguration {

	private Properties configuration;

	/** The parent configuration (null if this is the root configuration). */
	private transient ModifiableConfiguration parentConfiguration;

	/**
	 * Creates a new configuration.
	 */
	public HierarchicalConfiguration() {
		this.configuration = new Properties();
	}

	/**
	 * Creates a new configuration.
	 * 
	 * @param parentConfiguration
	 *           the parent configuration.
	 */
	public HierarchicalConfiguration(final ModifiableConfiguration parentConfiguration) {
		this();
		this.parentConfiguration = parentConfiguration;
	}

	/**
	 * Returns the configuration property with the specified key.
	 * 
	 * @param key
	 *           the property key.
	 * @return the property value.
	 */
	public String getConfigProperty(final String key) {
		return getConfigProperty(key, null);
	}

	/**
	 * Returns the configuration property with the specified key
	 * (or the specified default value if there is no such property).
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
		String value = this.configuration.getProperty(key);
		if (value == null) {
			if (isRootConfig()) {
				value = defaultValue;
			} else {
				value = this.parentConfiguration.getConfigProperty(key, defaultValue);
			}
		}
		return value;
	}

	/**
	 * Sets a configuration property.
	 * 
	 * @param key
	 *           the property key.
	 * @param value
	 *           the property value.
	 */
	public void setConfigProperty(final String key, final String value) {
		if (key == null) {
			throw new NullPointerException();
		}

		if (value == null) {
			this.configuration.remove(key);
		} else {
			this.configuration.setProperty(key, value);
		}
	}

	/**
	 * Returns true if this object has no parent.
	 * 
	 * @return true, if this report is the root configuration, false otherwise.
	 */
	private boolean isRootConfig() {
		return this.parentConfiguration == null;
	}

	/**
	 * Checks, whether the given key is localy defined in this instance or
	 * whether the key's value is inherited.
	 * 
	 * @param key
	 *           the key that should be checked.
	 * @return true, if the key is defined locally, false otherwise.
	 */
	public boolean isLocallyDefined(final String key) {
		return this.configuration.containsKey(key);
	}

	/**
	 * Returns the collection of properties for the configuration.
	 * 
	 * @return the properties.
	 */
	protected Properties getConfiguration() {
		return this.configuration;
	}

	/**
	 * The new configuartion will be inserted into the list of report configuration,
	 * so that this configuration has the given report configuration instance as parent.
	 * 
	 * @param config
	 *           the new report configuration.
	 */
	public void insertConfiguration(final HierarchicalConfiguration config) {
		config.setParentConfig(getParentConfig());
		setParentConfig(config);
	}

	/**
	 * Set the parent configuration. The parent configuration is queried, if the
	 * requested configuration values was not found in this report configuration.
	 * 
	 * @param config
	 *           the parent configuration.
	 */
	protected void setParentConfig(final ModifiableConfiguration config) {
		if (this.parentConfiguration == this) {
			throw new IllegalArgumentException("Cannot add myself as parent configuration.");
		}
		this.parentConfiguration = config;
	}

	/**
	 * Returns the parent configuration. The parent configuration is queried, if the
	 * requested configuration values was not found in this report configuration.
	 * 
	 * @return the parent configuration.
	 */
	protected ModifiableConfiguration getParentConfig() {
		return this.parentConfiguration;
	}

	/**
	 * Returns all defined configuration properties for the report. The enumeration
	 * contains all keys of the changed properties, properties set from files or
	 * the system properties are not included.
	 * 
	 * @return all defined configuration properties for the report.
	 */
	public Enumeration getConfigProperties() {
		return this.configuration.keys();
	}

	/**
	 * Searches all property keys that start with a given prefix.
	 * 
	 * @param prefix
	 *           the prefix that all selected property keys should share
	 * @return the properties as iterator.
	 */
	public Iterator findPropertyKeys(final String prefix) {
		final TreeSet keys = new TreeSet();
		collectPropertyKeys(prefix, this, keys);
		return Collections.unmodifiableSet(keys).iterator();
	}

	/**
	 * Collects property keys from this and all parent report configurations, which
	 * start with the given prefix.
	 * 
	 * @param prefix
	 *           the prefix, that selects the property keys.
	 * @param config
	 *           the currently processed report configuration.
	 * @param collector
	 *           the target list, that should receive all valid keys.
	 */
	private void collectPropertyKeys(final String prefix, final ModifiableConfiguration config,
													final TreeSet collector) {
		final Enumeration enum1 = config.getConfigProperties();
		while (enum1.hasMoreElements()) {
			final String key = (String) enum1.nextElement();
			if (key.startsWith(prefix)) {
				if (collector.contains(key) == false) {
					collector.add(key);
				}
			}
		}

		if (config instanceof HierarchicalConfiguration) {
			final HierarchicalConfiguration hconfig = (HierarchicalConfiguration) config;
			if (hconfig.parentConfiguration != null) {
				collectPropertyKeys(prefix, hconfig.parentConfiguration, collector);
			}
		}
	}

}
