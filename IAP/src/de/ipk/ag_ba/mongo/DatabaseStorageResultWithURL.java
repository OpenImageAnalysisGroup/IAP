package de.ipk.ag_ba.mongo;

import org.graffiti.plugin.io.resources.IOurl;

public class DatabaseStorageResultWithURL {
	
	private DatabaseStorageResult result;
	private IOurl resultURL;
	
	public DatabaseStorageResultWithURL(DatabaseStorageResult result, IOurl resultURL) {
		this.result = result;
		this.resultURL = resultURL;
	}
	
	public IOurl getResultURL() {
		return resultURL;
	}
	
	public DatabaseStorageResult getResult() {
		return result;
	}
}
