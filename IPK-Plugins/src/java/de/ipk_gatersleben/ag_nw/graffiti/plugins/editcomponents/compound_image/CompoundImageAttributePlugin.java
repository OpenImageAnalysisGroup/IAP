/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.AttributeDescription;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class CompoundImageAttributePlugin
					extends IPK_PluginAdapter
					implements EditorPlugin {
	private HashMap<Class<?>, Class<?>> valueEditComponents;
	private HashMap<Class<?>, Class<CompoundImageAttributeComponent>> attributeComponents;
	
	@SuppressWarnings("unchecked")
	public CompoundImageAttributePlugin() {
		this.attributes = new Class[1];
		this.attributes[0] = CompoundAttribute.class;
		
		this.algorithms = new Algorithm[] {
							new ImageAssignmentCommand()
		};
		
		StringAttribute.putAttributeType("image_url", CompoundAttribute.class);
		StringAttribute.putAttributeType("image_position", CompoundPositionAttribute.class);
		
		valueEditComponents = new HashMap();
		attributeComponents = new HashMap();
		
		valueEditComponents.put(
							CompoundAttribute.class,
							CompoundImageAttributeEditor.class);
		valueEditComponents.put(
							CompoundPositionAttribute.class,
							CompoundImagePositionAttributeEditor.class);
		
		attributeComponents.put(CompoundAttribute.class,
							CompoundImageAttributeComponent.class);
		
		this.attributeDescriptions = new AttributeDescription[] {
							new AttributeDescription("image_border", DoubleAttribute.class, "Image:<html>&nbsp;Border", true, false, null)
		};
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.EditorPlugin#getAttributeComponents()
	 */
	public Map<Class<?>, Class<CompoundImageAttributeComponent>> getAttributeComponents() {
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