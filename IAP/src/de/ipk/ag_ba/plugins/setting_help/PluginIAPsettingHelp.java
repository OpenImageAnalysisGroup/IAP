package de.ipk.ag_ba.plugins.setting_help;

import java.util.Collection;
import java.util.LinkedList;

import org.SystemOptions;

import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPsettingHelp extends AbstractIAPplugin {
	public PluginIAPsettingHelp() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP settings help plugin is beeing loaded");
	}
	
	@Override
	public Collection<String> getHelpForSettings(String iniFileName, String section, String setting) {
		String[] help = readHelp(iniFileName, section, setting);
		if (help == null)
			return null;
		LinkedList<String> hl = new LinkedList<String>();
		for (String h : help)
			hl.add(h);
		return hl;
	}
	
	private String[] readHelp(String iniFile, String section, String setting) {
		if (iniFile == null)
			iniFile = "iap";
		return SystemOptions.getInstance(iniFile + ".help.txt", null).getStringAll(section, setting, new String[] { "" });
	}
}