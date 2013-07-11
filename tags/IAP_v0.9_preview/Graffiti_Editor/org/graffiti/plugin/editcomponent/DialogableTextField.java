// ==============================================================================
//
// DialogableTextField.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DialogableTextField.java,v 1.1 2011-01-31 09:04:30 klukas Exp $

package org.graffiti.plugin.editcomponent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.graffiti.plugin.Displayable;

/**
 * <code>DialogableTextField</code> provides a component which contains a
 * textfield and a dialog for setting the value of the textfield. The value of
 * the displayable can then be set to the value within the textfield.
 * 
 * @version $Revision: 1.1 $
 * @see AbstractDialogableEditComponent
 * @see javax.swing.JTextField
 * @see javax.swing.JButton
 * @see javax.swing.JDialog
 * @see javax.swing.JPanel
 */
public class DialogableTextField
					extends AbstractDialogableEditComponent {
	// ~ Instance fields ========================================================
	
	/**
	 * The <code>ValueField</code> containing the components of this <code>DialogableTextField</code>.
	 */
	private ValueField valueField;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>DialogableTextField</code>.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public DialogableTextField(Displayable disp) {
		super(disp);
	}
	
	/**
	 * Constructs a new <code>DialogableTextField</code>.
	 * 
	 * @param disp
	 *           the<code>javax.swing.JTextField</code> containing the value
	 *           of the displayable to be set.
	 * @param button
	 *           the <code>javax.swing.JButton</code> to open the dialog.
	 * @param dialog
	 *           the <code>javax.swing.JDialog</code> to provide an
	 *           arbitrary possibility to specifiy the value of the displayable.
	 */
	public DialogableTextField(Displayable disp, JButton button, JDialog dialog) {
		super(disp);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>JComponent</code> containing the <code>ValueEditComponent</code>.
	 * 
	 * @return the <code>JComponent</code> containing the <code>ValueEditComponent</code>.
	 */
	public JComponent getComponent() {
		return this.valueField;
	}
	
	/**
	 * Sets the current value of the <code>Attribute</code> in the
	 * corresponding <code>JComponent</code>.
	 */
	public void setEditFieldValue() {
		valueField.setText(displayable.getValue().toString());
	}
	
	/**
	 * Sets the value of the displayable specified in the <code>JComponent</code>.
	 */
	public void setValue() {
		displayable.setValue(valueField.getText());
	}
	
	// ~ Inner Classes ==========================================================
	
	/**
	 * The inner class <code>ValueEditField</code> manages the interaction
	 * between the textfield and the dialog used to specify the value. It is
	 * responsible for setting the value of the displayable, updating the
	 * textfield etc.
	 */
	private class ValueField
						extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		// /**
		// * Constructs a new <code>ValueField</code> and adds its components.
		// *
		// * @param textField the <code>javax.swing.JTextField</code> for editing
		// * the value of the displayable.
		// * @param button the <code>javax.swing.JButton</code> for opening the
		// * dialog to edit the value of the displayable.
		// * @param dialog the <code>javax.swing.JDialog</code> for editing
		// * choosing the value for the displayable.
		// */
		// public ValueField(JTextField textField, JButton button, JDialog dialog)
		// {
		// super();
		//
		// // add the components using a layout such that they are placed in
		// // one row.
		// }
		
		/**
		 * Sets the String to be set within the textfield.
		 * 
		 * @param text
		 *           the String to be set within the textfield.
		 */
		public void setText(String text) {
		}
		
		/**
		 * Returns the text in the textfield.
		 * 
		 * @return the text in the textfield.
		 */
		public String getText() {
			return null; 
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
