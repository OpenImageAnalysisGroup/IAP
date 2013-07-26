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
 * TextMeasurer.java
 * -----------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TextMeasurer.java,v 1.1 2011-01-31 09:03:03 klukas Exp $
 * Changes
 * -------
 * 07-Jan-2004 : Version 1 (DG);
 */

package org.jfree.text;

/**
 * An object that can measure text.
 */
public interface TextMeasurer {

	/**
	 * Calculates the width of a <code>String</code> in the current <code>Graphics</code> context.
	 * 
	 * @param text
	 *           the text.
	 * @param start
	 *           the start position of the substring to be measured.
	 * @param end
	 *           the position of the last character to be measured.
	 * @return the width of the string in Java2D units.
	 */
	public float getStringWidth(String text, int start, int end);

}
