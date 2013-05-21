/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 1, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.webstart;

import javax.swing.JViewport;

import de.ipk.ag_ba.gui.IAPnavigationPanel;

/**
 * @author klukas
 */
public class MyViewPort extends JViewport {
	private static final long serialVersionUID = 1L;
	private final IAPnavigationPanel panel;
	
	public MyViewPort(IAPnavigationPanel panel) {
		super();
		this.panel = panel;
	}
	
}
