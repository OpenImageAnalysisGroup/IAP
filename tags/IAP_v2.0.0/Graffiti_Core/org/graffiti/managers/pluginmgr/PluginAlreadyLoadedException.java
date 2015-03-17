package org.graffiti.managers.pluginmgr;

public class PluginAlreadyLoadedException extends PluginManagerException {
	public PluginAlreadyLoadedException(String key) {
		super(key);
	}
	
	private static final long serialVersionUID = 1L;
	
}
