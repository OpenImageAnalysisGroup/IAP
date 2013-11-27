/*
 * ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jfreechart/index.html
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
 * DescriptionPanel.java
 * ---------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DescriptionPanel.java,v 1.1 2011-01-31 09:01:42 klukas Exp $
 * Changes
 * -------
 * 10-Dec-2001 : Version 1 (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A panel containing a chart description.
 */
public class DescriptionPanel extends JPanel {

	/** The preferred size for the panel. */
	public static final Dimension PREFERRED_SIZE = new Dimension(150, 50);

	/**
	 * Creates a new panel.
	 * 
	 * @param text
	 *           the component containing the text.
	 */
	public DescriptionPanel(final JTextArea text) {

		setLayout(new BorderLayout());
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		add(new JScrollPane(text,
										JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

	}

	/**
	 * Returns the preferred size.
	 * 
	 * @return the preferred size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

}
