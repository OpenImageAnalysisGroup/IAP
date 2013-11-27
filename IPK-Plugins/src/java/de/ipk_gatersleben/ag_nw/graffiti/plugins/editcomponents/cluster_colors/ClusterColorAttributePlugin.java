/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class ClusterColorAttributePlugin
					extends IPK_PluginAdapter
					implements EditorPlugin {
	private HashMap<Class<?>, Class<ClusterColorAttributeEditor>> valueEditComponents;
	private HashMap<?, ?> attributeComponents;
	
	public ClusterColorAttributePlugin() {
		this.attributes = new Class[1];
		this.attributes[0] = ClusterColorAttribute.class;
		
		StringAttribute.putAttributeType(ClusterColorAttribute.attributeName, ClusterColorAttribute.class);
		
		valueEditComponents = new HashMap<Class<?>, Class<ClusterColorAttributeEditor>>();
		valueEditComponents.put(
							ClusterColorAttribute.class,
							ClusterColorAttributeEditor.class);
		valueEditComponents.put(
							ClusterColorParameter.class,
							ClusterColorAttributeEditor.class);
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
	public Map<Class<?>, Class<ClusterColorAttributeEditor>> getValueEditComponents() {
		return valueEditComponents;
	}
}