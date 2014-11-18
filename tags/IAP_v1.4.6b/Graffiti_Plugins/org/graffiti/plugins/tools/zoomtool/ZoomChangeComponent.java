// ==============================================================================
//
// ZoomChangeComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ZoomChangeComponent.java,v 1.1 2011-01-31 09:03:37 klukas Exp $

package org.graffiti.plugins.tools.zoomtool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.JComboBox;

import org.graffiti.plugin.gui.AbstractGraffitiComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.plugin.view.ZoomListener;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * DOCUMENT ME!
 */
public class ZoomChangeComponent
					extends AbstractGraffitiComponent
					implements ActionListener, ViewListener, SessionListener {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** DOCUMENT ME! */
	private JComboBox combo;
	
	// /** DOCUMENT ME! */
	// private MainFrame mainframe;
	// /** DOCUMENT ME! */
	// private Object zoomValue;
	
	/** DOCUMENT ME! */
	private Session activeSession;
	
	/** DOCUMENT ME! */
	private final String[] zoomValues = new String[]
																	{
																						" 10%", " 25%", " 50%", " 75%", "100%", "125%", "150%", "175%",
																						"200%"
																	};
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for ZoomChangeComponent.
	 * 
	 * @param prefComp
	 *           DOCUMENT ME!
	 */
	public ZoomChangeComponent(String prefComp) {
		super(prefComp);
		setLayout(new BorderLayout());
		
		combo = new JComboBox(zoomValues);
		combo.setSelectedIndex(4);
		combo.setEditable(true);
		add(combo);
		
		combo.addActionListener(this);
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see javax.swing.JComponent#getMaximumSize()
	 */
	@Override
	public Dimension getMaximumSize() {
		return combo.getPreferredSize();
	}
	
	// /**
	// * @see org.graffiti.plugin.gui.AbstractGraffitiComponent#setMainFrame(org.graffiti.editor.MainFrame)
	// */
	// public void setMainFrame(MainFrame mf)
	// {
	// super.setMainFrame(mf);
	// this.mainframe = mf;
	// }
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object selectedZoom = combo.getSelectedItem();
		
		String zoomStr;
		
		if (selectedZoom instanceof AffineTransform) {
			AffineTransform at = (AffineTransform) selectedZoom;
			zoomStr = ((int) ((at.getScaleX() + at.getScaleY()) / 2d) * 100) +
								"%";
		} else {
			zoomStr = selectedZoom.toString();
		}
		
		double zoom = 1d;
		
		if (zoomStr.indexOf("%") != -1) {
			// crop "%"
			zoom = Double.parseDouble(zoomStr.substring(0, zoomStr.length() -
								1)) / 100d;
		} else {
			zoom = Double.parseDouble(zoomStr) / 100d;
		}
		
		if (activeSession != null) {
			ZoomListener zoomView = activeSession.getActiveView();
			AffineTransform at = new AffineTransform();
			at.setToScale(zoom, zoom);
			zoomView.zoomChanged(at);
		}
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	public void sessionChanged(Session s) {
		activeSession = s;
		
		if (s != null) {
			viewChanged(s.getActiveView());
		}
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
		activeSession = s;
		viewChanged(s.getActiveView());
	}
	
	/**
	 * @see org.graffiti.plugin.view.ViewListener#viewChanged(org.graffiti.plugin.view.View)
	 */
	public void viewChanged(View newView) {
		Object newZoom = newView.getZoom();
		String zoomStr;
		
		if (newZoom instanceof AffineTransform) {
			AffineTransform at = (AffineTransform) newZoom;
			zoomStr = (int) (((at.getScaleX() + at.getScaleY()) / 2d) * 100) +
								"%";
		} else {
			zoomStr = newZoom.toString();
		}
		
		combo.setSelectedItem(zoomStr);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
