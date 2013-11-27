/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 11.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.xml_data_tree_table_model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class MySpecialValueCellRenderer extends JLabel implements TableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	private static Font normFont = null;
	private static Font propFont = null;
	
	private static int numberTab = 4;
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table,
						Object value,
						boolean isSelected,
						boolean hasFocus,
						int row,
						int column) {
		
		SpecialTableValue sv = (SpecialTableValue) value;
		
		setOpaque(true);
		
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			if (sv.isWhiteCell) {
				setBackground(Color.WHITE);// ((JTreeTable)table).tree.getBackground());
				setForeground(((JTreeTable) table).tree.getForeground());
			} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
		}
		
		if (normFont == null) {
			normFont = getFont();
			propFont = new Font("Monospaced", Font.PLAIN, normFont.getSize());
		}
		
		if (sv.shiftRight) {
			setHorizontalAlignment(SwingConstants.RIGHT);
		} else {
			setHorizontalAlignment(SwingConstants.LEFT);
		}
		
		if (!sv.getIsDouble()) {
			setFont(normFont);
			setText(value.toString());
		} else {
			setFont(propFont);
			setText(getSpacedNumberText(value.toString()));
		}
		
		if (hasFocus) {
			// setBorder(BorderFactory.c(Color.white, Color.black));
		} else {
			// setBorder(null);
		}
		
		return this;
	}
	
	private String getSpacedNumberText(String text) {
		int pointPosition = text.indexOf(".");
		if (pointPosition >= 0) {
			int neededSpace = numberTab - pointPosition % numberTab;
			for (int i = 0; i < neededSpace; i++)
				text = " " + text;
			return text;
		} else
			return text;
	}
}
