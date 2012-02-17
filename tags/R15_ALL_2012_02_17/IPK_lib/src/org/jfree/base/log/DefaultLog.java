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
 * ---------------
 * DefaultLog.java
 * ---------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: DefaultLog.java,v 1.1 2011-01-31 09:02:55 klukas Exp $
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 */

package org.jfree.base.log;

import org.jfree.util.Log;
import org.jfree.util.LogTarget;
import org.jfree.util.PrintStreamLogTarget;

/**
 * A default log.
 */
public class DefaultLog extends Log {

	/** The default log target. */
	private static final PrintStreamLogTarget DEFAULT_LOG_TARGET =
						new PrintStreamLogTarget();

	/** The JFreeReport log instance. */
	private static final DefaultLog defaultLogInstance;

	/**
	 * Creates a new log.
	 */
	public DefaultLog() {
		// nothing required
	}

	static {
		defaultLogInstance = new DefaultLog();
		Log.defineLog(defaultLogInstance);
		defaultLogInstance.addTarget(DEFAULT_LOG_TARGET);
		try {
			// check the system property. This is the developers backdoor to activate
			// debug output as soon as possible.
			if (Boolean.getBoolean("org.jfree.DebugDefault")) {
				defaultLogInstance.setDebuglevel(LogTarget.DEBUG);
			} else {
				defaultLogInstance.setDebuglevel(LogTarget.WARN);
			}
		} catch (SecurityException se) {
			defaultLogInstance.setDebuglevel(LogTarget.WARN);
		}
	}

	/**
	 * Initializes the log system after the log module was loaded and a log target
	 * was defined. This is the second step of the log initialisation.
	 */
	public void init() {
		removeTarget(DEFAULT_LOG_TARGET);
		final String logLevel = LogConfiguration.getLogLevel();
		if (logLevel.equalsIgnoreCase("error")) {
			setDebuglevel(LogTarget.ERROR);
		} else
			if (logLevel.equalsIgnoreCase("warn")) {
				setDebuglevel(LogTarget.WARN);
			} else
				if (logLevel.equalsIgnoreCase("info")) {
					setDebuglevel(LogTarget.INFO);
				} else
					if (logLevel.equalsIgnoreCase("debug")) {
						setDebuglevel(LogTarget.DEBUG);
					}
	}

	/**
	 * Returns the default log.
	 * 
	 * @return The default log.
	 */
	public static DefaultLog getDefaultLog() {
		return defaultLogInstance;
	}

}
