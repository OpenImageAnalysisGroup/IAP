/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 10.02.2008 by Christian Klukas
 */
package org.graffiti.plugins.editcomponents.defaults;

import java.awt.Component;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Christian Klukas
 *         (c) 2008 IPK-Gatersleben
 */
public class NodeShapeCellRenderer implements ListCellRenderer {
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean isSelected, boolean cellHasFocus) {
		
		JLabel result = new JLabel();
		
		result.setText((String) value);
		
		if (isSelected)
			result.setBackground(list.getSelectionBackground());
		else {
			if (cellHasFocus)
				result.setBackground(list.getSelectionBackground().brighter().brighter());
			else
				result.setBackground(list.getBackground());
		}
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/') + "/nodeshapes";
		ImageIcon icon = null;
		URL u = cl.getResource(path + "/" + value + ".png");
		if (u != null)
			icon = new ImageIcon(u);
		
		if (icon != null)
			result.setIcon(icon);
		
		return result;
	}
	
}
