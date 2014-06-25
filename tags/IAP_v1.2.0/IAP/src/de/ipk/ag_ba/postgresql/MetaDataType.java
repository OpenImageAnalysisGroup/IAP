package de.ipk.ag_ba.postgresql;

public class MetaDataType {
	private final String meta_data_name;
	private final String meta_data_type;
	
	public MetaDataType(String meta_data_name, String meta_data_type) {
		this.meta_data_name = meta_data_name;
		this.meta_data_type = meta_data_type;
	}
	
	public String getMeta_data_name() {
		return meta_data_name;
	}
	
	public String getMeta_data_type() {
		return meta_data_type;
	}
}
