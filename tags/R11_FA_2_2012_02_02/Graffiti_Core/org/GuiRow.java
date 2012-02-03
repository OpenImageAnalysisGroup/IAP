/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 13.12.2004 by Christian Klukas
 */
package org;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class GuiRow {
	public JComponent left, right;
	public boolean span;
	
	public GuiRow(JComponent left, JComponent right) {
		if (left == null)
			left = new JLabel("");
		if (right == null)
			right = new JLabel("");
		this.left = left;
		this.right = right;
	}
}
