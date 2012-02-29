/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.enhanced_attribute_editors;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.plugin.editcomponent.NumberEditComponent;

/**
 * Represents a gui component, which handles double values. Can be left empty
 * because superclass handles all primitive types.
 * 
 * @see NumberEditComponent
 */
public class EnhDoubleEditComponent
					extends AbstractValueEditComponent
					implements ActionListener {
	Displayable disp;
	
	JComponent viewComp;
	JCheckBox nan;
	JSpinner spinner;
	
	/**
	 * @param disp
	 */
	public EnhDoubleEditComponent(Displayable disp) {
		super(disp);
		this.disp = disp;
		
		viewComp = new JPanel();
		try {
			double border = 0;
			double[][] size =
			{
								{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, border }, // Columns
					{ border, TableLayoutConstants.PREFERRED, border }
			}; // Rows
			viewComp.setLayout(new TableLayout(size));
			
			nan = new JCheckBox("NaN");
			nan.addActionListener(this);
			spinner = new JSpinner();
			SpinnerNumberModel nm;
			if (disp.getValue() == null || Double.isNaN(((Double) disp.getValue()).doubleValue())) {
				if (disp.getValue() == null)
					System.out.println(disp.getName() + ": NULL");
				else
					System.out.println(disp.getName() + ": NaN");
				nm = new SpinnerNumberModel(
									0,
									Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.5);
				nan.setSelected(true);
			} else {
				System.out.println(disp.getName() + ": " + disp.getValue());
				nm = new SpinnerNumberModel(
									((Double) disp.getValue()).doubleValue(),
									Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.5);
				nan.setSelected(false);
			}
			spinner.setModel(nm);
			
			viewComp.add(nan, "1,1");
			viewComp.add(spinner, "2,1");
		} catch (Exception e) {
			double border = 0;
			double[][] size =
			{
								{ border, TableLayoutConstants.FILL, border }, // Columns
					{ border, TableLayoutConstants.PREFERRED, border }
			}; // Rows
			viewComp.setLayout(new TableLayout(size));
			viewComp.add(new JLabel("Error: " + e.getLocalizedMessage()), "1,1");
		}
		viewComp.setMinimumSize(new Dimension(0, 30));
		viewComp.setPreferredSize(new Dimension(70, 30));
		viewComp.setMaximumSize(new Dimension(2000, 30));
		viewComp.validate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.editcomponent.ValueEditComponent#getComponent()
	 */
	public JComponent getComponent() {
		return viewComp;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.editcomponent.ValueEditComponent#setEditFieldValue()
	 */
	public void setEditFieldValue() {
		//
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.editcomponent.ValueEditComponent#setValue()
	 */
	public void setValue() {
		if (disp.getValue() != null) {
			// if (disp.getValue() instanceof Integer) disp.setValue(new Integer(spinner.getValue().toString()));
			// else
			if (disp.getValue() instanceof Double) {
				if (nan.isSelected())
					disp.setValue(new Double(Double.NaN));
				else {
					try {
						disp.setValue(new Double(spinner.getValue().toString()));
					} catch (Exception e) {
						disp.setValue(new Double(Double.NaN));
					}
				}
			} else
				disp.setValue(spinner.getValue());
		} else
			disp.setValue(spinner.getValue());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == nan)
			if (nan.isSelected()) {
				// spinner.setValue(new Double(0));
				// disp.setValue(new Double(Double.NaN));
			}
	}
	
}