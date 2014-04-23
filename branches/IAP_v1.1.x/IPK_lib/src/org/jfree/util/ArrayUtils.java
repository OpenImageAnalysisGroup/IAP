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
 * ArrayUtils.java
 * ---------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ArrayUtils.java,v 1.1 2011-01-31 09:01:40 klukas Exp $
 * Changes
 * -------
 * 21-Aug-2003 : Version 1 (DG);
 */

package org.jfree.util;

import java.util.Arrays;

/**
 * Utility methods for working with arrays.
 */
public abstract class ArrayUtils {

	/**
	 * Clones a two dimensional array of floats.
	 * 
	 * @param array
	 *           the array.
	 * @return A clone of the array.
	 */
	public static float[][] clone(float[][] array) {

		if (array == null) {
			return null;
		}
		float[][] result = new float[array.length][];
		System.arraycopy(array, 0, result, 0, array.length);

		for (int i = 0; i < array.length; i++) {
			float[] child = array[i];
			float[] copychild = new float[child.length];
			System.arraycopy(child, 0, copychild, 0, child.length);
			result[i] = copychild;
		}

		return result;

	}

	/**
	 * Tests two float arrays for equality.
	 * 
	 * @param array1
	 *           the first array.
	 * @param array2
	 *           the second arrray.
	 * @return A boolean.
	 */
	public static boolean equal(float[][] array1, float[][] array2) {
		if (array1 == null) {
			return (array2 == null);
		}

		if (array2 == null) {
			return false;
		}

		if (array1.length != array2.length) {
			return false;
		}

		for (int i = 0; i < array1.length; i++) {
			if (!Arrays.equals(array1[i], array2[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if any two items in the array are equal to one another.
	 * Any <code>null</code> values in the array are ignored.
	 * 
	 * @param array
	 *           the array to check.
	 * @return A boolean.
	 */
	public static boolean hasDuplicateItems(Object[] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < i; j++) {
				Object o1 = array[i];
				Object o2 = array[j];
				if (o1 != null & o2 != null) {
					if (o1.equals(o2)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
