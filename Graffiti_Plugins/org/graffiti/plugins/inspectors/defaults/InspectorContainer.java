// ==============================================================================
//
// InspectorContainer.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: InspectorContainer.java,v 1.1 2011-01-31 09:03:30 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;

import org.graffiti.plugin.gui.GraffitiContainer;
import org.graffiti.plugin.inspector.InspectorTab;

/**
 * Represents the central gui component of the inspector plugin.
 * 
 * @version $Revision: 1.1 $
 */
public class InspectorContainer
					extends JTabbedPane
					implements GraffitiContainer {
	// ~ Instance fields ========================================================
	
	/** The tabbed pane for the edge-, node- and graph-tab. */
	
	// private JTabbedPane tabbedPane;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The list of the inspector's tabs. */
	private List<InspectorTab> tabs;
	
	/** The id of this GraffitiContainer. */
	private String id = "inspector";
	
	/** DOCUMENT ME! */
	private String name = "Inspector";
	
	/** The id of the component this component wants to be added to. */
	private String preferredComponent = "pluginPanel";
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new InspectorContainer object.
	 */
	public InspectorContainer() {
		tabs = new LinkedList<InspectorTab>();
		this.revalidate();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiContainer#getId()
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return preferredComponent;
	}
	
	public synchronized List<InspectorTab> getTabs() {
		return tabs;
	}
	
	public synchronized String getTitle() {
		return name;
	}
	
	/**
	 * Adds a tab to the inspector.
	 * 
	 * @param tab
	 *           the tab to add to the inspector.
	 */
	public synchronized void addTab(InspectorTab tab, ImageIcon icon) {
		tabs.add(tab);
		
		addTab(tab.getTitle(), icon, tab);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param g
	 *           DOCUMENT ME!
	 */
	@Override
	public synchronized void paintComponent(java.awt.Graphics g) {
		super.paintComponent(g);
	}
	
	/**
	 * Removes a tab from the inspector.
	 * 
	 * @param tab
	 *           the tab to remove from the inspector.
	 */
	public void removeTab(InspectorTab tab) {
		int idx = indexOfTab(tab.getTitle());
		if (idx >= 0) {
			removeTabAt(idx);
		}
		if (tabs.contains(tab))
			tabs.remove(tab);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	@Override
	public synchronized Dimension getPreferredSize() {
		return getParent().getSize();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
