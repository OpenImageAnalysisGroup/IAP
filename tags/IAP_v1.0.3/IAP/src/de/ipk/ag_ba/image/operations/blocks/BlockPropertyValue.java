package de.ipk.ag_ba.image.operations.blocks;

public class BlockPropertyValue {
	
	private final String name;
	private final String unit;
	private final Double value;
	
	public BlockPropertyValue(String name, String unit, Double value) {
		this.name = name;
		this.unit = unit;
		this.value = value;
	}
	
	public BlockPropertyValue(String fromString) {
		this(
				fromString.split("|", 3)[0],
				fromString.split("|", 3)[1],
				Double.parseDouble(fromString.split("|", 3)[2]));
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
	
	public String getString() {
		return name + ";" + unit + ";" + value;
	}
}
