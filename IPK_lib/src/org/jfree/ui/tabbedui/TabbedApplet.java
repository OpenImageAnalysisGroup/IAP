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
 * TabbedApplet.java
 * -----------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: TabbedApplet.java,v 1.1 2011-01-31 09:02:57 klukas Exp $
 * Changes
 * -------------------------
 * 16-Feb-2004 : Initial version
 * 07-Jun-2004 : Added standard header (DG);
 */

package org.jfree.ui.tabbedui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JApplet;
import javax.swing.JPanel;

/**
 * A tabbed GUI.
 */
public class TabbedApplet extends JApplet {

	private AbstractTabbedUI tabbedUI;

	private class MenuBarChangeListener implements PropertyChangeListener {
		/**
		 * Creates a new change listener.
		 */
		public MenuBarChangeListener() {
		}

		/**
		 * This method gets called when a bound property is changed.
		 * 
		 * @param evt
		 *           A PropertyChangeEvent object describing the event source
		 *           and the property that has changed.
		 */

		public void propertyChange(final PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(AbstractTabbedUI.JMENUBAR_PROPERTY)) {
				setJMenuBar(TabbedApplet.this.tabbedUI.getJMenuBar());
			}
		}
	}

	/**
	 * Default constructor.
	 */
	public TabbedApplet() {
	}

	/**
	 * Initialises the applet.
	 * 
	 * @param tabbedUI
	 *           ???.
	 */
	public void init(final AbstractTabbedUI tabbedUI) {

		this.tabbedUI = tabbedUI;
		this.tabbedUI.addPropertyChangeListener
							(AbstractTabbedUI.JMENUBAR_PROPERTY, new MenuBarChangeListener());

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(tabbedUI, BorderLayout.CENTER);
		setContentPane(panel);
		setJMenuBar(tabbedUI.getJMenuBar());
	}

}
