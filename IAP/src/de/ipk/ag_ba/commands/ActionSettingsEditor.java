package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.SystemOptions;
import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public class ActionSettingsEditor extends AbstractNavigationAction {
	
	private String section;
	
	ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
	
	private String iniFileName;
	
	public ActionSettingsEditor(String tooltip) {
		super(tooltip);
	}
	
	public ActionSettingsEditor(String iniFileName, String tooltip, String section) {
		this(tooltip);
		this.iniFileName = iniFileName;
		this.section = section;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		res.clear();
		for (final String setting : SystemOptions.getInstance(iniFileName).getSectionSettings(section)) {
			res.add(new NavigationButton(new AbstractNavigationAction("Change setting " + section + "/" + setting) {
				boolean isBoolean = SystemOptions.getInstance(iniFileName).isBooleanSetting(section, setting);
				boolean isInteger = !isBoolean && SystemOptions.getInstance(iniFileName).isIntegerSetting(section, setting);
				boolean isFloat = !isBoolean && !isInteger && SystemOptions.getInstance(iniFileName).isFloatSetting(section, setting);
				
				@Override
				public void performActionCalculateResults(NavigationButton src) throws Exception {
					if (isBoolean) {
						boolean enabled = SystemOptions.getInstance(iniFileName).getBoolean(section, setting, false);
						enabled = !enabled;
						SystemOptions.getInstance(iniFileName).setBoolean(section, setting, enabled);
					} else
						if (isInteger) {
							Object[] inp = MyInputHelper.getInput("Please enter a whole number:",
									"Modify "
											+ setting,
									setting, SystemOptions.getInstance(iniFileName).getInteger(section, setting, 0));
							if (inp != null) {
								if (inp.length == 1) {
									Object o = inp[0];
									if (o != null && o instanceof Integer) {
										SystemOptions.getInstance(iniFileName).setInteger(section, setting, (Integer) o);
									}
								}
							}
						} else
							if (isFloat) {
								Object[] inp = MyInputHelper.getInput("Please enter a (floating point) number:",
										"Modify "
												+ setting,
										setting, SystemOptions.getInstance(iniFileName).getDouble(section, setting, 0d));
								if (inp != null) {
									if (inp.length == 1) {
										Object o = inp[0];
										if (o != null && o instanceof Double) {
											SystemOptions.getInstance(iniFileName).setDouble(section, setting, (Double) o);
										}
									}
								}
							} else {
								String[] ss = SystemOptions.getInstance(iniFileName).getStringAll(section, setting, new String[] {});
								boolean isString = ss.length == 1;
								boolean isStringArray = ss.length > 1;
								if (isString) {
									Object[] inp = MyInputHelper.getInput("You may modify the text:",
											"Modify "
													+ setting,
											setting, SystemOptions.getInstance(iniFileName).getString(section, setting, ""));
									if (inp != null) {
										if (inp.length == 1) {
											Object o = inp[0];
											if (o != null && o instanceof String) {
												SystemOptions.getInstance(iniFileName).setString(section, setting, (String) o);
											}
										}
									}
								} else
									if (isStringArray) {
										ArrayList<String> entries = new ArrayList<String>();
										int line = 1;
										for (String sl : ss) {
											entries.add("Item " + (line++));
											entries.add(sl);
										}
										Object[] inp = MyInputHelper.getInput(
												"You may modify multiple text entries (settings items '" + setting + "'). <br>" +
														"If an item contains '//', the entry is split into two items.<br>",
												"Modify "
														+ setting, entries.toArray());
										if (inp != null) {
											if (inp.length > 0) {
												ArrayList<String> newValues = new ArrayList<String>();
												for (Object o : inp) {
													if (o != null && o instanceof String) {
														String es = (String) o;
														for (String nn : es.split("//")) {
															nn = nn.trim();
															newValues.add(nn);
														}
													}
												}
												SystemOptions.getInstance(iniFileName).setStringArray(section, setting, newValues);
											}
										}
									}
							}
				}
				
				@Override
				public boolean getProvidesActions() {
					return false;
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
					return currentSet;
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewActionSet() {
					return null;
					// ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
					// res.add(null);
					// return res;
				}
				
				@Override
				public String getDefaultTitle() {
					String s = setting;
					s = StringManipulationTools.stringReplace(s, "_", "-");
					s = WordUtils.capitalizeFully(s, '-', ' ');
					if (isBoolean)
						return s;
					else {
						if (setting.equalsIgnoreCase("password")) {
							String sv = SystemOptions.getInstance(iniFileName).getObject(section, setting, -1);
							StringBuilder sb = new StringBuilder();
							while (sb.length() < sv.length())
								sb.append("*");
							return "<html><center><b>" + s + "</b><br>" +
									"&nbsp;" + sb + "&nbsp;" + "</center></html>";
						} else
							return "<html><center><b>" + s + "</b><br>" +
									"&nbsp;" + SystemOptions.getInstance(iniFileName).getObject(section, setting, 2) + "&nbsp;" + "</center></html>";
					}
				}
				
				@Override
				public boolean requestTitleUpdates() {
					return true;
				}
				
				@Override
				public String getDefaultImage() {
					if (isBoolean) {
						boolean enabled = SystemOptions.getInstance(iniFileName).getBoolean(section, setting, false);
						if (enabled)
							return "img/ext/gpl2/Dialog-Apply-64.png";// gtcf.png";
						else
							return "img/ext/gpl2/Gnome-Emblem-Unreadable-64.png";// gtcd.png";
					} else
						if (isInteger) {
							return "img/ext/gpl2/Gnome-Accessories-Calculator-64.png";
						} else
							if (isFloat) {
								return "img/ext/gpl2/Gnome-Accessories-Calculator-64.png";
							} else {
								if (setting.equalsIgnoreCase("password"))
									return "img/ext/gpl2/Gnome-Emblem-Readonly-64.png";
								else
									return "img/ext/gpl2/Gnome-Insert-Text-64.png";// Gnome-Accessories-Character-Map-64.png";
							}
					// Gnome-Applications-Accessories-64.png";
				}
				
			}, src.getGUIsetting()));
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return WordUtils.capitalizeFully(section, ' ');
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Emblem-Package-64.png";// "Gnome-Accessories-Text-Editor-64.png";
	}
	
	@Override
	public boolean getProvidesActions() {
		return true;
	}
}
