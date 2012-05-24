package org.graffiti.plugin.io.resources;

public class FileSystemIOConfig implements ResourceIOConfigObject {
	
	private String filedir;
	
	public FileSystemIOConfig(String path) {
		this.filedir = path;
	}
	
	public void setFiledir(String filedir) {
		this.filedir = filedir;
	}
	
	public String getFileDir() {
		return filedir;
	}
	
}
