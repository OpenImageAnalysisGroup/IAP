/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import java.util.HashMap;

import org.AttributeHelper;
import org.graffiti.attributes.AttributeDescription;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graphics.GradientFillAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.SourceDockingAttribute;
import org.graffiti.graphics.TargetDockingAttribute;
import org.graffiti.graphics.ThicknessAttribute;
import org.graffiti.plugins.views.defaults.DockingAttributeEditor;
import org.graffiti.plugins.views.defaults.GradientFillAttributeEditor;
import org.graffiti.plugins.views.defaults.ThicknessAttributeEditor;

import de.ipk_gatersleben.ag_nw.graffiti.DBE_EditorPluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_color.LabelColorAttribute;

/**
 * PlugIn for default view of graffiti graph editor
 * 
 * @version $Revision: 1.1 $
 */
public class IPKDefaultView
					extends DBE_EditorPluginAdapter {
	
	/**
	 * Constructor for DefaultView.
	 */
	@SuppressWarnings("unchecked")
	public IPKDefaultView() {
		super();
		
		this.views = new String[] {
							"de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView",

							// "de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.ButtonOverlayView"//,
							
							// "de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.NullView"
							};
		/*
		 * this.views[1] =
		 * "de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.ipk_graffitiview.IPKGraffitiViewFast";
		 */
		// GravistoService.getInstance().getMainFrame().getViewManager().removeViews();
		
		this.attributeDescriptions = new AttributeDescription[] {
							new AttributeDescription("graphbackgroundcolor", LabelColorAttribute.class, "Network Attributes: Background Color", false, false, null),
							new AttributeDescription(GraphicAttributeConstants.GRADIENT, GradientFillAttribute.class, null, true, true, null),
							new AttributeDescription(GraphicAttributeConstants.THICKNESS, ThicknessAttribute.class, null, false, true, null),
							new AttributeDescription(".graphics.docking.source", SourceDockingAttribute.class, null, false, true, null),
							new AttributeDescription(".graphics.docking.target", TargetDockingAttribute.class, null, false, true, null),
		};
		
		StringAttribute.putAttributeType("graphbackgroundcolor", LabelColorAttribute.class);
		
		AttributeHelper.setNiceId("Node:gradient", "Shape:<html>&nbsp;Gradient Fill (%)");
		AttributeHelper.setNiceId("Edge:gradient", "<html>Gradient");
		
		attributes = new Class[] {
							GradientFillAttribute.class,
							ThicknessAttribute.class,
							SourceDockingAttribute.class,
							TargetDockingAttribute.class,
		};
		
		valueEditComponents = new HashMap<Class<?>, Class<?>>();
		valueEditComponents.put(GradientFillAttribute.class, GradientFillAttributeEditor.class);
		valueEditComponents.put(ThicknessAttribute.class, ThicknessAttributeEditor.class);
		valueEditComponents.put(SourceDockingAttribute.class, DockingAttributeEditor.class);
		valueEditComponents.put(TargetDockingAttribute.class, DockingAttributeEditor.class);
		
	}
	
	@Override
	public String getDefaultView() {
		return "de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView";
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
