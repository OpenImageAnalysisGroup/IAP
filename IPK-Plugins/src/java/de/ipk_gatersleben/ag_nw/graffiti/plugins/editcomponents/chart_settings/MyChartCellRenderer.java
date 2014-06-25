/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class MyChartCellRenderer implements ListCellRenderer {
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean isSelected, boolean cellHasFocus) {
		
		JLabel result = new JLabel();
		
		result.setText(value.toString());
		
		if (isSelected)
			result.setBackground(list.getSelectionBackground());
		else {
			if (cellHasFocus)
				result.setBackground(list.getSelectionBackground().brighter().brighter());
			else
				result.setBackground(list.getBackground());
		}
		if (value instanceof ChartComponent) {
			result.setIcon(((ChartComponent) value).getIcon());
			result.setText(((ChartComponent) value).getComboboxText());
		}
		return result;
	}
	
}
