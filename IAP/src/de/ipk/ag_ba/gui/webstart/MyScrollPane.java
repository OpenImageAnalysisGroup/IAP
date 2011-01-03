/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 1, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.webstart;

import java.awt.Dimension;

import javax.swing.JScrollPane;

import de.ipk.ag_ba.gui.MyNavigationPanel;

/**
 * @author klukas
 */
public class MyScrollPane extends JScrollPane {
	private static final long serialVersionUID = 1L;
	private final MyNavigationPanel panel;
	
	public MyScrollPane(MyNavigationPanel panel, boolean border) {
		super(panel);
		this.panel = panel;
		if (!border)
			setBorder(null);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension res = super.getPreferredSize();
		res.setSize(res.width, panel.getMaxYY() + 30);
		return res;
	}
	
}
