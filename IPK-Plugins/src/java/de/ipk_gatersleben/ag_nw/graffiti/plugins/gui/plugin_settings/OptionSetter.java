/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 06.07.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.graffiti.options.OptionPane;

/**
 * @author klukas
 *         To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionSetter extends JComponent {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param component
	 */
	public OptionSetter(final OptionPane optionPane) {
		double border = 5;
		double[][] size =
		{ { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, 5, TableLayoutConstants.PREFERRED, border }
		}; // Rows
		
		setLayout(new TableLayout(size));
		final JComponent optionPaneDialog = optionPane.getOptionDialogComponent();
		add(optionPaneDialog, "1,1");
		
		JPanel buttonGroup = new JPanel();
		
		double border2 = 0;
		double[][] size2 =
		{ { border2, TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, border2 }, // Columns
				{ border2, TableLayoutConstants.FILL, border2 } }; // Rows
		buttonGroup.setLayout(new TableLayout(size2));
		
		JButton applyButton = new JButton("Apply");
		JButton resetButton = new JButton("Reset");
		
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				optionPane.save(optionPaneDialog);
			}
		});
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				optionPane.init(optionPaneDialog);
			}
		});
		
		buttonGroup.add(applyButton, "1,1");
		buttonGroup.add(resetButton, "3,1");
		buttonGroup.validate();
		
		add(buttonGroup, "1,3");
		validate();
	}
	
}
