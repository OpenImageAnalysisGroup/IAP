package de.ipk.ag_ba.commands.settings;

import iap.blocks.data_structures.ImageAnalysisBlockFIS;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.StringManipulationTools;
import org.SystemOptions;
import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

class ActionSettingsFieldEditor extends AbstractNavigationAction {
	private final ActionSettingsEditor actionSettingsEditor;
	private final String setting;
	boolean isRadioSelection;
	boolean isBoolean;
	boolean isInteger;
	boolean isFloat;
	
	public ActionSettingsFieldEditor(ActionSettingsEditor actionSettingsEditor,
			String tooltip, String setting) {
		super(tooltip);
		this.actionSettingsEditor = actionSettingsEditor;
		this.setting = setting;
		isRadioSelection = setting.endsWith("-radio-selection");
		if (isRadioSelection) {
			isBoolean = false;
			isInteger = false;
			isFloat = false;
		} else {
			isBoolean = SystemOptions.getInstance(
					this.actionSettingsEditor.iniFileName,
					this.actionSettingsEditor.iniIO)
					.isBooleanSetting(this.actionSettingsEditor.section, setting);
			isInteger = !isBoolean
					&& SystemOptions.getInstance(
							this.actionSettingsEditor.iniFileName,
							this.actionSettingsEditor.iniIO)
							.isIntegerSetting(this.actionSettingsEditor.section, setting);
			isFloat = !isBoolean && !isInteger
					&& SystemOptions.getInstance(
							this.actionSettingsEditor.iniFileName,
							this.actionSettingsEditor.iniIO)
							.isFloatSetting(this.actionSettingsEditor.section, setting);
		}
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (isRadioSelection) {
			ArrayList<Object> entries = new ArrayList<Object>();
			String poss = SystemOptions.getInstance(
					this.actionSettingsEditor.iniFileName,
					this.actionSettingsEditor.iniIO)
					.getString(this.actionSettingsEditor.section, setting, null);
			LinkedHashMap<String, JRadioButton> value2button = new LinkedHashMap<String, JRadioButton>();
			ButtonGroup group = new ButtonGroup();
			for (String sl : poss.split("//")) {
				entries.add("");
				boolean enable = false;
				if (sl.startsWith("[x]")) {
					enable = true;
					sl = sl.substring("[x]".length());
				}
				JRadioButton rb = new JRadioButton(sl);
				rb.setSelected(enable);
				group.add(rb);
				entries.add(rb);
				value2button.put(sl, rb);
			}
			String s2 = setting.substring(0, setting.length() - "-radio-selection".length());
			Object[] inp = MyInputHelper.getInput(
					"Select the desired option from the listed entries:<br>",
					s2, entries.toArray());
			if (inp != null) {
				if (inp.length > 0) {
					ArrayList<String> newValues = new ArrayList<String>();
					for (String k : value2button.keySet()) {
						if (value2button.get(k).isSelected())
							newValues.add("[x]" + k);
						else
							newValues.add(k);
					}
					SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
							this.actionSettingsEditor.iniIO)
							.setString(this.actionSettingsEditor.section, setting,
									StringManipulationTools.getStringList(newValues, "//"));
				}
			}
		} else
			if (isBoolean) {
				boolean enabled = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
						this.actionSettingsEditor.iniIO)
						.getBoolean(this.actionSettingsEditor.section, setting, false);
				enabled = !enabled;
				SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
						this.actionSettingsEditor.iniIO)
						.setBoolean(this.actionSettingsEditor.section, setting, enabled);
			} else
				if (isInteger) {
					Object[] inp = MyInputHelper.getInput("Please enter a whole number:",
							"Modify "
									+ setting,
							setting, SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
									this.actionSettingsEditor.iniIO)
									.getInteger(this.actionSettingsEditor.section, setting, 0));
					if (inp != null) {
						if (inp.length == 1) {
							Object o = inp[0];
							if (o != null && o instanceof Integer) {
								SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
										this.actionSettingsEditor.iniIO)
										.setInteger(this.actionSettingsEditor.section, setting, (Integer) o);
							}
						}
					}
				} else
					if (isFloat) {
						Object[] inp = MyInputHelper.getInput("Please enter a (floating point) number:",
								"Modify "
										+ setting,
								setting, SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
										this.actionSettingsEditor.iniIO)
										.getDouble(this.actionSettingsEditor.section, setting, 0d));
						if (inp != null) {
							if (inp.length == 1) {
								Object o = inp[0];
								if (o != null && o instanceof Double) {
									SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
											this.actionSettingsEditor.iniIO)
											.setDouble(this.actionSettingsEditor.section, setting, (Double) o);
								}
							}
						}
					} else {
						String[] ss = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
								this.actionSettingsEditor.iniIO)
								.getStringAll(this.actionSettingsEditor.section, setting,
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
									setting, SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
											this.actionSettingsEditor.iniIO)
											.getString(this.actionSettingsEditor.section, setting, "")
											+ "");
							if (inp != null) {
								if (inp.length == 1) {
									Object o = inp[0];
									if (o != null && o instanceof String) {
										SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
												this.actionSettingsEditor.iniIO)
												.setString(this.actionSettingsEditor.section, setting, (String) o);
									}
								}
							}
						} else
							if (isStringArray) {
								ArrayList<String> entries = new ArrayList<String>();
								int line = 1;
								for (String sl : ss) {
									if (!setting.equals("block"))
										entries.add("Item " + (line++));
									else {
										String inf = "Step " + (line++);
										if (line <= 10)
											inf = "Step 0" + (line - 1);
										try {
											ImageAnalysisBlockFIS inst = (ImageAnalysisBlockFIS) Class.forName(sl).newInstance();
											
											String gs = "<font color='green'>";
											String ge = "</font>";
											String rs = "<font color='red'>";
											String re = "</font>";
											String ns = "<font color='darkgray'>";
											String ne = "</font>";
											String is = "<font color='blue'>";
											String ie = "</font>";
											
											String vi = gs + (inst.getInputTypes().contains(FlexibleImageType.VIS) ? "V" : "&darr;") + ge;
											String fi = rs + (inst.getInputTypes().contains(FlexibleImageType.FLUO) ? "F" : "&darr;") + re;
											String ni = ns + (inst.getInputTypes().contains(FlexibleImageType.NIR) ? "N" : "&darr;") + ne;
											String ii = is + (inst.getInputTypes().contains(FlexibleImageType.IR) ? "I" : "&darr;") + ie;
											
											String vo = gs + (inst.getOutputTypes().contains(FlexibleImageType.VIS) ? "V" : "&darr;") + ge;
											String fo = rs + (inst.getOutputTypes().contains(FlexibleImageType.FLUO) ? "F" : "&darr;") + re;
											String no = ns + (inst.getOutputTypes().contains(FlexibleImageType.NIR) ? "N" : "&darr;") + ne;
											String io = is + (inst.getOutputTypes().contains(FlexibleImageType.IR) ? "I" : "&darr;") + ie;
											
											inf = "<html><table border='0'><tr><td>" + inf + "</td><td><font color='gray' size='-2'><code>"
													+ " IN &#9656; " + vi + " " + fi + " " + ni + " " + ii + ""
													+ "<br> OUT&#9656; " + vo + " " + fo + " " + no + " " + io + "</code></font></td></tr></table>";
										} catch (Exception e) {
											inf = "<html>" + inf + "<br>[" + e.getMessage() + "]";
										}
										entries.add(inf);
									}
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
													newValues.add(nn);
												}
											}
										}
										SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
												this.actionSettingsEditor.iniIO)
												.setStringArray(this.actionSettingsEditor.section, setting,
														newValues);
									}
								}
							}
					}
	}
	
	@Override
	public boolean isProvidingActions() {
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
		if (isRadioSelection) {
			String s2 = setting.substring(0, setting.length() - "-radio-selection".length());
			String sel = SystemOptions.getInstance().getStringRadioSelection(
					this.actionSettingsEditor.section, s2, null, null, false);
			s = s.substring(0, s.length() - "-radio-selection".length());
			return "<html><center><b>" + s + "</b><br>" +
					"&nbsp;" + sel + "&nbsp;" + "</center>";
		}
		if (isBoolean)
			return s;
		else {
			if (setting.toLowerCase().contains("password")) {
				String sv = SystemOptions.getInstance(
						this.actionSettingsEditor.iniFileName,
						this.actionSettingsEditor.iniIO)
						.getObject(this.actionSettingsEditor.section, setting, -1);
				StringBuilder sb = new StringBuilder();
				while (sb.length() < sv.length())
					sb.append("*");
				return "<html><center><b>" + s + "</b><br>" +
						"&nbsp;" + sb + "&nbsp;" + "</center>";
			} else {
				SystemOptions o = SystemOptions.getInstance(
						this.actionSettingsEditor.iniFileName,
						this.actionSettingsEditor.iniIO);
				if (o != null && this.actionSettingsEditor != null && this.actionSettingsEditor.section != null)
					return "<html><center><b>" + s + "</b><br>" +
							"&nbsp;" + o.getObject(this.actionSettingsEditor.section, setting, 2)
							+ "&nbsp;" + "</center>";
				else {
					return "(not available)";
				}
			}
		}
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultImage() {
		if (isRadioSelection)
			return "img/ext/gpl2/Gnome-View-Sort-Selection-64.png";
		else
			if (isBoolean) {
				boolean enabled = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
						this.actionSettingsEditor.iniIO)
						.getBoolean(this.actionSettingsEditor.section, setting, false);
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