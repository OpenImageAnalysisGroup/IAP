package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.graffiti.graph.GraphElement;

public interface ChartComponent {
	
	public String getName();
	
	public String getShortDescription();
	
	public ImageIcon getIcon();
	
	public JComponent getChart(GraphElement ge);
	
	public String getComboboxText();
	
}