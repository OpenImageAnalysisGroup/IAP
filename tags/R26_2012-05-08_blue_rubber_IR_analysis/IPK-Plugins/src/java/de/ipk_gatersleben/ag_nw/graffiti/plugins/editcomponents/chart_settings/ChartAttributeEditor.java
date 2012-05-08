/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ChartAttributeEditor
					extends AbstractValueEditComponent {
	protected JComboBox jComboBoxChartType;
	
	public ChartAttributeEditor(Displayable disp) {
		super(disp);
		
		jComboBoxChartType = new JComboBox() {
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
		jComboBoxChartType.setOpaque(false);
		jComboBoxChartType.setRenderer(new MyChartCellRenderer());
		jComboBoxChartType.addItem(EMPTY_STRING);
		for (ChartComponent c : ChartComponentManager.getInstance().getChartComponents())
			jComboBoxChartType.addItem(c);
		// jComboBoxChartType.addItem(GraffitiCharts.LINE);
		// jComboBoxChartType.addItem(GraffitiCharts.BAR);
		// jComboBoxChartType.addItem(GraffitiCharts.BAR_FLAT);
		// jComboBoxChartType.addItem(GraffitiCharts.PIE);
		// jComboBoxChartType.addItem(GraffitiCharts.PIE3D);
		// jComboBoxChartType.addItem(GraffitiCharts.HEATMAP);
		// // jComboBoxChartType.addItem(IPKnodeComponent.nodeTypeChart_legend_only);
		// jComboBoxChartType.addItem(GraffitiCharts.HIDDEN);
		// jComboBoxChartType.addItem(GraffitiCharts.AUTOMATIC);
		
		// if (disp.getValue().equals(XMLAttribute.nodeTypeChart2D_type1_line))
		jComboBoxChartType.setSelectedItem(disp.getValue());
		// if (disp.getValue().equals(XMLAttribute.nodeTypeChart2D_type2_bar))
		// jComboBoxChartType.setSelectedIndex(2);
		// if (disp.getValue().equals(XMLAttribute.nodeTypeChart2D_type3_bar_flat))
		// jComboBoxChartType.setSelectedIndex(3);
		// if (disp.getValue().equals(XMLAttribute.nodeTypeChart2D_type4_pie))
		// jComboBoxChartType.setSelectedIndex(4);
		// if (disp.getValue().equals(XMLAttribute.nodeTypeChart2D_type5_pie3d))
		// jComboBoxChartType.setSelectedIndex(5);
		// if (disp.getValue().equals(XMLAttribute.nodeTypeChart2D_type6_heatmap))
		// jComboBoxChartType.setSelectedIndex(6);
		// // if (disp.getValue().equals(IPKnodeComponent.nodeTypeChart_legend_only))
		// // jComboBoxChartType.setSelectedIndex(4);
		// if (disp.getValue().equals(XMLAttribute.nodeTypeChart_hide))
		// jComboBoxChartType.setSelectedIndex(7);
		// if (disp.getValue().equals(XMLAttribute.nodeTypeChart_auto))
		// jComboBoxChartType.setSelectedIndex(8);
	}
	
	public JComponent getComponent() {
		jComboBoxChartType.setMinimumSize(new Dimension(0, jComboBoxChartType.getMinimumSize().height));
		return jComboBoxChartType;
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			jComboBoxChartType.setSelectedItem(EMPTY_STRING);
		} else {
			String value = (String) displayable.getValue();
			for (ChartComponent c : ChartComponentManager.getInstance().getChartComponents())
				if (c.getName().equals(value)) {
					jComboBoxChartType.setSelectedItem(c);
					break;
				}
			// if (displayable.getValue().equals(XMLAttribute.nodeTypeChart2D_type1_line))
			// jComboBoxChartType.setSelectedIndex(1);
			// if (displayable.getValue().equals(XMLAttribute.nodeTypeChart2D_type2_bar))
			// jComboBoxChartType.setSelectedIndex(2);
			// if (displayable.getValue().equals(XMLAttribute.nodeTypeChart2D_type3_bar_flat))
			// jComboBoxChartType.setSelectedIndex(3);
			// // if (displayable.getValue().equals(IPKnodeComponent.nodeTypeChart_legend_only))
			// // jComboBoxChartType.setSelectedIndex(4);
			// if (displayable.getValue().equals(XMLAttribute.nodeTypeChart2D_type4_pie))
			// jComboBoxChartType.setSelectedIndex(4);
			// if (displayable.getValue().equals(XMLAttribute.nodeTypeChart2D_type5_pie3d))
			// jComboBoxChartType.setSelectedIndex(5);
			// if (displayable.getValue().equals(XMLAttribute.nodeTypeChart2D_type6_heatmap))
			// jComboBoxChartType.setSelectedIndex(6);
			// if (displayable.getValue().equals(XMLAttribute.nodeTypeChart_hide))
			// jComboBoxChartType.setSelectedIndex(7);
			// if (displayable.getValue().equals(XMLAttribute.nodeTypeChart_auto))
			// jComboBoxChartType.setSelectedIndex(8);
		}
	}
	
	public void setValue() {
		Object text = jComboBoxChartType.getSelectedItem();
		
		// if(!text.toString().equals(EMPTY_STRING) &&
		// !this.displayable.getValue().equals(text))
		// {
		if (text instanceof ChartComponent)
			this.displayable.setValue(((ChartComponent) text).getName());
		// else
		// this.displayable.setValue(text);
		// }
	}
}
