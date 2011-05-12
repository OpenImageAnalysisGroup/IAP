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

	public String getName() {
		return name;
	}

	public String getUnit() {
		return unit;
	}

	public Double getValue() {
		return value;
	}
	
}
