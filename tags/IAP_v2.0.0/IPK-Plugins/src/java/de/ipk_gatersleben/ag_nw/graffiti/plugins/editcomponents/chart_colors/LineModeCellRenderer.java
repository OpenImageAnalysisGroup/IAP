/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 10.02.2008 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.graffiti.plugin.editcomponent.ValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2008 IPK-Gatersleben
 */
public class LineModeCellRenderer implements ListCellRenderer {
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean isSelected, boolean cellHasFocus) {
		
		JLabel result = new JLabel();
		
		if (value == null)
			result.setText(ValueEditComponent.EMPTY_STRING);
		
		if (isSelected)
			result.setBackground(list.getSelectionBackground());
		else {
			if (cellHasFocus)
				result.setBackground(list.getSelectionBackground().brighter().brighter());
			else
				result.setBackground(list.getBackground());
		}
		
		if (value == null)
			return result;
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/') + "/images";
		ImageIcon icon = null;
		
		LineModeSetting lms = (LineModeSetting) value;
		try {
			icon = new ImageIcon(cl.getResource(path + "/line_" + (int) lms.a + "_" + (int) lms.b + ".png"));
			result.setText(null);
		} catch (Exception e) {
			System.out.println("A/B: " + (int) lms.a + "/" + (int) lms.b);
			result.setText("A/B: " + (int) lms.a + "/" + (int) lms.b);
		}
		
		if (icon != null)
			result.setIcon(icon);
		
		return result;
	}
	
}
