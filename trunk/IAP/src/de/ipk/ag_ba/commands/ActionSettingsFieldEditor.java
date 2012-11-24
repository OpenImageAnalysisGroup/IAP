package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.SystemOptions;
import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

class ActionSettingsFieldEditor extends AbstractNavigationAction {
	private final ActionSettingsEditor actionSettingsEditor;
	private final String setting;
	boolean isBoolean;
	boolean isInteger;
	boolean isFloat;
	
	public ActionSettingsFieldEditor(ActionSettingsEditor actionSettingsEditor, String tooltip, String setting) {
		super(tooltip);
		this.actionSettingsEditor = actionSettingsEditor;
		this.setting = setting;
		isBoolean = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).isBooleanSetting(this.actionSettingsEditor.section, setting);
		isInteger = !isBoolean && SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).isIntegerSetting(this.actionSettingsEditor.section, setting);
		isFloat = !isBoolean && !isInteger
				&& SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).isFloatSetting(this.actionSettingsEditor.section, setting);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (isBoolean) {
			boolean enabled = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).getBoolean(this.actionSettingsEditor.section, setting, false);
			enabled = !enabled;
			SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).setBoolean(this.actionSettingsEditor.section, setting, enabled);
		} else
			if (isInteger) {
				Object[] inp = MyInputHelper.getInput("Please enter a whole number:",
						"Modify "
								+ setting,
						setting, SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).getInteger(this.actionSettingsEditor.section, setting, 0));
				if (inp != null) {
					if (inp.length == 1) {
						Object o = inp[0];
						if (o != null && o instanceof Integer) {
							SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).setInteger(this.actionSettingsEditor.section, setting, (Integer) o);
						}
					}
				}
			} else
				if (isFloat) {
					Object[] inp = MyInputHelper.getInput("Please enter a (floating point) number:",
							"Modify "
									+ setting,
							setting, SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).getDouble(this.actionSettingsEditor.section, setting, 0d));
					if (inp != null) {
						if (inp.length == 1) {
							Object o = inp[0];
							if (o != null && o instanceof Double) {
								SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).setDouble(this.actionSettingsEditor.section, setting, (Double) o);
							}
						}
					}
				} else {
					String[] ss = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).getStringAll(this.actionSettingsEditor.section, setting,
							new String[] {});
					boolean isString = ss.length == 1;
					boolean isStringArray = ss.length > 1;
					if (isString) {
						if (setting.toLowerCase().contains("password")) {
							Object[] i = MyInputHelper.getInput(
									"WARNING: The <u>password will be shown now and saved as clear text</u> in the settings-ini-file!" +
											"<br>Click 'Cancel' to interrupt the process of displaying and " +
											"editing the password.", "WARNING");
							if (i == null)
								return;
						}
						Object[] inp = MyInputHelper.getInput("You may modify the text:",
								"Modify "
										+ setting,
								setting, SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).getString(this.actionSettingsEditor.section, setting, "")
										+ "");
						if (inp != null) {
							if (inp.length == 1) {
								Object o = inp[0];
								if (o != null && o instanceof String) {
									SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).setString(this.actionSettingsEditor.section, setting, (String) o);
								}
							}
						}
					} else
						if (isStringArray) {
							ArrayList<String> entries = new ArrayList<String>();
							int line = 1;
							for (String sl : ss) {
								entries.add("Item " + (line++));
								entries.add(sl + "");
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
									SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).setStringArray(this.actionSettingsEditor.section, setting,
											newValues);
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
		if (s.contains("//"))
			s = s.substring(s.indexOf("//") + "//".length());
		s = StringManipulationTools.stringReplace(s, "_", "-");
		s = WordUtils.capitalizeFully(s, '-', ' ');
		if (isBoolean)
			return s;
		else {
			if (setting.toLowerCase().contains("password")) {
				String sv = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).getObject(this.actionSettingsEditor.section, setting, -1);
				StringBuilder sb = new StringBuilder();
				while (sb.length() < sv.length())
					sb.append("*");
				return "<html><center><b>" + s + "</b><br>" +
						"&nbsp;" + sb + "&nbsp;" + "</center></html>";
			} else
				return "<html><center><b>" + s + "</b><br>" +
						"&nbsp;" + SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).getObject(this.actionSettingsEditor.section, setting, 2)
						+ "&nbsp;" + "</center></html>";
		}
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultImage() {
		if (isBoolean) {
			boolean enabled = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName).getBoolean(this.actionSettingsEditor.section, setting, false);
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
}