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
 * BasicProjectInfo.java
 * ---------------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: BasicProjectInfo.java,v 1.1 2011-01-31 09:03:06 klukas Exp $
 * Changes
 * -------
 * 07-Jun-2004 : Added source headers (DG);
 */

package org.jfree.base;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic project info.
 */
public class BasicProjectInfo extends Library {

	/** The project copyright statement. */
	private String copyright;

	/** A list of libraries used by the project. */
	private List libraries;

	/**
	 * Default constructor.
	 */
	public BasicProjectInfo() {
		this.libraries = new ArrayList();
	}

	/**
	 * Creates a new library reference.
	 * 
	 * @param name
	 *           the name.
	 * @param version
	 *           the version.
	 * @param licence
	 *           the licence.
	 * @param info
	 *           the web address or other info.
	 */
	public BasicProjectInfo(final String name, final String version,
										final String licence, final String info) {
		this();
		setName(name);
		setVersion(version);
		setLicenceName(licence);
		setInfo(info);
	}

	/**
	 * Creates a new project info instance.
	 * 
	 * @param name
	 *           the project name.
	 * @param version
	 *           the project version.
	 * @param info
	 *           the project info (web site for example).
	 * @param copyright
	 *           the copyright statement.
	 * @param licenceName
	 *           the license name.
	 */
	public BasicProjectInfo(final String name, final String version,
										final String info, final String copyright,
										final String licenceName) {
		this(name, version, licenceName, info);
		setCopyright(copyright);
	}

	/**
	 * Returns the copyright statement.
	 * 
	 * @return The copyright statement.
	 */
	public String getCopyright() {
		return this.copyright;
	}

	/**
	 * Sets the project copyright statement.
	 * 
	 * @param copyright
	 *           the project copyright statement.
	 */
	public void setCopyright(final String copyright) {
		this.copyright = copyright;
	}

	/**
	 * Sets the project info string (for example, this could be the project URL).
	 * 
	 * @param info
	 *           the info string.
	 */
	public void setInfo(final String info) {
		super.setInfo(info);
	}

	/**
	 * Sets the license name.
	 * 
	 * @param licence
	 *           the license name.
	 */
	public void setLicenceName(final String licence) {
		super.setLicenceName(licence);
	}

	/**
	 * Sets the project name.
	 * 
	 * @param name
	 *           the project name.
	 */
	public void setName(final String name) {
		super.setName(name);
	}

	/**
	 * Sets the project version number.
	 * 
	 * @param version
	 *           the version number.
	 */
	public void setVersion(final String version) {
		super.setVersion(version);
	}

	/**
	 * Returns a list of libraries used by the project.
	 * 
	 * @return the list of libraries.
	 */
	public Library[] getLibraries() {
		return (Library[]) this.libraries.toArray(new Library[this.libraries.size()]);
	}

	/**
	 * Adds a library.
	 * 
	 * @param library
	 *           the library.
	 */
	public void addLibrary(final Library library) {
		if (library == null) {
			throw new NullPointerException();
		}
		this.libraries.add(library);
	}

}
