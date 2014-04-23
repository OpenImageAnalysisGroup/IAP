/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_settings;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class LabelFontAttributeEditor
					extends AbstractValueEditComponent {
	
	protected JComboBox jFontSelection;
	private static final String[] fontnames =
						GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	private static final int defaultSize = (int) (new JLabel("").getFont().getSize() * 1.8);
	private static final int defaultStyle = new JLabel("").getFont().getStyle();
	
	public LabelFontAttributeEditor(final Displayable disp) {
		super(disp);
		jFontSelection = new JComboBox(getFontLabels(fontnames));
		jFontSelection.setOpaque(false);
		jFontSelection.setMinimumSize(new Dimension(10, 5));
		jFontSelection.setPreferredSize(new Dimension(10, jFontSelection.getPreferredSize().height));
		jFontSelection.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				String sel = (String) value;
				if (sel.equalsIgnoreCase(EMPTY_STRING)) {
					return new JLabel("~");
				} else {
					JLabel result = new JLabel(sel);
					result.setFont(new Font(sel, defaultStyle, defaultSize));
					if (cellHasFocus)
						result.setBackground(Color.LIGHT_GRAY);
					if (isSelected)
						result.setBackground(new Color(220, 220, 255));
					return result;
				}
			}
		});
	}
	
	private String[] getFontLabels(String[] fonts) {
		ArrayList<String> result = new ArrayList<String>();
		result.add("~");
		for (String font : fonts) {
			result.add(font);
		}
		return result.toArray(new String[] {});
	}
	
	public JComponent getComponent() {
		return jFontSelection;
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			jFontSelection.setSelectedIndex(0);
		} else {
			String sel = ((LabelFontAttribute) displayable).getString();
			for (int i = 0; i < fontnames.length; i++) {
				if (fontnames[i].equalsIgnoreCase(sel)) {
					jFontSelection.setSelectedIndex(i + 1);
					break;
				}
			}
		}
	}
	
	public void setValue() {
		String selected = (String) jFontSelection.getSelectedItem();
		if (!selected.equalsIgnoreCase(EMPTY_STRING))
			((LabelFontAttribute) displayable).setString(selected);
	}
}
