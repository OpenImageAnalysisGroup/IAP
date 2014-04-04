package de.ipk.ag_ba.image.operations.blocks;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

public class BlockResultValue {
	
	private final String name;
	private final String unit;
	private final Double value;
	private Object object;
	private Double position;
	private NumericMeasurement3D binary;
	
	public BlockResultValue(String name, String unit, Double value) {
		this.name = name;
		this.unit = unit;
		this.value = value;
	}
	
	public BlockResultValue(String name, NumericMeasurement3D binary) {
		this.name = name;
		this.binary = binary;
		this.unit = null;
		this.value = null;
	}
	
	public BlockResultValue(String name, Object object) {
		this.name = name;
		this.binary = null;
		this.unit = null;
		this.value = null;
		this.object = object;
	}
	
	public BlockResultValue(String fromString) {
		this(
				fromString.split("|", 3)[0],
				fromString.split("|", 3)[1],
				Double.parseDouble(fromString.split("|", 3)[2]));
	}
	
	public BlockResultValue(String name, String unit, Double value, Double position) {
		this(name, unit, value);
		setPosition(position);
	}
	
	public String getName() {
		return name;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public Double getValue() {
		return value;
	}
	
	public NumericMeasurement3D getBinary() {
		return binary;
	}
	
	public String getString() {
		return name + ";" + unit + ";" + value;
	}
	
	@Override
	public String toString() {
		return getString();
	}
	
	public Double getPosition() {
		return position;
	}
	
	public void setPosition(Double d) {
		position = d;
	}
	
	public Object getObject() {
		return object;
	}
}
