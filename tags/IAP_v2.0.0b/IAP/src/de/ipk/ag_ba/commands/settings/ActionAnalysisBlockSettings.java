package de.ipk.ag_ba.commands.settings;

import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.ImageAnalysisBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.IniIoProvider;
import org.StringManipulationTools;
import org.SystemOptions;
import org.graffiti.util.InstanceLoader;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

class ActionAnalysisBlockSettings extends AbstractNavigationAction {
	private final NavigationButton src;
	private final LinkedHashMap<String, ArrayList<NavigationButton>> group2button;
	private final String group;
	private final String section;
	private final String iniFileName;
	private final IniIoProvider iniIO;
	
	ActionAnalysisBlockSettings(String tooltip, NavigationButton src, LinkedHashMap<String, ArrayList<NavigationButton>> group2button,
			String iniFileName, IniIoProvider iniIO, String section, String group) {
		super("<html>" + tooltip + getBlockDesc(group));
		this.src = src;
		this.group2button = group2button;
		this.group = group;
		this.section = section;
		this.iniFileName = iniFileName;
		this.iniIO = iniIO;
	}
	
	private static String getBlockDesc(String group) {
		try {
			ImageAnalysisBlock inst = (ImageAnalysisBlock) InstanceLoader.createInstance(group);
			return "<br><br>" + StringManipulationTools.getWordWrap(inst.getDescription(), 60);
		} catch (Exception e) {
			return "";
		}
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public String getDefaultTitle() {
		try {
			ImageAnalysisBlock inst = (ImageAnalysisBlock) InstanceLoader.createInstance(group);
			BlockType bt = inst.getBlockType();
			String pre = "";
			if (bt != null)
				pre = "<html><font bgcolor='" + bt.getColor() + "'>&nbsp;";
			return pre + inst.getName() + (pre.isEmpty() ? "" : "&nbsp;");
		} catch (Exception e) {
			String g = group;
			if (g != null && g.indexOf(".Block") > 0)
				g = g.substring(g.lastIndexOf(".Block") + ".Block".length());
			else
				if (g != null && g.indexOf(".Bl") > 0)
					g = g.substring(g.lastIndexOf(".Bl") + ".Bl".length());
				else
					if (g != null && g.indexOf(".") > 0)
						g = g.substring(g.lastIndexOf(".") + ".".length());
			if (g != null && g.startsWith("_"))
				g = g.substring("_".length());
			if (g != null)
				g = g.replace('_', ' ');
			return g;
		}
	}
	
	@Override
	public String getDefaultImage() {
		boolean disabled = false;
		boolean debug = false;
		SystemOptions inst = SystemOptions.getInstance(iniFileName, iniIO);
		for (final String setting : inst.getSectionSettings(section)) {
			if (!setting.startsWith(group + "//"))
				continue;
			if (setting != null && setting.endsWith("//enabled")) {
				if (!inst.getBoolean(section, setting, false)) {
					disabled = true;
				}
			}
			if (setting != null && setting.endsWith("//debug")) {
				if (inst.getBoolean(section, setting, false)) {
					debug = true;
				}
			}
		}
		if (disabled)
			return "img/ext/gpl2/Glade-3-64_disabled.png";
		else
			if (debug)
				return "img/ext/gpl2/Glade-3-64_debug.png";
			else
				return "img/ext/gpl2/Glade-3-64.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(group2button.get(group));
		NavigationAction resetSettingsAction = new ActionResetActions(
				SystemOptions.getInstance(iniFileName, iniIO), section, group);
		NavigationButton restSettingsButton = new NavigationButton(
				resetSettingsAction, src.getGUIsetting());
		restSettingsButton.setRightAligned(true);
		res.add(0, restSettingsButton);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		try {
			ImageAnalysisBlock inst = (ImageAnalysisBlock) InstanceLoader.createInstance(group);
			String calc = "";
			if (inst instanceof CalculatesProperties) {
				CalculatesProperties c = (CalculatesProperties) inst;
				CalculatedPropertyDescription[] cp = c.getCalculatedProperties();
				if (cp != null && cp.length > 0) {
					calc = "Calculates the following properties:<br>" +
							StringManipulationTools.getStringList("<li>", cp, "");
				}
			}
			return new MainPanelComponent("<html><b>" + inst.getName() + "</b><br><br>" +
					"Description:" +
					"<ul><li>" +
					StringManipulationTools.getWordWrap(inst.getDescription(), 80) + "</ul>" +
					(inst.getDescriptionForParameters() != null ?
							"Parameter:" + StringManipulationTools.getWordWrap(inst.getDescriptionForParameters(), 80) : "") +
					"Annotation:<ul><li>" + inst.getBlockType().getName() + " block<br><br>" +
					"<li>Input/Output:<code><ul><li>In:&nbsp;&nbsp;" +
					StringManipulationTools.getStringList(inst.getCameraInputTypes(), ", ")
					+ "<br>" +
					"<li>Out:&nbsp;" +
					StringManipulationTools.getStringList(inst.getCameraOutputTypes(), ", ")
					+ "<br></ul></code></ul>" + calc);
		} catch (Exception e) {
			return super.getResultMainPanel();
		}
	}
}