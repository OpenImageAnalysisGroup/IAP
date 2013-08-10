package org;

public interface IniIoProvider {
	
	public String getString();
	
	/**
	 * @return Storage time.
	 */
	public Long setString(String value);
	
	public void setInstance(SystemOptions i);
	
	public SystemOptions getInstance();
	
	public Long lastModified() throws Exception;
	
	public long storedLastUpdateTime();
	
	public void setStoredLastUpdateTime(long mt);
	
	public boolean isAbleToSaveData();
}
