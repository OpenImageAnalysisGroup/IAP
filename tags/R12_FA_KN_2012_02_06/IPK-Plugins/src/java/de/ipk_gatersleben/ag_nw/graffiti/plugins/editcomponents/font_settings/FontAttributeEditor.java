/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_settings;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.FontChooser;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class FontAttributeEditor
					extends AbstractValueEditComponent {
	protected JButton jButtonFontAndColor;
	
	public FontAttributeEditor(final Displayable disp) {
		super(disp);
		
		jButtonFontAndColor = new JButton("Font");
		jButtonFontAndColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontChooser fc = FontChooser.showFontChooser(disp);
				Font newFont = fc.getNewFont();
				Color newColor = fc.getNewColor();
				if (newFont != null)
					jButtonFontAndColor.setFont(newFont);
				if (newColor != null)
					jButtonFontAndColor.setForeground(newColor);
			}
		});
		// if (disp.getValue().equals(IPKnodeComponent.nodeTypeChart2D_type1_line))
		// jComboBoxChartType.setSelectedIndex(1);
	}
	
	public JComponent getComponent() {
		return jButtonFontAndColor;
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			jButtonFontAndColor.setFont(FontAttribute.getDefaultFont());
			jButtonFontAndColor.setForeground(FontAttribute.getDefaultColor());
		} else {
			jButtonFontAndColor.setFont(((FontAttribute) displayable).getFont());
			jButtonFontAndColor.setForeground(((FontAttribute) displayable).getColor());
		}
	}
	
	public void setValue() {
		((FontAttribute) displayable).setFont(jButtonFontAndColor.getFont());
		((FontAttribute) displayable).setColor(jButtonFontAndColor.getForeground());
		// String text = jComboBoxChartType.getSelectedItem().toString();
		//
		// if(!text.equals(EMPTY_STRING) &&
		// !this.displayable.getValue().toString().equals(text))
		// {
		// this.displayable.setValue(text);
		// }
	}
}
