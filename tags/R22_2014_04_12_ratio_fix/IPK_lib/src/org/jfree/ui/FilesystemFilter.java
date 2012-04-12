/*
 * Copyright (c) 1998, 1999 by Free Software Foundation, Inc.
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by the Free Software Foundation, version 2. (see COPYING.LIB)
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307 USA
 */
package org.jfree.ui;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

/**
 * A filesystem filter.
 */
public class FilesystemFilter extends FileFilter implements FilenameFilter {

	private String fileext;

	private String descr;

	private boolean accDirs;

	/**
	 * Creates a new filter.
	 * 
	 * @param fileext
	 *           the file extension.
	 * @param descr
	 *           the description.
	 */
	public FilesystemFilter(final String fileext, final String descr) {
		this(fileext, descr, true);
	}

	/**
	 * Creates a new filter.
	 * 
	 * @param fileext
	 *           the file extension.
	 * @param descr
	 *           the description.
	 * @param accDirs
	 *           accept directories?
	 */
	public FilesystemFilter(final String fileext, final String descr,
										final boolean accDirs) {
		this.fileext = fileext;
		this.descr = descr;
		this.accDirs = accDirs;
	}

	/**
	 * Returns <code>true</code> if the file is accepted, and <code>false</code> otherwise.
	 * 
	 * @param dir
	 *           the directory.
	 * @param name
	 *           the file name.
	 * @return A boolean.
	 */
	public boolean accept(final File dir, final String name) {
		final File f = new File(dir, name);
		if (f.isDirectory() && acceptsDirectories()) {
			return true;
		}

		return (name.endsWith(this.fileext));
	}

	/**
	 * Returns <code>true</code> if the specified file matches the requirements of this filter,
	 * and <code>false</code> otherwise.
	 * 
	 * @param dir
	 *           the file or directory.
	 * @return A boolean.
	 */
	public boolean accept(final File dir) {
		if (dir.isDirectory() && acceptsDirectories()) {
			return true;
		}

		return (dir.getName().endsWith(this.fileext));
	}

	/**
	 * Returns the filter description.
	 * 
	 * @return The filter description.
	 */
	public String getDescription() {
		return this.descr;
	}

	/**
	 * Sets the flag that controls whether or not the filter accepts directories.
	 * 
	 * @param b
	 *           a boolean.
	 */
	public void acceptDirectories(final boolean b) {
		this.accDirs = b;
	}

	/**
	 * Returns the flag that indicates whether or not the filter accepts directories.
	 * 
	 * @return A boolean.
	 */
	public boolean acceptsDirectories() {
		return this.accDirs;
	}

}
