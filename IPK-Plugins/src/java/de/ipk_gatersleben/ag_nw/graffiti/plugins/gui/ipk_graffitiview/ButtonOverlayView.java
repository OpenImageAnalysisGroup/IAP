/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
// ==============================================================================
//
// GraffitiView.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ButtonOverlayView.java,v 1.1 2011-01-31 09:00:42 klukas Exp $

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import javax.swing.JComponent;

/**
 * An implementation of <code>org.graffiti.plugin.view.View2D</code>, that
 * displays a graph. Since it also shows changes in the graph it listens for
 * changes in the graph, attributes, nodes and edges.
 * 
 * @see javax.swing.JPanel
 * @see org.graffiti.plugin.view.View2D
 */
public class ButtonOverlayView
					extends IPKGraffitiView {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public Object getViewToolbarComponentTop() {
		return 100d;
	}
	
	@Override
	public Object getViewToolbarComponentBottom() {
		return 100d;
	}
	
	@Override
	public Object getViewToolbarComponentLeft() {
		return 100d;
	}
	
	@Override
	public Object getViewToolbarComponentRight() {
		return 100d;
	}
	
	@Override
	public JComponent getViewToolbarComponentBackground() {
		return new Star();
	}
	
	@Override
	public boolean putInScrollPane() {
		return true;
	}
	
	@Override
	public JComponent getViewComponent() {
		JComponent jc = super.getViewComponent();
		return jc;
	}
	
	@Override
	public String getViewName() {
		return "Button Overlay (demo)";
	}
}
