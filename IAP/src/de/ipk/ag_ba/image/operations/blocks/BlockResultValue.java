package de.ipk.ag_ba.image.operations.blocks;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

public class BlockResultValue {
	
	private final String name;
	private final String unit;
	private final Double value;
	private Object object;
	private Double position;
	private final NumericMeasurement3D binary;
	
	public BlockResultValue(String name, String unit, Double value, NumericMeasurement3D imageRef) {
		this.name = name;
		this.unit = unit;
		this.value = value;
		this.binary = new NumericMeasurement3D(imageRef.getParentSample());
		this.binary.setValue(value);
		this.binary.setUnit(unit);
		this.binary.setQualityAnnotation(imageRef.getQualityAnnotation());
		this.binary.setReplicateID(imageRef.getReplicateID());
		this.binary.setPosition(imageRef.getPosition());
		this.binary.setPositionUnit(imageRef.getPositionUnit());
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
				fromString.split("\\|", 3)[0],
				fromString.split("\\|", 3)[1],
				Double.parseDouble(fromString.split("\\|", 3)[2]), null);
	}
	
	public BlockResultValue(String name, String unit, Double value, Double position, NumericMeasurement3D ref) {
		this(name, unit, value, ref);
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
	
	public DoubleAndImageData getDaV() {
		return new DoubleAndImageData(value, binary);
	}
}
