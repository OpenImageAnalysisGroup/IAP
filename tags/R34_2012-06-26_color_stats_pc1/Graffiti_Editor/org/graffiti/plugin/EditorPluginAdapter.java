// ==============================================================================
//
// EditorPluginAdapter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EditorPluginAdapter.java,v 1.1 2011-01-31 09:04:35 klukas Exp $

package org.graffiti.plugin;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;

/**
 * Plugin for editor. Returns null everywhere.
 */
public class EditorPluginAdapter
					extends GenericPluginAdapter
					implements EditorPlugin {
	// ~ Instance fields ========================================================
	
	/**
	 * Maps from an attribute class to an AttributeComponent class. old
	 * comment: A <code>java.util.Map</code> from <code>Attribute</code> to
	 * the corresponding <code>LabelValueRow</code>-instance.
	 */
	@SuppressWarnings("unchecked")
	protected Map attributeComponents;
	
	/** The mapping between attribute classes and attributeComponent classes. */
	@SuppressWarnings("unchecked")
	protected Map valueEditComponents;
	
	/** The gui components the plugin provides. */
	protected GraffitiComponent[] guiComponents;
	
	/** The modes the plugin provides. */
	protected Mode[] modes;
	
	/** The shapes the plugin provides. */
	protected GraffitiShape[] shapes;
	
	/** The tools the plugin provides. */
	public Tool[] tools;
	
	/** The InspectorTabs the plugin provides. */
	protected InspectorTab[] tabs;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for EditorPluginAdapter.
	 */
	public EditorPluginAdapter() {
		super();
		this.guiComponents = new GraffitiComponent[0];
		this.modes = new Mode[0];
		this.tools = new Tool[0];
		this.shapes = new GraffitiShape[0];
		this.valueEditComponents = new HashMap<Object, Object>();
		this.attributeComponents = new HashMap<Object, Object>();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns a mapping between attribute classnames and attributeComponent
	 * classnames.
	 * 
	 * @return a mapping between attribute classnames and attributeComponent
	 *         classnames.
	 */
	public Map<?, ?> getAttributeComponents() {
		return this.attributeComponents;
	}
	
	/**
	 * Returns the array of <code>GraffitiComponent</code>s the plugin
	 * contains.
	 * 
	 * @return the array of <code>GraffitiComponent</code>s the plugin
	 *         contains.
	 */
	public GraffitiComponent[] getGUIComponents() {
		return this.guiComponents;
	}
	
	/**
	 * Returns the array of <code>org.graffiti.plugin.mode.Mode</code>s the
	 * plugin contains.
	 * 
	 * @return the array of <code>org.graffiti.plugin.mode.Mode</code>s the
	 *         plugin contains.
	 */
	public Mode[] getModes() {
		return this.modes;
	}
	
	/**
	 * Returns the array of <code>org.graffiti.plugin.view.GraffitiShape</code>s the plugin
	 * contains.
	 * 
	 * @return the array of <code>org.graffiti.plugin.view.GraffitiShape</code>s the plugin
	 *         contains.
	 */
	public GraffitiShape[] getShapes() {
		return this.shapes;
	}
	
	/**
	 * Returns an array of <code>org.graffiti.plugin.mode.Tool</code>s the
	 * plugin provides.
	 * 
	 * @return an array of tools the plugin provides.
	 */
	public Tool[] getTools() {
		return this.tools;
	}
	
	/**
	 * Returns a mapping from attribute classes to attributeComponent classes.
	 * 
	 * @return DOCUMENT ME!
	 */
	public Map<?, ?> getValueEditComponents() {
		return this.valueEditComponents;
	}
	
	public InspectorTab[] getInspectorTabs() {
		return tabs;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
