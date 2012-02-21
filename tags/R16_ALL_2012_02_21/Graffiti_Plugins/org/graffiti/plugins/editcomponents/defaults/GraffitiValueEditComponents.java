// ==============================================================================
//
// GraffitiValueEditComponents.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GraffitiValueEditComponents.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import org.graffiti.attributes.ArrowShapeAttribute;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.ByteAttribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.EdgeShapeAttribute;
import org.graffiti.attributes.FloatAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.LongAttribute;
import org.graffiti.attributes.NodeShapeAttribute;
import org.graffiti.attributes.ShortAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.editcomponent.NodeEditComponent;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.FloatParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.MultiFileSelectionParameter;
import org.graffiti.plugin.parameter.NodeParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.StringParameter;

/**
 * This class is a plugin providing some default value edit components.
 * 
 * @version $Revision: 1.1 $
 * @see org.graffiti.plugin.editcomponent.ValueEditComponent
 * @see org.graffiti.plugin.GenericPlugin
 */
public class GraffitiValueEditComponents
					extends EditorPluginAdapter {
	// ~ Constructors ===========================================================
	
	// /**
	// * The number of edit components this plugin provides.
	// */
	// private static final int NUMBER_OF_VECS = 20;
	
	/**
	 * Constructs a new <code>GraffitiValueEditComponent</code>.
	 */
	@SuppressWarnings("unchecked")
	public GraffitiValueEditComponents() {
		super();
		
		// register the ui compoents for the displayable types
		valueEditComponents.put(ColorAttribute.class,
							ColorChooserEditComponent.class);
		// valueEditComponents.put(LineModeAttribute.class,
		// LineModeEditComponent.class);
		valueEditComponents.put(IntegerAttribute.class,
							IntegerEditComponent.class);
		valueEditComponents.put(ShortAttribute.class, ShortEditComponent.class);
		valueEditComponents.put(LongAttribute.class, LongEditComponent.class);
		valueEditComponents.put(ByteAttribute.class, ByteEditComponent.class);
		valueEditComponents.put(FloatAttribute.class, FloatEditComponent.class);
		valueEditComponents.put(DoubleAttribute.class, DoubleEditComponent.class);
		valueEditComponents.put(BooleanAttribute.class,
							BooleanEditComponent.class);
		valueEditComponents.put(NodeShapeAttribute.class,
							NodeShapeEditComponent.class);
		valueEditComponents.put(EdgeShapeAttribute.class,
							EdgeShapeEditComponent.class);
		
		valueEditComponents.put(ArrowShapeAttribute.class, EdgeArrowShapeEditComponent.class);
		
		valueEditComponents.put(StringAttribute.class, StringEditComponent.class);
		
		// register the ui compoents for the parameter types
		valueEditComponents.put(IntegerParameter.class,
							IntegerEditComponent.class);
		valueEditComponents.put(DoubleParameter.class, DoubleEditComponent.class);
		valueEditComponents.put(FloatParameter.class, FloatEditComponent.class);
		valueEditComponents.put(StringParameter.class, StringEditComponent.class);
		valueEditComponents.put(MultiFileSelectionParameter.class, MultiFileSelectionEditComponent.class);
		valueEditComponents.put(BooleanParameter.class,
							BooleanEditComponent.class);
		valueEditComponents.put(NodeParameter.class, NodeEditComponent.class);
		valueEditComponents.put(ObjectListParameter.class,
							ObjectListComponent.class);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
