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
 * PaintUtils.java
 * ---------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: PaintUtils.java,v 1.1 2011-01-31 09:01:41 klukas Exp $
 * Changes
 * -------
 * 13-Nov-2003 : Version 1 (DG);
 */

package org.jfree.util;

import java.awt.GradientPaint;
import java.awt.Paint;

/**
 * Utility code that relates to <code>Paint</code> objects.
 */
public abstract class PaintUtils {

	/**
	 * Returns <code>true</code> if the two <code>Paint</code> objects are equal
	 * OR both <code>null</code>.
	 * 
	 * @param p1
	 *           paint 1.
	 * @param p2
	 *           paint 2.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public static boolean equal(final Paint p1, final Paint p2) {

		boolean result = false;
		if (p1 != null) {
			if (p2 != null) {
				if (p1 instanceof GradientPaint && p2 instanceof GradientPaint) {
					final GradientPaint gp1 = (GradientPaint) p1;
					final GradientPaint gp2 = (GradientPaint) p2;
					result = ObjectUtils.equal(gp1.getColor1(), gp2.getColor1())
										&& ObjectUtils.equal(gp1.getColor2(), gp2.getColor2())
										&& ObjectUtils.equal(gp1.getPoint1(), gp2.getPoint1())
										&& ObjectUtils.equal(gp1.getPoint2(), gp2.getPoint2())
										&& gp1.isCyclic() == gp2.isCyclic()
										&& gp1.getTransparency() == gp1.getTransparency();
				} else {
					result = p1.equals(p2);
				}
			}
		} else {
			result = (p2 == null);
		}
		return result;

	}

}
