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
 * BooleanUtils.java
 * -----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: BooleanUtils.java,v 1.1 2011-01-31 09:01:42 klukas Exp $
 * Changes
 * -------
 * 23-Oct-2003 : Version 1 (DG);
 */

package org.jfree.util;

/**
 * Utility methods for working with <code>Boolean</code> objects.
 */
public abstract class BooleanUtils {

	/**
	 * Returns the object equivalent of the boolean primitive.
	 * <p>
	 * A similar method is provided by the Boolean class in JDK 1.4, but you can use this one to remain compatible with earlier JDKs.
	 * 
	 * @param b
	 *           the boolean value.
	 * @return <code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>.
	 */
	public static Boolean valueOf(final boolean b) {
		return (b ? Boolean.TRUE : Boolean.FALSE);
	}

}
