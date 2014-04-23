/*******************************************************************************
 * Copyright (c) 2003-2008 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 10.02.2008 by Christian Klukas
 */
package org.graffiti.plugins.editcomponents.defaults;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Christian Klukas
 *         (c) 2008 IPK-Gatersleben
 */
public class ArrowShapeCellRenderer implements ListCellRenderer {
	
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
		String path = this.getClass().getPackage().getName().replace('.', '/') + "/images";
		ImageIcon icon = null;
		if (value.equals("-->")) {
			icon = new ImageIcon(cl.getResource(path + "/StandardArrowShape.png"));
			result.setText("");
		}
		if (value.equals("--l>")) {
			icon = new ImageIcon(cl.getResource(path + "/StandardArrowShapeLeft.png"));
			result.setText("");
		}
		if (value.equals("--r>")) {
			icon = new ImageIcon(cl.getResource(path + "/StandardArrowShapeRight.png"));
			result.setText("");
		}
		if (value.equals("-+>")) {
			icon = new ImageIcon(cl.getResource(path + "/ThinStandardArrowShape.png"));
			result.setText("");
		}
		if (value.equals("--o")) {
			icon = new ImageIcon(cl.getResource(path + "/CircleArrowShape.png"));
			result.setText("");
		}
		if (value.equals("--(+)")) {
			icon = new ImageIcon(cl.getResource(path + "/ThinCircleArrowShape.png"));
			result.setText("");
		}
		if (value.equals("-o|")) {
			icon = new ImageIcon(cl.getResource(path + "/CircleConnectArrowShape.png"));
			result.setText("");
		}
		if (value.equals("--<>")) {
			icon = new ImageIcon(cl.getResource(path + "/DiamondArrowShape.png"));
			result.setText("");
		}
		if (value.equals("-<+>")) {
			icon = new ImageIcon(cl.getResource(path + "/ThinDiamondArrowShape.png"));
			result.setText("");
		}
		if (value.equals("--|")) {
			icon = new ImageIcon(cl.getResource(path + "/InhibitorArrowShape.png"));
			result.setText("");
		}
		if (value.equals("-||")) {
			icon = new ImageIcon(cl.getResource(path + "/AbsoluteInhibitorArrowShape.png"));
			result.setText("");
		}
		if (value.equals("->>")) {
			icon = new ImageIcon(cl.getResource(path + "/AbsoluteStimulationArrowShape.png"));
			result.setText("");
		}
		if (value.equals("-|>")) {
			icon = new ImageIcon(cl.getResource(path + "/ThinTriggerArrowShape.png"));
			result.setText("");
		}
		if (value.equals("--/")) {
			icon = new ImageIcon(cl.getResource(path + "/AssignmentArrowShape.png"));
			result.setText("");
		}
		if (value.equals("---")) {
			icon = new ImageIcon(cl.getResource(path + "/NoArrowShape.png"));
			result.setText("");
		}
		
		if (icon != null)
			result.setIcon(icon);
		
		return result;
	}
	
}
