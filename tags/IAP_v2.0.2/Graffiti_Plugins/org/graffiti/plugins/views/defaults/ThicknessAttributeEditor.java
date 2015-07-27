package org.graffiti.plugins.views.defaults;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

public class ThicknessAttributeEditor extends AbstractValueEditComponent {
	
	private final JComboBox combo;
	private final JSpinner spinner;
	private final JPanel pan;
	
	public ThicknessAttributeEditor(Displayable disp) {
		super(disp);
		combo = new JComboBox(ArrowHeadModes.values()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public Dimension getMinimumSize() {
				Dimension res = super.getMinimumSize();
				res.setSize(20, res.getHeight());
				return res;
			}
			
			@Override
			public Dimension getPreferredSize() {
				Dimension res = super.getPreferredSize();
				res.setSize(20, res.getHeight());
				return res;
			}
		};
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pan.removeAll();
				if (combo.getSelectedItem() instanceof String)
					pan.add(combo, "0,0");
				else {
					setGuiFromMode((ArrowHeadModes) combo.getSelectedItem());
				}
				pan.validate();
				pan.repaint();
			}
		});
		spinner = new JSpinner(new SpinnerNumberModel(10d, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.5d));
		
		pan = new JPanel();
		pan.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
	}
	
	public JComponent getComponent() {
		return pan;
	}
	
	public void setEditFieldValue() {
		
		if (showEmpty) {
			combo.addItem(EMPTY_STRING);
			combo.setSelectedItem(EMPTY_STRING);
			pan.add(combo, "0,0");
			return;
		}
		double value = (Double) ((Attribute) getDisplayable()).getValue();
		
		pan.removeAll();
		
		setGuiFromMode(ArrowHeadModes.getMode(value));
		pan.validate();
		pan.repaint();
	}
	
	private void setGuiFromMode(ArrowHeadModes mode) {
		if (mode == ArrowHeadModes.ABSOLUTE) {
			double value = (Double) ((Attribute) getDisplayable()).getValue();
			pan.add(TableLayout.getSplit(combo, spinner, TableLayout.FILL, TableLayout.FILL), "0,0");
			if (Math.abs(value - 1d) < 0.0001d)
				spinner.setValue(AttributeHelper.getFrameThickNess((GraphElement)
									((Attribute) getDisplayable()).getAttributable()) * 3.5d);
			else
				spinner.setValue(value);
		} else {
			pan.add(combo, "0,0");
		}
		combo.setSelectedItem(mode);
	}
	
	public void setValue() {
		Object selitem = combo.getSelectedItem();
		if (selitem.equals(EMPTY_STRING))
			return;
		if (selitem == ArrowHeadModes.RELATIVE)
			((Attribute) getDisplayable()).setValue(1d);
		else
			((Attribute) getDisplayable()).setValue(spinner.getValue());
	}
	
	public enum ArrowHeadModes {
		ABSOLUTE("Absolute in Pixel"), RELATIVE("Relative to Thickness");
		
		private String name;
		
		private ArrowHeadModes(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static ArrowHeadModes getMode(double value) {
			if (Math.abs((value - 1d)) < 0.0001d)
				return RELATIVE;
			else
				return ABSOLUTE;
		}
		
	}
	
}
