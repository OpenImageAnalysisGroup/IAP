package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.graffiti.editor.GravistoService;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.XmlDataChartComponent;

public enum GraffitiCharts implements ChartComponent {
	
	LINE("chart2d_type1", "Line Chart", "line"),
	BAR("chart2d_type2", "Bar Chart", "bar"),
	BAR_FLAT("chart2d_type3", "Bar Chart (flat)", "bar_flat"),
	PIE("chart2d_type4", "Pie Chart", "piechart"),
	PIE3D("chart2d_type5", "Pie Chart (skewed)", "piechart3d"),
	HEATMAP("heatmap", null, "heatmap"),
	HIDDEN("hidden", "Hidden", null),
	AUTOMATIC("auto", "Automatic", "automatic"),
	LEGEND_ONLY("legend", "Legend", null);
	
	private String name;
	private String desc;
	private String iconname;
	
	GraffitiCharts(String name, String desc, String iconname) {
		this.name = name;
		this.desc = desc;
		this.iconname = iconname;
	}
	
	@Override
	public JComponent getChart(GraphElement ge) {
		return new XmlDataChartComponent(name, ge.getGraph(), ge);
	}
	
	@Override
	public ImageIcon getIcon() {
		if (iconname == null)
			return new ImageIcon(new BufferedImage(72, 35, BufferedImage.TYPE_INT_ARGB), "");// empty image
		else
			return new ImageIcon(GravistoService.getResource(getClass(), iconname, "png"));
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public String getShortDescription() {
		return desc;
	}
	
	@Override
	public String getComboboxText() {
		switch (this) {
			case LINE:
				return "<html>" + getShortDescription() + "<br><small>- display time series";
			case BAR:
				return "<html>" + getShortDescription() + "<br>" + "<small>- display conditions/lines";
			case BAR_FLAT:
				return "<html>" + getShortDescription() + "<br>" + "<small>- display cond./lines (std. dev.)";
			case LEGEND_ONLY:
				return getShortDescription();
			case PIE:
				return "<html>" + getShortDescription() + "<br>" + "<small>- display ratios";
			case PIE3D:
				return "<html>" + getShortDescription() + "<br>" + "<small>- display ratios";
			case HEATMAP:
				return "<html>Color-code<br><small>- display expression data";
			case HIDDEN:
				return GraffitiCharts.HIDDEN.getShortDescription();
			case AUTOMATIC:
				return GraffitiCharts.AUTOMATIC.getShortDescription();
		}
		return null;
	}
	
	public static GraffitiCharts getChartStyleFromString(String type) {
		for (GraffitiCharts c : values())
			if (c.getName().equals(type))
				return c;
		
		// if (type.equals(nodeTypeChart_auto))
		// return -1;
		// if (type.equals(nodeTypeChart_hide))
		// return 0;
		// if (type.equals(nodeTypeChart2D_type1_line))
		// return 1;
		// if (type.equals(nodeTypeChart2D_type2_bar))
		// return 2;
		// if (type.equals(nodeTypeChart2D_type3_bar_flat))
		// return 3;
		// if (type.equals(nodeTypeChart2D_type4_pie))
		// return 4;
		// if (type.equals(nodeTypeChart2D_type5_pie3d))
		// return 5;
		// if (type.equals(nodeTypeChart2D_type6_heatmap))
		// return 6;
		// return 0;
		return null;
	}
	
	public static boolean isNoAutoOrHide(String diagramStyle) {
		GraffitiCharts c = getChartStyleFromString(diagramStyle);
		return c != AUTOMATIC && c != HIDDEN;
	}
	
	public static boolean isNotHeatmap(String diagramStyle) {
		return getChartStyleFromString(diagramStyle) != HEATMAP;
	}
	
}
