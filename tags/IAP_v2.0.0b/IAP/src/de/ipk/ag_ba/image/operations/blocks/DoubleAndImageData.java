package de.ipk.ag_ba.image.operations.blocks;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

/**
 * @author Christian Klukas
 */
public class DoubleAndImageData implements Comparable<DoubleAndImageData> {
	private final Double value;
	private final NumericMeasurement3D imageData;
	
	public DoubleAndImageData(Double value, NumericMeasurement3D imageData) {
		this.value = value;
		this.imageData = imageData;
	}
	
	public Double getValue() {
		return value;
	}
	
	public NumericMeasurement3D getImageData() {
		return imageData;
	}
	
	@Override
	public int compareTo(DoubleAndImageData o) {
		return value.compareTo(o.value);
	}
	
	@Override
	public String toString() {
		return value + " (ref=" + imageData + ")";
	}
}
