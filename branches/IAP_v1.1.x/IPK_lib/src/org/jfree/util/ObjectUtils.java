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
 * ----------------
 * ObjectUtils.java
 * ----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ObjectUtils.java,v 1.1 2011-01-31 09:01:41 klukas Exp $
 * Changes
 * -------
 * 25-Mar-2003 : Version 1 (DG);
 * 15-Sep-2003 : Fixed bug in clone(List) method (DG);
 */

package org.jfree.util;

import java.util.Iterator;
import java.util.List;

/**
 * A collection of useful static utility methods.
 */
public abstract class ObjectUtils {

	/**
	 * Returns <code>true</code> if the two objects are equal OR both <code>null</code>.
	 * 
	 * @param o1
	 *           object 1 (<code>null</code> permitted).
	 * @param o2
	 *           object 2 (<code>null</code> permitted).
	 * @return <code>true</code> or <code>false</code>.
	 */
	public static boolean equal(Object o1, Object o2) {

		if (o1 != null) {
			return o1.equals(o2);
		} else {
			return (o2 == null);
		}

	}

	/**
	 * Returns a hash code for an object, or zero if the object is <code>null</code>.
	 * 
	 * @param object
	 *           the object (<code>null</code> permitted).
	 * @return The object's hash code (or zero if the object is <code>null</code>).
	 */
	public static int hashCode(final Object object) {
		int result = 0;
		if (object != null) {
			result = object.hashCode();
		}
		return result;
	}

	/**
	 * Returns a clone of the object, if the object implements the {@link PublicCloneable} interface, otherwise the original object reference is returned.
	 * 
	 * @param object
	 *           the object to clone (<code>null</code> permitted).
	 * @return A clone or the original object reference.
	 * @throws CloneNotSupportedException
	 *            if the object cannot be cloned.
	 */
	public static Object clone(final Object object) throws CloneNotSupportedException {
		Object result = object;
		if (object != null) {
			if (object instanceof PublicCloneable) {
				final PublicCloneable pc = (PublicCloneable) object;
				result = pc.clone();
			}
		}
		return result;
	}

	/**
	 * Returns a clone of the list. The objects within the list are cloned IF they implement
	 * the {@link PublicCloneable} interface, otherwise the reference to the original object is
	 * retained in the cloned list.
	 * 
	 * @param list
	 *           the list.
	 * @return A clone of the list.
	 * @throws CloneNotSupportedException
	 *            if the list could not be cloned.
	 */
	public static List clone(final List list) throws CloneNotSupportedException {
		List result = null;
		if (list != null) {
			try {
				final List clone = (List) list.getClass().newInstance();
				final Iterator iterator = list.iterator();
				while (iterator.hasNext()) {
					clone.add(ObjectUtils.clone(iterator.next()));
				}
				result = clone;
			} catch (Exception e) {
				throw new CloneNotSupportedException("ObjectUtils.clone(List) - Exception.");
			}
		}
		return result;
	}

	/**
	 * Returns <code>true</code> if the two objects are equal OR both <code>null</code>.
	 * 
	 * @param o1
	 *           object 1.
	 * @param o2
	 *           object 2.
	 * @return <code>true</code> or <code>false</code>.
	 * @deprecated Use ObjectUtils.equal(...).
	 */
	public static boolean equalOrBothNull(final Object o1, final Object o2) {

		if (o1 != null) {
			return o1.equals(o2);
		} else {
			return (o2 == null);
		}

	}

}
