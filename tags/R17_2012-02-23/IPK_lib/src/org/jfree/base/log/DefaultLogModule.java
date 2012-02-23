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
 * ---------------------
 * DefaultLogModule.java
 * ---------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultLogModule.java,v 1.1 2011-01-31 09:02:55 klukas Exp $
 * Changes
 * -------
 * 11-Jul-2003 : Initial version
 * 07-Jun-2004 : Added JCommon header (DG);
 */

package org.jfree.base.log;

import org.jfree.base.modules.AbstractModule;
import org.jfree.base.modules.ModuleInitializeException;
import org.jfree.base.modules.SubSystem;
import org.jfree.util.Log;
import org.jfree.util.PrintStreamLogTarget;

/**
 * The module definition for the System.out-Logging. This is the
 * default log implementation and is provided to insert the logging
 * initialisation in the module loading process.
 * 
 * @author Thomas Morgner
 */
public class DefaultLogModule extends AbstractModule {
	/**
	 * DefaultConstructor. Loads the module specification.
	 * 
	 * @throws ModuleInitializeException
	 *            if an error occured.
	 */
	public DefaultLogModule() throws ModuleInitializeException {
		// final InputStream in = getClass().getResourceAsStream
		// ("logmodule.properties");
		// if (in == null)
		// {
		// throw new ModuleInitializeException
		// ("File 'logmodule.properties' not found in JFreeReport package.");
		// }
		// loadModuleInfo(in);
		loadModuleInfo();
	}

	/**
	 * Initalizes the module. This method initializes the logging system,
	 * if the System.out logtarget is selected.
	 * 
	 * @param subSystem
	 *           the sub-system.
	 * @throws ModuleInitializeException
	 *            if an error occured.
	 */
	public void initialize(final SubSystem subSystem) throws ModuleInitializeException {
		if (LogConfiguration.isDisableLogging()) {
			return;
		}
		if (LogConfiguration.getLogTarget().equals
							(PrintStreamLogTarget.class.getName())) {
			Log.getInstance().addTarget(new PrintStreamLogTarget());
			// Log.getInstance().
			Log.info("System.out log target started ... previous log messages could have been ignored.");
		}
	}
}
