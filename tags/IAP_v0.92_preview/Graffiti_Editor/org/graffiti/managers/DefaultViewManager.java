// ==============================================================================
//
// DefaultViewManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultViewManager.java,v 1.1 2011-01-31 09:04:29 klukas Exp $

package org.graffiti.managers;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

/**
 * Manages a list of view types.
 * 
 * @version $Revision: 1.1 $
 */
public class DefaultViewManager
					implements ViewManager {
	// ~ Instance fields ========================================================
	
	/** Contains the list of listeners. */
	private LinkedHashSet<ViewManagerListener> listeners;
	
	/** Contains the list of listeners. */
	private LinkedHashSet<ViewListener> viewListeners;
	
	/** Contains the class names of the available views. */
	private Set<String> views;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new view manager.
	 */
	public DefaultViewManager() {
		views = new TreeSet<String>();
		listeners = new LinkedHashSet<ViewManagerListener>();
		viewListeners = new LinkedHashSet<ViewListener>();
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.managers.ViewManager#getViewNames()
	 */
	public String[] getViewNames() {
		Object[] names = views.toArray();
		String[] stringNames = new String[names.length];
		
		for (int i = 0; i < stringNames.length; i++) {
			stringNames[i] = (String) names[i];
		}
		
		return stringNames;
	}
	
	public String[] getViewDescriptions() {
		Object[] names = views.toArray();
		String[] stringNames = new String[names.length];
		
		for (int i = 0; i < stringNames.length; i++) {
			View v;
			try {
				v = createView((String) names[i]);
				stringNames[i] = v.getViewName();
				v.close();
			} catch (InstanceCreationException e) {
				stringNames[i] = (String) names[i] + " (invalid)";
			}
			// stringNames[i] = (String) names[i];
		}
		
		return stringNames;
	}
	
	/*
	 * @see org.graffiti.managers.ViewManager#addListener(org.graffiti.managers.ViewManager.ViewManagerListener)
	 */
	public void addListener(ViewManagerListener viewManagerListener) {
		listeners.add(viewManagerListener);
	}
	
	/*
	 * @see org.graffiti.managers.ViewManager#addView(java.lang.String)
	 */
	public void addView(String viewType) {
		views.add(viewType);
		// logger.info("new view registered: " + viewType);
		
		fireViewTypeAdded(viewType);
	}
	
	/*
	 * @see org.graffiti.managers.ViewManager#addViewListener(org.graffiti.plugin.view.ViewListener)
	 */
	public void addViewListener(ViewListener viewListener) {
		viewListeners.add(viewListener);
	}
	
	/*
	 * @see org.graffiti.managers.ViewManager#addViews(java.lang.String[])
	 */
	public void addViews(String[] views) {
		for (int i = 0; i < views.length; i++) {
			addView(views[i]);
		}
	}
	
	/*
	 * @see org.graffiti.managers.ViewManager#createView(java.lang.String)
	 */
	public View createView(String name)
						throws InstanceCreationException {
		return (View) InstanceLoader.createInstance(name);
	}
	
	/*
	 * @see org.graffiti.managers.ViewManager#hasViews()
	 */
	public boolean hasViews() {
		return !views.isEmpty();
	}
	
	/*
	 * @see org.graffiti.managers.pluginmgr.PluginManagerListener#pluginAdded(org.graffiti.plugin.GenericPlugin,
	 * org.graffiti.managers.pluginmgr.PluginDescription)
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		addViews(plugin.getViews());
		if (plugin.getDefaultView() != null)
			setDefaultView(plugin.getDefaultView());
	}
	
	/*
	 * @see org.graffiti.managers.ViewManager#removeListener(org.graffiti.managers.ViewManager.ViewManagerListener)
	 */
	public boolean removeListener(ViewManagerListener l) {
		return listeners.remove(l);
	}
	
	/*
	 * @see org.graffiti.managers.ViewManager#removeViewListener(org.graffiti.plugin.view.ViewListener)
	 */
	public boolean removeViewListener(ViewListener l) {
		return viewListeners.remove(l);
	}
	
	/*
	 * @see org.graffiti.plugin.view.ViewListener#viewChanged(org.graffiti.plugin.view.View)
	 */
	public void viewChanged(View newView) {
		for (ViewListener vl : viewListeners) {
			vl.viewChanged(newView);
		}
	}
	
	/**
	 * Informs all view manager listeners, that the given view type is
	 * available.
	 * 
	 * @param viewType
	 *           the new view type.
	 */
	private void fireViewTypeAdded(String viewType) {
		for (Iterator<ViewManagerListener> i = listeners.iterator(); i.hasNext();) {
			ViewManagerListener l = i.next();
			l.viewTypeAdded(viewType);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.managers.ViewManager#removeViews()
	 */
	public void removeViews() {
		views.clear();
	}
	
	String defaultView;
	
	public String getDefaultView() {
		return defaultView;
	}
	
	public void setDefaultView(String defaultView) {
		this.defaultView = defaultView;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
