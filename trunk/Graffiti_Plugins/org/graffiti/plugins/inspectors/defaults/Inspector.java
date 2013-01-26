// ==============================================================================
//
// Inspector.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Inspector.java,v 1.2 2013-01-26 18:02:16 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.editcomponent.NeedEditComponents;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.inspector.InspectorPlugin;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.inspector.SubtabHostTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * Represents the main class of the inspector plugin.
 * 
 * @version $Revision: 1.2 $
 */
public class Inspector extends EditorPluginAdapter implements InspectorPlugin,
					SessionListener, SelectionListener, NeedEditComponents, ViewListener {
	// ~ Static fields/initializers =============================================
	
	/** The default width of the inspector components. */
	public static final int DEFAULT_WIDTH = 120;
	
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private final InspectorContainer container;
	
	private final HashMap<String, InspectorTab> rememberedTabs = new HashMap<String, InspectorTab>();
	
	private final LinkedHashSet<InspectorTab> hiddenTabs = new LinkedHashSet<InspectorTab>();
	
	/** DOCUMENT ME! */
	private Session activeSession;
	
	private String oldviewname = "null1";
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new inspector instance.
	 */
	public Inspector() {
		super();
		this.container = new InspectorContainer();
		
		// the container should be made visible, if the
		// session changed. See the sessionChanged method for details (jf).
		// this.container.setVisible(false);
		this.guiComponents = new GraffitiComponent[] {
							container
		};
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.editcomponent.NeedEditComponents#setEditComponentMap(java.util.Map)
	 */
	public void setEditComponentMap(Map<?, ?> ecMap) {
		this.valueEditComponents = ecMap;
	}
	
	/**
	 * Returns the <code>InspectorContainer</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public InspectorContainer getInspectorContainer() {
		return this.container;
	}
	
	/**
	 * @see org.graffiti.plugin.GenericPlugin#isSelectionListener()
	 */
	@Override
	public boolean isSelectionListener() {
		return true;
	}
	
	/**
	 * States whether this class wants to be registered as a <code>SessionListener</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isSessionListener() {
		return true;
	}
	
	@Override
	public boolean isViewListener() {
		return true;
	}
	
	/**
	 * Returns an array containing all the <code>InspectorTab</code>s of the <code>InspectorPlugin</code>.
	 * 
	 * @return an array containing all the <code>InspectorTab</code>s of the <code>InspectorPlugin</code>.
	 */
	public synchronized InspectorTab[] getTabs() {
		return container.getTabs().toArray(new InspectorTab[0]);
	}
	
	/**
	 * Adds another <code>InspectorTab</code> to the current <code>InspectorPlugin</code>.
	 * 
	 * @param tab
	 *           the <code>InspectorTab</code> to be added to the <code>InspectorPlugin</code>.
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	public synchronized void addTab(InspectorTab tab) {
		InspectorTab[] tabs = getTabs();
		boolean found = false;
		if (tabs != null && tab != null && tab.getTitle() != null)
			for (int i = 0; i < tabs.length; i++) {
				if (tabs[i].getTitle() != null)
					if (tabs[i].getTitle().equals(tab.getTitle())) {
						found = true;
						break;
					}
			}
		if (found || tab == null || tab.getTitle() == null) {
			return;
		}
		
		EditorSession editorSession = null;
		
		try {
			editorSession = (EditorSession) activeSession;
		} catch (ClassCastException cce) {
			// No selection is made if no EditorSession is active (?)
			throw new RuntimeException("WARNING: should rarely happen " + cce);
		}
		
		tab.setEditPanelInformation(valueEditComponents, editorSession != null ?
				editorSession.getGraphElementsMap() : null);
		
		if (!container.getTabs().contains(tab)) {
			container.addTab(tab, tab.getIcon());
		}
		
		if (MainFrame.getInstance() != null && MainFrame.getInstance().getActiveSession() != null)
			viewChanged(MainFrame.getInstance().getActiveSession().getActiveView());
		else
			viewChanged(null);
	}
	
	/**
	 * Inspector relies on the edit components to be up-to-date.
	 * 
	 * @see org.graffiti.plugin.GenericPlugin#needsEditComponents()
	 */
	@Override
	public boolean needsEditComponents() {
		return true;
	}
	
	/**
	 * Is called, if something in the selection model changed.
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void selectionChanged(SelectionEvent e) {
		for (InspectorTab tab : getTabs()) {
			if (tab.isSelectionListener()) {
				SelectionListener sl = (SelectionListener) tab;
				sl.selectionChanged(e);
			}
		}
	}
	
	/**
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionListChanged(SelectionEvent e) {
		for (InspectorTab tab : getTabs()) {
			if (tab.isSelectionListener()) {
				SelectionListener sl = (SelectionListener) tab;
				sl.selectionListChanged(e);
			}
		}
	}
	
	/**
	 * This method is called when the session changes.
	 * 
	 * @param s
	 *           the new Session.
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	public synchronized void sessionChanged(Session s) {
		for (InspectorTab tab : getTabs()) {
			if (tab instanceof SessionListener) {
				SessionListener sl = (SessionListener) tab;
				try {
					sl.sessionChanged(s);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		if (s == null)
			viewChanged(null);
	}
	
	/**
	 * This method is called when the session data (but not the session's graph
	 * data) changed.
	 * 
	 * @param s
	 *           Session
	 */
	public void sessionDataChanged(Session s) {
		for (InspectorTab tab : getTabs()) {
			if (tab instanceof SessionListener) {
				SessionListener sl = (SessionListener) tab;
				sl.sessionDataChanged(s);
			}
		}
	}
	
	/**
	 *
	 */
	private void removeTab(InspectorTab tab) {
		container.removeTab(tab);
	}
	
	public void viewChanged(View newView) {
		
		InspectorTab selTab = getSelectedTab();
		
		// System.out.println("STO "+oldviewname+" /// "+selTab.getTitle()+" NEW VIEW: "+
		// (newView!=null ? newView.getViewName() :" NULL"));
		rememberedTabs.put(oldviewname, selTab);
		
		LinkedHashSet<InspectorTab> allTabs = new LinkedHashSet<InspectorTab>();
		for (InspectorTab tab : getTabs())
			allTabs.add(tab);
		allTabs.addAll(hiddenTabs);
		
		ArrayList<InspectorTab> added = new ArrayList<InspectorTab>();
		for (InspectorTab tab : allTabs) {
			if (!tab.visibleForView(newView) || (newView != null && !newView.worksWithTab(tab))) {
				removeTab(tab);
				hiddenTabs.add(tab);
			} else {
				if (hiddenTabs.contains(tab)) {
					addTab(tab);
					added.add(tab);
				}
			}
		}
		
		for (InspectorTab tab : getTabs()) {
			if (tab instanceof ViewListener) {
				ViewListener sl = (ViewListener) tab;
				sl.viewChanged(newView);
			}
		}
		if (newView == null)
			for (InspectorTab tab : hiddenTabs) {
				if (tab instanceof ViewListener) {
					ViewListener sl = (ViewListener) tab;
					sl.viewChanged(newView);
				}
			}
		
		if (newView != null) {
			setSelectedTab(rememberedTabs.get(newView.getClass().getName()));
			oldviewname = newView.getClass().getName();
		}
	}
	
	@Override
	public synchronized InspectorTab[] getInspectorTabs() {
		return new InspectorTab[] { new SubtabHostTab("Network", new InspectorTab[] {
							new GraphTab(),
							new NodeTab(),
							new EdgeTab()
				}) };
	}
	
	public void setSelectedTab(InspectorTab tab) {
		if (tab != null && container != null && container.getTabs() != null && container.getTabs().contains(tab))
			container.setSelectedComponent(tab);
	}
	
	public InspectorTab getSelectedTab() {
		Component c = container.getSelectedComponent();
		if (c != null && c instanceof InspectorTab) {
			return (InspectorTab) c;
		} else
			return null;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
