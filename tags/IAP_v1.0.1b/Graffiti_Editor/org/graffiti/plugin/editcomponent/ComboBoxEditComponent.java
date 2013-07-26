// ==============================================================================
//
// ComboBoxEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ComboBoxEditComponent.java,v 1.1 2011-01-31 09:04:29 klukas Exp $

package org.graffiti.plugin.editcomponent;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.Displayable;

/**
 * Displays a combo box to let the user choose from several possibilities.
 * 
 * @version $Revision: 1.1 $
 */
public class ComboBoxEditComponent
					extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================
	
	/** The combobox component used. */
	protected JComboBox comboBox;
	
	/** Text that is displayed in the combo box. */
	protected Object[] comboText;
	
	/** The value that corresponds to the text specified in comboText. */
	protected Object[] comboValue;
	
	protected JComponent searchComponent;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new ComboBoxEditComponent object.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public ComboBoxEditComponent(Displayable disp) {
		super(disp);
		
		searchComponent = getSearchComponent();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>ValueEditComponent</code>'s <code>JComponent</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	public JComponent getComponent() {
		comboBox.setOpaque(false);
		if (!(getDisplayable() instanceof Attribute))
			return comboBox;
		else
			return TableLayout.getSplit(comboBox, searchComponent, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
	}
	
	/**
	 * Sets the current value of the <code>Attribute</code> in the
	 * corresponding <code>JComponent</code>.
	 */
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
				if (value.equals(comboValue[i])) {
					this.comboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		searchComponent.setEnabled(!showEmpty);
	}
	
	private JComponent getSearchComponent() {
		final JButton s = new JButton("Select");
		s.setOpaque(false);
		s.setToolTipText("Click to select all graph elements with the same attribute value");
		s.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object disp = comboBox.getSelectedItem();
				Object search = null;
				for (int i = 0; i < comboText.length; i++) {
					Object d = comboText[i];
					if (d == disp) {
						search = comboValue[i];
						break;
					}
				}
				
				if (search == null) {
					MainFrame.showMessage("Invalid value, nothing to search for!", MessageType.INFO);
					return;
				}
				
				if (!(getDisplayable() instanceof Attribute)) {
					s.setEnabled(false);
					ErrorMsg.addErrorMessage("Internal error, can't perform attribute value search");
					return;
				}
				Attribute attr = (Attribute) getDisplayable();
				
				String path = attr.getPath();
				if (path.indexOf(".") >= 0)
					path = path.substring(0, path.lastIndexOf("."));
				String attributeName = attr.getId();
				
				boolean isShapeSearch = false;
				if (attributeName.equals("shape")) {
					isShapeSearch = true;
					if (search instanceof String)
						search = AttributeHelper.getShapeClassFromShapeName((String) search);
				}
				
				Collection<GraphElement> select = new ArrayList<GraphElement>();
				
				for (GraphElement ge : MainFrame.getInstance().getActiveEditorSession().getGraph().getGraphElements()) {
					Object val = AttributeHelper.getAttributeValue(ge, path, attributeName, null, "");
					if (val == null)
						continue;
					if (val.equals(search))
						select.add(ge);
					else {
						if (isShapeSearch && val != null && val instanceof String) {
							val = AttributeHelper.getShapeClassFromShapeName((String) val);
							if (val != null && val.equals(search))
								select.add(ge);
						}
					}
				}
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection().addAll(select);
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
				
				MainFrame.showMessage("Added " + select.size() + " elements to selection.", MessageType.INFO);
			}
		});
		return s;
	}
	
	/**
	 * Sets the value of the displayable specified in the <code>JComponent</code>. Probably not usefull or overwritten by
	 * subclasses.
	 */
	public void setValue() {
		if (this.comboBox.getSelectedItem().equals(EMPTY_STRING) ||
							(displayable.getValue() != null && this.displayable.getValue().equals(this.comboBox.getSelectedItem()))) {
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
