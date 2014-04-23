/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.AttributeDescription;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class ChartAttributePlugin
					extends IPK_PluginAdapter
					implements EditorPlugin {
	private HashMap<Class<?>, Class<?>> valueEditComponents;
	private HashMap<Class<?>, Class<?>> attributeComponents;
	
	public ChartAttributePlugin() {
		this.attributes = new Class[] {
							ChartAttribute.class,
							ChartsColumnAttribute.class
		};
		
		StringAttribute.putAttributeType("component", ChartAttribute.class);
		
		valueEditComponents = new HashMap<Class<?>, Class<?>>();
		
		valueEditComponents.put(
							ChartAttribute.class,
							ChartAttributeEditor.class);
		valueEditComponents.put(
							ChartsColumnAttribute.class,
							ChartsColumnAttributeEditor.class);
		
		attributeComponents = new HashMap<Class<?>, Class<?>>();
		
		attributeComponents.put(ChartAttribute.class,
							ChartAttributeComponent.class);
		
		for (GraffitiCharts c : GraffitiCharts.values())
			ChartComponentManager.getInstance().registerChartComponent(c);
		
		attributeDescriptions = new AttributeDescription[] {
							new AttributeDescription(ChartsColumnAttribute.name, ChartsColumnAttribute.class, null, true, true, null),
		};
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.EditorPlugin#getAttributeComponents()
	 */
	public Map<Class<?>, Class<?>> getAttributeComponents() {
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
	public Map<Class<?>, Class<?>> getValueEditComponents() {
		return valueEditComponents;
	}
}