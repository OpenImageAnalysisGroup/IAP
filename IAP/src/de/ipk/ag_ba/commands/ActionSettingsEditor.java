package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.SystemOptions;
import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

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
							String sv = SystemOptions.getInstance(iniFileName).getObject(section, setting);
							StringBuilder sb = new StringBuilder();
							while (sb.length() < sv.length())
								sb.append("*");
							return "<html><center><b>" + s + "</b><br>" +
									"&nbsp;" + sb + "&nbsp;" + "</center></html>";
						} else
							return "<html><center><b>" + s + "</b><br>" +
									"&nbsp;" + SystemOptions.getInstance(iniFileName).getObject(section, setting) + "&nbsp;" + "</center></html>";
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
