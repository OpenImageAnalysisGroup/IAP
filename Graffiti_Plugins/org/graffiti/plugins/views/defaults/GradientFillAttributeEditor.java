package org.graffiti.plugins.views.defaults;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;

import org.graffiti.attributes.Attribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Edge;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

public class GradientFillAttributeEditor extends AbstractValueEditComponent {
	
	private JComboBox combo;
	private JCheckBox chkbx;
	private JSpinner spinner;
	private JPanel pan;
	private JComponent emptylabel;
	
	public GradientFillAttributeEditor(Displayable disp) {
		super(disp);
		combo = new JComboBox(NodeGradientModes.values());
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pan.removeAll();
				if (combo.getSelectedItem() instanceof String)
					pan.add(TableLayout.getSplit(combo, emptylabel, TableLayout.PREFERRED, TableLayout.FILL), "0,0");
				else
					setGuiFromMode((NodeGradientModes) combo.getSelectedItem());
				pan.validate();
				pan.repaint();
			}
		});
		combo.setRenderer(new NodeGradientRenderer());
		spinner = new JSpinner(new SpinnerNumberModel(0d, 0d, 5d, 1d));
		chkbx = new JCheckBox();
		chkbx.setOpaque(true);
		chkbx.setBackground(Color.white);
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
		
		pan.removeAll();
		double value = (Double) ((Attribute) getDisplayable()).getValue();
		
		if (((Attribute) getDisplayable()).getAttributable() instanceof Edge) {
			pan.add(chkbx, "0,0");
			chkbx.setSelected(value > 0.00001d || value < -0.00001); // !=0 :-)
		} else {
			
			if (showEmpty) {
				combo.addItem(EMPTY_STRING);
				combo.setSelectedItem(EMPTY_STRING);
				pan.add(TableLayout.getSplit(combo, emptylabel, TableLayout.PREFERRED, TableLayout.FILL), "0,0");
				return;
			}
			
			setGuiFromMode(NodeGradientModes.getMode(value));
		}
		pan.validate();
		pan.repaint();
	}
	
	private void setGuiFromMode(NodeGradientModes mode) {
		if (mode == NodeGradientModes.NONE)
			pan.add(TableLayout.getSplit(combo, emptylabel, TableLayout.PREFERRED, TableLayout.FILL), "0,0");
		else {
			double value = (Double) ((Attribute) getDisplayable()).getValue();
			pan.add(TableLayout.getSplit(combo, spinner, TableLayout.PREFERRED, TableLayout.FILL), "0,0");
			double newvalue;
			if (NodeGradientModes.getMode(value) == mode)
				newvalue = getGuiValue(mode, value);
			else
				newvalue = 30;
			spinner.setModel(new SpinnerNumberModel(newvalue, 0, mode == NodeGradientModes.VERTICAL_THRESHOLD ? 100d : 300d, 5d));
		}
		combo.setSelectedItem(mode);
	}
	
	public void setValue() {
		
		if (((Attribute) getDisplayable()).getAttributable() instanceof Edge) {
			((Attribute) getDisplayable()).setValue(chkbx.isSelected() ? 0.1d : 0d);
		} else {
			
			Object selitem = combo.getSelectedItem();
			if (selitem.equals(EMPTY_STRING))
				return;
			double value = getAttributeValue((NodeGradientModes) selitem, ((Double) spinner.getValue()));
			((Attribute) getDisplayable()).setValue(value);
		}
	}
	
	private double getGuiValue(NodeGradientModes mode, double value) {
		switch (mode) {
			case RADIAL:
				return value * 100;
			case VERTICAL_THRESHOLD:
				return (-value) * 100;
			case VERTICAL:
				return (-value - 1) * 100;
			case NONE:
				return 0d;
		}
		return Double.NEGATIVE_INFINITY;
	}
	
	private double getAttributeValue(NodeGradientModes mode, double value) {
		switch (mode) {
			case RADIAL:
				return value / 100d;
			case VERTICAL_THRESHOLD:
				return -value / 100d;
			case VERTICAL:
				return -value / 100d - 1;
			case NONE:
				return 0d;
		}
		return Double.NEGATIVE_INFINITY;
	}
	
	public enum NodeGradientModes {
		VERTICAL_THRESHOLD("threshold"), VERTICAL("vertical"), RADIAL("radial"), NONE("empty");
		
		private ImageIcon icon;
		
		private NodeGradientModes(String iconname) {
			if (iconname != null)
				this.icon = new ImageIcon(GravistoService.getResource(GradientFillAttributeEditor.class, "editorimages/" + iconname + ".png"));
		}
		
		public static NodeGradientModes getMode(double value) {
			if (value < -1)
				return VERTICAL;
			if (value >= -1 && value < 0)
				return VERTICAL_THRESHOLD;
			if (value > 0)
				return RADIAL;
			if (value == 0)
				return NONE;
			return null;
		}
		
		public ImageIcon getIcon() {
			return icon;
		}
		
	}
	
	public class NodeGradientRenderer extends JLabel implements ListCellRenderer {
		
		private static final long serialVersionUID = 1L;
		
		public NodeGradientRenderer() {
			setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof String) {
				setText("~");
				setIcon(null);
			} else {
				setIcon(((NodeGradientModes) value).getIcon());
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
