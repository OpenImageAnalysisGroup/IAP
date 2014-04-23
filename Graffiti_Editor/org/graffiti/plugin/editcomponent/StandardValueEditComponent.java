// ==============================================================================
//
// StandardValueEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StandardValueEditComponent.java,v 1.1 2011-01-31 09:04:30 klukas Exp $

package org.graffiti.plugin.editcomponent;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.graffiti.plugin.Displayable;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 */
public class StandardValueEditComponent
					extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private JTextField textField;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Standard constructor.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public StandardValueEditComponent(Displayable disp) {
		super(disp);
		this.textField = new JTextField();
		textField.setEditable(false);
		if (displayable.getValue() != null)
			textField.setText(displayable.getValue().toString());
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Return the component used to display the displayable.
	 * 
	 * @return DOCUMENT ME!
	 */
	public JComponent getComponent() {
		// textField = new JTextField(displayable.getValue().toString());
		// textField.setEditable(false);
		// textField.setColumns(30);
		// panel.add(textField);
		textField.setMinimumSize(new Dimension(0, 20));
		textField.setPreferredSize(new Dimension(50, 30));
		textField.setMaximumSize(new Dimension(2000, 40));
		
		// textField.setSize(100, 30);
		return textField;
	}
	
	/**
	 * Updates this component with the value from the displayable.
	 */
	public void setEditFieldValue() {
		if (showEmpty) {
			this.textField.setText(EMPTY_STRING);
		} else {
			if (this != null && displayable != null && this.textField != null && displayable.getValue() != null) {
				this.textField.setText(displayable.getValue().toString());
			}
		}
	}
	
	/**
	 * Standard edit component is not editable.
	 */
	public void setValue() {
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
