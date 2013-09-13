package de.ipk.ag_ba.commands.settings;

import iap.blocks.data_structures.ImageAnalysisBlock;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.GuiRow;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.commons.lang3.text.WordUtils;
import org.graffiti.plugins.editcomponents.ComponentBorder;
import org.graffiti.plugins.editcomponents.ComponentBorder.Edge;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.image.structures.CameraType;
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
								for (String sl : ss) {
									String blockName = null;
									String blockDesc = null;
									if (!setting.equals("block"))
										entries.add("Item " + (line++));
									else {
										specialHelp = "The block item list shows differently colored rectangles to visually more easily separate<br>" +
												"the data acquisition, preprocessing, segmentation, feature-extraction and postprocessing blocks.<br>" +
												"The package name in the text field also indicates the purpose of a particular block.<br>" +
												"The input and output information boxes (IN/OUT) indicate whether a block (potentially) processes<br>" +
												"visible light images, fluorescence images, near-infrared and/or infrared images (in this order).<br>" +
												"If the box is filled, the block processes images from a certain type as input and/or output.<br>" +
												"If it is not filled, the corresponding image is not processed (for IN) or remains unchanged (OUT).<br>" +
												"All of this informtion is derived from static meta-data. Depending on availability of input<br>" +
												"images and block settings, images from certain camera types are not processed by the blocks.<br><br>";
										String inf = "Step " + (line++);
										if (line <= 10)
											inf = "Step 0" + (line - 1);
										
										String type = "";
										try {
											ImageAnalysisBlock inst = (ImageAnalysisBlock) Class.forName(sl).newInstance();
											blockName = inst.getName();
											blockDesc = inst.getDescription();
											switch (inst.getBlockType()) {
												case ACQUISITION:
													type = "<span style=\"background-color:#DDFFDD\">";
													break;
												case FEATURE_EXTRACTION:
													type = "<span style=\"background-color:#DDDDFF\">";
													break;
												case POSTPROCESSING:
													type = "<span style=\"background-color:#DDFFFF\">";
													break;
												case PREPROCESSING:
													type = "<span style=\"background-color:#FFFFDD\">";
													break;
												case SEGMENTATION:
													type = "<span style=\"background-color:#FFDDDD\">";
													break;
												default:
													break;
											}
											if (!type.isEmpty())
												type += "<font color='gray' size='-4'>" +
														"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br>" +
														"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font></span>";
											String gs = "<font color='green'>";
											String ge = "</font>";
											String rs = "<font color='red'>";
											String re = "</font>";
											String ns = "<font color='gray'>";
											String ne = "</font>";
											String is = "<font color='blue'>";
											String ie = "</font>";
											
											String vi = gs + (inst.getCameraInputTypes().contains(CameraType.VIS) ? "&#9632;" : "&#9633;") + ge;
											String fi = rs + (inst.getCameraInputTypes().contains(CameraType.FLUO) ? "&#9632;" : "&#9633;") + re;
											String ni = ns + (inst.getCameraInputTypes().contains(CameraType.NIR) ? "&#9632;" : "&#9633;") + ne;
											String ii = is + (inst.getCameraInputTypes().contains(CameraType.IR) ? "&#9632;" : "&#9633;") + ie;
											
											String vo = gs + (inst.getCameraOutputTypes().contains(CameraType.VIS) ? "&#9632;" : "&#9633;") + ge;
											String fo = rs + (inst.getCameraOutputTypes().contains(CameraType.FLUO) ? "&#9632;" : "&#9633;") + re;
											String no = ns + (inst.getCameraOutputTypes().contains(CameraType.NIR) ? "&#9632;" : "&#9633;") + ne;
											String io = is + (inst.getCameraOutputTypes().contains(CameraType.IR) ? "&#9632;" : "&#9633;") + ie;
											
											inf = "<html>" +
													"<table border='0'><tr>" +
													"<td>" + type + "</td>" +
													"<td>" + inf
													+ "</td><td><font color='gray' size='-2'><code>"
													+ " IN &#9656; " + vi + " " + fi + " " + ni + " " + ii + ""
													+ "<br> OUT&#9656; " + vo + " " + fo + " " + no + " " + io + "</code></font></td></tr></table>";
										} catch (Exception e) {
											inf = "<html>" + inf + "<br>[" + e.getMessage() + "]";
										}
										entries.add(inf);
									}
									// JComboBox dropDown = new JComboBox(new String[] { "Load Images", "Segmentation" });
									final ObjectRef inFocus = new ObjectRef();
									inFocus.setObject(false);
									final String blockNameF = blockName;
									final JTextField textField = new JTextField(sl + "") {
										
										@Override
										public void paint(Graphics g) {
											if (!(Boolean) inFocus.getObject() && blockNameF != null) {
												g.setColor(new JDialog().getBackground());
												g.fillRect(0, 0, getWidth(), getHeight());
												g.setColor(Color.GRAY);
												g.drawRoundRect(2, 4, getWidth() - 4, getHeight() - 8, 5, 5);
												g.setColor(Color.BLACK);
												g.drawString(blockNameF, 8, getHeight() / 2 + 4);
											} else
												super.paint(g);
										}
										
									};
									Action ba = new AbstractAction("...") {
										private static final long serialVersionUID = 1L;
										
										@Override
										public void actionPerformed(ActionEvent e) {
											String blockSelection = new BlockSelector(true, "Select Analysis Block",
													"Select the block type. [TODO NOT YET IMPLEMENTED!]").getBlockSelection();
											if (blockSelection != null) {
												textField.setText(blockSelection);
											}
										}
									};
									final JButton selButton = new JButton(ba);
									selButton.setVisible(false);
									if (blockDesc != null)
										textField.setToolTipText("<html>" +
												StringManipulationTools.getWordWrap(blockDesc, 60));
									
									if (blockName != null) {
										textField.addFocusListener(new FocusListener() {
											@Override
											public void focusLost(FocusEvent e) {
												inFocus.setObject(false);
												selButton.setVisible(false);
												textField.repaint();
											}
											
											@Override
											public void focusGained(FocusEvent e) {
												inFocus.setObject(true);
												selButton.setVisible(true);
												textField.repaint();
											}
										});
									}
									
									selButton.setToolTipText("Select Block");
									ComponentBorder cb = new ComponentBorder(selButton, Edge.RIGHT) {
										
										@Override
										public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
											if ((Boolean) inFocus.getObject())
												super.paintBorder(c, g, x, y, width, height);
										}
									};
									cb.install(textField);
									GuiRow gr = new GuiRow(new JLabel(), textField);
									entries.add(gr.getRowGui());// + "");
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
												String es = ((JTextField) gr.right).getText();
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