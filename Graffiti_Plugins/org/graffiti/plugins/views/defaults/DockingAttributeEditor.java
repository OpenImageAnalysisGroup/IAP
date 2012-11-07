package org.graffiti.plugins.views.defaults;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;

import org.graffiti.attributes.Attribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

public class DockingAttributeEditor extends AbstractValueEditComponent {
	
	private final JComboBox combo;
	private final JSpinner spinnerx, spinnery;
	private final JTextField textcustom;
	private final JPanel pan;
	private final JLabel emptylabel;
	
	public DockingAttributeEditor(Displayable disp) {
		super(disp);
		combo = new JComboBox(DockingModes.values());
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pan.removeAll();
				if (combo.getSelectedItem().equals(EMPTY_STRING))
					pan.add(combo, "0,0");
				else {
					setGuiFromMode((DockingModes) combo.getSelectedItem());
				}
				pan.validate();
				pan.repaint();
			}
		});
		combo.setRenderer(new EdgeDockingRenderer());
		spinnerx = new JSpinner(new SpinnerNumberModel(1d, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1d));
		spinnerx.setToolTipText("Specify x-value");
		spinnery = new JSpinner(new SpinnerNumberModel(1d, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1d));
		spinnery.setToolTipText("Specify y-value");
		textcustom = new JTextField();
		pan = new JPanel();
		pan.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		emptylabel = new JLabel();
		emptylabel.setOpaque(true);
		emptylabel.setBackground(Color.white);
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
		String value = (String) ((Attribute) getDisplayable()).getValue();
		
		pan.removeAll();
		
		setGuiFromMode(DockingModes.getMode(value));
		pan.validate();
		pan.repaint();
	}
	
	private void setGuiFromMode(DockingModes mode) {
		String value = (String) ((Attribute) getDisplayable()).getValue();
		
		if (mode == null)
			mode = DockingModes.CUSTOM;
		
		switch (mode) {
			case NONE:
			case MIDDLE:
				pan.add(combo, "0,0");
				break;
			case INSIDE:
				spinnerx.setModel(new SpinnerNumberModel(getBorderXValue(value), -1d, 1d, 0.1d));
				spinnery.setModel(new SpinnerNumberModel(getBorderYValue(value), -1d, 1d, 0.1d));
				pan.add(TableLayout.get3Split(combo, spinnerx, spinnery, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.FILL), "0,0");
				break;
			case OUTSIDE:
				spinnerx.setModel(new SpinnerNumberModel(getOutlineXValue(value), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 4d));
				spinnery.setModel(new SpinnerNumberModel(getOutlineYValue(value), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 4d));
				pan.add(TableLayout.get3Split(combo, spinnerx, spinnery, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.FILL), "0,0");
				break;
			case CUSTOM:
				textcustom.setText(value);
				pan.add(TableLayout.getSplit(combo, textcustom, TableLayout.PREFERRED, TableLayout.FILL), "0,0");
				break;
		}
		combo.setSelectedItem(mode);
	}
	
	private Number getBorderXValue(String value) {
		double val = 0.5d;
		if (value.indexOf(";") < 0)
			return val;
		try {
			val = DockingModes.getXValue(value);
		} catch (NumberFormatException e) { // empty
		}
		return val > 1 ? 0.5d : val;
	}
	
	private Number getBorderYValue(String value) {
		double val = 0.5d;
		if (value.indexOf(";") < 0)
			return val;
		try {
			val = DockingModes.getYValue(value);
		} catch (NumberFormatException e) { // empty
		}
		return val > 1 ? 0.5d : val;
	}
	
	private Number getOutlineXValue(String value) {
		double val = 2.0d;
		if (value.indexOf(";") < 0)
			return val;
		try {
			val = DockingModes.getXValue(value);
		} catch (NumberFormatException e) { // empty
		}
		return val;// <= 1 ? 2.0d : val;
	}
	
	private Number getOutlineYValue(String value) {
		double val = 2.0d;
		if (value.indexOf(";") < 0)
			return val;
		try {
			val = DockingModes.getYValue(value);
		} catch (NumberFormatException e) { // empty
		}
		return val;// <= 1 ? 2.0d : val;
	}
	
	public void setValue() {
		Object selitem = combo.getSelectedItem();
		if (selitem.equals(EMPTY_STRING))
			return;
		
		String value = "";
		switch ((DockingModes) selitem) {
			case NONE:
				break;
			case MIDDLE:
				value = "0;0";
				break;
			case INSIDE:
			case OUTSIDE:
				value = spinnerx.getValue() + ";" + spinnery.getValue();
				break;
			case CUSTOM:
				value = textcustom.getText();
				break;
		}
		((Attribute) getDisplayable()).setValue(value);
		
	}
	
	public enum DockingModes {
		NONE("dynamic"), MIDDLE("middle"), INSIDE("inside"), OUTSIDE("outside"), CUSTOM("custom");
		
		private ImageIcon icon;
		private String name;
		
		private DockingModes(String iconname) {
			this.name = iconname;
			if (!name.equals("custom"))
				this.icon = new ImageIcon(GravistoService.getResource(GradientFillAttributeEditor.class, "editorimages/" + iconname + ".png"));
		}
		
		public static double getYValue(String value) {
			return Double.parseDouble(getYStringValue(value));
		}
		
		public static double getXValue(String value) {
			return Double.parseDouble(getXStringValue(value));
		}
		
		public static String getYStringValue(String value) {
			return value.substring(value.indexOf(";") + ";".length(), value.length());
		}
		
		public static String getXStringValue(String value) {
			return value.substring(0, value.indexOf(";"));
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public ImageIcon getIcon() {
			return icon;
		}
		
		public static DockingModes getMode(String value) {
			if (value.length() <= 0)
				return NONE;
			if (value.equals("0;0") || value.equals("0.0;0.0"))
				return MIDDLE;
			
			if (value.indexOf(";") < 0)
				return null;
			else {
				try {
					double xval = getXValue(value);
					if (xval > 1 || xval < -1)
						return OUTSIDE;
					double yval = getYValue(value);
					if (yval > 1 || yval < -1)
						return OUTSIDE;
				} catch (NumberFormatException e) {
					return CUSTOM;
				}
				return INSIDE;
			}
			
		}
	}
	
	public class EdgeDockingRenderer extends JLabel implements ListCellRenderer {
		
		private static final long serialVersionUID = 1L;
		
		public EdgeDockingRenderer() {
			setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof String) {
				setText("~");
				setIcon(null);
			} else {
				setIcon(((DockingModes) value).getIcon());
				if (value == DockingModes.CUSTOM)
					setText("custom");
				else
					setText(null);
			}
			
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}
	}
	
}
