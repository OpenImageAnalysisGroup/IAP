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
 * -----------------
 * AbstractBoot.java
 * -----------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: AbstractBoot.java,v 1.1 2011-01-31 09:03:06 klukas Exp $
 * Changes
 * -------
 * 07-Jun-2004 : Added source headers (DG);
 */

package org.jfree.base;

import org.jfree.base.config.HierarchicalConfiguration;
import org.jfree.base.config.ModifiableConfiguration;
import org.jfree.base.modules.PackageManager;
import org.jfree.base.modules.SubSystem;
import org.jfree.util.Log;

/**
 * Boot class.
 */
public abstract class AbstractBoot implements SubSystem {

	/** A singleton instance of the package manager. */
	private PackageManager singleton;

	/** Global configuration. */
	private HierarchicalConfiguration globalConfig;

	/** A flag indicating whether the booting is currenly in progress. */
	private boolean bootInProgress;

	/** A flag indicating whether the booting is complete. */
	private boolean bootDone;

	/**
	 * Returns the singleton instance of the package manager.
	 * 
	 * @return The package manager.
	 */
	public PackageManager getPackageManager() {
		if (this.singleton == null) {
			this.singleton = PackageManager.createInstance(this);
		}
		return this.singleton;
	}

	/**
	 * Returns the global configuration.
	 * 
	 * @return The global configuration.
	 */
	public ModifiableConfiguration getGlobalConfig() {
		if (this.globalConfig == null) {
			this.globalConfig = loadConfiguration();
			start();
		}
		return this.globalConfig;
	}

	/**
	 * Checks, whether the booting is in progress.
	 * 
	 * @return true, if the booting is in progress, false otherwise.
	 */
	public boolean isBootInProgress() {
		return this.bootInProgress;
	}

	/**
	 * Checks, whether the booting is complete.
	 * 
	 * @return true, if the booting is complete, false otherwise.
	 */
	public boolean isBootDone() {
		return this.bootDone;
	}

	/**
	 * Loads the configuration.
	 * 
	 * @return The configuration.
	 */
	protected abstract HierarchicalConfiguration loadConfiguration();

	/**
	 * Starts the boot process.
	 */
	public final void start() {

		if (isBootInProgress() || isBootDone()) {
			return;
		}
		this.bootInProgress = true;

		// boot dependent libraries ...
		final BootableProjectInfo info = getProjectInfo();
		if (info != null) {
			Log.info(info.getName() + " " + info.getVersion());
			final BootableProjectInfo[] childs = info.getDependencies();
			for (int i = 0; i < childs.length; i++) {
				final AbstractBoot boot = loadBooter(childs[i].getBootClass());
				if (boot != null) {
					boot.start();
				}
			}
		}
		performBoot();

		this.bootInProgress = false;
		this.bootDone = true;
	}

	/**
	 * Performs the boot.
	 */
	protected abstract void performBoot();

	/**
	 * Returns the project info.
	 * 
	 * @return The project info.
	 */
	protected abstract BootableProjectInfo getProjectInfo();

	/**
	 * Loads the booter.
	 * 
	 * @param classname
	 *           the class name.
	 * @return The boot class.
	 */
	protected AbstractBoot loadBooter(final String classname) {
		if (classname == null) {
			return null;
		}
		try {
			final Class c = this.getClass().getClassLoader().loadClass(classname);
			return (AbstractBoot) c.newInstance();
		} catch (Exception e) {
			Log.info("Unable to boot dependent class: " + classname);
			return null;
		}
	}

}
