/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.ErrorMsg;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class MyDiagramPlacementSettingCellRenderer implements ListCellRenderer {
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean isSelected, boolean cellHasFocus) {
		
		JLabel result = new JLabel();
		
		result.setText(value + "");
		
		if (isSelected)
			result.setBackground(list.getSelectionBackground());
		else {
			if (cellHasFocus)
				result.setBackground(list.getSelectionBackground().brighter().brighter());
			else
				result.setBackground(list.getBackground());
		}
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = null;
		if (value.equals(-2)) {
			icon = new ImageIcon(cl.getResource(path + "/grid/onerow.png"));
			result.setText("One row, flexible no. of columns");
		}
		if (value.equals(-1)) {
			icon = new ImageIcon(cl.getResource(path + "/grid/quadratic.png"));
			result.setText("Quadratic grid");
		}
		if (value.equals(1)) {
			icon = new ImageIcon(cl.getResource(path + "/grid/onecolumn.png"));
			result.setText("One column, flexible no. of rows");
		}
		if (value.equals(2)) {
			icon = new ImageIcon(cl.getResource(path + "/grid/twocolumns.png"));
			result.setText("Two columns, flexible no. of rows");
		}
		if (value.equals(3)) {
			icon = new ImageIcon(cl.getResource(path + "/grid/threecolumns.png"));
			result.setText("Three columns, flexible no. of rows");
		}
		if (value.equals(4)) {
			icon = new ImageIcon(cl.getResource(path + "/grid/fourcolumns.png"));
			result.setText("Four columns, flexible no. of rows");
		}
		try {
			if (value instanceof String)
				value = Integer.parseInt(((String) value).trim());
			if (value instanceof Integer && (Integer) value > 4) {
				result.setText(value + " columns, flexible no. of rows");
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		if (icon != null)
			result.setIcon(icon);
		
		return result;
	}
	
}
