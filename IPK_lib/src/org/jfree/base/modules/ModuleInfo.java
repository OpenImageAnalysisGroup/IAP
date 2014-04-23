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
 * ModuleInfo.java
 * ---------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: ModuleInfo.java,v 1.1 2011-01-31 09:02:06 klukas Exp $
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 */

package org.jfree.base.modules;

/**
 * Module info.
 */
public interface ModuleInfo {

	/**
	 * Returns the module class of the desired base module.
	 * 
	 * @return The module class.
	 */
	public String getModuleClass();

	/**
	 * Returns the major version of the base module. The string should
	 * contain a compareable character sequence so that higher versions
	 * of the module are considered greater than lower versions.
	 * 
	 * @return The major version of the module.
	 */
	public String getMajorVersion();

	/**
	 * Returns the minor version of the base module. The string should
	 * contain a compareable character sequence so that higher versions
	 * of the module are considered greater than lower versions.
	 * 
	 * @return The minor version of the module.
	 */
	public String getMinorVersion();

	/**
	 * Returns the patchlevel version of the base module. The patch level
	 * should be used to mark bugfixes. The string should
	 * contain a compareable character sequence so that higher versions
	 * of the module are considered greater than lower versions.
	 * 
	 * @return The patch level version of the module.
	 */
	public String getPatchLevel();

}
