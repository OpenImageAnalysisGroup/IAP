package de.ipk.ag_ba.mongo;

public enum MongoExperimentCollections {
	EXPERIMENTS("experiments");
	
	private String collection_or_field;
	
	private MongoExperimentCollections(String collection_or_filed) {
		this.collection_or_field = collection_or_filed;
	}
	
	public String toString() {
		return collection_or_field;
	}
}
