package de.ipk.ag_ba.commands.experiment.scripts;

import org.IniIoProvider;
import org.SystemOptions;

/**
 * @author klukas
 */
public class VirtualIoProvider implements IniIoProvider {
	
	private long lastUpdate;
	private String val = "";
	private SystemOptions so = SystemOptions.getInstance(null, this);
	
	@Override
	public String getString() {
		return val;
	}
	
	@Override
	public Long setString(String value) {
		val = value;
		lastUpdate = System.currentTimeMillis();
		return lastUpdate;
	}
	
	@Override
	public void setInstance(SystemOptions i) {
		this.so = i;
	}
	
	@Override
	public SystemOptions getInstance() {
		return so;
	}
	
	@Override
	public Long lastModified() throws Exception {
		return lastUpdate;
	}
	
	@Override
	public long storedLastUpdateTime() {
		return lastUpdate;
	}
	
	@Override
	public void setStoredLastUpdateTime(long mt) {
		this.lastUpdate = mt;
	}
	
	@Override
	public boolean isAbleToSaveData() {
		return true;
	}
	
}
