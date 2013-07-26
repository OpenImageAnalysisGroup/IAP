/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fast_view;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.TabStatistics;

public class MyColorButton extends JButton {
	
	private static final long serialVersionUID = 6188138418568271704L;
	
	private Color currentColor;
	
	public MyColorButton(String label, Color initColor) {
		super(label);
		currentColor = initColor;
		
		setBorder(BorderFactory.createLineBorder(initColor, 3));
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = TabStatistics.getChoosenColor(currentColor);
				if (c != null) {
					currentColor = c;
					((JButton) e.getSource()).setBorder(BorderFactory.createLineBorder(c, 3));
				}
			}
		});
	}
	
	public Paint getSelectedColor() {
		return currentColor;
	}
}
