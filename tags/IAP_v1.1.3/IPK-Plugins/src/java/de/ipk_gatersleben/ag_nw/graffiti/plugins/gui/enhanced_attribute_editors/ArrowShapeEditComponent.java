/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
// ==============================================================================
//
// NodeShapeEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ArrowShapeEditComponent.java,v 1.1 2011-01-31 09:01:00 klukas Exp $

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.enhanced_attribute_editors;

import java.awt.Dimension;

import javax.swing.JComboBox;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ComboBoxEditComponent;

/**
 * Class used to display different node shapes.
 * 
 * @version $Revision: 1.1 $
 */
public class ArrowShapeEditComponent
					extends ComboBoxEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor sets the correct entries of the combo box. And creates a new
	 * combo box.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public ArrowShapeEditComponent(Displayable disp) {
		super(disp);
		this.comboText = new String[] { "Rectangle", "Circle", "Ellipse" };
		this.comboValue = new String[]
												{
																	"org.graffiti.plugins.views.defaults.RectangleNodeShape",
																	"org.graffiti.plugins.views.defaults.CircleNodeShape",
																	"org.graffiti.plugins.views.defaults.EllipseNodeShape"
												};
		this.comboBox = new JComboBox(this.comboText) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Dimension getMinimumSize() {
				Dimension res = super.getMinimumSize();
				res.setSize(20, res.getHeight());
				return res;
			}
			
			@Override
			public Dimension getPreferredSize() {
				Dimension res = super.getPreferredSize();
				res.setSize(20, res.getHeight());
				return res;
			}
		};
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
