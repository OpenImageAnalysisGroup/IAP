// ==============================================================================
//
// MaximizeLayout.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: MaximizeLayout.java,v 1.1 2011-01-31 09:04:26 klukas Exp $

package org.graffiti.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JInternalFrame;

/**
 * Layour wrapper that modifies the behaviour of a {@link java.awt.LayoutManager} for maximized {@link javax.swing.JInternalFrame}s.
 * If the frame is maximized, its title bar is hidden. This is intended to be
 * used together with {@link org.graffiti.util.MaximizeManager}.
 * 
 * @author Michael Forster
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:04:26 $
 * @see org.graffiti.util.MaximizeManager
 * @see org.graffiti.util.MaximizeFrame
 */
public class MaximizeLayout
					implements LayoutManager {
	// ~ Instance fields ========================================================
	
	/** Original layout of the frame. Handles most of the method calls. */
	private LayoutManager originalLayout;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Wrap an existing layout and overide its behaviour for maximized frames.
	 * 
	 * @param originalLayout
	 *           The wrapped layout.
	 */
	public MaximizeLayout(LayoutManager originalLayout) {
		this.originalLayout = originalLayout;
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 */
	public void addLayoutComponent(String name, Component comp) {
		originalLayout.addLayoutComponent(name, comp);
	}
	
	/*
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	public void layoutContainer(Container parent) {
		if ((parent != null) && parent instanceof JInternalFrame) {
			JInternalFrame frame = (JInternalFrame) parent;
			
			if (frame.isMaximum()) {
				frame.getRootPane().setBounds(0, 0, frame.getWidth(),
									frame.getHeight());
				
				return;
			}
		}
		
		originalLayout.layoutContainer(parent);
	}
	
	/*
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	public Dimension minimumLayoutSize(Container parent) {
		return originalLayout.minimumLayoutSize(parent);
	}
	
	/*
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	public Dimension preferredLayoutSize(Container parent) {
		return originalLayout.preferredLayoutSize(parent);
	}
	
	/*
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	public void removeLayoutComponent(Component comp) {
		originalLayout.removeLayoutComponent(comp);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
