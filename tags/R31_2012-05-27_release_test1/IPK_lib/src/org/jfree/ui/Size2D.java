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
 * -----------
 * Size2D.java
 * -----------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: Size2D.java,v 1.1 2011-01-31 09:02:23 klukas Exp $
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.ui;

/**
 * A simple class for representing the dimensions of an object. I would use Dimension2D, but refer
 * to Bug ID 4189446 on the Java Developer Connection for why not (last checked 20 July 2000, maybe
 * it's been fixed now).
 */
public class Size2D {

	/** The width. */
	public double width;

	/** The height. */
	public double height;

	/**
	 * Standard constructor - builds a Size2D with the specified width and height.
	 * 
	 * @param width
	 *           the width.
	 * @param height
	 *           the height.
	 */
	public Size2D(final double width, final double height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Returns the height.
	 * 
	 * @return the height.
	 */
	public double getHeight() {
		return this.height;
	}

	/**
	 * Returns the width.
	 * 
	 * @return the width.
	 */
	public double getWidth() {
		return this.width;
	}

}
