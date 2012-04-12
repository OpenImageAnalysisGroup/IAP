/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_color;

import java.util.HashMap;
import java.util.Map;

import org.graffiti.attributes.StringAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.parameter.ColorParameter;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;
import org.graffiti.plugins.editcomponents.defaults.ColorChooserEditComponent;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class LabelColorAttributePlugin extends IPK_PluginAdapter implements EditorPlugin {
	private final HashMap<Class<?>, Class<ColorChooserEditComponent>> valueEditComponents;
	private final HashMap<?, ?> attributeComponents;
	
	@SuppressWarnings("unchecked")
	public LabelColorAttributePlugin() {
		this.attributes = new Class[1];
		this.attributes[0] = LabelColorAttribute.class;
		
		StringAttribute.putAttributeType(GraphicAttributeConstants.TEXTCOLOR, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.CHARTBACKGROUNDCOLOR, LabelColorAttribute.class);
		for (int i = 0; i < 100; i++)
			StringAttribute
								.putAttributeType(GraphicAttributeConstants.CHARTBACKGROUNDCOLOR + i, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.GRIDCOLOR, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.AXISCOLOR, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.CATEGORY_BACKGROUND_A, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.CATEGORY_BACKGROUND_B, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.CATEGORY_BACKGROUND_C, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.SHADOWCOLOR, LabelColorAttribute.class);
		
		StringAttribute.putAttributeType(GraphicAttributeConstants.HEATMAP_LOWER_COL, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.HEATMAP_MIDDLE_COL, LabelColorAttribute.class);
		StringAttribute.putAttributeType(GraphicAttributeConstants.HEATMAP_UPPER_COL, LabelColorAttribute.class);
		
		valueEditComponents = new HashMap();
		attributeComponents = new HashMap();
		
		valueEditComponents.put(LabelColorAttribute.class, ColorChooserEditComponent.class);
		valueEditComponents.put(ColorParameter.class, ColorChooserEditComponent.class);
		
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
	public Map<Class<?>, Class<ColorChooserEditComponent>> getValueEditComponents() {
		return valueEditComponents;
	}
}