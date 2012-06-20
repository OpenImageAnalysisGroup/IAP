package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.graffiti.attributes.Attribute;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

public class ChartsColumnAttributeEditor extends AbstractValueEditComponent {
	
	private final JComboBox combo;
	
	public ChartsColumnAttributeEditor(Displayable disp) {
		super(disp);
		ArrayList<Integer> validOptions = new ArrayList<Integer>();
		for (int i = -2; i < 10; i++)
			if (i != 0)
				validOptions.add(i);
		combo = new JComboBox(validOptions.toArray()) {
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
		combo.setRenderer(new MyDiagramPlacementSettingCellRenderer());
	}
	
	@Override
	public JComponent getComponent() {
		return combo;
	}
	
	@Override
	public void setEditFieldValue() {
		if (showEmpty) {
			combo.addItem(EMPTY_STRING);
			combo.setSelectedItem(EMPTY_STRING);
			return;
		}
		combo.setSelectedItem(((Attribute) getDisplayable()).getValue());
	}
	
	@Override
	public void setValue() {
		Object selitem = combo.getSelectedItem();
		if (selitem.equals(EMPTY_STRING))
			return;
		((Attribute) getDisplayable()).setValue(selitem);
	}
	
}
