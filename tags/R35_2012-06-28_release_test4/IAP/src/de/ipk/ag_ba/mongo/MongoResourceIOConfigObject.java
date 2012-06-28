package de.ipk.ag_ba.mongo;

import org.graffiti.plugin.io.resources.ResourceIOConfigObject;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;

public class MongoResourceIOConfigObject implements ResourceIOConfigObject {
	
	private final DataStorageType storageType;
	private final MeasurementNodeType datatype;
	
	public MongoResourceIOConfigObject(MeasurementNodeType datatype, DataStorageType storageType) {
		this.datatype = datatype;
		this.storageType = storageType;
	}
	
	public MeasurementNodeType getDatatype() {
		return datatype;
	}
	
	public DataStorageType getStorageType() {
		return storageType;
	}
	
}
