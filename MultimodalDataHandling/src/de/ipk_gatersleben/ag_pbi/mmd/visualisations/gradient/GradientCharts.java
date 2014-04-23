/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.visualisations.gradient;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.graffiti.editor.GravistoService;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.ChartComponent;

public enum GradientCharts implements ChartComponent {
	
	LINEGRADIENT("linegradient", "Line Gradient", "linegradient"), BARGRADIENT("bargradient", "Bar Gradient", "bargradient");
	
	private String name;
	private String desc;
	private String iconname;
	
	GradientCharts(String name, String desc, String iconname) {
		this.name = name;
		this.desc = desc;
		this.iconname = iconname;
	}
	
	public ImageIcon getIcon() {
		return new ImageIcon(GravistoService.getResource(getClass(), iconname, "png"));
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getShortDescription() {
		return desc;
	}
	
	public JComponent getChart(GraphElement ge) {
		return new GradientDataChartComponent(ge, this);
	}
	
	public String getComboboxText() {
		switch (this) {
			case LINEGRADIENT:
				return "<html>" + getShortDescription() + "<br><small> - display 1D spatial data as line chart";
			case BARGRADIENT:
				return "<html>" + getShortDescription() + "<br><small> - display 1D spatial data as bar chart";
		}
		return null;
	}
	
	public static GradientCharts getChartFromString(String type) {
		for (GradientCharts c : values())
			if (c.getName().equals(type))
				return c;
		return null;
	}
	
}
