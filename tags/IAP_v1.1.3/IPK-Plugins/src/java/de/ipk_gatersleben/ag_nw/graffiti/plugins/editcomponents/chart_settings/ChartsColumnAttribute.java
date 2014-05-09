package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings;

import org.graffiti.attributes.IntegerAttribute;

public class ChartsColumnAttribute extends IntegerAttribute {
	
	public static final String name = "max_charts_in_column";
	
	public ChartsColumnAttribute(String id) {
		super(id);
	}
	
	public ChartsColumnAttribute(String id, int value) {
		super(id, value);
	}
	
	public ChartsColumnAttribute(String id, Integer value) {
		super(id, value);
	}
	
	public ChartsColumnAttribute(int diagramsPerRow) {
		this("max_charts_in_column", diagramsPerRow);
	}
	
}
