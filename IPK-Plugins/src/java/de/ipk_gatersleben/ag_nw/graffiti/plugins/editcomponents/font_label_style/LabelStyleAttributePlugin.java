/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_style;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class LabelStyleAttributePlugin
					extends IPK_PluginAdapter
					implements EditorPlugin {
	private HashMap<Class<LabelStyleAttribute>, Class<LabelStyleAttributeEditor>> valueEditComponents;
	private HashMap<?, ?> attributeComponents;
	
	public LabelStyleAttributePlugin() {
		this.attributes = new Class[1];
		this.attributes[0] = LabelStyleAttribute.class;
		
		StringAttribute.putAttributeType("fontStyle", LabelStyleAttribute.class);
		
		valueEditComponents = new HashMap<Class<LabelStyleAttribute>, Class<LabelStyleAttributeEditor>>();
		attributeComponents = new HashMap<Object, Object>();
		
		valueEditComponents.put(
							LabelStyleAttribute.class,
							LabelStyleAttributeEditor.class);
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
	public Map<Class<LabelStyleAttribute>, Class<LabelStyleAttributeEditor>> getValueEditComponents() {
		return valueEditComponents;
	}
}