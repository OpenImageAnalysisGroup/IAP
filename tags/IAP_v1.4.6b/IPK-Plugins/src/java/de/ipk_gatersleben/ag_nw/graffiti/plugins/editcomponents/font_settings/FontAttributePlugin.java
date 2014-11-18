/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_settings;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class FontAttributePlugin
					extends IPK_PluginAdapter
					implements EditorPlugin {
	private HashMap<Class<FontAttribute>, Class<FontAttributeEditor>> valueEditComponents;
	private HashMap<?, ?> attributeComponents;
	
	public FontAttributePlugin() {
		this.attributes = new Class[1];
		this.attributes[0] = FontAttribute.class;
		
		StringAttribute.putAttributeType("nodefont", FontAttribute.class);
		StringAttribute.putAttributeType("charttitlefont", FontAttribute.class);
		
		valueEditComponents = new HashMap<Class<FontAttribute>, Class<FontAttributeEditor>>();
		attributeComponents = new HashMap<Object, Object>();
		
		valueEditComponents.put(
							FontAttribute.class,
							FontAttributeEditor.class);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.EditorPlugin#getAttributeComponents()
	 */
	public Map<?, ?> getAttributeComponents() {
		return attributeComponents;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.EditorPlugin#getGUIComponents()
	 */
	public GraffitiComponent[] getGUIComponents() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.EditorPlugin#getModes()
	 */
	public Mode[] getModes() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.EditorPlugin#getShapes()
	 */
	public GraffitiShape[] getShapes() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.EditorPlugin#getTools()
	 */
	public Tool[] getTools() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.EditorPlugin#getValueEditComponents()
	 */
	public Map<Class<FontAttribute>, Class<FontAttributeEditor>> getValueEditComponents() {
		return valueEditComponents;
	}
}