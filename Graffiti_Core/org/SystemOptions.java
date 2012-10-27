package org;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ini4j.Ini;

public class SystemOptions {
	
	protected SystemOptions() {
		instance = this;
	}
	
	protected static SystemOptions instance;
	
	public synchronized static SystemOptions getInstance() {
		if (instance == null)
			instance = new SystemOptions();
		return instance;
	}
	
	Ini ini = readIni();
	
	private Ini readIni() {
		String fn = ReleaseInfo.getAppFolderWithFinalSep() + "iap.ini";
		try {
			return new Ini(new File(fn));
		} catch (FileNotFoundException fne) {
			try {
				Ini ini = new Ini();
				File f = new File(fn);
				f.createNewFile();
				ini.store(f);
				ini.setFile(f);
				return ini;
			} catch (IOException e) {
				e.printStackTrace();
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	public synchronized boolean getBoolean(String group, String setting, boolean defaultValue) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			if (setting.contains("|")) {
				group = setting.split("\\|", 2)[0];
				setting = setting.split("\\|", 2)[1];
			}
			
			Boolean r = ini.get(group, setting, Boolean.class);
			if (r == null) {
				ini.put(group, setting, defaultValue);
				try {
					ini.store();
				} catch (IOException e) {
					e.printStackTrace();
					ErrorMsg.addErrorMessage(e);
				}
				return defaultValue;
			} else
				return r;
		}
	}
	
	public synchronized void setBoolean(String group, String setting, boolean value) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			if (setting.contains("|")) {
				group = setting.split("\\|", 2)[0];
				setting = setting.split("\\|", 2)[1];
			}
			ini.put(group, setting, value);
			try {
				ini.store();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	public synchronized int getInteger(String group, String setting, int defaultValue) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			Integer r = ini.get(group, setting, Integer.class);
			if (r == null) {
				ini.put(group, setting, defaultValue);
				try {
					ini.store();
				} catch (IOException e) {
					e.printStackTrace();
					ErrorMsg.addErrorMessage(e);
				}
				return defaultValue;
			} else
				return r;
		}
	}
	
	public synchronized double getDouble(String group, String setting, double defaultValue) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			Double r = ini.get(group, setting, Double.class);
			if (r == null) {
				ini.put(group, setting, defaultValue);
				try {
					ini.store();
				} catch (IOException e) {
					e.printStackTrace();
					ErrorMsg.addErrorMessage(e);
				}
				return defaultValue;
			} else
				return r;
		}
	}
	
	public synchronized String getString(String group, String setting, String defaultValue) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			String r = ini.get(group, setting, String.class);
			if (r == null) {
				ini.put(group, setting, defaultValue + "");
				try {
					ini.store();
				} catch (IOException e) {
					e.printStackTrace();
					ErrorMsg.addErrorMessage(e);
				}
				return defaultValue;
			} else {
				if (r == null || r.equals("null"))
					return null;
				else
					return r;
			}
		}
	}
	
	public synchronized void setString(String group, String setting, String value) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			ini.put(group, setting, value + "");
			try {
				ini.store();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	public synchronized String[] getStringAll(String group, String setting, String[] defaultValue) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			Ini.Section section = ini.get(group);
			if (section == null) {
				getString(group, "hint", "Start a block name with '#' to disable it.");
				section = ini.get(group);
			}
			String[] r = section.getAll(setting, String[].class);
			if (r == null || r.length == 0) {
				int idx = 0;
				for (String v : defaultValue)
					section.add(setting, v, idx++);
				ini.put(group, section);
				try {
					ini.store();
				} catch (IOException e) {
					e.printStackTrace();
					ErrorMsg.addErrorMessage(e);
				}
				return defaultValue;
			} else
				return r;
		}
	}
}
