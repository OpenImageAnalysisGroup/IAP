package de.ipk.ag_ba.commands.settings;

import iap.blocks.data_structures.ImageAnalysisBlock;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.AttributeHelper;
import org.GuiRow;
import org.MarkComponent;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.commons.lang3.text.WordUtils;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.Unicode;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.plugins.IAPpluginManager;
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
			Object[] inp = MyInputHelper.getInput(getHelp() +
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
					Object[] inp = MyInputHelper.getInput(getHelp() + "Please enter a whole number:",
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
						Object[] inp = MyInputHelper.getInput(getHelp() + "Please enter a (floating point) number:",
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
							String possiblyColorValue = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
									this.actionSettingsEditor.iniIO)
									.getString(this.actionSettingsEditor.section, setting, null, false);
							try {
								if (setting.toLowerCase().contains("color") && possiblyColorValue != null && possiblyColorValue.startsWith("#")
										&& possiblyColorValue.length() == 7) {
									Color c = StringManipulationTools.getColorFromHTMLdef(possiblyColorValue);
									JColorChooser colorChooser = new JColorChooser(c);
									c = colorChooser.showDialog(MainFrame.getInstance(), "Change Color Value", c);
									if (c != null) {
										SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
												this.actionSettingsEditor.iniIO)
												.setColor(this.actionSettingsEditor.section, setting, c);
									}
									return;
								}
							} catch (Exception e) {
								// no color value
							}
							if (setting.toLowerCase().contains("password")) {
								if (!SystemOptions.getInstance("secret", null).getBoolean("Information for user",
										"Warning about symmetric encryption and secret file displayed", false)) {
									Object[] i = MyInputHelper.getInput(
											"Passwords are saved using symetric encryption in the settings-ini-file.<br>" +
													"The encryption/decryption key is stored in the file 'secret'.<br>" +
													"If needed, adjust access rights to the 'secret' file in the application settings folder.<br><br>" +
													"<b>This information is only displayed once.</b><br><br>" +
													"Click 'Cancel' if you need further information.",
											"Information");
									if (i == null)
										return;
									SystemOptions.getInstance("secret", null).setBoolean("Information for user",
											"Warning about symmetric encryption and secret file displayed", true);
									SystemOptions.getInstance("secret", null).setString("Information for user",
											"User name", SystemAnalysis.getUserName());
									SystemOptions.getInstance("secret", null).setString("Information for user",
											"Message displayed", SystemAnalysis.getCurrentTime());
								}
							}
							Object[] inp = MyInputHelper.getInput(getHelp() + "You may modify the text:",
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
								String specialHelp = "";
								ArrayList<Object> entries = new ArrayList<Object>();
								int line = 1;
								String leftEntryDesc;
								for (String sl : ss) {
									String blockName = null;
									String blockDesc = null;
									final int startLine = line;
									if (!setting.equals("block"))
										leftEntryDesc = "Item " + (line++);
									else {
										specialHelp = BlockListEditHelper.getHelpText();
										String inf = "Step " + (line++);
										if (line <= 10)
											inf = "Step 0" + (line - 1);
										
										try {
											ImageAnalysisBlock inst = (ImageAnalysisBlock) Class.forName(sl).newInstance();
											blockName = inst.getName();
											blockDesc = inst.getDescription();
											inf = "<html>" + BlockSelector.getBlockDescriptionAnnotation(inf, inst);
										} catch (Exception e) {
											if (sl != null && sl.startsWith("#"))
												inf = "<html>" + inf + "<br>[Disabled Block]";
											else
												inf = "<html>" + inf + "<br>[Unknown Block]";
										}
										leftEntryDesc = inf;
										
										final JLabel leftLabel = new JLabel(leftEntryDesc);
										entries.add("");
										BlockListEditHelper.installEditButton(entries, blockName, blockDesc, leftLabel, sl, startLine);
									}
								}
								Object[] inp = MyInputHelper.getInput(getHelp() + specialHelp +
										"You may modify multiple text entries (settings items '" + setting + "'). <br>" +
										"Add '//' and then additional text to a line, to add/insert a new line.<br>",
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
											if (o != null && o instanceof JComponent) {
												GuiRow gr = (GuiRow) ((JComponent) o).getClientProperty("guiRow");
												String es;
												if (gr.right instanceof MarkComponent)
													es = ((JTextField) ((MarkComponent) gr.right).getMarkedComponent()).getText();
												else
													es = ((JTextField) gr.right).getText();
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
	
	private String getHelp() {
		Collection<String> help = IAPpluginManager.getInstance().getSettingHelp(
				this.actionSettingsEditor.iniFileName,
				this.actionSettingsEditor.section, setting);
		if (help != null && !help.isEmpty()) {
			boolean empty = true;
			for (String h : help)
				if (!h.trim().isEmpty()) {
					empty = false;
					break;
				}
			if (empty)
				return "";
			else
				return "<html>" + StringManipulationTools.getStringList(help, "<br><br>") + "<br><br>";
		} else
			return "";
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
				if (o != null && this.actionSettingsEditor != null && this.actionSettingsEditor.section != null) {
					String val = o.getObject(this.actionSettingsEditor.section, setting, 2);
					try {
						if (setting.toLowerCase().contains("color") && val != null && val.startsWith("#")
								&& val.length() == 7) {
							Color c = StringManipulationTools.getColorFromHTMLdef(val);
							return "<html><center>" +
									"<table><tr><td>" +
									"<font color='" + val + "' size='+2'><b>" + Unicode.PEN + "</b></font>&nbsp;" +
									"</td><td><center><b>" + s + "</b><br>" +
									AttributeHelper.getColorName(c) + " (" + val + ")" +
									""
									+ "&nbsp;" + "</td></tr></table></center>";
						}
					} catch (Exception e) {
						// empty
					}
					return "<html><center><b>" + s + "</b><br>" +
							"&nbsp;" + o.getObject(this.actionSettingsEditor.section, setting, 2)
							+ "&nbsp;" + "</center>";
				} else {
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
						else {
							String possiblyColorValue = SystemOptions.getInstance(this.actionSettingsEditor.iniFileName,
									this.actionSettingsEditor.iniIO)
									.getString(this.actionSettingsEditor.section, setting, null, false);
							if (setting.toLowerCase().contains("color") && possiblyColorValue != null && possiblyColorValue.startsWith("#")
									&& possiblyColorValue.length() == 7)
								return "img/ext/gpl2/Gnome-Applications-Graphics-64.png";// Gnome-Accessories-Character-Map-64.png";
							else
								return "img/ext/gpl2/Gnome-Insert-Text-64.png";// Gnome-Accessories-Character-Map-64.png";
						}
					}
		// Gnome-Applications-Accessories-64.png";
	}
}