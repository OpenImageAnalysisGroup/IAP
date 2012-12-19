package org;

public interface IniIoProvider {
	
	public String getString();
	
	public void setString(String value);
	
	public void setInstance(SystemOptions i);
	
	public SystemOptions getInstance();
}
