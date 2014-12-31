package de.ipk.ag_ba.commands.experiment;

import org.IniIoProvider;
import org.SystemOptions;

import de.ipk.ag_ba.commands.experiment.scripts.VirtualIoProvider;

/**
 * @author klukas
 */
public class ChartSettings {
	
	private IniIoProvider iniIoProvider;
	private final boolean localSettingObject;
	private boolean userUseLocal = false;
	private boolean savePossible = true;
	
	public ChartSettings(boolean local) {
		this.localSettingObject = local;
		if (local)
			iniIoProvider = new VirtualIoProvider();
	}
	
	public void setIniIOprovider(IniIoProvider iniIoProvider) {
		this.iniIoProvider = iniIoProvider;
		
	}
	
	private IniIoProvider getIniProvider() {
		return iniIoProvider;
	}
	
	public boolean getUseLocalSettings() {
		return userUseLocal || !savePossible;
	}
	
	public void setUseLocalSettings(boolean b) {
		userUseLocal = b;
	}
	
	public SystemOptions getSettings() {
		return SystemOptions.getInstance(null, getIniProvider());
	}
	
	public void setSavePossible(boolean savePossible) {
		this.savePossible = savePossible;
	}
	
	public boolean isSavePossible() {
		return savePossible;
	}
}
