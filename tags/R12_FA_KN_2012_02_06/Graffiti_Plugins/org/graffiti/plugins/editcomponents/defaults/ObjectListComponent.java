// ==============================================================================
//
// NodeShapeEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ObjectListComponent.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import javax.swing.JComboBox;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ComboBoxEditComponent;
import org.graffiti.plugin.parameter.ObjectListParameter;

/**
 * Class used to display different node shapes.
 * 
 * @version $Revision: 1.1 $
 */
public class ObjectListComponent
					extends ComboBoxEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor sets the correct entries of the combo box. And creates a new
	 * combo box.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public ObjectListComponent(Displayable disp) {
		super(disp);
		if (disp instanceof ObjectListParameter) {
			ObjectListParameter ola = (ObjectListParameter) disp;
			this.comboText = ola.getPossibleValues().toArray();
			this.comboValue = ola.getPossibleValues().toArray();
			this.comboBox = new JComboBox(this.comboText);
			if (ola.getValue() != null)
				comboBox.setSelectedItem(ola.getValue());
			if (ola.getRenderer() != null)
				comboBox.setRenderer(ola.getRenderer());
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
