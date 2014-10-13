package org;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 * @author klukas
 */
public class SystemOptions {
	
	private final String iniFileName;
	private Ini ini;
	
	private final LinkedHashMap<String, LinkedHashSet<Runnable>> changeListeners = new LinkedHashMap<String, LinkedHashSet<Runnable>>();
	
	private final IniIoProvider iniIO;
	
	private final static ArrayList<Runnable> updateCheckTasks = new ArrayList<Runnable>();
	
	private boolean settingsInvalid = false;;
	
	private final static HashMap<String, ObjectRef> lastModification = new HashMap<String, ObjectRef>();
	
	private static Thread updateCheckThread;
	
	private SystemOptions(final String iniFileName, final IniIoProvider iniIO) throws Exception {
		if (updateCheckThread == null) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						do {
							Thread.sleep(1000);
							synchronized (SystemOptions.getInstance()) {
								synchronized (updateCheckTasks) {
									for (Runnable r : new ArrayList<Runnable>(updateCheckTasks)) {
										try {
											r.run();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
						} while (true);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			});
			t.setName("Settings Change Monitor");
			t.setPriority(Thread.MIN_PRIORITY);
			t.setDaemon(true);
			t.start();
			updateCheckThread = t;
		}
		
		this.iniFileName = iniFileName;
		this.iniIO = iniIO;
		if (iniIO != null) {
			String nnn = iniIO.getString();
			ini = nnn != null ? new Ini(new StringReader(nnn)) : new Ini();
			WeakReference<IniIoProvider> wr = new WeakReference<IniIoProvider>(iniIO);
			readIniAndPrepareProviderCheck(wr);
			return;
		}
		readIniAndPrepareFileCheck(iniFileName);
	}
	
	private synchronized void readIniAndPrepareProviderCheck(final WeakReference<IniIoProvider> iniIOref) {
		if (iniIOref == null)
			return;
		if (iniIOref.get() == null)
			return;
		iniIOref.get().setStoredLastUpdateTime(iniIOref.get().storedLastUpdateTime());
		
		Runnable updateChecker = new Runnable() {
			@Override
			public void run() {
				try {
					IniIoProvider iniIO = iniIOref.get();
					synchronized (iniIO) {
						if (iniIO == null) {
							System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: INI-Provider is not available any more. Update-check stops for this object.");
							updateCheckTasks.remove(this);
							return;
						}
						if (!iniIO.isAbleToSaveData())
							return; // no need to update from source in this case (normally read-only sources are not updated regularily
						// or interactively
						Long mt;
						try {
							mt = iniIO.lastModified();
						} catch (Exception e) {
							System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: INI-Provider can't be accessed. Update-check stops for this object. Error: "
									+ e.getMessage());
							updateCheckTasks.remove(this);
							ErrorMsg.addErrorMessage(e);
							return;
						}
						if (mt != null && mt != iniIO.storedLastUpdateTime()) {
							iniIO.setStoredLastUpdateTime(mt);
							iniIO.getInstance().ini = readIniFileOrProvider();
							ini = iniIO.getInstance().ini;
							for (LinkedHashSet<Runnable> rr : changeListeners.values()) {
								for (Runnable r : rr) {
									try {
										r.run();
									} catch (Exception e) {
										ErrorMsg.addErrorMessage(e);
									}
								}
							}
						}
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		};
		updateCheckTasks.add(updateChecker);
	}
	
	private void readIniAndPrepareFileCheck(final String iniFileName) {
		synchronized (fileIniInstances) {
			fileIniInstances.put(iniFileName, this);
		}
		ini = readIniFileOrProvider();
		
		String ffnn = ReleaseInfo.getAppFolderWithFinalSep() + iniFileName;
		if (!lastModification.containsKey(ffnn)) {
			lastModification.put(ffnn, new ObjectRef());
			lastModification.get(ffnn).setObject(new File(ffnn).lastModified());
		}
		
		Runnable updateChecker = new Runnable() {
			@Override
			public void run() {
				synchronized (ini) {
					try {
						String ffnn = ReleaseInfo.getAppFolderWithFinalSep() + iniFileName;
						if (!new File(ffnn).exists()) {
							System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: INI-File " + iniFileName
									+ " is not available any more. Update-check stops for this file.");
							updateCheckTasks.remove(this);
							lastModification.remove(ffnn);
							return;
						}
						long mt = new File(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName).lastModified();
						if (mt != ((Long) lastModification.get(ffnn).getObject()).longValue()) {
							lastModification.get(ffnn).setObject(mt);
							ini = readIniFileOrProvider();
							System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: INI-File " + iniFileName
									+ " has been changed and will be reloaded later.");
							SystemOptions.this.settingsInvalid = true;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		updateCheckTasks.add(updateChecker);
	}
	
	protected static HashMap<String, SystemOptions> fileIniInstances = new HashMap<String, SystemOptions>();
	
	public static synchronized SystemOptions getInstance(String iniFileName, IniIoProvider iniIO) {
		if (iniIO != null)
			try {
				if (iniIO.getInstance() != null)
					return iniIO.getInstance();
				synchronized (iniIO) {
					SystemOptions i = new SystemOptions(iniFileName, iniIO);
					iniIO.setInstance(i);
					return i;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		
		if (iniFileName == null)
			iniFileName = "iap.ini";
		synchronized (fileIniInstances) {
			SystemOptions instance = fileIniInstances.get(iniFileName);
			try {
				if (instance == null)
					instance = new SystemOptions(iniFileName, iniIO);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return instance;
		}
	}
	
	public synchronized static SystemOptions getInstance() {
		try {
			return getInstance(null, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public synchronized void reload() {
		readIniFileOrProvider();
	}
	
	public synchronized Ini readIniFileOrProvider() {
		String fn = null;
		try {
			if (iniIO != null) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Read INI Provider Content");
				String ss = iniIO.getString();
				if (ss == null)
					return null;
				ini = new Ini(new StringReader(ss));
				return ini;
			} else {
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Read INI File " + ReleaseInfo.getAppFolderWithFinalSep() + iniFileName);
				fn = ReleaseInfo.getAppFolderWithFinalSep() + iniFileName;
				return new Ini(new File(fn));
			}
		} catch (FileNotFoundException fne) {
			try {
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Create INI settings file: " + fn);
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
	
	public boolean getBoolean(String group, String setting, boolean defaultValue) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			synchronized (ini) {
				if (setting.contains("|")) {
					group = setting.split("\\|", 2)[0];
					setting = setting.split("\\|", 2)[1];
				}
				
				checkInvalidStatus();
				Boolean r = ini.get(group, setting, Boolean.class);
				if (r == null) {
					ini.put(group, setting, defaultValue);
					store(group, setting);
					return defaultValue;
				} else
					return r;
			}
		}
	}
	
	private void checkInvalidStatus() {
		synchronized (changeListeners) {
			if (settingsInvalid) {
				settingsInvalid = false;
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
	}
	
	public void setBoolean(String group, String setting, boolean value) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			synchronized (ini) {
				boolean current = getBoolean(group, setting, value);
				if (current == value)
					return;
				if (setting.contains("|")) {
					group = setting.split("\\|", 2)[0];
					setting = setting.split("\\|", 2)[1];
				}
				ini.put(group, setting, value);
				store(group, setting);
			}
		}
	}
	
	public int getInteger(String group, String setting, int defaultValue) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			synchronized (ini) {
				Integer r;
				checkInvalidStatus();
				try {
					r = ini.get(group, setting, Integer.class);
				} catch (Exception err) {
					r = ini.get(group, setting, Double.class).intValue();
					ini.put(group, setting, r);
					store(group, setting);
				}
				if (r == null) {
					ini.put(group, setting, defaultValue);
					store(group, setting);
					return defaultValue;
				} else
					return r;
			}
		}
	}
	
	public void setInteger(String group, String setting, Integer value) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			synchronized (ini) {
				checkInvalidStatus();
				ini.put(group, setting, value);
				store(group, setting);
			}
		}
	}
	
	public double getDouble(String group, String setting, double defaultValue) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			synchronized (ini) {
				checkInvalidStatus();
				Double r = ini.get(group, setting, Double.class);
				if (r == null) {
					ini.put(group, setting, defaultValue);
					store(group, setting);
					return defaultValue;
				} else
					return r;
			}
		}
	}
	
	public void setDouble(String group, String setting, Double value) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			synchronized (ini) {
				checkInvalidStatus();
				ini.put(group, setting, value);
				store(group, setting);
			}
		}
	}
	
	public String getString(String group, String setting, String defaultValue) {
		return getString(group, setting, defaultValue, true);
	}
	
	public String getString(String group, String setting, String defaultValue, boolean addDefaultIfNeeded) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			synchronized (ini) {
				checkInvalidStatus();
				String r = ini.get(group, setting, String.class);
				if (r == null) {
					if (addDefaultIfNeeded) {
						String storeValue = ExternalPasswordStorage.encryptValueIfNeeded(group, setting, defaultValue);
						ini.put(group, setting, storeValue + "");
						store(group, setting);
					}
					return defaultValue;
				} else {
					if (r == null || r.equals("null"))
						return null;
					else
						return ExternalPasswordStorage.decryptValueIfNeeded(group, setting, r);
				}
			}
		}
	}
	
	public Color getColor(String group, String setting, Color defaultValue) {
		return getColor(group, setting, defaultValue, true);
	}
	
	public Color getColor(String group, String setting, Color defaultValue, boolean addDefaultIfNeeded) {
		if (ini == null) {
			System.out.println("WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			synchronized (ini) {
				checkInvalidStatus();
				String r = ini.get(group, setting, String.class);
				if (r == null) {
					String storeValue = StringManipulationTools.getColorHTMLdef(defaultValue);
					ini.put(group, setting, storeValue);
					store(group, setting);
					return defaultValue;
				}
				if (r.equals("null"))
					return null;
				try {
					Color c = StringManipulationTools.getColorFromHTMLdef(r);
					return c;
				} catch (NumberFormatException e) {
					String storeValue = StringManipulationTools.getColorHTMLdef(defaultValue);
					ini.put(group, setting, storeValue);
					store(group, setting);
					return defaultValue;
				}
			}
		}
	}
	
	public Integer[] getIntArray(String group, String setting, Integer[] defaultValue) {
		String defS = StringManipulationTools.getStringList(defaultValue, "/");
		String s = getString(group, setting, defS);
		String[] os = s.split("/");
		Integer[] res = new Integer[os.length];
		try {
			for (int i = 0; i < os.length; i++) {
				res[i] = Integer.parseInt(os[i]);
			}
		} catch (Exception e) {
			setString(group, setting, defS);
			return defaultValue;
		}
		return res;
	}
	
	protected void store(String srcSection, String srcSetting) {
		try {
			synchronized (ini) {
				if (iniIO != null) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Updated setting: " + srcSection + "//" + srcSetting);
					Long saveTime = iniIO.setString(getIniValue());
					if (saveTime != null) {
						iniIO.setStoredLastUpdateTime(saveTime);
					} else {
						System.err.println(SystemAnalysis.getCurrentTime() + ">" +
								"INFO: Changes could not be saved by the INI-Provider. " +
								"Tried to update: " + srcSection + "//" + srcSetting);
					}
					return;
				}
				ini.store();
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Changes in INI-File " + iniFileName
						+ " have been saved. Updated setting: " + srcSection + "//" + srcSetting);
				if (!new File(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName).exists())
					new File(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName).createNewFile();
				if (!lastModification.containsKey(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName))
					lastModification.put(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName, new ObjectRef());
				lastModification.get(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName).setObject(
						new File(ReleaseInfo.getAppFolderWithFinalSep() + iniFileName).lastModified());
				LinkedHashSet<Runnable> rr = changeListeners.get(getKey(srcSection, srcSetting));
				if (rr != null)
					for (Runnable r : new ArrayList<Runnable>(rr)) {
						r.run();
					}
			}
		} catch (IOException e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public void setString(String group, String setting, String value) {
		if (ini == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			synchronized (ini) {
				value = ExternalPasswordStorage.encryptValueIfNeeded(group, setting, value);
				checkInvalidStatus();
				ini.put(group, setting, value + "");
				store(group, setting);
			}
		}
	}
	
	public void setColor(String group, String setting, Color value) {
		if (ini == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Settings file can't be used, setting value is not stored!");
			return;
		} else {
			synchronized (ini) {
				String svalue = StringManipulationTools.getColorHTMLdef(value);
				checkInvalidStatus();
				ini.put(group, setting, svalue + "");
				store(group, setting);
			}
		}
	}
	
	public String[] getStringAll(String group, String setting, String[] defaultValue) {
		if (ini == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Settings file can't be used, returning default setting value!");
			return defaultValue;
		} else {
			synchronized (ini) {
				checkInvalidStatus();
				Ini.Section section = ini.get(group);
				if (section == null) {
					getString(group, "hint", "Start an entry with # to disable it.");
					section = ini.get(group);
				}
				String[] r = section.getAll(setting, String[].class);
				if (r == null || r.length == 0) {
					int idx = 0;
					if (defaultValue != null) {
						for (String v : defaultValue)
							section.add(setting, v, idx++);
						if (defaultValue.length == 1)
							section.add(setting, "", idx++);
						ini.put(group, section);
						store(group, setting);
					}
					return defaultValue;
				} else
					return r;
			}
		}
	}
	
	public ArrayList<String> getSectionTitles() {
		ArrayList<String> res = new ArrayList<String>();
		if (ini == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Settings file can't be used, returning default setting value!");
			return res;
		} else {
			synchronized (ini) {
				for (String s : ini.keySet())
					res.add(s);
			}
		}
		return res;
	}
	
	public ArrayList<String> getSectionSettings(String section) {
		ArrayList<String> res = new ArrayList<String>();
		if (ini == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Settings file can't be used, returning default setting value!");
			return res;
		} else {
			synchronized (ini) {
				checkInvalidStatus();
				if (ini.get(section) != null)
					for (String s : new ArrayList<String>(ini.get(section).keySet()))
						res.add(s);
			}
		}
		return res;
	}
	
	public String getObject(String section, String setting, int returnMax) {
		synchronized (ini) {
			checkInvalidStatus();
			List<String> l = ini.get(section) != null ? ini.get(section).getAll(setting) : new ArrayList<String>();
			if (l == null)
				l = new ArrayList<String>();
			ArrayList<String> items = new ArrayList<String>(l);
			if (returnMax > 0 && items.size() > returnMax) {
				while (items.size() > returnMax)
					items.remove(returnMax);
				items.add("...");
			}
			return StringManipulationTools.getStringList(items, ", ");
		}
	}
	
	public boolean isBooleanSetting(String section, String setting) {
		synchronized (ini) {
			checkInvalidStatus();
			String v = ini.get(section).get(setting);
			return v != null && (v.equals("true") || v.equals("false"));
		}
	}
	
	public boolean isIntegerSetting(String section, String setting) {
		synchronized (ini) {
			checkInvalidStatus();
			String v = ini.get(section).get(setting);
			Integer i = null;
			try {
				i = Integer.parseInt(v);
				return i != Integer.MAX_VALUE;
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	public boolean isFloatSetting(String section, String setting) {
		synchronized (ini) {
			checkInvalidStatus();
			String v = ini.get(section).get(setting);
			Double d = null;
			try {
				d = Double.parseDouble(v);
				return !Double.isNaN(d);
			} catch (Exception e) {
				return false;
			}
		}
	}
	
	public void setStringArray(String section, String setting, ArrayList<String> newValues) {
		if (ini == null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Settings file can't be used, returning default setting value!");
			return;
		}
		synchronized (ini) {
			checkInvalidStatus();
			Ini.Section sec = ini.get(section);
			if (sec == null)
				sec = ini.add(section);
			if (sec.containsKey(setting))
				sec.remove(setting);
			for (String nv : newValues)
				sec.add(setting, nv);
			if (newValues.size() == 0) {
				sec.add(setting, "");
				sec.add(setting, "");
			}
			if (newValues.size() == 1) {
				sec.add(setting, "");
			}
			store(section, setting);
		}
	}
	
	public void addChangeListener(String section, String setting, Runnable runnable) {
		synchronized (changeListeners) {
			String key = getKey(section, setting);
			if (!changeListeners.containsKey(key))
				changeListeners.put(key, new LinkedHashSet<Runnable>());
			changeListeners.get(key).add(runnable);
		}
	}
	
	private String getKey(String section, String setting) {
		return section + ":" + setting;
	}
	
	public void removeChangeListener(Runnable runnable) {
		synchronized (changeListeners) {
			for (LinkedHashSet<Runnable> cl : changeListeners.values())
				cl.remove(runnable);
		}
	}
	
	public void removeValuesOfSectionAndGroup(String section, String group) {
		synchronized (ini) {
			checkInvalidStatus();
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
				if (del.size() > 0)
					store(section, section);
			}
		}
	}
	
	public String getStringRadioSelection(String group, String setting,
			ArrayList<String> possibleValues, String defaultSelection, boolean addDefaultIfNeeded) {
		synchronized (ini) {
			String settingTemplateEmpty = possibleValues != null ? StringManipulationTools.getStringList(possibleValues, "//") : null;
			String settingTemplateDefaultSelected = settingTemplateEmpty != null ? StringManipulationTools.stringReplace(
					settingTemplateEmpty, defaultSelection, "[x]" + defaultSelection) : null;
			String val = getString(group, setting + "-radio-selection", settingTemplateDefaultSelected, addDefaultIfNeeded);
			String valNoSeletion = val != null ? StringManipulationTools.stringReplace(val, "[x]", "") : null;
			if (valNoSeletion == null)
				return defaultSelection;
			if ((settingTemplateEmpty != null && !settingTemplateEmpty.equals(valNoSeletion)) || !val.contains("[x]")) {
				setString(group, setting + "-radio-selection", settingTemplateDefaultSelected);
				return defaultSelection;
			} else {
				for (String s : val.split("//"))
					if (s.startsWith("[x]"))
						return s.substring("[x]".length());
			}
			if (settingTemplateDefaultSelected != null && !settingTemplateDefaultSelected.equals("null"))
				setString(group, setting + "-radio-selection", settingTemplateDefaultSelected);
			return defaultSelection;
		}
	}
	
	public String getIniValue() throws IOException {
		synchronized (ini) {
			checkInvalidStatus();
			StringWriter sw = new StringWriter();
			ini.store(sw);
			return sw.toString();
		}
	}
}
