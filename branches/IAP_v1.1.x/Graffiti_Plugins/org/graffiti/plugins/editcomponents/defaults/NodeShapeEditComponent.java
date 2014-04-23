// ==============================================================================
//
// NodeShapeEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: NodeShapeEditComponent.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import java.awt.Dimension;

import javax.swing.JComboBox;

import org.AttributeHelper;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.ComboBoxEditComponent;

/**
 * Class used to display different node shapes.
 * 
 * @version $Revision: 1.1 $
 */
public class NodeShapeEditComponent
					extends ComboBoxEditComponent {
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor sets the correct entries of the combo box. And creates a new
	 * combo box.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public NodeShapeEditComponent(Displayable disp) {
		super(disp);
		this.comboText = AttributeHelper.getShapeDescritions();
		this.comboValue = AttributeHelper.getShapeClasses();
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
		this.comboBox.setRenderer(new NodeShapeCellRenderer());
	}
	
	@Override
	public void setEditFieldValue() {
		Object value = this.displayable.getValue();
		if (value == null)
			showEmpty = true;
		
		if (showEmpty) {
			comboBox.insertItemAt(EMPTY_STRING, 0);
			comboBox.setSelectedIndex(0);
		} else {
			if (comboBox.getItemCount() > 0 && comboBox.getItemAt(0).equals(EMPTY_STRING)) {
				comboBox.removeItemAt(0);
			}
			for (int i = comboValue.length - 1; i >= 0; i--) {
				if (value.equals(comboValue[i]) || ((String) value).equals(AttributeHelper.getShapeClassFromShapeName((String) comboValue[i]))) {
					this.comboBox.setSelectedIndex(i);
					
					break;
				}
			}
		}
		searchComponent.setEnabled(!showEmpty);
	}
	
	@Override
	public void setValue() {
		if (this.comboBox.getSelectedItem().equals(EMPTY_STRING)
							||
							(displayable.getValue() != null && ((String) this.displayable.getValue()).equalsIgnoreCase(AttributeHelper
												.getShapeClassFromDescription((String) this.comboBox.getSelectedItem())))) {
			return;
		}
		
		if (this.comboBox.getItemAt(0).equals(EMPTY_STRING)) {
			this.displayable.setValue(comboValue[this.comboBox.getSelectedIndex() - 1]);
		} else {
			this.displayable.setValue(comboValue[this.comboBox.getSelectedIndex()]);
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
