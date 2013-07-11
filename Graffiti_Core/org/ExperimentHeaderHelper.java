package org;

public interface ExperimentHeaderHelper {
	
	public void readSourceForUpdate() throws Exception;
	
	public Long getLastModified() throws Exception;
	
	public Long saveUpdatedProperties() throws Exception;
	
	public boolean isAbleToSaveData();
	
}
