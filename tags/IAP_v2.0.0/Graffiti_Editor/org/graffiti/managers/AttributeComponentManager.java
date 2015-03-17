// ==============================================================================
//
// AttributeComponentManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AttributeComponentManager.java,v 1.1 2011-01-31 09:04:29 klukas Exp $

package org.graffiti.managers;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.editor.AttributeComponentNotFoundException;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

/**
 * Contains the mapping between attribute classes and their representation as <code>AttributeComponent</code> classes.
 * 
 * @author ph
 * @version $Revision: 1.1 $
 */
public class AttributeComponentManager
					implements PluginManagerListener {
	// ~ Instance fields ========================================================
	
	/** Maps attribute classes to attributeComponent classes. */
	@SuppressWarnings("unchecked")
	private Map attributeComponents;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs an AttributeComponentManager.
	 */
	public AttributeComponentManager() {
		this.attributeComponents = new HashMap<Object, Object>();
	}
	
	// ~ Methods ================================================================
	
	public boolean hasAttributeComponent(Class<?> aType) {
		return attributeComponents.containsKey(aType);
	}
	
	/**
	 * Returns an instance of the AttributeComponent that is capable of drawing
	 * the attribute with type <code>aType</code>.
	 * 
	 * @param aType
	 *           the class of the attribute to retrieve a component for.
	 * @return an instance of an AttributeComponent.
	 * @throws AttributeComponentNotFoundException
	 *            DOCUMENT ME!
	 */
	public AttributeComponent getAttributeComponent(Class<?> aType)
						throws AttributeComponentNotFoundException {
		if (!(attributeComponents.containsKey(aType))) {
			throw new AttributeComponentNotFoundException(
								"No registered GraffitiViewComponent for AttributeType " +
													aType);
		}
		
		Class<?> ac = (Class<?>) attributeComponents.get(aType);
		
		try {
			AttributeComponent component = (AttributeComponent) InstanceLoader.createInstance(ac);
			
			return component;
		} catch (InstanceCreationException ice) {
			throw new AttributeComponentNotFoundException(ice.getMessage());
		}
	}
	
	/**
	 * Returns the map of attribute components.
	 * 
	 * @return DOCUMENT ME!
	 */
	public Map<?, ?> getAttributeComponents() {
		return attributeComponents;
	}
	
	/**
	 * Called by the plugin manager, iff a plugin has been added.
	 * 
	 * @param plugin
	 *           the added plugin.
	 * @param desc
	 *           the description of the new plugin.
	 */
	@SuppressWarnings("unchecked")
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		// System.out.println("puting: " + plugin.getAttributeComponents());
		if (plugin instanceof EditorPlugin) {
			if (((EditorPlugin) plugin).getAttributeComponents() != null) {
				attributeComponents.putAll(((EditorPlugin) plugin).getAttributeComponents());
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
