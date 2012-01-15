// ==============================================================================
//
// EdgeShapeEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: EdgeShapeEditComponent.java,v 1.1 2011-01-31 09:03:25 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JComboBox;

import org.AttributeHelper;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ComboBoxEditComponent;

/**
 * Class used to display different edge shapes.
 * 
 * @version $Revision: 1.1 $
 */
public class EdgeShapeEditComponent
					extends ComboBoxEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor sets the correct entries for the combo box. And creates a
	 * new combo box.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public EdgeShapeEditComponent(Displayable disp) {
		super(disp);
		
		HashMap<String, String> shapes = AttributeHelper.getEdgeShapes();
		
		comboText = new String[shapes.size()];
		comboValue = new String[shapes.size()];
		
		int i = 0;
		for (Entry<String, String> e : shapes.entrySet()) {
			comboText[i] = e.getKey();
			comboValue[i++] = e.getValue();
		}
		
		comboBox = new JComboBox(this.comboText) {
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
