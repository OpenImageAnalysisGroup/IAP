package org;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 * @author klukas
 */
public class SystemOptions {
	
	private final String iniFileName;
	private Ini ini;
	
	private LinkedHashMap<String, LinkedHashSet<Runnable>> changeListeners = new LinkedHashMap<String, LinkedHashSet<Runnable>>();
	
	private long lastModificationTime = 0;
	
	private SystemOptions(final String iniFileName) {
		this.iniFileName = iniFileName;
		instances.put(iniFileName, this);
		ini = readIni();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				SystemOptions.this.lastModificationTime = new File(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName).lastModified();
				try {
					do {
						Thread.sleep(1000);
						synchronized (SystemOptions.this) {
							long mt = new File(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName).lastModified();
							if (mt != SystemOptions.this.lastModificationTime) {
								SystemOptions.this.lastModificationTime = mt;
								ini = readIni();
								System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: INI-File " + iniFileName + " has been changed and has been reloaded.");
								for (LinkedHashSet<Runnable> rr : changeListeners.values()) {
									for (Runnable r : rr) {
										try {
											r.run();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					} while (true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setName("File Change Monitor " + iniFileName);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
	
	protected static HashMap<String, SystemOptions> instances = new HashMap<String, SystemOptions>();
	
	public synchronized static SystemOptions getInstance(String iniFileName) {
		if (iniFileName == null)
			iniFileName = "iap.ini";
		SystemOptions instance = instances.get(iniFileName);
		if (instance == null)
			instance = new SystemOptions(iniFileName);
		return instance;
	}
	
	public synchronized static SystemOptions getInstance() {
		return getInstance(null);
	}
	
	private synchronized Ini readIni() {
		String fn = ReleaseInfo.getAppFolderWithFinalSep() + iniFileName;
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
				store(group, setting);
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
			store(group, setting);
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
				store(group, setting);
				return defaultValue;
			} else
				return r;
		}
	}
	
	public synchronized void setInteger(String group, String setting, Integer value) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			ini.put(group, setting, value);
			store(group, setting);
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
				store(group, setting);
				return defaultValue;
			} else
				return r;
		}
	}
	
	public synchronized void setDouble(String group, String setting, Double value) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			ini.put(group, setting, value);
			store(group, setting);
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
				store(group, setting);
				return defaultValue;
			} else {
				if (r == null || r.equals("null"))
					return null;
				else
					return r;
			}
		}
	}
	
	protected synchronized void store(String srcSection, String srcSetting) {
		try {
			ini.store();
			lastModificationTime = new File(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName).lastModified();
			LinkedHashSet<Runnable> rr = changeListeners.get(getKey(srcSection, srcSetting));
			if (rr != null)
				for (Runnable r : new ArrayList<Runnable>(rr)) {
					r.run();
				}
		} catch (IOException e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public synchronized void setString(String group, String setting, String value) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			ini.put(group, setting, value + "");
			store(group, setting);
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
				store(group, setting);
				return defaultValue;
			} else
				return r;
		}
	}
	
	public synchronized ArrayList<String> getSectionTitles() {
		ArrayList<String> res = new ArrayList<String>();
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return res;
		} else {
			for (String s : ini.keySet())
				res.add(s);
		}
		return res;
	}
	
	public synchronized ArrayList<String> getSectionSettings(String section) {
		ArrayList<String> res = new ArrayList<String>();
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return res;
		} else {
			for (String s : ini.get(section).keySet())
				res.add(s);
		}
		return res;
	}
	
	public synchronized String getObject(String section, String setting, int returnMax) {
		ArrayList<String> items = new ArrayList<String>(ini.get(section).getAll(setting));
		if (returnMax > 0 && items.size() > returnMax) {
			while (items.size() > returnMax)
				items.remove(returnMax);
			items.add("...");
		}
		return StringManipulationTools.getStringList(items, ", ");
	}
	
	public synchronized boolean isBooleanSetting(String section, String setting) {
		String v = ini.get(section).get(setting);
		return v != null && (v.equals("true") || v.equals("false"));
	}
	
	public synchronized boolean isIntegerSetting(String section, String setting) {
		String v = ini.get(section).get(setting);
		Integer i = null;
		try {
			i = Integer.parseInt(v);
			return i != Integer.MAX_VALUE;
		} catch (Exception e) {
			return false;
		}
	}
	
	public synchronized boolean isFloatSetting(String section, String setting) {
		String v = ini.get(section).get(setting);
		Double d = null;
		try {
			d = Double.parseDouble(v);
			return !Double.isNaN(d);
		} catch (Exception e) {
			return false;
		}
	}
	
	public synchronized void setStringArray(String section, String setting, ArrayList<String> newValues) {
		Ini.Section sec = ini.get(section);
		sec.remove(setting);
		for (String nv : newValues)
			sec.add(setting, nv);
	}
	
	public synchronized void addChangeListener(String section, String setting, Runnable runnable) {
		String key = getKey(section, setting);
		if (!changeListeners.containsKey(key))
			changeListeners.put(key, new LinkedHashSet<Runnable>());
		changeListeners.get(key).add(runnable);
	}
	
	private String getKey(String section, String setting) {
		return section + ":" + setting;
	}
	
	public synchronized void removeChangeListener(Runnable runnable) {
		for (LinkedHashSet<Runnable> cl : changeListeners.values())
			cl.remove(runnable);
	}
	
	public synchronized void removeValuesOfSectionAndGroup(String section, String group) {
		Section sec = ini.get(section);
		if (sec != null) {
			// remove only the right "group"-values
			ArrayList<String> del = new ArrayList<String>();
			for (String setting : sec.keySet()) {
				String ggroup = "";
				if (setting != null && setting.contains("//")) {
					ggroup = setting.split("//")[0];
				}
				if (ggroup.equals(group)) {
					del.add(setting);
				}
			}
			for (String d : del)
				sec.remove(d);
			for (String d : del) {
				LinkedHashSet<Runnable> rr = changeListeners.get(getKey(section, d));
				if (rr != null)
					for (Runnable r : new ArrayList<Runnable>(rr)) {
						r.run();
					}
			}
		}
	}
}
