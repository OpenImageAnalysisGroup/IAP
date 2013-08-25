package org;

public interface ExperimentHeaderHelper {
	
	public void readSourceForUpdate() throws Exception;
	
	public Long getLastModified() throws Exception;
	
	public Long saveUpdatedProperties(BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception;
	
	public boolean isAbleToSaveData();
	
}
