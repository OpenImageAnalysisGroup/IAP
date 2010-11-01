/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Nov 1, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.webstart;

import javax.swing.JScrollPane;

import de.ipk.ag_ba.gui.MyNavigationPanel;

/**
 * @author klukas
 * 
 */
public class MyScrollPane extends JScrollPane {
	private static final long serialVersionUID = 1L;

	public MyScrollPane(MyNavigationPanel actionPanel, boolean border) {
		super(actionPanel);
		if (!border)
			setBorder(null);
	}

}
