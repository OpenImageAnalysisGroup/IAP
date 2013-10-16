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
 * ------------------
 * JTextObserver.java
 * ------------------
 * (C) Copyright 2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: JTextObserver.java,v 1.1 2011-01-31 09:02:25 klukas Exp $
 * Changes
 * -------
 * 07-Jun-2004 : Added JCommon header (DG);
 */

package org.jfree.ui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.text.JTextComponent;

/**
 * An observer that selects all the text when a field gains the focus.
 */
public final class JTextObserver implements FocusListener {

	private static JTextObserver singleton;

	/**
	 * Creates a new instance.
	 */
	private JTextObserver() {
		// nothing required
	}

	/**
	 * Returns the single instance.
	 * 
	 * @return The single instance.
	 */
	public static JTextObserver getInstance() {
		if (singleton == null) {
			singleton = new JTextObserver();
		}
		return singleton;
	}

	/**
	 * Selects all the text when a field gains the focus.
	 * 
	 * @param e
	 *           the focus event.
	 */
	public void focusGained(FocusEvent e) {
		if (e.getSource() instanceof JTextComponent) {
			JTextComponent tex = (JTextComponent) e.getSource();
			tex.selectAll();
		}
	}

	/**
	 * Deselects the text when a field loses the focus.
	 * 
	 * @param e
	 *           the event.
	 */
	public void focusLost(FocusEvent e) {
		if (e.getSource() instanceof JTextComponent) {
			JTextComponent tex = (JTextComponent) e.getSource();
			tex.select(0, 0);
		}
	}

	/**
	 * Adds this instance as a listener for the specified text component.
	 * 
	 * @param t
	 *           the text component.
	 */
	public static void addTextComponent(JTextComponent t) {
		if (singleton == null) {
			singleton = new JTextObserver();
		}
		t.addFocusListener(singleton);
	}

	/**
	 * Removes this instance as a listener for the specified text component.
	 * 
	 * @param t
	 *           the text component.
	 */
	public static void removeTextComponent(JTextComponent t) {
		if (singleton == null) {
			singleton = new JTextObserver();
		}
		t.removeFocusListener(singleton);
	}

}
