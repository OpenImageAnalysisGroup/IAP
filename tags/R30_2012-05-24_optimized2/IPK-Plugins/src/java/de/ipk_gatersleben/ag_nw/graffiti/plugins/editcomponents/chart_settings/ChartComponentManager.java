package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import java.util.LinkedHashSet;

import javax.swing.JComponent;

import org.graffiti.graph.GraphElement;

public class ChartComponentManager {
	
	private static ChartComponentManager instance;
	
	public static ChartComponentManager getInstance() {
		if (instance == null)
			instance = new ChartComponentManager();
		return instance;
		
	}
	
	private LinkedHashSet<ChartComponent> chartComponents;
	
	private ChartComponentManager() {
		super();
		chartComponents = new LinkedHashSet<ChartComponent>();
	}
	
	public void registerChartComponent(ChartComponent chartComponent) {
		chartComponents.add(chartComponent);
	}
	
	public void removeChartComponent(ChartComponent chartComponent) {
		chartComponents.remove(chartComponent.getName());
	}
	
	public JComponent getChartComponent(String ct, GraphElement ge) {
		for (ChartComponent c : chartComponents)
			if (c.getName().equals(ct))
				return c.getChart(ge);
		return null;
	}
	
	public LinkedHashSet<ChartComponent> getChartComponents() {
		return chartComponents;
	}
	
}
