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
		if (panel.getEntitySet(true).size() > 0)
			res.setSize(res.width, panel.getMaxYY() + 10);
		else
			res.setSize(res.width, panel.getMaxYY());
		return res;
	}
	
}
